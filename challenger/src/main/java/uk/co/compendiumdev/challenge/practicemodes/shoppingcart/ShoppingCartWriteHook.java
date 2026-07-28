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

    ShoppingCartWriteHook(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance) {
        this.thingifier = thingifier;
        this.bugMode = bugMode;
        this.state = state;
        this.maintenance = maintenance;
        this.auth = new ShoppingCartAuth(thingifier);
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {
        final List<String> segments = ShoppingCartSupport.segments(request.getPath());
        if (segments.isEmpty()) {
            return null;
        }

        if (isCartItemCreate(request, segments)) {
            return handleCartItemCreate(request, config, segments.get(1));
        }

        if (isCartItemRelationshipDelete(request, segments)) {
            return handleCartItemRelationshipDelete(request, config, segments.get(1), segments.get(3));
        }

        if (isCartDelete(request, segments)) {
            return handleCartDelete(request, config, segments.get(1));
        }

        if (isBlockedWrite(request, segments)) {
            return ShoppingCartSupport.error(
                    request, config, 405, "Use the Shopping Cart workflow routes for writes");
        }

        return null;
    }

    private boolean isCartItemCreate(final HttpApiRequest request, final List<String> segments) {
        return request.getVerb() == HttpApiRequest.VERB.POST
                && segments.size() == 3
                && "carts".equals(segments.get(0))
                && "items".equals(segments.get(2));
    }

    private boolean isCartItemRelationshipDelete(
            final HttpApiRequest request, final List<String> segments) {
        return request.getVerb() == HttpApiRequest.VERB.DELETE
                && segments.size() == 4
                && "carts".equals(segments.get(0))
                && "items".equals(segments.get(2));
    }

    private boolean isCartDelete(final HttpApiRequest request, final List<String> segments) {
        return request.getVerb() == HttpApiRequest.VERB.DELETE
                && segments.size() == 2
                && "carts".equals(segments.get(0));
    }

    private boolean isBlockedWrite(final HttpApiRequest request, final List<String> segments) {
        if (request.getVerb() != HttpApiRequest.VERB.POST
                && request.getVerb() != HttpApiRequest.VERB.PUT
                && request.getVerb() != HttpApiRequest.VERB.DELETE) {
            return false;
        }

        final String root = segments.get(0);
        if ("products".equals(root)) {
            return true;
        }
        if ("cartitems".equals(root)) {
            return true;
        }
        if ("carts".equals(root)) {
            return request.getVerb() != HttpApiRequest.VERB.GET
                    && request.getVerb() != HttpApiRequest.VERB.HEAD;
        }
        return false;
    }

    private HttpApiResponse handleCartItemCreate(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final String cartId) {
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        final EntityInstance cart =
                ShoppingCartSupport.findById(thingifier, store, "cart", cartId);
        if (cart == null) {
            return ShoppingCartSupport.error(request, config, 404, "Cart not found");
        }

        final CartItemRequest body = parseBody(request, CartItemRequest.class);
        if (body == null) {
            return ShoppingCartSupport.error(request, config, 400, "Invalid JSON body");
        }

        final boolean updateExistingItem = body.id != null;
        final boolean allowAnyValidToken = bugMode.bugsEnabled() && updateExistingItem;
        final AuthResult authResult =
                auth.authorize(
                        store, cart, request.getHeader("Authorization", ""), allowAnyValidToken);
        if (!authResult.authorized()) {
            return ShoppingCartSupport.error(
                    request, config, authResult.status(), authResult.message());
        }

        if (cartIsClosed(cart) && !bugMode.bugsEnabled()) {
            return ShoppingCartSupport.error(
                    request, config, 409, "Closed carts cannot be modified");
        }

        if (updateExistingItem) {
            return updateCartItemQuantity(
                    request, config, store, cart, String.valueOf(body.id), body.quantity);
        }

        return addCartItem(request, config, store, cart, body);
    }

    private HttpApiResponse handleCartItemRelationshipDelete(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final String cartId,
            final String itemId) {
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

        if (cartIsClosed(cart) && !bugMode.bugsEnabled()) {
            return ShoppingCartSupport.error(
                    request, config, 409, "Closed carts cannot be modified");
        }

        return deleteCartItem(request, config, store, cart, itemId);
    }

    private HttpApiResponse addCartItem(
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
                validateRequestedQuantity(request, config, product, body.quantity);
        if (quantityError != null) {
            return quantityError;
        }

        try {
            final float unitPriceAtAdd =
                    bugMode.bugsEnabled() && body.unitPriceAtAdd != null
                            ? body.unitPriceAtAdd
                            : ShoppingCartSupport.floatValue(product, "unitPrice");
            final int stockAtAdd =
                    bugMode.bugsEnabled() && body.stockAtAdd != null
                            ? body.stockAtAdd
                            : ShoppingCartSupport.intValue(product, "stock");
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

    private HttpApiResponse updateCartItemQuantity(
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
                validateRequestedQuantity(request, config, product, quantity);
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

    private HttpApiResponse handleCartDelete(
            final HttpApiRequest request, final ThingifierApiConfig config, final String cartId) {
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

    private HttpApiResponse validateRequestedQuantity(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final EntityInstance product,
            final int quantity) {
        if (bugMode.bugsEnabled()) {
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
}
