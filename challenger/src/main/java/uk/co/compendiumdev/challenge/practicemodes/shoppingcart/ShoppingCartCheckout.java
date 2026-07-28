package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartAuth.AuthResult;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartCheckout {

    private static final String STALE_STOCK_PRODUCT = "BOOK_API";
    private static final String DECREMENT_ONE_PRODUCT = "DVD_BUGS";
    private static final String NEGATIVE_STOCK_PRODUCT = "CD_STATUS";

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

        final AuthResult authResult =
                auth.authorize(store, cart, request.header("Authorization"), false);
        if (!authResult.authorized()) {
            return json(
                    response,
                    authResult.status(),
                    ShoppingCartSupport.body("errorMessages", List.of(authResult.message())));
        }

        if ("closed".equals(ShoppingCartSupport.stringValue(cart, "state"))
                && !bugMode.bugsEnabled()) {
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
            final String stockProblem = firstStockProblem(lines);
            if (stockProblem != null) {
                return json(
                        response,
                        409,
                        ShoppingCartSupport.body("errorMessages", List.of(stockProblem)));
            }
        }

        if (bugMode.bugsEnabled()) {
            final String stockProblem = firstBuggyStockProblem(lines);
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

    private String firstStockProblem(final List<CheckoutLine> lines) {
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

    private String firstBuggyStockProblem(final List<CheckoutLine> lines) {
        final Map<String, Integer> requiredByProduct = new LinkedHashMap<>();
        final Map<String, Integer> availableByProduct = new LinkedHashMap<>();
        for (CheckoutLine line : lines) {
            requiredByProduct.merge(line.productCode(), line.quantity(), Integer::sum);
            availableByProduct.put(line.productCode(), availableForBuggyStockCheck(line));
        }

        for (Map.Entry<String, Integer> required : requiredByProduct.entrySet()) {
            if (NEGATIVE_STOCK_PRODUCT.equals(required.getKey())) {
                continue;
            }
            if (required.getValue() > availableByProduct.get(required.getKey())) {
                return "Not enough stock for product " + required.getKey();
            }
        }
        return null;
    }

    private int availableForBuggyStockCheck(final CheckoutLine line) {
        if (STALE_STOCK_PRODUCT.equals(line.productCode())) {
            return line.stockAtAdd();
        }
        return line.currentStock();
    }

    private double totalFor(final List<CheckoutLine> lines) {
        double total = 0;
        for (int index = 0; index < lines.size(); index++) {
            if (bugMode.bugsEnabled() && index == lines.size() - 1 && lines.size() > 1) {
                continue;
            }
            final CheckoutLine line = lines.get(index);
            total += line.quantity() * line.unitPriceAtAdd();
        }
        return total;
    }

    private List<Map<String, Object>> applyStockReductions(
            final ThingStore store, final List<CheckoutLine> lines) {
        final List<Map<String, Object>> changes = new ArrayList<>();
        for (CheckoutLine line : lines) {
            final int reduction = stockReductionFor(line);
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

    private int stockReductionFor(final CheckoutLine line) {
        if (bugMode.bugsEnabled() && DECREMENT_ONE_PRODUCT.equals(line.productCode())) {
            return 1;
        }
        return line.quantity();
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
