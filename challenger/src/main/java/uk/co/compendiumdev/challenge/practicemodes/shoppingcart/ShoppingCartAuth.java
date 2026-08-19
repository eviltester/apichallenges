package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartAuth {
    static final String CART_TOKEN_SCHEME = "cartToken";

    private final Thingifier thingifier;

    ShoppingCartAuth(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    ThingifierApiAuthenticationResult authenticateKnownCartToken(
            final ThingifierApiAuthenticationContext context) {
        final EntityInstance cart = cartWithToken(context.store(), context.bearerToken());
        if (cart == null) {
            return ThingifierApiAuthenticationResult.rejected(
                    ShoppingCartSupport.apiError(403, "Bearer token does not match cart"));
        }

        return ThingifierApiAuthenticationResult.authenticated(
                new CartTokenPrincipal(cart.getPrimaryKeyValue()));
    }

    ThingifierApiAuthorizationResult requireParentCartToExist(
            final ThingifierApiAuthorizationContext context) {
        return requireCartToExist(context.store(), context.parentIdentifier());
    }

    ThingifierApiAuthorizationResult authorizeTokenForParentCart(
            final ThingifierApiAuthorizationContext context) {
        return authorizeTokenForCart(context, context.parentIdentifier());
    }

    ThingifierApiAuthorizationResult authorizeTokenForTargetCart(
            final ThingifierApiAuthorizationContext context) {
        return authorizeTokenForCart(context, context.targetIdentifier());
    }

    boolean authenticatedTokenMatchesCart(
            final ThingifierRequestContext context, final EntityInstance cart) {
        if (cart == null) {
            return false;
        }
        final Object principal = context.authenticatedPrincipal(CART_TOKEN_SCHEME);
        return principal instanceof CartTokenPrincipal
                && ((CartTokenPrincipal) principal).cartId().equals(cart.getPrimaryKeyValue());
    }

    CustomRouteAuthResult authorizeCustomRouteBearerTokenForCart(
            final EntityInstance cart, final String authorizationHeader) {
        final String token = ShoppingCartSupport.bearerToken(authorizationHeader);
        if (token.isEmpty()) {
            return CustomRouteAuthResult.rejected(401, "Missing bearer token");
        }

        if (cart == null) {
            return CustomRouteAuthResult.rejected(404, "Cart not found");
        }

        if (token.equals(ShoppingCartSupport.stringValue(cart, "token"))) {
            return CustomRouteAuthResult.ok();
        }

        return CustomRouteAuthResult.rejected(403, "Bearer token does not match cart");
    }

    private ThingifierApiAuthorizationResult authorizeTokenForCart(
            final ThingifierApiAuthorizationContext context, final String cartId) {
        final EntityInstance cart = cartById(context.store(), cartId);
        if (cart == null) {
            return cartNotFound();
        }

        if (authenticatedTokenMatchesCart(context.requestContext(), cart)) {
            return ThingifierApiAuthorizationResult.authorized();
        }

        return bearerTokenDoesNotMatchCart();
    }

    private ThingifierApiAuthorizationResult requireCartToExist(
            final ThingStore store, final String cartId) {
        if (cartById(store, cartId) == null) {
            return cartNotFound();
        }
        return ThingifierApiAuthorizationResult.authorized();
    }

    private EntityInstance cartWithToken(final ThingStore store, final String token) {
        final List<EntityInstance> carts = ShoppingCartSupport.list(thingifier, store, "cart");
        return carts.stream()
                .filter(cart -> token.equals(ShoppingCartSupport.stringValue(cart, "token")))
                .findFirst()
                .orElse(null);
    }

    private EntityInstance cartById(final ThingStore store, final String cartId) {
        return ShoppingCartSupport.findById(thingifier, store, "cart", cartId);
    }

    private ThingifierApiAuthorizationResult cartNotFound() {
        return ThingifierApiAuthorizationResult.rejected(
                ShoppingCartSupport.apiError(404, "Cart not found"));
    }

    private ThingifierApiAuthorizationResult bearerTokenDoesNotMatchCart() {
        return ThingifierApiAuthorizationResult.rejected(
                ShoppingCartSupport.apiError(403, "Bearer token does not match cart"));
    }

    private record CartTokenPrincipal(String cartId) {}

    record CustomRouteAuthResult(boolean authorized, int status, String message) {
        static CustomRouteAuthResult ok() {
            return new CustomRouteAuthResult(true, 0, "");
        }

        static CustomRouteAuthResult rejected(final int status, final String message) {
            return new CustomRouteAuthResult(false, status, message);
        }
    }
}
