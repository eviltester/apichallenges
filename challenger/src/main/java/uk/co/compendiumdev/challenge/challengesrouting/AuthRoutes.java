package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.SimpleHttpRouteCreator;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

public class AuthRoutes {
    static final String READ_ONLY_AUTH_TOKEN = "00000000-0000-4000-8000-000000000000";
    static final String READ_ONLY_SECRET_NOTE = "This is the read-only secret note.";

    public void configure(
            final Challengers challengers, final ThingifierApiDocumentationDefn apiDefn) {
        configure(challengers, apiDefn, "");
    }

    public void configure(
            final Challengers challengers,
            final ThingifierApiDocumentationDefn apiDefn,
            final String pathPrefix) {

        final String secretTokenPath =
                ApiChallengeRoutePath.withPrefix(pathPrefix, "/secret/token");
        final String secretNotePath = ApiChallengeRoutePath.withPrefix(pathPrefix, "/secret/note");

        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token

        SimpleHttpRouteCreator.addHandler(
                secretTokenPath,
                "options",
                (request, result) -> {
                    result.status(204);
                    result.header("Allow", "GET, OPTIONS");
                    return "";
                });

        SimpleHttpRouteCreator.addHandler(
                secretNotePath,
                "options",
                (request, result) -> {
                    result.status(204);
                    // disallow POST, DELETE, PATCH, TRACE
                    result.header("Allow", "GET, HEAD, POST, OPTIONS");
                    return "";
                });

        final SecretNoteAuth secretNoteAuth = new SecretNoteAuth(challengers);
        final Thingifier secretNoteStore =
                new SecretThingifier().get(pathPrefix, secretNoteAuth, challengers);
        final SecretNoteModelSupport secretNoteModel =
                new SecretNoteModelSupport(secretNoteStore, challengers);
        final ThingifierHttpApiRoutings secretRoutes =
                new ThingifierHttpApiRoutings(
                        secretNoteStore, routingDocumentation(secretNoteStore, pathPrefix));
        secretRoutes.registerHttpApiResponseHook(secretNoteModel.responseHook());
        secretRoutes.registerInternalHttpRequestHook(this::acceptXAuthTokenAsBearerToken);

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                secretTokenPath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "GET %s with basic auth to get an X-AUTH-TOKEN header and token response body for access to %s."
                                        .formatted(secretTokenPath, secretNotePath))
                        .addPossibleStatuses(200, 401)
                        .secureWithBasicAuth());

        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                secretNotePath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "GET %s with X-AUTH-TOKEN to return the secret note for the user."
                                        .formatted(secretNotePath))
                        .addPossibleStatuses(200, 401, 403)
                        .addCustomHeader("X-AUTH-TOKEN", "string"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                secretNotePath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "POST %s with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note."
                                        .formatted(secretNotePath))
                        .addPossibleStatuses(200, 400, 401, 403, 422)
                        .addCustomHeader("X-AUTH-TOKEN", "string"));
    }

    private ThingifierApiDocumentationDefn routingDocumentation(
            final Thingifier secretNoteStore, final String pathPrefix) {
        final ThingifierApiDocumentationDefn routeDefn = new ThingifierApiDocumentationDefn();
        routeDefn.setThingifier(secretNoteStore);
        if (!missing(pathPrefix)) {
            routeDefn.setPathPrefix(pathPrefix);
        }
        return routeDefn;
    }

    private InternalHttpResponse acceptXAuthTokenAsBearerToken(final InternalHttpRequest request) {
        if (!isSecretNoteRoute(request.getPath())) {
            return null;
        }

        final String token = request.getHeader("X-AUTH-TOKEN");
        if (!missing(token) && !isBearerAuth(request.getHeader("Authorization"))) {
            request.addHeader("Authorization", "Bearer " + token);
        }

        return null;
    }

    private boolean isSecretNoteRoute(final String path) {
        final String normalizedPath = path == null ? "" : path.trim().replace('\\', '/');
        return "secret/note".equals(withoutLeadingSlash(normalizedPath))
                || withoutLeadingSlash(normalizedPath).endsWith("/secret/note");
    }

    private String withoutLeadingSlash(final String value) {
        String normalized = value;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private boolean missing(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isBearerAuth(final String value) {
        return value != null && value.trim().toLowerCase().startsWith("bearer ");
    }
}
