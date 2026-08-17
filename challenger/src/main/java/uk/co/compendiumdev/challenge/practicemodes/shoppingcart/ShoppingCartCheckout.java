package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartAuth.CustomRouteAuthResult;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartCheckout {

    private static final String PRODUCT_ALLOWED_TO_USE_STALE_STOCK_CAPTURED_AT_ADD = "BOOK_API";
    private static final String PRODUCT_REDUCED_BY_ONE_INSTEAD_OF_QUANTITY = "DVD_BUGS";
    private static final String PRODUCT_ALLOWED_TO_CHECKOUT_INTO_NEGATIVE_STOCK = "CD_STATUS";

    private final Thingifier thingifier;
    private final ShoppingCartBugMode bugMode;
    private final ShoppingCartState state;
    private final ShoppingCartAuth auth;

    ShoppingCartCheckout(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state) {
        this.thingifier = thingifier;
        this.bugMode = bugMode;
        this.state = state;
        this.auth = new ShoppingCartAuth(thingifier);
    }

    String checkout(final HttpServerRequest request, final HttpServerResponse response) {
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        final String cartId = request.params("cartId");
        final EntityInstance cart = ShoppingCartSupport.findById(thingifier, store, "cart", cartId);
        if (cart == null) {
            return json(
                    response,
                    404,
                    ShoppingCartSupport.body("errorMessages", List.of("Cart not found")));
        }

        final CustomRouteAuthResult authResult =
                auth.authorizeCustomRouteBearerTokenForCart(cart, request.header("Authorization"));
        if (!authResult.authorized()) {
            return json(
                    response,
                    authResult.status(),
                    ShoppingCartSupport.body("errorMessages", List.of(authResult.message())));
        }

        if (cartIsClosed(cart) && !doubleCheckoutBugAllowsClosedCartCheckout(cart)) {
            return json(
                    response,
                    409,
                    ShoppingCartSupport.body(
                            "errorMessages", List.of("Closed carts cannot be checked out again")));
        }

        final List<EntityInstance> items =
                ShoppingCartSupport.sortedById(ShoppingCartSupport.related(store, cart, "items"));
        final List<CheckoutLine> lines = checkoutLines(store, items);

        if (!bugMode.bugsEnabled()) {
            final String stockProblem = firstCurrentStockShortage(lines);
            if (stockProblem != null) {
                return json(
                        response,
                        409,
                        ShoppingCartSupport.body("errorMessages", List.of(stockProblem)));
            }
        }

        if (bugMode.bugsEnabled()) {
            final String stockProblem =
                    bookApiStaleStockAndCdStatusNegativeStockBugsFirstStockShortage(lines);
            if (stockProblem != null) {
                return json(
                        response,
                        409,
                        ShoppingCartSupport.body("errorMessages", List.of(stockProblem)));
            }
        }

        final double total = totalFor(lines);
        final List<Map<String, Object>> stockChanges = applyStockReductions(store, lines);
        final int checkoutTick = state.nextTick();
        ShoppingCartSupport.patch(
                store,
                cart,
                "state",
                "closed",
                "updatedTick",
                String.valueOf(checkoutTick),
                "checkoutTick",
                String.valueOf(checkoutTick));

        return json(
                response,
                200,
                ShoppingCartSupport.body(
                        "cartId", Integer.parseInt(cartId),
                        "state", "closed",
                        "itemCount", lines.size(),
                        "total", roundedMoney(total),
                        "stockChanges", stockChanges));
    }

    private List<CheckoutLine> checkoutLines(
            final ThingStore store, final List<EntityInstance> items) {
        final List<CheckoutLine> lines = new ArrayList<>();
        for (EntityInstance item : items) {
            final EntityInstance product = ShoppingCartSupport.firstRelated(store, item, "product");
            if (product == null) {
                continue;
            }
            lines.add(
                    new CheckoutLine(
                            item,
                            product,
                            ShoppingCartSupport.stringValue(product, "productCode"),
                            ShoppingCartSupport.intValue(item, "quantity"),
                            ShoppingCartSupport.floatValue(item, "unitPriceAtAdd"),
                            ShoppingCartSupport.intValue(item, "stockAtAdd"),
                            ShoppingCartSupport.intValue(product, "stock")));
        }
        return lines;
    }

    private String firstCurrentStockShortage(final List<CheckoutLine> lines) {
        final Map<String, Integer> requiredByProduct = new LinkedHashMap<>();
        final Map<String, Integer> currentStockByProduct = new LinkedHashMap<>();
        for (CheckoutLine line : lines) {
            requiredByProduct.merge(line.productCode(), line.quantity(), Integer::sum);
            currentStockByProduct.put(line.productCode(), line.currentStock());
        }

        for (Map.Entry<String, Integer> required : requiredByProduct.entrySet()) {
            if (required.getValue() > currentStockByProduct.get(required.getKey())) {
                return "Not enough stock for product " + required.getKey();
            }
        }
        return null;
    }

    private String bookApiStaleStockAndCdStatusNegativeStockBugsFirstStockShortage(
            final List<CheckoutLine> lines) {
        final Map<String, Integer> requiredByProduct = new LinkedHashMap<>();
        final Map<String, Integer> availableByProduct = new LinkedHashMap<>();
        for (CheckoutLine line : lines) {
            requiredByProduct.merge(line.productCode(), line.quantity(), Integer::sum);
            availableByProduct.put(
                    line.productCode(), bookApiStaleStockAtAddBugOrCurrentStock(line));
        }

        for (Map.Entry<String, Integer> required : requiredByProduct.entrySet()) {
            if (cdStatusNegativeStockBugAllowsCheckout(required.getKey())) {
                continue;
            }
            if (required.getValue() > availableByProduct.get(required.getKey())) {
                return "Not enough stock for product " + required.getKey();
            }
        }
        return null;
    }

    private boolean cdStatusNegativeStockBugAllowsCheckout(final String productCode) {
        return PRODUCT_ALLOWED_TO_CHECKOUT_INTO_NEGATIVE_STOCK.equals(productCode);
    }

    private int bookApiStaleStockAtAddBugOrCurrentStock(final CheckoutLine line) {
        if (PRODUCT_ALLOWED_TO_USE_STALE_STOCK_CAPTURED_AT_ADD.equals(line.productCode())
                || line.currentStock() == 0) {
            return line.stockAtAdd();
        }
        return line.currentStock();
    }

    private double totalFor(final List<CheckoutLine> lines) {
        double total = 0;
        for (CheckoutLine line : lines) {
            total += line.quantity() * line.unitPriceAtAdd();
        }
        return total;
    }

    private List<Map<String, Object>> applyStockReductions(
            final ThingStore store, final List<CheckoutLine> lines) {
        final List<Map<String, Object>> changes = new ArrayList<>();
        for (CheckoutLine line : lines) {
            final int reduction = dvdBugsReduceStockByOneBugOrQuantity(line);
            final int before = ShoppingCartSupport.intValue(line.product(), "stock");
            final int after = before - reduction;
            ShoppingCartSupport.patch(store, line.product(), "stock", String.valueOf(after));
            changes.add(
                    ShoppingCartSupport.body(
                            "productId", Integer.parseInt(line.product().getPrimaryKeyValue()),
                            "productCode", line.productCode(),
                            "before", before,
                            "reducedBy", reduction,
                            "after", after));
        }
        return changes;
    }

    private int dvdBugsReduceStockByOneBugOrQuantity(final CheckoutLine line) {
        if (bugMode.bugsEnabled()
                && PRODUCT_REDUCED_BY_ONE_INSTEAD_OF_QUANTITY.equals(line.productCode())) {
            return 1;
        }
        return line.quantity();
    }

    private boolean doubleCheckoutBugAllowsClosedCartCheckout(final EntityInstance cart) {
        return bugMode.bugsEnabled() && cartIsClosed(cart);
    }

    private boolean cartIsClosed(final EntityInstance cart) {
        return "closed".equals(ShoppingCartSupport.stringValue(cart, "state"));
    }

    private float roundedMoney(final double amount) {
        return Math.round(amount * 100.0) / 100.0f;
    }

    private String json(
            final HttpServerResponse response, final int status, final Map<String, Object> body) {
        response.status(status);
        response.type("application/json");
        return ShoppingCartSupport.GSON.toJson(body);
    }

    record CheckoutLine(
            EntityInstance item,
            EntityInstance product,
            String productCode,
            int quantity,
            float unitPriceAtAdd,
            int stockAtAdd,
            int currentStock) {}
}
