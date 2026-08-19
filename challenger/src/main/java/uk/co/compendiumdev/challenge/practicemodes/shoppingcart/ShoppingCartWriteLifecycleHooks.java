package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.LinkedHashMap;
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
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartWriteLifecycleHooks {

    private final Thingifier thingifier;
    private final ShoppingCartBugMode bugMode;
    private final ShoppingCartState state;
    private final ShoppingCartAuth auth;

    ShoppingCartWriteLifecycleHooks(
            final Thingifier thingifier,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state) {
        this.thingifier = thingifier;
        this.bugMode = bugMode;
        this.state = state;
        this.auth = new ShoppingCartAuth(thingifier);
    }

    /**
     * The Buggy API deliberately accepts loose numeric values such as "1.0" for integer fields.
     * Thingifier's field-reference relationship binding uses productId to find the product, so the
     * product id is normalized before that binding runs.
     */
    void coerceProductIdForFieldReference(final BodyParsedContext context) {
        final Integer productId = bodyInteger(context, "productId");
        if (productId == null) {
            return;
        }

        final Map<String, Object> body = editableBody(context);
        if (productIdAlreadyNormalized(body, productId)) {
            return;
        }

        body.put("productId", productId);
        context.replaceBodyFields(ApiBodyFields.fromMap(body));
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
     * A POST body id means "update this existing cart item". Thingifier now performs that update
     * for connected items, but this public API still reports 404 when the id is missing or belongs
     * to a different cart.
     */
    void rejectUnknownOrUnconnectedCartItemForPostBodyId(final BeforeValidationContext context) {
        if (!postBodyContainsCartItemId(context)) {
            return;
        }

        final EntityInstance cart = cartFromParent(context);
        final EntityInstance item = cartItemFromPostBody(context);
        if (!cartItemBelongsToCart(context.store(), cart, item)) {
            rejectCartItemNotFound(context);
        }
    }

    /**
     * Thingifier can update a connected cart item when the POST body contains an id. The Buggy API
     * still requires callers to send quantity for that update, so this rejects id-only update
     * bodies before Thingifier would treat them as a no-op update.
     */
    void rejectMissingQuantityForExistingCartItemUpdate(final BeforeValidationContext context) {
        if (!postBodyContainsCartItemId(context)) {
            return;
        }

        if (!quantityWasSupplied(context)) {
            rejectQuantityRequired(context);
        }
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
     * header. Thingifier now connects the item to the product from productId automatically. This
     * replaces Thingifier's generated create/update response after the data change succeeds.
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

        touchCart(context.store(), cart);
        returnCartItemWriteResponse(context, cart, item);
    }

    /**
     * The model tells Thingifier to delete the cart item when it is removed from the cart
     * relationship. This hook keeps the public JSON body and cart update tick used by the Shopping
     * Cart API.
     */
    void returnCartItemDeleteWorkflowResponse(final AfterActionContext context) {
        if (writeActionFailed(context)) {
            return;
        }

        final EntityInstance cart = cartFromParent(context);
        if (cart == null) {
            return;
        }

        touchCart(context.store(), cart);
        returnDeletedCartItemResponse(context, cart);
    }

    /**
     * The model tells Thingifier to delete cart items when their cart is deleted. This hook keeps
     * the public JSON body used by the Shopping Cart API.
     */
    void returnCartDeleteWorkflowResponse(final AfterActionContext context) {
        if (writeActionFailed(context)) {
            return;
        }

        returnDeletedCartResponse(context);
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

    private boolean productIdAlreadyNormalized(
            final Map<String, Object> body, final Integer productId) {
        final Object rawProductId = body.get("productId");
        return rawProductId instanceof Number
                && ((Number) rawProductId).intValue() == productId
                && String.valueOf(rawProductId).equals(String.valueOf(productId));
    }

    private boolean anyValidCartTokenCanUpdateExistingCartItemBugApplies(
            final ThingifierApiLifecycleContextView context) {
        return bugMode.bugsEnabled() && postBodyContainsCartItemId(context);
    }

    private EntityInstance cartItemFromPostBody(final ThingifierApiLifecycleContextView context) {
        return findById(context.store(), "cartitem", String.valueOf(bodyInteger(context, "id")));
    }

    private boolean cartItemBelongsToCart(
            final ThingStore store, final EntityInstance cart, final EntityInstance item) {
        return cart != null && item != null && cartContainsItem(store, cart, item);
    }

    private Integer quantityFromBody(final ThingifierApiLifecycleContextView context) {
        return bodyInteger(context, "quantity");
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

    private void returnDeletedCartResponse(final AfterActionContext context) {
        context.replaceApiResponse(
                ShoppingCartSupport.apiJsonResponse(
                        200,
                        ShoppingCartSupport.body(
                                "deletedCartId", Integer.parseInt(context.targetIdentifier()))));
    }

    private void rejectCartNotFound(final BeforeValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(404, "Cart not found"));
    }

    private void rejectBearerTokenDoesNotMatchCart(final BeforeValidationContext context) {
        context.shortCircuitWith(
                ShoppingCartSupport.apiError(403, "Bearer token does not match cart"));
    }

    private void rejectQuantityRequired(final BeforeValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(422, "quantity is required"));
    }

    private void rejectCartItemNotFound(final BeforeValidationContext context) {
        context.shortCircuitWith(ShoppingCartSupport.apiError(404, "Cart item not found"));
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

    private EntityInstance cartFromParent(final ThingifierApiLifecycleContextView context) {
        return findById(context.store(), "cart", context.parentIdentifier());
    }

    private boolean cartContainsItem(
            final ThingStore store, final EntityInstance cart, final EntityInstance item) {
        return ShoppingCartSupport.related(store, cart, "items").stream()
                .anyMatch(
                        related -> related.getPrimaryKeyValue().equals(item.getPrimaryKeyValue()));
    }

    private EntityInstance findById(
            final ThingStore store, final String entityName, final String identifier) {
        if (identifier == null) {
            return null;
        }
        return ShoppingCartSupport.findById(thingifier, store, entityName, identifier);
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
