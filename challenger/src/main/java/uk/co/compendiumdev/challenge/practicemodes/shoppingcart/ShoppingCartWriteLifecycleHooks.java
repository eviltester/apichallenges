package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.AfterActionContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.AfterValidationContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.BeforeActionContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.BeforeValidationContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.BodyParsedContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContextView;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.BodyFieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartWriteLifecycleHooks {

    private final Thingifier thingifier;
    private final ShoppingCartBugMode bugMode;
    private final ShoppingCartState state;
    private final ShoppingCartMaintenance maintenance;
    private final ShoppingCartAuth auth;

    ShoppingCartWriteLifecycleHooks(
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

    /**
     * unitPriceAtAdd should be the product price at the moment the item is added. The classic bug
     * lets callers submit their own hidden unitPriceAtAdd value, so clean mode overwrites it with
     * the product price.
     */
    void allowHiddenUnitPriceAtAddOverrideBug(final BodyParsedContext context) {
        if (postBodyContainsCartItemId(context)) {
            return;
        }

        final EntityInstance product = productFromBody(context);
        if (product == null) {
            return;
        }

        final Map<String, Object> body = editableBody(context);
        if (hiddenUnitPriceAtAddOverrideBugApplies(body)) {
            return;
        }

        setUnitPriceAtAddFromProduct(context, body, product);
    }

    /**
     * stockAtAdd should be the product stock at the moment the item is added. The classic bug lets
     * callers submit their own hidden stockAtAdd value, so clean mode overwrites it with the
     * product stock.
     */
    void allowHiddenStockAtAddOverrideBug(final BodyParsedContext context) {
        if (postBodyContainsCartItemId(context)) {
            return;
        }

        final EntityInstance product = productFromBody(context);
        if (product == null) {
            return;
        }

        final Map<String, Object> body = editableBody(context);
        if (hiddenStockAtAddOverrideBugApplies(body)) {
            return;
        }

        setStockAtAddFromProduct(context, body, product);
    }

    /**
     * Thingifier route auth has already checked that the bearer token belongs to a cart. Normally
     * that cart must be the one being changed, but the classic bug lets a caller update an existing
     * item with any valid cart token.
     */
    void allowAnyValidCartTokenToUpdateExistingCartItemBug(final BeforeValidationContext context) {
        final EntityInstance cart = cartFromParent(context);
        if (cart == null) {
            rejectCartNotFound(context);
            return;
        }

        if (anyValidCartTokenCanUpdateExistingCartItemBugApplies(context)) {
            return;
        }

        if (!auth.authenticatedTokenMatchesCart(context.requestContext(), cart)) {
            rejectBearerTokenDoesNotMatchCart(context);
        }
    }

    /**
     * POST /shop/carts/{cartId}/items creates a new item unless the body contains an item id. When
     * an id is present, the Buggy API treats the request as a quantity update for that existing
     * item, so this replaces the generated create command with an update command.
     */
    void updateExistingCartItemWhenPostBodyContainsId(final BeforeValidationContext context) {
        final Integer itemId = cartItemIdFromPostBody(context);
        if (itemId == null) {
            return;
        }

        final EntityInstance cart = cartFromParent(context);
        final EntityInstance item = cartItemFromPostBody(context, itemId);
        if (!cartItemBelongsToCart(context.store(), cart, item)) {
            rejectCartItemNotFound(context);
            return;
        }

        final Integer quantity = quantityFromBody(context);
        if (quantity == null) {
            rejectQuantityRequired(context);
            return;
        }

        replacePostWithCartItemQuantityUpdateCommand(context, itemId, quantity);
    }

    /**
     * The model has default values for productId and quantity, but this public API requires callers
     * to send both fields when creating a cart item. A POST with an item id is different because it
     * updates an existing item quantity.
     */
    void rejectMissingProductOrQuantity(final AfterValidationContext context) {
        if (postBodyContainsCartItemId(context)) {
            return;
        }

        if (!productAndQuantityArePresent(context)) {
            rejectProductAndQuantityRequired(context);
        }
    }

    /**
     * The model allows negative quantities so the buggy API can demonstrate this defect. Clean mode
     * rejects negative quantities here without changing the shared model.
     */
    void allowQuantityLessThanZeroBug(final AfterValidationContext context) {
        if (allowQuantityLessThanZeroBugApplies(context)) {
            return;
        }

        if (quantityIsLessThanZero(context)) {
            rejectQuantityMustBeGreaterThanZero(context);
        }
    }

    /**
     * The model accepts zero so the buggy API can demonstrate this defect. Clean mode rejects zero
     * here because a cart item should have a quantity greater than zero.
     */
    void allowQuantityZeroBug(final AfterValidationContext context) {
        if (allowQuantityZeroBugApplies(context)) {
            return;
        }

        if (quantityIsZero(context)) {
            rejectQuantityMustBeGreaterThanZero(context);
        }
    }

    /**
     * Quantity has to be checked against the selected product's current stock. That requires
     * looking up the product record, so this happens after the request body has been validated. The
     * classic bug lets callers order more than the available stock.
     */
    void allowQuantityGreaterThanStockBug(final AfterValidationContext context) {
        if (allowQuantityGreaterThanStockBugApplies(context)) {
            return;
        }

        if (quantityIsGreaterThanAvailableStock(context)) {
            rejectQuantityExceedsCurrentProductStock(context);
        }
    }

    /**
     * productId points to a separate product record. Thingifier can check the shape of the field,
     * but this API also needs to return 404 when the requested product does not exist.
     */
    void rejectUnknownProduct(final AfterValidationContext context) {
        if (!quantityWasSupplied(context)) {
            return;
        }

        if (productForCartItemWrite(context) == null) {
            rejectProductNotFound(context);
        }
    }

    /**
     * The request changes an item inside a cart, so the cart state matters. The classic bug allows
     * closed carts to be changed; clean mode rejects the write before any data is changed.
     */
    void allowClosedCartModificationBug(final BeforeActionContext context) {
        final EntityInstance cart = cartFromParent(context);
        if (cart == null) {
            return;
        }

        if (allowClosedCartModificationBugApplies(cart)) {
            return;
        }

        if (cartIsClosed(cart)) {
            rejectClosedCartModification(context);
        }
    }

    /**
     * The public response includes cart and product details and deliberately omits the Location
     * header. This replaces Thingifier's generated create/update response after the data change
     * succeeds.
     */
    void returnCartItemWriteWorkflowResponse(final AfterActionContext context) {
        if (writeActionFailed(context)) {
            return;
        }

        final EntityInstance cart = cartFromParent(context);
        final EntityInstance item = context.writeCommandResult().getInstance();
        if (cart == null || item == null) {
            return;
        }

        if (creatingNewCartItem(context)) {
            connectItemToProduct(context.store(), item);
        }

        touchCart(context.store(), cart);
        returnCartItemWriteResponse(context, cart, item);
    }

    /**
     * The generated relationship DELETE only removes the link between cart and item. The Shopping
     * Cart API also deletes the item record itself and returns JSON showing which item was removed.
     */
    void returnCartItemDeleteWorkflowResponse(final AfterActionContext context) {
        if (writeActionFailed(context)) {
            return;
        }

        final EntityInstance cart = cartFromParent(context);
        final EntityInstance item =
                findById(context.store(), "cartitem", context.childIdentifier());
        if (cart == null || item == null) {
            return;
        }

        deleteCartItemEntity(context.store(), item);
        touchCart(context.store(), cart);
        returnDeletedCartItemResponse(context, cart);
    }

    /**
     * A generated cart delete would remove only the cart. The Shopping Cart API must delete the
     * cart's items first and return the documented JSON body.
     */
    void deleteCartAndItemsInsteadOfNativeDelete(final BeforeActionContext context) {
        final EntityInstance cart = cartFromTarget(context);
        if (cart == null) {
            rejectCartNotFound(context);
            return;
        }

        deleteCartAndItemsAndReturnWorkflowResponse(context, cart);
    }

    private boolean hiddenUnitPriceAtAddOverrideBugApplies(final Map<String, Object> body) {
        return bugMode.bugsEnabled() && body.get("unitPriceAtAdd") != null;
    }

    private void setUnitPriceAtAddFromProduct(
            final BodyParsedContext context,
            final Map<String, Object> body,
            final EntityInstance product) {
        body.put("unitPriceAtAdd", ShoppingCartSupport.floatValue(product, "unitPrice"));
        context.replaceBodyFields(ApiBodyFields.fromMap(body));
    }

    private boolean hiddenStockAtAddOverrideBugApplies(final Map<String, Object> body) {
        return bugMode.bugsEnabled() && body.get("stockAtAdd") != null;
    }

    private void setStockAtAddFromProduct(
            final BodyParsedContext context,
            final Map<String, Object> body,
            final EntityInstance product) {
        body.put("stockAtAdd", ShoppingCartSupport.intValue(product, "stock"));
        context.replaceBodyFields(ApiBodyFields.fromMap(body));
    }

    private boolean anyValidCartTokenCanUpdateExistingCartItemBugApplies(
            final ThingifierApiLifecycleContextView context) {
        return bugMode.bugsEnabled() && postBodyContainsCartItemId(context);
    }

    private Integer cartItemIdFromPostBody(final ThingifierApiLifecycleContextView context) {
        return bodyInteger(context, "id");
    }

    private EntityInstance cartItemFromPostBody(
            final ThingifierApiLifecycleContextView context, final Integer itemId) {
        return findById(context.store(), "cartitem", String.valueOf(itemId));
    }

    private boolean cartItemBelongsToCart(
            final ThingStore store, final EntityInstance cart, final EntityInstance item) {
        return cart != null && item != null && cartContainsItem(store, cart, item);
    }

    private Integer quantityFromBody(final ThingifierApiLifecycleContextView context) {
        return bodyInteger(context, "quantity");
    }

    private void replacePostWithCartItemQuantityUpdateCommand(
            final BeforeValidationContext context, final Integer itemId, final Integer quantity) {
        final List<NamedValue> fieldValues =
                List.of(new NamedValue("quantity", String.valueOf(quantity)));
        context.replaceWriteCommand(
                new AmendThingCommand(
                        "cartitem",
                        String.valueOf(itemId),
                        fieldValues,
                        BodyFieldValue.fromNamedValues(fieldValues),
                        false,
                        List.of()));
    }

    private boolean productAndQuantityArePresent(final ThingifierApiLifecycleContextView context) {
        return bodyInteger(context, "productId") != null && quantityWasSupplied(context);
    }

    private void rejectProductAndQuantityRequired(final AfterValidationContext context) {
        context.replaceValidationResult(
                ThingCommandResult.error("productId and quantity are required"));
    }

    private boolean allowQuantityLessThanZeroBugApplies(
            final ThingifierApiLifecycleContextView context) {
        return bugMode.bugsEnabled() && quantityIsLessThanZero(context);
    }

    private boolean quantityIsLessThanZero(final ThingifierApiLifecycleContextView context) {
        final Integer quantity = quantityFromBody(context);
        return quantity != null && quantity < 0;
    }

    private boolean allowQuantityZeroBugApplies(final ThingifierApiLifecycleContextView context) {
        return bugMode.bugsEnabled() && quantityIsZero(context);
    }

    private boolean quantityIsZero(final ThingifierApiLifecycleContextView context) {
        final Integer quantity = quantityFromBody(context);
        return quantity != null && quantity == 0;
    }

    private void rejectQuantityMustBeGreaterThanZero(final AfterValidationContext context) {
        context.replaceValidationResult(
                ThingCommandResult.error("quantity must be greater than 0"));
    }

    private boolean allowQuantityGreaterThanStockBugApplies(
            final ThingifierApiLifecycleContextView context) {
        return bugMode.bugsEnabled() && quantityIsGreaterThanAvailableStock(context);
    }

    private boolean quantityIsGreaterThanAvailableStock(
            final ThingifierApiLifecycleContextView context) {
        final Integer quantity = quantityFromBody(context);
        final EntityInstance product = productForCartItemWrite(context);
        return quantity != null
                && product != null
                && quantity > ShoppingCartSupport.intValue(product, "stock");
    }

    private void rejectQuantityExceedsCurrentProductStock(final AfterValidationContext context) {
        context.shortCircuitWith(
                ShoppingCartSupport.apiError(409, "quantity exceeds current product stock"));
    }

    private boolean quantityWasSupplied(final ThingifierApiLifecycleContextView context) {
        return quantityFromBody(context) != null;
    }

    private void rejectProductNotFound(final AfterValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(404, "Product not found"));
    }

    private boolean allowClosedCartModificationBugApplies(final EntityInstance cart) {
        return bugMode.bugsEnabled() && cartIsClosed(cart);
    }

    private void rejectClosedCartModification(final BeforeActionContext context) {
        context.shortCircuitWith(
                ShoppingCartSupport.apiError(409, "Closed carts cannot be modified"));
    }

    private boolean writeActionFailed(final AfterActionContext context) {
        return context.writeCommandResult() == null || context.writeCommandResult().isError();
    }

    private boolean creatingNewCartItem(final ThingifierApiLifecycleContextView context) {
        return !postBodyContainsCartItemId(context);
    }

    private void returnCartItemWriteResponse(
            final AfterActionContext context,
            final EntityInstance cart,
            final EntityInstance item) {
        context.replaceApiResponse(
                ShoppingCartSupport.apiJsonResponse(
                        creatingNewCartItem(context) ? 201 : 200,
                        itemResponse(context.store(), cart, item)));
    }

    private void deleteCartItemEntity(final ThingStore store, final EntityInstance item) {
        store.relationships().removeAll(item);
        store.entities().delete(item);
    }

    private void returnDeletedCartItemResponse(
            final AfterActionContext context, final EntityInstance cart) {
        context.replaceApiResponse(
                ShoppingCartSupport.apiJsonResponse(
                        200,
                        ShoppingCartSupport.body(
                                "cartId", Integer.parseInt(cart.getPrimaryKeyValue()),
                                "deletedItemId", Integer.parseInt(context.childIdentifier()),
                                "state", ShoppingCartSupport.stringValue(cart, "state"))));
    }

    private void deleteCartAndItemsAndReturnWorkflowResponse(
            final BeforeActionContext context, final EntityInstance cart) {
        final int cartId = Integer.parseInt(cart.getPrimaryKeyValue());
        maintenance.deleteCartAndItems(context.store(), cart);
        context.shortCircuitWith(
                ShoppingCartSupport.apiJsonResponse(
                        200, ShoppingCartSupport.body("deletedCartId", cartId)));
    }

    private void rejectCartNotFound(final BeforeValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(404, "Cart not found"));
    }

    private void rejectCartNotFound(final BeforeActionContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(404, "Cart not found"));
    }

    private void rejectCartItemNotFound(final BeforeValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(404, "Cart item not found"));
    }

    private void rejectBearerTokenDoesNotMatchCart(final BeforeValidationContext context) {
        context.shortCircuitWith(
                ShoppingCartSupport.apiError(403, "Bearer token does not match cart"));
    }

    private void rejectQuantityRequired(final BeforeValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(422, "quantity is required"));
    }

    private EntityInstance productForCartItemWrite(
            final ThingifierApiLifecycleContextView context) {
        if (postBodyContainsCartItemId(context)) {
            final EntityInstance item =
                    findById(
                            context.store(),
                            "cartitem",
                            String.valueOf(bodyInteger(context, "id")));
            return ShoppingCartSupport.firstRelated(context.store(), item, "product");
        }
        return productFromBody(context);
    }

    private EntityInstance productFromBody(final ThingifierApiLifecycleContextView context) {
        final Integer productId = bodyInteger(context, "productId");
        if (productId == null) {
            return null;
        }
        return findById(context.store(), "product", String.valueOf(productId));
    }

    private void connectItemToProduct(final ThingStore store, final EntityInstance item) {
        final EntityInstance product =
                findById(
                        store,
                        "product",
                        String.valueOf(ShoppingCartSupport.intValue(item, "productId")));
        if (product != null) {
            store.relationships().connect(product, "cartitems", item);
        }
    }

    private EntityInstance cartFromParent(final ThingifierApiLifecycleContextView context) {
        return findById(context.store(), "cart", context.parentIdentifier());
    }

    private EntityInstance cartFromTarget(final ThingifierApiLifecycleContextView context) {
        return findById(context.store(), "cart", context.targetIdentifier());
    }

    private EntityInstance findById(
            final ThingStore store, final String entityName, final String identifier) {
        if (identifier == null) {
            return null;
        }
        return ShoppingCartSupport.findById(thingifier, store, entityName, identifier);
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

    private boolean postBodyContainsCartItemId(final ThingifierApiLifecycleContextView context) {
        return bodyInteger(context, "id") != null;
    }

    private Integer bodyInteger(
            final ThingifierApiLifecycleContextView context, final String fieldName) {
        final Object value = context.bodyFields().asMap().get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            try {
                return (int) Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignoredAgain) {
                return null;
            }
        }
    }

    private Map<String, Object> editableBody(final ThingifierApiLifecycleContextView context) {
        return new LinkedHashMap<>(context.bodyFields().asMap());
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
}
