package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

/**
 * Thingifier creates the basic Shopping Cart routes from the model. The public Buggy API needs
 * extra behaviour on top of those generated routes, such as cart token checks, custom response
 * bodies, data cleanup, and deliberate bugs. This class registers those hooks in one place.
 */
final class ShoppingCartHookRoutes {
    private static final List<String> SHOP_API_HOOK_ENDPOINTS =
            List.of(
                    "/shop/products",
                    "/shop/products/:productId",
                    "/shop/carts",
                    "/shop/carts/:cartId",
                    "/shop/carts/:cartId/items",
                    "/shop/carts/:cartId/items/:itemId",
                    "/shop/cartitems",
                    "/shop/cartitems/:itemId",
                    "/shop/register",
                    "/shop/checkout/:cartId");

    private final ThingifierHttpApiRoutings shopRouting;
    private final ShoppingCartWriteLifecycleHooks writeHooks;
    private final ShoppingCartMaintenance maintenance;

    ShoppingCartHookRoutes(
            final ThingifierHttpApiRoutings shopRouting,
            final Thingifier shoppingCart,
            final ShoppingCartBugMode bugMode,
            final ShoppingCartState state,
            final ShoppingCartMaintenance maintenance) {
        this.shopRouting = shopRouting;
        this.writeHooks =
                new ShoppingCartWriteLifecycleHooks(shoppingCart, bugMode, state, maintenance);
        this.maintenance = maintenance;
    }

    /** Register every hook that changes generated routes into the public Shopping Cart API. */
    void register() {
        registerCartItemWriteLifecycleHooks();
        registerCartItemDeleteLifecycleHooks();
        registerCartDeleteLifecycleHooks();
        registerMaintenanceAfterShopApiResponses();
        registerBuggyCorsHeadersThatAllowCredentialsForWildcardOrigin();
        registerBuggyCorsPreflightThatReadsAllowMethodsRequestHeader();
    }

    /**
     * POST /shop/carts/{cartId}/items is where users add a product to a cart. The generated route
     * can create a cart item record, and Thingifier route auth checks that the bearer token belongs
     * to a cart. These hooks add the remaining shopping-cart rules: treat a body id as an item
     * quantity update, preserve the classic cross-cart token bug for that update, copy price and
     * stock from the product, enforce quantity and closed-cart rules, and return JSON with cart and
     * item details.
     */
    private void registerCartItemWriteLifecycleHooks() {
        final HookScope postCartItems =
                HookScope.endpointAndVerbs("/carts/:cartId/items", RoutingVerb.POST);

        shopRouting.registerBodyParsedHook(
                postCartItems, writeHooks::allowHiddenUnitPriceAtAddOverrideBug);
        shopRouting.registerBodyParsedHook(
                postCartItems, writeHooks::allowHiddenStockAtAddOverrideBug);
        shopRouting.registerBeforeValidationHook(
                postCartItems, writeHooks::allowAnyValidCartTokenToUpdateExistingCartItemBug);
        shopRouting.registerBeforeValidationHook(
                postCartItems, writeHooks::updateExistingCartItemWhenPostBodyContainsId);
        shopRouting.registerAfterValidationHook(
                postCartItems, writeHooks::rejectMissingProductOrQuantity);
        shopRouting.registerAfterValidationHook(
                postCartItems, writeHooks::allowQuantityLessThanZeroBug);
        shopRouting.registerAfterValidationHook(postCartItems, writeHooks::allowQuantityZeroBug);
        shopRouting.registerAfterValidationHook(
                postCartItems, writeHooks::allowQuantityGreaterThanStockBug);
        shopRouting.registerAfterValidationHook(postCartItems, writeHooks::rejectUnknownProduct);
        shopRouting.registerBeforeActionHook(
                postCartItems, writeHooks::allowClosedCartModificationBug);
        shopRouting.registerAfterActionHook(
                postCartItems, writeHooks::returnCartItemWriteWorkflowResponse);
    }

    /**
     * DELETE /shop/carts/{cartId}/items/{itemId} removes an item from a user's cart. The generated
     * route can remove the cart-to-item relationship, and Thingifier route auth checks the cart's
     * bearer token. These hooks add closed-cart bug handling, deletion of the underlying item
     * record, cart update tracking, and a JSON body showing what changed.
     */
    private void registerCartItemDeleteLifecycleHooks() {
        final HookScope deleteCartItem =
                HookScope.endpointAndVerbs("/carts/:cartId/items/:itemId", RoutingVerb.DELETE);

        shopRouting.registerBeforeActionHook(
                deleteCartItem, writeHooks::allowClosedCartModificationBug);
        shopRouting.registerAfterActionHook(
                deleteCartItem, writeHooks::returnCartItemDeleteWorkflowResponse);
    }

    /**
     * DELETE /shop/carts/{cartId} must delete the cart's items before the cart disappears, so the
     * Shopping Cart API replaces Thingifier's generated delete action after Thingifier route auth
     * checks the cart's bearer token.
     */
    private void registerCartDeleteLifecycleHooks() {
        final HookScope deleteCart =
                HookScope.endpointAndVerbs("/carts/:cartId", RoutingVerb.DELETE);

        shopRouting.registerBeforeActionHook(
                deleteCart, writeHooks::deleteCartAndItemsInsteadOfNativeDelete);
    }

    /**
     * Keep the practice API self-resetting after normal use: old carts are pruned and the product
     * catalogue is restocked so repeated exploratory tests do not run out of usable products.
     */
    private void registerMaintenanceAfterShopApiResponses() {
        registerShopResponseHook(new ShoppingCartMaintenanceHook(maintenance));
    }

    /**
     * The Buggy API intentionally exposes bad CORS headers so testers have something realistic to
     * find: wildcard origin and headers combined with credentials, methods, and exposed headers.
     */
    private void registerBuggyCorsHeadersThatAllowCredentialsForWildcardOrigin() {
        registerShopResponseHook(new ShoppingCartCorsHeadersResponseHook());
    }

    /**
     * Preserve the deliberate preflight bug where the response trusts a request's
     * Access-Control-Allow-Methods header instead of checking the methods the client actually
     * requested.
     */
    private void registerBuggyCorsPreflightThatReadsAllowMethodsRequestHeader() {
        registerShopResponseHook(new ShoppingCartCorsPreflightResponseHook(), RoutingVerb.OPTIONS);
    }

    /**
     * Attach a response hook to every Shopping Cart API endpoint that might change data or need
     * restocking and cleanup afterwards.
     */
    private void registerShopResponseHook(final InternalHttpResponseHook hook) {
        for (String endpoint : SHOP_API_HOOK_ENDPOINTS) {
            shopRouting.registerInternalHttpResponseHook(endpoint, hook);
        }
    }

    /** Attach a response hook only for selected verbs, such as OPTIONS preflight handling. */
    private void registerShopResponseHook(
            final InternalHttpResponseHook hook, final RoutingVerb... verbs) {
        for (String endpoint : SHOP_API_HOOK_ENDPOINTS) {
            shopRouting.registerInternalHttpResponseHook(
                    HookScope.endpointAndVerbs(endpoint, verbs), hook);
        }
    }
}
