package dev.eviltester.fromhell;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ApiFromHellJavalinMain {

    private static final int DEFAULT_PORT = 3001;
    private static final List<HandlerType> ROUTE_HANDLER_TYPES =
            List.of(
                    HandlerType.GET,
                    HandlerType.HEAD,
                    HandlerType.OPTIONS,
                    HandlerType.POST,
                    HandlerType.PUT,
                    HandlerType.PATCH,
                    HandlerType.DELETE,
                    HandlerType.TRACE);

    private ApiFromHellJavalinMain() {}

    public static void main(final String[] args) throws IOException {
        final int port = Integer.parseInt(envOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        final String prefix = normalisePrefix(envOrDefault("FROMHELL_PREFIX", "/fromhell"));
        final CatalogDocument catalog = loadCatalog();
        final Map<String, Map<String, EndpointDocument>> endpointsByPath = index(catalog);

        Javalin.create(
                        config -> {
                            config.router.ignoreTrailingSlashes = false;
                            for (String endpointPath : endpointsByPath.keySet()) {
                                configurePath(config, prefix, endpointPath, endpointsByPath);
                            }
                            config.routes.addHttpHandler(
                                    HandlerType.GET,
                                    "/docs/openapi.json",
                                    ctx -> {
                                        commonHeaders(ctx);
                                        ctx.contentType("application/json");
                                        ctx.result(new Gson().toJson(openApiFor(catalog, ctx, prefix)));
                                    });
                        })
                .start(port);

        System.out.println(
                "API From Hell java-javalin listening on http://localhost:" + port + prefix);
    }

    private static void configurePath(
            final io.javalin.config.JavalinConfig config,
            final String prefix,
            final String endpointPath,
            final Map<String, Map<String, EndpointDocument>> endpointsByPath) {
        final String routePath = prefix + endpointPath;
        final Map<String, EndpointDocument> endpointsForPath = endpointsByPath.get(endpointPath);
        final List<String> allowedMethods = allowedMethods(endpointsForPath);

        for (EndpointDocument endpoint : endpointsForPath.values()) {
            config.routes.addHttpHandler(
                    handlerType(endpoint.method),
                    routePath,
                    ctx -> {
                        commonHeaders(ctx);
                        applyEndpoint(ctx, endpoint);
                        ctx.result(endpoint.body == null ? "" : endpoint.body);
                    });
        }

        config.routes.addHttpHandler(
                HandlerType.OPTIONS,
                routePath,
                ctx -> {
                    commonHeaders(ctx);
                    ctx.status(204);
                    ctx.header("Allow", String.join(", ", allowedMethods));
                    ctx.result("");
                });

        for (HandlerType handlerType : ROUTE_HANDLER_TYPES) {
            if (!allowedMethods.contains(handlerType.name())) {
                config.routes.addHttpHandler(
                        handlerType,
                        routePath,
                        ctx -> {
                            commonHeaders(ctx);
                            ctx.status(405);
                            ctx.header("Allow", String.join(", ", allowedMethods));
                            ctx.result("Method Not Allowed");
                        });
            }
        }
    }

    private static CatalogDocument loadCatalog() throws IOException {
        final String catalogPath =
                envOrDefault(
                        "FROMHELL_CATALOG",
                        Path.of("..", "..", "catalog", "fromhell-catalog.json").toString());
        try (Reader reader = Files.newBufferedReader(Path.of(catalogPath), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, CatalogDocument.class);
        }
    }

    private static Map<String, Map<String, EndpointDocument>> index(final CatalogDocument catalog) {
        final Map<String, Map<String, EndpointDocument>> indexed = new LinkedHashMap<>();
        for (EndpointDocument endpoint : catalog.endpoints) {
            indexed.putIfAbsent(endpoint.path, new LinkedHashMap<>());
            indexed.get(endpoint.path).put(endpoint.method.toUpperCase(Locale.ROOT), endpoint);
        }
        return indexed;
    }

    private static void applyEndpoint(final Context ctx, final EndpointDocument endpoint) {
        ctx.status(endpoint.statusCode);
        if (endpoint.headers != null) {
            for (HeaderDocument header : endpoint.headers) {
                ctx.header(header.name, header.value);
            }
        }
        if (!hasHeader(endpoint, "Content-Type")) {
            ctx.res().setHeader("Content-Type", null);
        }
    }

    private static boolean hasHeader(
            final EndpointDocument endpoint, final String headerName) {
        if (endpoint.headers == null) {
            return false;
        }
        for (HeaderDocument header : endpoint.headers) {
            if (headerName.equalsIgnoreCase(header.name)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> allowedMethods(
            final Map<String, EndpointDocument> endpointsForPath) {
        final List<String> methods = new ArrayList<>(endpointsForPath.keySet());
        if (methods.contains("GET") && !methods.contains("HEAD")) {
            methods.add("HEAD");
        }
        methods.add("OPTIONS");
        return new ArrayList<>(new java.util.LinkedHashSet<>(methods));
    }

    private static HandlerType handlerType(final String method) {
        switch (method.toUpperCase(Locale.ROOT)) {
            case "GET":
                return HandlerType.GET;
            case "HEAD":
                return HandlerType.HEAD;
            case "OPTIONS":
                return HandlerType.OPTIONS;
            case "POST":
                return HandlerType.POST;
            case "PUT":
                return HandlerType.PUT;
            case "PATCH":
                return HandlerType.PATCH;
            case "DELETE":
                return HandlerType.DELETE;
            case "TRACE":
                return HandlerType.TRACE;
            default:
                throw new IllegalStateException("Unsupported method " + method);
        }
    }

    private static void commonHeaders(final Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS");
        ctx.header(
                "Access-Control-Allow-Headers",
                "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With");
    }

    private static Map<String, Object> openApiFor(
            final CatalogDocument catalog, final Context ctx, final String prefix) {
        final Map<String, Object> openApi = new LinkedHashMap<>();
        final Map<String, Object> info = new LinkedHashMap<>();
        final Map<String, Object> paths = new LinkedHashMap<>();

        openApi.put("openapi", "3.0.3");
        info.put("title", catalog.name);
        info.put("version", "1.0.0");
        info.put("description", catalog.description);
        openApi.put("info", info);
        openApi.put("servers", List.of(Map.of("url", ctx.scheme() + "://" + ctx.host())));

        for (EndpointDocument endpoint : catalog.endpoints) {
            final String path = prefix + endpoint.path;
            @SuppressWarnings("unchecked")
            final Map<String, Object> pathItem =
                    (Map<String, Object>) paths.computeIfAbsent(path, ignored -> new LinkedHashMap<>());
            pathItem.put(
                    endpoint.method.toLowerCase(Locale.ROOT),
                    Map.of(
                            "summary",
                            endpoint.label,
                            "description",
                            endpoint.documentation == null ? "" : endpoint.documentation,
                            "responses",
                            Map.of(
                                    String.valueOf(endpoint.statusCode),
                                    Map.of(
                                            "description",
                                            endpoint.documentation == null
                                                    ? endpoint.label
                                                    : endpoint.documentation))));
        }

        openApi.put("paths", paths);
        return openApi;
    }

    private static String normalisePrefix(final String prefix) {
        if (prefix == null || prefix.isBlank() || "/".equals(prefix)) {
            return "";
        }
        return "/" + prefix.replaceAll("^/+|/+$", "");
    }

    private static String envOrDefault(final String name, final String defaultValue) {
        final String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static final class CatalogDocument {
        String name;
        String description;
        List<EndpointDocument> endpoints;
    }

    private static final class EndpointDocument {
        String method;
        String path;
        int statusCode;
        String label;
        String documentation;
        List<HeaderDocument> headers;
        String body;
    }

    private static final class HeaderDocument {
        String name;
        String value;
    }
}
