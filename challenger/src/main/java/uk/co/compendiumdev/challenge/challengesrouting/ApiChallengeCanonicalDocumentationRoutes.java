package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import java.util.function.Supplier;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.RestApiDocumentationGenerator;
import uk.co.compendiumdev.thingifier.swaggerizer.OpenApiSpecificationVersion;
import uk.co.compendiumdev.thingifier.swaggerizer.ScalarUiPage;
import uk.co.compendiumdev.thingifier.swaggerizer.SwaggerGenerationConfig;
import uk.co.compendiumdev.thingifier.swaggerizer.SwaggerUiPage;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

public final class ApiChallengeCanonicalDocumentationRoutes {

    private static final String API_PREFIX = "/api";
    private static final String THINGIFIER_DOC_PREFIX = "//api";
    private static final String DOCS_PATH = API_PREFIX + "/docs";
    private static final String OPENAPI_PATH = DOCS_PATH + "/openapi.json";
    private static final String OPENAPI_30_PATH = DOCS_PATH + "/openapi-3.0.json";
    private static final String OPENAPI_31_PATH = DOCS_PATH + "/openapi-3.1.json";
    private static final String OPENAPI_32_PATH = DOCS_PATH + "/openapi-3.2.json";
    private static final String SWAGGER_PATH = DOCS_PATH + "/swagger";
    private static final String SWAGGER_UI_PATH = DOCS_PATH + "/swagger-ui";
    private static final String SCALAR_UI_PATH = DOCS_PATH + "/scalar-ui";

    private final Thingifier thingifier;
    private final ThingifierApiDocumentationDefn apiDefn;
    private final DefaultGUIHTML guiTemplates;

    public ApiChallengeCanonicalDocumentationRoutes(
            final Thingifier thingifier,
            final ThingifierApiDocumentationDefn apiDefn,
            final DefaultGUIHTML guiTemplates) {
        this.thingifier = thingifier;
        this.apiDefn = apiDefn;
        this.guiTemplates = guiTemplates;
    }

    public void configure() {
        get(DOCS_PATH, (request, response) -> apiDocs(response));
        get(OPENAPI_PATH, this::openApi31);
        get(OPENAPI_30_PATH, this::openApi30);
        get(OPENAPI_31_PATH, this::openApi31);
        get(OPENAPI_32_PATH, this::openApi32);
        get(SWAGGER_PATH, this::legacySwaggerJson);
        get(SWAGGER_UI_PATH, (request, response) -> swaggerUi(response));
        get(SCALAR_UI_PATH, (request, response) -> scalarUi(response));
    }

    private String apiDocs(final HttpServerResponse response) {
        response.type("text/html");
        response.status(200);

        return withCanonicalThingifierDocsPrefix(
                () -> {
                    final ApiRoutingDefinition routes =
                            new ApiRoutingDefinitionDocGenerator(thingifier)
                                    .generate(THINGIFIER_DOC_PREFIX);
                    return normaliseCanonicalDocsHtml(
                            new RestApiDocumentationGenerator(thingifier, guiTemplates)
                                    .getApiDocumentation(
                                            routes,
                                            apiDefn.getAdditionalRoutes(),
                                            apiDefn,
                                            API_PREFIX,
                                            DOCS_PATH));
                });
    }

    private String openApi30(final HttpServerRequest request, final HttpServerResponse response) {
        return openApi(request, response, OPENAPI_30_PATH, OpenApiSpecificationVersion.OPENAPI_3_0);
    }

    private String openApi31(final HttpServerRequest request, final HttpServerResponse response) {
        return openApi(request, response, request.path(), OpenApiSpecificationVersion.OPENAPI_3_1);
    }

    private String openApi32(final HttpServerRequest request, final HttpServerResponse response) {
        return openApi(request, response, OPENAPI_32_PATH, OpenApiSpecificationVersion.OPENAPI_3_2);
    }

    private String legacySwaggerJson(
            final HttpServerRequest request, final HttpServerResponse response) {
        final SwaggerGenerationConfig config =
                swaggerGenerationConfig(OpenApiSpecificationVersion.OPENAPI_3_1, request);
        response.header(
                "Content-Disposition",
                "attachment; filename=\"" + downloadFilename("swagger.json", config) + "\"");
        response.header("Content-Type", "application/octet-stream");
        response.status(200);
        return withCanonicalThingifierDocsPrefix(
                () ->
                        new Swaggerizer(apiDefn)
                                .asJsonWithPreferredServer(config, requestOrigin(request)));
    }

    private String openApi(
            final HttpServerRequest request,
            final HttpServerResponse response,
            final String openApiPath,
            final OpenApiSpecificationVersion version) {
        response.type("application/json");
        response.status(200);

        final SwaggerGenerationConfig config = swaggerGenerationConfig(version, request);
        if (request.queryParam("download") != null) {
            response.header(
                    "Content-Disposition",
                    "attachment; filename=\""
                            + openApiDownloadFilename(openApiPath, config)
                            + "\"");
        }

        return withCanonicalThingifierDocsPrefix(
                () ->
                        new Swaggerizer(apiDefn)
                                .asJsonWithPreferredServer(config, requestOrigin(request)));
    }

    private SwaggerGenerationConfig swaggerGenerationConfig(
            final OpenApiSpecificationVersion version, final HttpServerRequest request) {
        final boolean permissive = request.queryParam("permissive") != null;
        final boolean strongSchemas = truthyQueryParam(request.queryParam("strongschema"));

        final SwaggerGenerationConfig config = new SwaggerGenerationConfig();
        config.openApiSpecificationVersion = version;
        config.includeMethodNotAllowedEndpoints = permissive;
        config.includeFieldValidation = !permissive || strongSchemas;
        config.strongSchemas = strongSchemas;
        config.pathParameterPlacement = pathParameterPlacement(request.queryParam("pathparams"));
        return config;
    }

    private boolean truthyQueryParam(final String value) {
        if (value == null) {
            return false;
        }
        return !"false".equalsIgnoreCase(value.trim()) && !"0".equals(value.trim());
    }

    private SwaggerGenerationConfig.PathParameterPlacement pathParameterPlacement(
            final String placement) {
        if ("operation".equalsIgnoreCase(placement)) {
            return SwaggerGenerationConfig.PathParameterPlacement.OPERATION;
        }
        return SwaggerGenerationConfig.PathParameterPlacement.PATH;
    }

    private String openApiDownloadFilename(
            final String openApiPath, final SwaggerGenerationConfig config) {
        return downloadFilename(openApiPath.substring(openApiPath.lastIndexOf("/") + 1), config);
    }

    private String downloadFilename(final String filename, final SwaggerGenerationConfig config) {
        return variantFilenamePrefix(config) + filename;
    }

    private String variantFilenamePrefix(final SwaggerGenerationConfig config) {
        final StringBuilder prefix = new StringBuilder();

        if (config.includeMethodNotAllowedEndpoints) {
            prefix.append("permissive-");
        }
        if (config.strongSchemas) {
            prefix.append("strong-");
        }
        if (config.pathParameterPlacement
                == SwaggerGenerationConfig.PathParameterPlacement.OPERATION) {
            prefix.append("operational-");
        }

        return prefix.toString();
    }

    private String swaggerUi(final HttpServerResponse response) {
        response.type("text/html");
        response.status(200);
        return withCanonicalThingifierDocsPrefix(
                () ->
                        new SwaggerUiPage(
                                        apiDefn,
                                        guiTemplates,
                                        OPENAPI_PATH,
                                        OPENAPI_30_PATH,
                                        OPENAPI_31_PATH,
                                        OPENAPI_32_PATH,
                                        DOCS_PATH,
                                        SWAGGER_UI_PATH)
                                .html());
    }

    private String scalarUi(final HttpServerResponse response) {
        response.type("text/html");
        response.status(200);
        return withCanonicalThingifierDocsPrefix(
                () ->
                        new ScalarUiPage(
                                        apiDefn,
                                        guiTemplates,
                                        OPENAPI_PATH,
                                        OPENAPI_30_PATH,
                                        OPENAPI_31_PATH,
                                        OPENAPI_32_PATH,
                                        DOCS_PATH,
                                        SCALAR_UI_PATH)
                                .html());
    }

    private String withCanonicalThingifierDocsPrefix(final Supplier<String> htmlGenerator) {
        synchronized (apiDefn) {
            final String originalPrefix = apiDefn.getPathPrefix();
            apiDefn.setPathPrefix(THINGIFIER_DOC_PREFIX);
            try {
                return htmlGenerator.get();
            } finally {
                apiDefn.setPathPrefix(originalPrefix);
            }
        }
    }

    private String normaliseCanonicalDocsHtml(final String html) {
        return html.replace("//api/", "/api/");
    }

    private String requestOrigin(final HttpServerRequest request) {
        return firstHeaderValue(request, "X-Forwarded-Proto", request.scheme())
                + "://"
                + firstHeaderValue(request, "X-Forwarded-Host", request.host());
    }

    private String firstHeaderValue(
            final HttpServerRequest request, final String headerName, final String fallback) {
        final String headerValue = request.header(headerName);
        if (headerValue == null || headerValue.isBlank()) {
            return fallback;
        }

        final int commaIndex = headerValue.indexOf(",");
        if (commaIndex < 0) {
            return headerValue.trim();
        }

        return headerValue.substring(0, commaIndex).trim();
    }
}
