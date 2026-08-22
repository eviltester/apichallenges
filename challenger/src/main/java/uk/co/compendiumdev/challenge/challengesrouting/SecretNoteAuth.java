package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;

final class SecretNoteAuth {
    private static final String LIVE_WIDGET_HEADER = "X-API-Challenges-Live-Widget";
    private final Challengers challengers;

    SecretNoteAuth(final Challengers challengers) {
        this.challengers = challengers;
    }

    ThingifierApiAuthenticationResult authenticateAdminPassword(
            final ThingifierApiAuthenticationContext context) {
        if (!"admin".equals(context.basicUsername())
                || !"password".equals(context.basicPassword())) {
            if ("true".equalsIgnoreCase(context.headers().get(LIVE_WIDGET_HEADER))) {
                final ApiResponse response = ApiResponse.error(401, "");
                response.clearBody();
                return ThingifierApiAuthenticationResult.rejected(response);
            }
            return ThingifierApiAuthenticationResult.rejected(401, "");
        }

        final String challengerId = context.headers().get("X-CHALLENGER");
        if (missing(challengerId)) {
            return ThingifierApiAuthenticationResult.authenticated("admin").useDefaultDataScope();
        }

        final ChallengerAuthData challenger = challengers.getChallenger(challengerId);
        if (challenger == null) {
            final ApiResponse response = ApiResponse.error(401, "");
            response.clearBody();
            response.setHeader("X-CHALLENGER", XChallengerHeader.NOT_FOUND_ERROR_MESSAGE);
            return ThingifierApiAuthenticationResult.rejected(response);
        }

        return ThingifierApiAuthenticationResult.authenticated("admin")
                .useDataScope(
                        challenger.getXChallenger(),
                        DataScopeCreationPolicy.ENSURE_CREATED_AND_POPULATED);
    }

    ThingifierApiAuthenticationResult authenticateSecretToken(
            final ThingifierApiAuthenticationContext context) {
        final String challengerId = context.headers().get("X-CHALLENGER");
        final String token = context.authCredential();

        if (missing(challengerId)) {
            if (AuthRoutes.READ_ONLY_AUTH_TOKEN.equals(token)) {
                return ThingifierApiAuthenticationResult.authenticated(
                                SecretPrincipal.readOnlyPrincipal())
                        .useDefaultDataScope();
            }
            return ThingifierApiAuthenticationResult.rejected(rejection(403));
        }

        final ChallengerAuthData challenger = challengers.getChallenger(challengerId);
        if (challenger == null) {
            return ThingifierApiAuthenticationResult.rejected(rejection(401));
        }

        if (!challenger.getXAuthToken().equals(token)) {
            return ThingifierApiAuthenticationResult.rejected(rejection(403));
        }

        return ThingifierApiAuthenticationResult.authenticated(
                        SecretPrincipal.challenger(challenger.getXChallenger()))
                .useDataScope(
                        challenger.getXChallenger(),
                        DataScopeCreationPolicy.ENSURE_CREATED_AND_POPULATED);
    }

    ThingifierApiAuthorizationResult authorizeSecretNote(
            final ThingifierApiAuthorizationContext context) {
        final SecretPrincipal principal = SecretPrincipal.from(context.principal());
        if (principal == null) {
            return unauthorized();
        }

        if (principal.readOnly()
                && context.verb() != RoutingVerb.GET
                && context.verb() != RoutingVerb.HEAD) {
            return unauthorized();
        }

        return ThingifierApiAuthorizationResult.authorized();
    }

    private ThingifierApiAuthorizationResult unauthorized() {
        return ThingifierApiAuthorizationResult.rejected(rejection(401));
    }

    private ApiResponse rejection(final int statusCode) {
        final ApiResponse response = ApiResponse.error(statusCode, "");
        response.clearBody();
        if (statusCode == 401) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
        return response;
    }

    private boolean missing(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private record SecretPrincipal(String challengerId, boolean readOnly) {
        static SecretPrincipal readOnlyPrincipal() {
            return new SecretPrincipal("", true);
        }

        static SecretPrincipal challenger(final String challengerId) {
            return new SecretPrincipal(challengerId, false);
        }

        static SecretPrincipal from(final Object principal) {
            if (principal instanceof SecretPrincipal secretPrincipal) {
                return secretPrincipal;
            }
            return null;
        }
    }
}
