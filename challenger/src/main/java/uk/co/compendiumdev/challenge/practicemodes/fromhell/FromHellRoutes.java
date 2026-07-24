package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.redirect;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteVerb;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.swaggerizer.SwaggerUiPage;

public final class FromHellRoutes {

    public static final String PREFIX = "/fromhell";
    public static final String ABOUT_PATH = "/practice-modes/fromhell";
    public static final String OPENAPI_JSON_PATH = PREFIX + "/docs/openapi.json";
    public static final String SWAGGER_DOWNLOAD_PATH = PREFIX + "/docs/swagger";
    public static final String SWAGGER_UI_PATH = PREFIX + "/docs/swagger-ui";

    private final DefaultGUIHTML guiTemplates;
    private final FromHellCatalog catalog;
    private final FromHellOpenApi openApi;

    public FromHellRoutes(final DefaultGUIHTML guiTemplates) {
        this(guiTemplates, FromHellCatalog.loadDefault());
    }

    FromHellRoutes(final DefaultGUIHTML guiTemplates, final FromHellCatalog catalog) {
        this.guiTemplates = guiTemplates;
        this.catalog = catalog;
        this.openApi = new FromHellOpenApi(catalog);
    }

    public void configure() {
        redirect.get(PREFIX, ABOUT_PATH);

        for (String path : catalog.paths()) {
            configurePath(path);
        }

        configureDocsRoutes();
    }

    private void configurePath(final String endpointPath) {
        final String path = PREFIX + endpointPath;
        final List<HttpRouteVerb> allowedVerbs = allowedVerbs(endpointPath);

        for (FromHellEndpoint endpoint : catalog.endpointsForPath(endpointPath)) {
            route(
                    routeVerb(endpoint.method()),
                    path,
                    (request, response) -> {
                        applyEndpointResponse(response, endpoint);
                        if (shouldForceBody(endpoint)) {
                            response.forceBody(endpoint.body());
                            return "";
                        }
                        return endpoint.body();
                    });
        }

        if (allowedVerbs.contains(HttpRouteVerb.HEAD)) {
            final FromHellEndpoint getEndpoint = catalog.endpoint("GET", endpointPath);
            route(
                    HttpRouteVerb.HEAD,
                    path,
                    (request, response) -> {
                        applyEndpointResponse(response, getEndpoint);
                        return "";
                    });
        }

        route(
                HttpRouteVerb.OPTIONS,
                path,
                (request, response) -> {
                    response.status(204);
                    response.header("Allow", allowHeader(allowedVerbs));
                    applyCommonHeaders(response::header);
                    return "";
                });

        for (HttpRouteVerb verb : HttpRouteVerb.values()) {
            if (!allowedVerbs.contains(verb)) {
                route(verb, path, (request, response) -> methodNotAllowed(response, allowedVerbs));
            }
        }
    }

    private void applyEndpointResponse(
            final HttpServerResponse response, final FromHellEndpoint endpoint) {
        response.status(endpoint.statusCode());
        applyCommonHeaders(response::header);
        for (FromHellHeader header : endpoint.headers()) {
            response.header(header.name(), header.value());
        }
        if (endpoint.contentType().isEmpty()) {
            response.suppressContentType();
        }
    }

    private boolean shouldForceBody(final FromHellEndpoint endpoint) {
        return !endpoint.body().isEmpty()
                && (endpoint.statusCode() == 204 || endpoint.statusCode() == 304);
    }

    private String methodNotAllowed(
            final HttpServerResponse response, final List<HttpRouteVerb> allowedVerbs) {
        response.status(405);
        response.header("Allow", allowHeader(allowedVerbs));
        applyCommonHeaders(response::header);
        return "Method Not Allowed";
    }

    private List<HttpRouteVerb> allowedVerbs(final String endpointPath) {
        final List<HttpRouteVerb> allowedVerbs = new ArrayList<>();
        boolean hasGet = false;
        boolean hasHead = false;
        for (FromHellEndpoint endpoint : catalog.endpointsForPath(endpointPath)) {
            final HttpRouteVerb verb = routeVerb(endpoint.method());
            addIfMissing(allowedVerbs, verb);
            hasGet = hasGet || verb == HttpRouteVerb.GET;
            hasHead = hasHead || verb == HttpRouteVerb.HEAD;
        }
        if (hasGet && !hasHead) {
            addIfMissing(allowedVerbs, HttpRouteVerb.HEAD);
        }
        addIfMissing(allowedVerbs, HttpRouteVerb.OPTIONS);
        return allowedVerbs;
    }

    private void addIfMissing(final List<HttpRouteVerb> verbs, final HttpRouteVerb verb) {
        if (!verbs.contains(verb)) {
            verbs.add(verb);
        }
    }

    private String allowHeader(final List<HttpRouteVerb> verbs) {
        final List<String> methodNames = new ArrayList<>();
        for (HttpRouteVerb verb : verbs) {
            methodNames.add(verb.name());
        }
        return String.join(", ", methodNames);
    }

    private HttpRouteVerb routeVerb(final String method) {
        try {
            return HttpRouteVerb.valueOf(method.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unsupported API From Hell method " + method, e);
        }
    }

    private void configureDocsRoutes() {
        route(
                HttpRouteVerb.GET,
                OPENAPI_JSON_PATH,
                (request, response) -> {
                    response.status(200);
                    response.type("application/json");
                    return openApi.asJsonWithPreferredServer(FromHellRequestOrigin.from(request));
                });

        route(
                HttpRouteVerb.GET,
                SWAGGER_DOWNLOAD_PATH,
                (request, response) -> {
                    response.status(200);
                    response.header("Content-Type", "application/octet-stream");
                    response.header(
                            "Content-Disposition",
                            "attachment; filename=\"api-from-hell-openapi.json\"");
                    return openApi.asJsonWithPreferredServer(FromHellRequestOrigin.from(request));
                });

        route(
                HttpRouteVerb.GET,
                SWAGGER_UI_PATH,
                (request, response) -> {
                    response.status(200);
                    response.type("text/html");
                    return new SwaggerUiPage(
                                    docsDefinition(),
                                    guiTemplates,
                                    OPENAPI_JSON_PATH,
                                    ABOUT_PATH,
                                    SWAGGER_DOWNLOAD_PATH,
                                    SWAGGER_UI_PATH)
                            .html();
                });
    }

    private ThingifierApiDocumentationDefn docsDefinition() {
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setTitle(catalog.name());
        apiDefn.setDescription(catalog.description());
        apiDefn.setVersion("1.0.0");
        apiDefn.setSeoTitle("API From Hell Swagger UI | API Challenges");
        apiDefn.setSwaggerUiTitle("API From Hell - Swagger UI");
        apiDefn.setSeoDescription(
                "Use the API From Hell Swagger UI to import and exercise deliberately awkward"
                        + " canned API responses.");
        apiDefn.setMetaRobots("noindex,follow");
        apiDefn.setOgType("website");
        apiDefn.setTwitterCard("summary_large_image");
        return apiDefn;
    }

    private void applyCommonHeaders(final HeaderConsumer headerConsumer) {
        headerConsumer.header("Access-Control-Allow-Origin", "*");
        headerConsumer.header(
                "Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS");
        headerConsumer.header(
                "Access-Control-Allow-Headers",
                "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With");
    }

    private interface HeaderConsumer {
        void header(String name, String value);
    }
}
