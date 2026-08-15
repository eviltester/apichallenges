package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.delete;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.head;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.options;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.patch;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.post;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.put;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.query;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.trace;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.InternalHttpResponseToHttpServer;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.ThingifierHttpApiBridge;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

public final class ApiChallengeCanonicalThingifierRoutes {

    private static final String CANONICAL_API_PREFIX = "/api";
    private static final String TODOS_ROUTE = "/todos";
    private static final String LOCATION_HEADER = "Location";

    private final ThingifierHttpApiBridge bridge;
    private final List<HttpApiRequestHook> httpApiRequestHooks = new ArrayList<>();
    private final List<HttpApiResponseHook> httpApiResponseHooks = new ArrayList<>();
    private final Thingifier thingifier;
    private final String routePrefix;
    private final String locationPrefix;

    public ApiChallengeCanonicalThingifierRoutes(final Thingifier thingifier) {
        this(thingifier, CANONICAL_API_PREFIX, CANONICAL_API_PREFIX);
    }

    ApiChallengeCanonicalThingifierRoutes(
            final Thingifier thingifier, final String routePrefix, final String locationPrefix) {
        this.thingifier = thingifier;
        this.routePrefix = normalizePrefix(routePrefix);
        this.locationPrefix = normalizePrefix(locationPrefix);
        this.bridge =
                new ThingifierHttpApiBridge(thingifier, httpApiRequestHooks, httpApiResponseHooks);
    }

    public ApiChallengeCanonicalThingifierRoutes configure() {
        final ApiRoutingDefinition routeDefinitions =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate("");

        for (final RoutingDefinition routeDefinition : routeDefinitions.definitions()) {
            if (routeDefinition.isDisabled() || !isTodosRoute(routeDefinition.url())) {
                continue;
            }

            register(routeDefinition, canonicalPathFor(routeDefinition.url()));
        }

        return this;
    }

    public void registerHttpApiRequestHook(final HttpApiRequestHook hook) {
        httpApiRequestHooks.add(hook);
    }

    public void registerHttpApiResponseHook(final HttpApiResponseHook hook) {
        httpApiResponseHooks.add(hook);
    }

    private boolean isTodosRoute(final String routeUrl) {
        final String normalizedRouteUrl = withoutLeadingSlash(routeUrl);
        return withoutLeadingSlash(TODOS_ROUTE).equals(normalizedRouteUrl)
                || normalizedRouteUrl.startsWith(withoutLeadingSlash(TODOS_ROUTE) + "/");
    }

    private String canonicalPathFor(final String routeUrl) {
        return ApiChallengeRoutePath.withPrefix(routePrefix, "/" + withoutLeadingSlash(routeUrl));
    }

    private String withoutLeadingSlash(final String routeUrl) {
        if (routeUrl == null || routeUrl.isEmpty()) {
            return "";
        }

        if (routeUrl.startsWith("/")) {
            return routeUrl.substring(1);
        }

        return routeUrl;
    }

    private void register(final RoutingDefinition routeDefinition, final String path) {
        switch (routeDefinition.verb()) {
            case GET ->
                    get(
                            path,
                            dynamicHandler(
                                    (internalRequest, request) -> bridge.get(internalRequest)));
            case POST ->
                    post(
                            path,
                            dynamicHandler(
                                    (internalRequest, request) -> bridge.post(internalRequest)));
            case QUERY -> registerQuery(routeDefinition, path);
            case HEAD ->
                    head(
                            path,
                            dynamicHandler(
                                    (internalRequest, request) -> bridge.head(internalRequest)));
            case DELETE -> registerDelete(routeDefinition, path);
            case PATCH -> registerPatch(routeDefinition, path);
            case PUT -> registerPut(routeDefinition, path);
            case OPTIONS -> options(path, staticHandler(routeDefinition));
            case TRACE -> trace(path, staticHandler(routeDefinition));
            default -> {
                // No route required.
            }
        }
    }

    private void registerQuery(final RoutingDefinition routeDefinition, final String path) {
        if (routeDefinition.status().isReturnedFromCall()) {
            query(
                    path,
                    dynamicHandler(
                            (internalRequest, request) -> bridge.queryRequest(internalRequest)));
        } else {
            query(path, staticHandler(routeDefinition));
        }
    }

    private void registerDelete(final RoutingDefinition routeDefinition, final String path) {
        if (routeDefinition.status().isReturnedFromCall()) {
            delete(
                    path,
                    dynamicHandler((internalRequest, request) -> bridge.delete(internalRequest)));
        } else {
            delete(path, staticHandler(routeDefinition));
        }
    }

    private void registerPatch(final RoutingDefinition routeDefinition, final String path) {
        if (routeDefinition.status().isReturnedFromCall()) {
            patch(
                    path,
                    dynamicHandler((internalRequest, request) -> bridge.patch(internalRequest)));
        } else {
            patch(path, staticHandler(routeDefinition));
        }
    }

    private void registerPut(final RoutingDefinition routeDefinition, final String path) {
        if (routeDefinition.status().isReturnedFromCall()) {
            put(path, dynamicHandler((internalRequest, request) -> bridge.put(internalRequest)));
        } else {
            put(path, staticHandler(routeDefinition));
        }
    }

    private HttpRouteHandler dynamicHandler(final BridgeCall bridgeCall) {
        return (request, response) -> {
            final InternalHttpRequest internalRequest = internalRequestFrom(request);
            final InternalHttpResponse internalResponse = bridgeCall.run(internalRequest, request);
            rewriteLocationHeader(internalResponse);
            return InternalHttpResponseToHttpServer.convert(internalResponse, response);
        };
    }

    private HttpRouteHandler staticHandler(final RoutingDefinition routeDefinition) {
        return (request, response) -> {
            applyStaticResponse(routeDefinition, response);
            return "";
        };
    }

    private InternalHttpRequest internalRequestFrom(final HttpServerRequest request) {
        final InternalHttpRequest internalRequest =
                HttpServerRequestToInternalHttpRequest.convert(request);
        internalRequest.setPath(stripRoutePrefix(request.pathInfo()));
        internalRequest.setUrl(stripRoutePrefixFromUrl(request.url()));
        return internalRequest;
    }

    private String stripRoutePrefix(final String path) {
        if (path == null) {
            return "";
        }

        if (routePrefix.isEmpty()) {
            return path;
        }

        if (path.startsWith(routePrefix + "/")) {
            return path.substring(routePrefix.length());
        }

        return path;
    }

    private String stripRoutePrefixFromUrl(final String url) {
        if (url == null) {
            return "";
        }

        if (routePrefix.isEmpty()) {
            return url;
        }

        final String canonicalTodosPath = routePrefix + TODOS_ROUTE;
        final int prefixIndex = url.indexOf(canonicalTodosPath);
        if (prefixIndex < 0) {
            return url;
        }

        return url.substring(0, prefixIndex) + url.substring(prefixIndex + routePrefix.length());
    }

    private void rewriteLocationHeader(final InternalHttpResponse internalResponse) {
        if (!internalResponse.hasHeader(LOCATION_HEADER)) {
            return;
        }

        internalResponse.setHeader(
                LOCATION_HEADER, canonicalLocation(internalResponse.getHeader(LOCATION_HEADER)));
    }

    private String canonicalLocation(final String location) {
        if (location == null || location.isBlank()) {
            return location;
        }

        if (locationPrefix.isEmpty()) {
            return legacyLocation(location);
        }

        if (location.startsWith(locationPrefix + "/")) {
            return location;
        }

        if (location.startsWith(TODOS_ROUTE)) {
            return locationPrefix + location;
        }

        if (location.startsWith("todos")) {
            return locationPrefix + "/" + location;
        }

        try {
            final URI uri = new URI(location);
            if (uri.getPath() != null && uri.getPath().startsWith(locationPrefix + TODOS_ROUTE)) {
                return location;
            }

            if (uri.getPath() != null && uri.getPath().startsWith(TODOS_ROUTE)) {
                return new URI(
                                uri.getScheme(),
                                uri.getUserInfo(),
                                uri.getHost(),
                                uri.getPort(),
                                locationPrefix + uri.getPath(),
                                uri.getQuery(),
                                uri.getFragment())
                        .toString();
            }
        } catch (final URISyntaxException | IllegalArgumentException ignored) {
            // Leave unfamiliar Location values unchanged.
        }

        return location;
    }

    private String legacyLocation(final String location) {
        if (location.startsWith(CANONICAL_API_PREFIX + TODOS_ROUTE)) {
            return location.substring(CANONICAL_API_PREFIX.length());
        }

        if (location.startsWith(withoutLeadingSlash(CANONICAL_API_PREFIX + TODOS_ROUTE))) {
            return location.substring(withoutLeadingSlash(CANONICAL_API_PREFIX + "/").length());
        }

        try {
            final URI uri = new URI(location);
            if (uri.getPath() != null
                    && uri.getPath().startsWith(CANONICAL_API_PREFIX + TODOS_ROUTE)) {
                return new URI(
                                uri.getScheme(),
                                uri.getUserInfo(),
                                uri.getHost(),
                                uri.getPort(),
                                uri.getPath().substring(CANONICAL_API_PREFIX.length()),
                                uri.getQuery(),
                                uri.getFragment())
                        .toString();
            }
        } catch (final URISyntaxException | IllegalArgumentException ignored) {
            // Leave unfamiliar Location values unchanged.
        }

        return location;
    }

    private String normalizePrefix(final String prefix) {
        if (prefix == null || prefix.isBlank() || "/".equals(prefix.trim())) {
            return "";
        }

        final String trimmed = prefix.trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    private void applyStaticResponse(
            final RoutingDefinition routeDefinition, final HttpServerResponse response) {
        response.status(routeDefinition.status().value());

        if (!routeDefinition.header().isEmpty()) {
            response.header(routeDefinition.header(), routeDefinition.headerValue());
        }

        if (routeDefinition.hasResponseHeaders()) {
            for (final String headerName : routeDefinition.getResponseHeaderNames()) {
                response.header(headerName, routeDefinition.getResponseHeaderValue(headerName));
            }
        }

        if (routeDefinition.headerValue().contains("QUERY")) {
            response.header(
                    ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                    ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
        }
    }

    @FunctionalInterface
    private interface BridgeCall {
        InternalHttpResponse run(
                InternalHttpRequest internalRequest, HttpServerRequest serverRequest);
    }
}
