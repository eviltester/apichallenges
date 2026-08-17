package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartAuth.AuthResult;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartWriteHook implements HttpApiRequestHook {

    private final Thingifier thingifier;
    private final ShoppingCartBugMode bugMode;
    private final ShoppingCartState state;
    private final ShoppingCartMaintenance maintenance;
    private final ShoppingCartAuth auth;
    private final HookBehaviour behaviour;

    static ShoppingCartWriteHook cartItemWriteBugsForPostCartItems(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance) {
        return new ShoppingCartWriteHook(
                thingifier,
                bugMode,
                state,
                maintenance,
                ShoppingCartWriteHook::cartItemWriteBugsCreateOrUpdateByBodyId);
    }

    static ShoppingCartWriteHook closedCartCanStillDeleteCartItemBug(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance) {
        return new ShoppingCartWriteHook(
                thingifier,
                bugMode,
                state,
                maintenance,
                ShoppingCartWriteHook::closedCartCanStillDeleteCartItemBug);
    }

    static ShoppingCartWriteHook deleteCartAndItsItems(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance) {
        return new ShoppingCartWriteHook(
                thingifier, bugMode, state, maintenance, ShoppingCartWriteHook::deleteCartAndItems);
    }

    static ShoppingCartWriteHook rejectWritesToReadOnlyShoppingCartRoutes(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance) {
        return new ShoppingCartWriteHook(
                thingifier,
                bugMode,
                state,
                maintenance,
                ShoppingCartWriteHook::rejectReadOnlyRouteWrite);
    }

    ShoppingCartWriteHook(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance,
            final HookBehaviour behaviour) {
        this.thingifier = thingifier;
        this.bugMode = bugMode;
        this.state = state;
        this.maintenance = maintenance;
        this.auth = new ShoppingCartAuth(thingifier);
        this.behaviour = behaviour;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {
        final List<String> segments = ShoppingCartSupport.segments(request.getPath());
        return behaviour.run(this, request, config, segments);
    }

    private HttpApiResponse cartItemWriteBugsCreateOrUpdateByBodyId(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        final String cartId = segments.get(1);
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        final EntityInstance cart = ShoppingCartSupport.findById(thingifier, store, "cart", cartId);
        if (cart == null) {
            return ShoppingCartSupport.error(request, config, 404, "Cart not found");
        }

        final CartItemRequest body = parseBody(request, CartItemRequest.class);
        if (body == null) {
            return ShoppingCartSupport.error(request, config, 400, "Invalid JSON body");
        }

        final AuthResult authResult =
                auth.authorize(
                        store,
                        cart,
                        request.getHeader("Authorization", ""),
                        allowAnyValidCartTokenToUpdateAnyCartItemBug(body));
        if (!authResult.authorized()) {
            return ShoppingCartSupport.error(
                    request, config, authResult.status(), authResult.message());
        }

        final HttpApiResponse closedCartError =
                closedCartMutationBugOrRejectClosedCartWrite(request, config, cart);
        if (closedCartError != null) {
            return closedCartError;
        }

        if (requestBodyUpdatesExistingCartItem(body)) {
            return badQuantityBugUpdatesExistingCartItem(
                    request, config, store, cart, String.valueOf(body.id), body.quantity);
        }

        return badQuantityAndHiddenCapturedFieldBugsCreateNewCartItem(
                request, config, store, cart, body);
    }

    private HttpApiResponse closedCartCanStillDeleteCartItemBug(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        final String cartId = segments.get(1);
        final String itemId = segments.get(3);
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        final EntityInstance cart = ShoppingCartSupport.findById(thingifier, store, "cart", cartId);
        if (cart == null) {
            return ShoppingCartSupport.error(request, config, 404, "Cart not found");
        }

        final AuthResult authResult =
                auth.authorize(store, cart, request.getHeader("Authorization", ""), false);
        if (!authResult.authorized()) {
            return ShoppingCartSupport.error(
                    request, config, authResult.status(), authResult.message());
        }

        final HttpApiResponse closedCartError =
                closedCartMutationBugOrRejectClosedCartWrite(request, config, cart);
        if (closedCartError != null) {
            return closedCartError;
        }

        return deleteCartItem(request, config, store, cart, itemId);
    }

    private HttpApiResponse badQuantityAndHiddenCapturedFieldBugsCreateNewCartItem(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final EntityInstance cart,
            final CartItemRequest body) {
        if (body.productId == null || body.quantity == null) {
            return ShoppingCartSupport.error(
                    request, config, 422, "productId and quantity are required");
        }

        final EntityInstance product =
                ShoppingCartSupport.findById(
                        thingifier, store, "product", String.valueOf(body.productId));
        if (product == null) {
            return ShoppingCartSupport.error(request, config, 404, "Product not found");
        }

        final HttpApiResponse quantityError =
                badQuantityBugsOrRejectInvalidQuantity(request, config, product, body.quantity);
        if (quantityError != null) {
            return quantityError;
        }

        try {
            final float unitPriceAtAdd =
                    hiddenUnitPriceAtAddOverrideBugOrProductPrice(product, body);
            final int stockAtAdd = hiddenStockAtAddOverrideBugOrProductStock(product, body);
            final EntityInstance item =
                    store.entities()
                            .create(
                                    EntityInstanceDraft.forEntity(
                                                    ShoppingCartSupport.definition(
                                                            thingifier, "cartitem"))
                                            .withField("productId", String.valueOf(body.productId))
                                            .withField("quantity", String.valueOf(body.quantity))
                                            .withField(
                                                    "unitPriceAtAdd",
                                                    String.valueOf(unitPriceAtAdd))
                                            .withField("stockAtAdd", String.valueOf(stockAtAdd)));
            store.relationships().connect(cart, "items", item);
            store.relationships().connect(product, "cartitems", item);
            touchCart(store, cart);
            return ShoppingCartSupport.jsonResponse(
                    request, config, 201, itemResponse(store, cart, item));
        } catch (RuntimeException e) {
            return ShoppingCartSupport.error(request, config, 409, e.getMessage());
        }
    }

    private HttpApiResponse badQuantityBugUpdatesExistingCartItem(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final EntityInstance cart,
            final String itemId,
            final Integer quantity) {
        final EntityInstance item =
                ShoppingCartSupport.findById(thingifier, store, "cartitem", itemId);
        if (item == null || !cartContainsItem(store, cart, item)) {
            return ShoppingCartSupport.error(request, config, 404, "Cart item not found");
        }

        if (quantity == null) {
            return ShoppingCartSupport.error(request, config, 422, "quantity is required");
        }

        final EntityInstance product = ShoppingCartSupport.firstRelated(store, item, "product");
        final HttpApiResponse quantityError =
                badQuantityBugsOrRejectInvalidQuantity(request, config, product, quantity);
        if (quantityError != null) {
            return quantityError;
        }

        final EntityInstance updatedItem =
                ShoppingCartSupport.patch(store, item, "quantity", String.valueOf(quantity));
        touchCart(store, cart);
        return ShoppingCartSupport.jsonResponse(
                request, config, 200, itemResponse(store, cart, updatedItem));
    }

    private HttpApiResponse deleteCartItem(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final EntityInstance cart,
            final String itemId) {
        final EntityInstance item =
                ShoppingCartSupport.findById(thingifier, store, "cartitem", itemId);
        if (item == null || !cartContainsItem(store, cart, item)) {
            return ShoppingCartSupport.error(request, config, 404, "Cart item not found");
        }

        store.relationships().removeAll(item);
        store.entities().delete(item);
        touchCart(store, cart);
        return ShoppingCartSupport.jsonResponse(
                request,
                config,
                200,
                ShoppingCartSupport.body(
                        "cartId", Integer.parseInt(cart.getPrimaryKeyValue()),
                        "deletedItemId", Integer.parseInt(itemId),
                        "state", ShoppingCartSupport.stringValue(cart, "state")));
    }

    private HttpApiResponse deleteCartAndItems(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        final String cartId = segments.get(1);
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        final EntityInstance cart = ShoppingCartSupport.findById(thingifier, store, "cart", cartId);
        if (cart == null) {
            return ShoppingCartSupport.error(request, config, 404, "Cart not found");
        }

        final AuthResult authResult =
                auth.authorize(store, cart, request.getHeader("Authorization", ""), false);
        if (!authResult.authorized()) {
            return ShoppingCartSupport.error(
                    request, config, authResult.status(), authResult.message());
        }

        maintenance.deleteCartAndItems(store, cart);
        return ShoppingCartSupport.jsonResponse(
                request,
                config,
                200,
                ShoppingCartSupport.body("deletedCartId", Integer.parseInt(cartId)));
    }

    private HttpApiResponse rejectReadOnlyRouteWrite(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        if (segments.isEmpty()) {
            return null;
        }
        return ShoppingCartSupport.error(
                request, config, 405, "Use the Shopping Cart workflow routes for writes");
    }

    private HttpApiResponse badQuantityBugsOrRejectInvalidQuantity(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final EntityInstance product,
            final int quantity) {
        if (allowQuantityLessThanZeroBug(quantity)) {
            return null;
        }
        if (allowQuantityZeroBug(quantity)) {
            return null;
        }
        if (allowQuantityGreaterThanStockBug(product, quantity)) {
            return null;
        }
        if (quantity <= 0) {
            return ShoppingCartSupport.error(
                    request, config, 422, "quantity must be greater than 0");
        }
        if (product == null) {
            return ShoppingCartSupport.error(request, config, 404, "Product not found");
        }
        if (quantity > ShoppingCartSupport.intValue(product, "stock")) {
            return ShoppingCartSupport.error(
                    request, config, 409, "quantity exceeds current product stock");
        }
        return null;
    }

    private boolean cartContainsItem(
            final ThingStore store, final EntityInstance cart, final EntityInstance item) {
        return ShoppingCartSupport.related(store, cart, "items").stream()
                .anyMatch(
                        related -> related.getPrimaryKeyValue().equals(item.getPrimaryKeyValue()));
    }

    private boolean cartIsClosed(final EntityInstance cart) {
        return "closed".equals(ShoppingCartSupport.stringValue(cart, "state"));
    }

    private boolean requestBodyUpdatesExistingCartItem(final CartItemRequest body) {
        return body.id != null;
    }

    private boolean allowAnyValidCartTokenToUpdateAnyCartItemBug(final CartItemRequest body) {
        return bugMode.bugsEnabled() && requestBodyUpdatesExistingCartItem(body);
    }

    private HttpApiResponse closedCartMutationBugOrRejectClosedCartWrite(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final EntityInstance cart) {
        if (!cartIsClosed(cart) || allowClosedCartMutationBug(cart)) {
            return null;
        }
        return ShoppingCartSupport.error(request, config, 409, "Closed carts cannot be modified");
    }

    private boolean allowClosedCartMutationBug(final EntityInstance cart) {
        return bugMode.bugsEnabled() && cartIsClosed(cart);
    }

    private boolean allowQuantityLessThanZeroBug(final int quantity) {
        return bugMode.bugsEnabled() && quantity < 0;
    }

    private boolean allowQuantityZeroBug(final int quantity) {
        return bugMode.bugsEnabled() && quantity == 0;
    }

    private boolean allowQuantityGreaterThanStockBug(
            final EntityInstance product, final int quantity) {
        return bugMode.bugsEnabled()
                && product != null
                && quantity > ShoppingCartSupport.intValue(product, "stock");
    }

    private float hiddenUnitPriceAtAddOverrideBugOrProductPrice(
            final EntityInstance product, final CartItemRequest body) {
        if (allowHiddenUnitPriceAtAddOverrideBug(body)) {
            return body.unitPriceAtAdd;
        }
        return ShoppingCartSupport.floatValue(product, "unitPrice");
    }

    private boolean allowHiddenUnitPriceAtAddOverrideBug(final CartItemRequest body) {
        return bugMode.bugsEnabled() && body.unitPriceAtAdd != null;
    }

    private int hiddenStockAtAddOverrideBugOrProductStock(
            final EntityInstance product, final CartItemRequest body) {
        if (allowHiddenStockAtAddOverrideBug(body)) {
            return body.stockAtAdd;
        }
        return ShoppingCartSupport.intValue(product, "stock");
    }

    private boolean allowHiddenStockAtAddOverrideBug(final CartItemRequest body) {
        return bugMode.bugsEnabled() && body.stockAtAdd != null;
    }

    private void touchCart(final ThingStore store, final EntityInstance cart) {
        ShoppingCartSupport.patch(store, cart, "updatedTick", String.valueOf(state.nextTick()));
    }

    private Map<String, Object> itemResponse(
            final ThingStore store, final EntityInstance cart, final EntityInstance item) {
        final EntityInstance product = ShoppingCartSupport.firstRelated(store, item, "product");
        return ShoppingCartSupport.body(
                "id", Integer.parseInt(item.getPrimaryKeyValue()),
                "cartId", Integer.parseInt(cart.getPrimaryKeyValue()),
                "productId", Integer.parseInt(product.getPrimaryKeyValue()),
                "quantity", ShoppingCartSupport.intValue(item, "quantity"),
                "unitPriceAtAdd", ShoppingCartSupport.floatValue(item, "unitPriceAtAdd"),
                "stockAtAdd", ShoppingCartSupport.intValue(item, "stockAtAdd"));
    }

    private <T> T parseBody(final HttpApiRequest request, final Class<T> bodyType) {
        try {
            return ShoppingCartSupport.GSON.fromJson(request.getBody(), bodyType);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final class CartItemRequest {
        Integer id;
        Integer productId;
        Integer quantity;
        Float unitPriceAtAdd;
        Integer stockAtAdd;
    }

    private interface HookBehaviour {
        HttpApiResponse run(
                ShoppingCartWriteHook hook,
                HttpApiRequest request,
                ThingifierApiConfig config,
                List<String> segments);
    }
}
