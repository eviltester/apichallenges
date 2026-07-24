package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FromHellOpenApi {

    static final String CLOUD_SERVER = "https://apichallenges.eviltester.com";
    static final String LOCAL_SERVER = "http://localhost:4567";

    private final FromHellCatalog catalog;
    private final Gson gson;

    public FromHellOpenApi(final FromHellCatalog catalog) {
        this.catalog = catalog;
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    public String asJsonWithPreferredServer(final String preferredServer) {
        final Map<String, Object> openApi = new LinkedHashMap<>();
        openApi.put("openapi", "3.0.0");
        openApi.put("info", info());
        openApi.put("servers", servers(preferredServer));
        openApi.put("paths", paths());
        return gson.toJson(openApi);
    }

    private Map<String, Object> info() {
        final Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", catalog.name());
        info.put("version", "1.0.0");
        info.put("description", catalog.description());
        return info;
    }

    private List<Map<String, String>> servers(final String preferredServer) {
        final List<Map<String, String>> servers = new ArrayList<>();
        addServer(servers, preferredServer, "current request");
        addServer(servers, CLOUD_SERVER, "cloud hosted version");
        addServer(servers, LOCAL_SERVER, "local execution");
        return servers;
    }

    private void addServer(
            final List<Map<String, String>> servers,
            final String serverUrl,
            final String description) {
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            return;
        }

        final String cleanServer = serverUrl.trim();
        for (Map<String, String> server : servers) {
            if (cleanServer.equals(server.get("url"))) {
                return;
            }
        }

        final Map<String, String> server = new LinkedHashMap<>();
        server.put("url", cleanServer);
        server.put("description", description);
        servers.add(server);
    }

    private Map<String, Object> paths() {
        final Map<String, Object> paths = new LinkedHashMap<>();
        for (FromHellEndpoint endpoint : catalog.endpoints()) {
            final Map<String, Object> route =
                    getOrCreateRoute(paths, FromHellRoutes.PREFIX + endpoint.path());
            route.put(endpoint.method().toLowerCase(Locale.ROOT), operation(endpoint));
        }
        return paths;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateRoute(
            final Map<String, Object> paths, final String path) {
        if (!paths.containsKey(path)) {
            paths.put(path, new LinkedHashMap<String, Object>());
        }
        return (Map<String, Object>) paths.get(path);
    }

    private Map<String, Object> operation(final FromHellEndpoint endpoint) {
        final Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("tags", List.of(catalog.name()));
        operation.put("summary", endpoint.label());
        operation.put("description", descriptionFor(endpoint));
        operation.put("operationId", operationIdFor(endpoint));
        operation.put("responses", responses(endpoint));
        return operation;
    }

    private String descriptionFor(final FromHellEndpoint endpoint) {
        final List<String> parts = new ArrayList<>();
        addIfPresent(parts, endpoint.documentation());
        addIfPresent(parts, "What is wrong: " + endpoint.problem());
        addIfPresent(parts, "What to check in your client: " + endpoint.expectation());
        return String.join("\n\n", parts);
    }

    private void addIfPresent(final List<String> parts, final String value) {
        if (value != null && !value.trim().isEmpty()) {
            parts.add(value.trim());
        }
    }

    private String operationIdFor(final FromHellEndpoint endpoint) {
        return endpoint.method().toLowerCase(Locale.ROOT)
                + "FromHell"
                + endpoint.path().replace("/", " ").trim().replaceAll("[^a-zA-Z0-9]+", "_");
    }

    private Map<String, Object> responses(final FromHellEndpoint endpoint) {
        final Map<String, Object> responses = new LinkedHashMap<>();
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", endpoint.label());
        response.put("headers", responseHeaders(endpoint));

        final String contentType = endpoint.contentType();
        if (!contentType.isEmpty()) {
            response.put("content", responseContent(contentType, endpoint.body()));
        }

        responses.put(String.valueOf(endpoint.statusCode()), response);
        return responses;
    }

    private Map<String, Object> responseHeaders(final FromHellEndpoint endpoint) {
        final Map<String, Object> headers = new LinkedHashMap<>();
        for (FromHellHeader header : endpoint.headers()) {
            headers.put(header.name(), headerSpec(header.value()));
        }
        headers.put("Access-Control-Allow-Origin", headerSpec("*"));
        headers.put(
                "Access-Control-Allow-Methods",
                headerSpec("GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS"));
        headers.put(
                "Access-Control-Allow-Headers",
                headerSpec(
                        "Content-Type, Origin, Accept, Authorization, Content-Length,"
                                + " X-Requested-With"));
        return headers;
    }

    private Map<String, Object> headerSpec(final String example) {
        final Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("schema", Map.of("type", "string"));
        spec.put("example", example);
        return spec;
    }

    private Map<String, Object> responseContent(final String contentType, final String body) {
        final Map<String, Object> mediaType = new LinkedHashMap<>();
        mediaType.put("schema", Map.of("type", "string"));
        mediaType.put("example", body);

        final Map<String, Object> content = new LinkedHashMap<>();
        content.put(contentType, mediaType);
        return content;
    }
}
