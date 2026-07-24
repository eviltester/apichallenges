package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FromHellCatalog {

    public static final String RESOURCE_PATH = "fromhell/fromhell-catalog.json";

    private final String name;
    private final String description;
    private final List<FromHellEndpoint> endpoints;
    private final Map<String, List<FromHellEndpoint>> endpointsByPath;

    private FromHellCatalog(
            final String name, final String description, final List<FromHellEndpoint> endpoints) {
        this.name = name;
        this.description = description;
        this.endpoints = Collections.unmodifiableList(new ArrayList<>(endpoints));
        this.endpointsByPath = indexByPath(endpoints);
    }

    public static FromHellCatalog loadDefault() {
        final InputStream stream =
                FromHellCatalog.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
        if (stream == null) {
            throw new IllegalStateException("Could not load " + RESOURCE_PATH);
        }
        return loadFrom(stream);
    }

    static FromHellCatalog loadFrom(final InputStream stream) {
        final CatalogDocument document =
                new Gson()
                        .fromJson(
                                new InputStreamReader(stream, StandardCharsets.UTF_8),
                                CatalogDocument.class);
        if (document == null) {
            throw new IllegalStateException("API From Hell catalog was empty");
        }

        final List<FromHellEndpoint> endpoints = new ArrayList<>();
        if (document.endpoints != null) {
            for (EndpointDocument endpoint : document.endpoints) {
                endpoints.add(endpoint.toEndpoint());
            }
        }

        return new FromHellCatalog(
                valueOrEmpty(document.name), valueOrEmpty(document.description), endpoints);
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public List<FromHellEndpoint> endpoints() {
        return endpoints;
    }

    public FromHellEndpoint endpoint(final String path) {
        final List<FromHellEndpoint> matching = endpointsByPath.get(path);
        return matching == null || matching.isEmpty() ? null : matching.get(0);
    }

    public FromHellEndpoint endpoint(final String method, final String path) {
        final List<FromHellEndpoint> matching = endpointsByPath.get(path);
        if (matching == null) {
            return null;
        }

        for (FromHellEndpoint endpoint : matching) {
            if (endpoint.method().equalsIgnoreCase(method)) {
                return endpoint;
            }
        }
        return null;
    }

    public List<FromHellEndpoint> endpointsForPath(final String path) {
        final List<FromHellEndpoint> matching = endpointsByPath.get(path);
        return matching == null ? Collections.emptyList() : matching;
    }

    public List<String> paths() {
        return Collections.unmodifiableList(new ArrayList<>(endpointsByPath.keySet()));
    }

    private static Map<String, List<FromHellEndpoint>> indexByPath(
            final List<FromHellEndpoint> endpoints) {
        final Map<String, List<FromHellEndpoint>> indexed = new LinkedHashMap<>();
        for (FromHellEndpoint endpoint : endpoints) {
            indexed.putIfAbsent(endpoint.path(), new ArrayList<>());
            if (containsMethod(indexed.get(endpoint.path()), endpoint.method())) {
                throw new IllegalStateException(
                        "Duplicate API From Hell endpoint "
                                + endpoint.method()
                                + " "
                                + endpoint.path());
            }
            indexed.get(endpoint.path()).add(endpoint);
        }
        return unmodifiablePathIndex(indexed);
    }

    private static boolean containsMethod(
            final List<FromHellEndpoint> endpoints, final String method) {
        for (FromHellEndpoint endpoint : endpoints) {
            if (endpoint.method().equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, List<FromHellEndpoint>> unmodifiablePathIndex(
            final Map<String, List<FromHellEndpoint>> indexed) {
        final Map<String, List<FromHellEndpoint>> immutable = new LinkedHashMap<>();
        for (Map.Entry<String, List<FromHellEndpoint>> entry : indexed.entrySet()) {
            immutable.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }

    private static String valueOrEmpty(final String value) {
        return value == null ? "" : value;
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
        String problem;
        String expectation;
        List<HeaderDocument> headers;
        String body;

        FromHellEndpoint toEndpoint() {
            final List<FromHellHeader> endpointHeaders = new ArrayList<>();
            if (headers != null) {
                for (HeaderDocument header : headers) {
                    endpointHeaders.add(
                            new FromHellHeader(
                                    valueOrEmpty(header.name), valueOrEmpty(header.value)));
                }
            }

            return new FromHellEndpoint(
                    method,
                    path,
                    statusCode,
                    label,
                    documentation,
                    problem,
                    expectation,
                    endpointHeaders,
                    body);
        }
    }

    private static final class HeaderDocument {
        String name;
        String value;
    }
}
