package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartAuth {

    private final Thingifier thingifier;

    ShoppingCartAuth(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    AuthResult authorize(
            final ThingStore store,
            final EntityInstance cart,
            final String authorizationHeader,
            final boolean allowAnyValidCartToken) {

        final String token = ShoppingCartSupport.bearerToken(authorizationHeader);
        if (token.isEmpty()) {
            return AuthResult.rejected(401, "Missing bearer token");
        }

        if (cart == null) {
            return AuthResult.rejected(404, "Cart not found");
        }

        if (token.equals(ShoppingCartSupport.stringValue(cart, "token"))) {
            return AuthResult.ok();
        }

        if (allowAnyValidCartToken && tokenBelongsToAnyCart(store, token)) {
            return AuthResult.ok();
        }

        return AuthResult.rejected(403, "Bearer token does not match cart");
    }

    private boolean tokenBelongsToAnyCart(final ThingStore store, final String token) {
        final List<EntityInstance> carts = ShoppingCartSupport.list(thingifier, store, "cart");
        return carts.stream()
                .anyMatch(cart -> token.equals(ShoppingCartSupport.stringValue(cart, "token")));
    }

    record AuthResult(boolean authorized, int status, String message) {
        static AuthResult ok() {
            return new AuthResult(true, 0, "");
        }

        static AuthResult rejected(final int status, final String message) {
            return new AuthResult(false, status, message);
        }
    }
}
