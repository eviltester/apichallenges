package dev.eviltester.fromhell;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApiFromHellRawHttpMain {

    private static final int DEFAULT_PORT = 3001;
    private static final int HEADER_LIMIT_BYTES = 64 * 1024;

    private ApiFromHellRawHttpMain() {}

    public static void main(final String[] args) throws IOException {
        final int port = Integer.parseInt(envOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        final String prefix = normalisePrefix(envOrDefault("FROMHELL_PREFIX", "/fromhell"));
        final CatalogDocument catalog = loadCatalog();
        final Map<String, Map<String, EndpointDocument>> endpointsByPath = index(catalog);

        try (ServerSocket serverSocket = new ServerSocket(port);
                ExecutorService executor = Executors.newCachedThreadPool()) {
            System.out.println(
                    "API From Hell java-raw-http listening on http://localhost:" + port + prefix);
            while (true) {
                final Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket, prefix, catalog, endpointsByPath));
            }
        }
    }

    private static void handleClient(
            final Socket socket,
            final String prefix,
            final CatalogDocument catalog,
            final Map<String, Map<String, EndpointDocument>> endpointsByPath) {
        try (socket;
                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream())) {
            socket.setSoTimeout(5000);
            final HttpRequest request = readRequest(input);
            if (request == null) {
                return;
            }

            output.write(responseFor(request, prefix, catalog, endpointsByPath));
            output.flush();
        } catch (IOException ignored) {
            // Test clients sometimes disconnect aggressively; the next request is independent.
        }
    }

    private static HttpRequest readRequest(final BufferedInputStream input) throws IOException {
        final ByteArrayOutputStream headers = new ByteArrayOutputStream();
        final byte[] terminator = new byte[] {'\r', '\n', '\r', '\n'};
        int matched = 0;

        while (matched < terminator.length) {
            final int next = input.read();
            if (next == -1) {
                return headers.size() == 0 ? null : parseRequest(headers.toByteArray());
            }
            headers.write(next);
            if (headers.size() > HEADER_LIMIT_BYTES) {
                throw new IOException("HTTP request headers exceeded " + HEADER_LIMIT_BYTES + " bytes");
            }
            if ((byte) next == terminator[matched]) {
                matched += 1;
            } else {
                matched = (byte) next == terminator[0] ? 1 : 0;
            }
        }

        return parseRequest(headers.toByteArray());
    }

    private static HttpRequest parseRequest(final byte[] rawHeaders) throws IOException {
        final String headerText = new String(rawHeaders, StandardCharsets.ISO_8859_1);
        final String[] lines = headerText.split("\\r\\n");
        if (lines.length == 0 || lines[0].isBlank()) {
            throw new IOException("Empty HTTP request");
        }

        final String[] requestLine = lines[0].split(" ", 3);
        if (requestLine.length < 2) {
            throw new IOException("Invalid HTTP request line: " + lines[0]);
        }

        final Map<String, String> headers = new LinkedHashMap<>();
        for (int index = 1; index < lines.length; index += 1) {
            final String line = lines[index];
            final int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            headers.put(
                    line.substring(0, separator).trim().toLowerCase(Locale.ROOT),
                    line.substring(separator + 1).trim());
        }

        return new HttpRequest(
                requestLine[0],
                requestLine[1],
                requestLine.length == 3 ? requestLine[2] : "HTTP/1.1",
                headers);
    }

    private static byte[] responseFor(
            final HttpRequest request,
            final String prefix,
            final CatalogDocument catalog,
            final Map<String, Map<String, EndpointDocument>> endpointsByPath)
            throws IOException {
        final String method = request.method.toUpperCase(Locale.ROOT);
        final String path = requestPath(request.target);

        if ("GET".equals(method) && "/docs/openapi.json".equals(path)) {
            final byte[] body =
                    new Gson()
                            .toJson(
                                    openApiFor(
                                            catalog,
                                            request.headers.getOrDefault("host", "localhost"),
                                            prefix))
                            .getBytes(StandardCharsets.UTF_8);
            return httpResponse(
                    200,
                    List.of(header("Content-Type", "application/json")),
                    Map.of(),
                    body,
                    body.length);
        }

        if (!prefix.isEmpty() && !path.startsWith(prefix)) {
            return textResponse(404, "Not Found");
        }

        final String endpointPath = prefix.isEmpty() ? path : path.substring(prefix.length());
        final Map<String, EndpointDocument> endpointsForPath = endpointsByPath.get(endpointPath);
        if (endpointsForPath == null) {
            return textResponse(404, "Not Found");
        }

        final List<String> allowedMethods = allowedMethods(endpointsForPath);
        if ("OPTIONS".equals(method)) {
            return httpResponse(
                    204,
                    List.of(),
                    Map.of("Allow", String.join(", ", allowedMethods)),
                    new byte[0],
                    0);
        }

        EndpointDocument endpoint = endpointsForPath.get(method);
        final boolean isImplicitHead = endpoint == null && "HEAD".equals(method);
        if (isImplicitHead) {
            endpoint = endpointsForPath.get("GET");
        }

        if (endpoint != null) {
            final byte[] body = endpointBody(endpoint);
            return httpResponse(
                    endpoint.statusCode,
                    endpoint.headers,
                    Map.of(),
                    isImplicitHead ? new byte[0] : body,
                    body.length);
        }

        final byte[] body = "Method Not Allowed".getBytes(StandardCharsets.UTF_8);
        return httpResponse(
                405,
                List.of(),
                Map.of("Allow", String.join(", ", allowedMethods)),
                body,
                body.length);
    }

    private static byte[] textResponse(final int statusCode, final String text) throws IOException {
        final byte[] body = text.getBytes(StandardCharsets.UTF_8);
        return httpResponse(
                statusCode, List.of(header("Content-Type", "text/plain")), Map.of(), body, body.length);
    }

    private static byte[] httpResponse(
            final int statusCode,
            final List<HeaderDocument> catalogHeaders,
            final Map<String, String> extraHeaders,
            final byte[] body,
            final int contentLength)
            throws IOException {
        final ByteArrayOutputStream response = new ByteArrayOutputStream();
        final StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ")
                .append(statusCode)
                .append(' ')
                .append(reasonPhrase(statusCode))
                .append("\r\n");

        appendCommonHeaders(head);
        if (catalogHeaders != null) {
            for (HeaderDocument header : catalogHeaders) {
                appendHeader(head, header.name, header.value);
            }
        }
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            appendHeader(head, header.getKey(), header.getValue());
        }
        appendHeader(head, "Content-Length", String.valueOf(contentLength));
        appendHeader(head, "Connection", "close");
        head.append("\r\n");

        response.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
        response.write(body);
        return response.toByteArray();
    }

    private static void appendCommonHeaders(final StringBuilder head) {
        appendHeader(head, "Access-Control-Allow-Origin", "*");
        appendHeader(head, "Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS");
        appendHeader(
                head,
                "Access-Control-Allow-Headers",
                "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With");
    }

    private static void appendHeader(
            final StringBuilder head, final String headerName, final String headerValue) {
        head.append(headerName).append(": ").append(headerValue).append("\r\n");
    }

    private static HeaderDocument header(final String name, final String value) {
        final HeaderDocument header = new HeaderDocument();
        header.name = name;
        header.value = value;
        return header;
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

    private static byte[] endpointBody(final EndpointDocument endpoint) {
        final String body = endpoint.body == null ? "" : endpoint.body;
        return body.getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> allowedMethods(
            final Map<String, EndpointDocument> endpointsForPath) {
        final List<String> methods = new ArrayList<>(endpointsForPath.keySet());
        if (methods.contains("GET") && !methods.contains("HEAD")) {
            methods.add("HEAD");
        }
        methods.add("OPTIONS");
        return new ArrayList<>(new LinkedHashSet<>(methods));
    }

    private static Map<String, Object> openApiFor(
            final CatalogDocument catalog, final String host, final String prefix) {
        final Map<String, Object> openApi = new LinkedHashMap<>();
        final Map<String, Object> info = new LinkedHashMap<>();
        final Map<String, Object> paths = new LinkedHashMap<>();

        openApi.put("openapi", "3.0.3");
        info.put("title", catalog.name);
        info.put("version", "1.0.0");
        info.put("description", catalog.description);
        openApi.put("info", info);
        openApi.put("servers", List.of(Map.of("url", "http://" + host)));

        for (EndpointDocument endpoint : catalog.endpoints) {
            final String path = prefix + endpoint.path;
            @SuppressWarnings("unchecked")
            final Map<String, Object> pathItem =
                    (Map<String, Object>)
                            paths.computeIfAbsent(path, ignored -> new LinkedHashMap<>());
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

    private static String requestPath(final String target) {
        final int query = target.indexOf('?');
        final String targetWithoutQuery = query >= 0 ? target.substring(0, query) : target;
        if (targetWithoutQuery.startsWith("/")) {
            return targetWithoutQuery;
        }
        try {
            final URI uri = new URI(targetWithoutQuery);
            return uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath();
        } catch (URISyntaxException ignored) {
            return targetWithoutQuery;
        }
    }

    private static String reasonPhrase(final int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 205 -> "Reset Content";
            case 206 -> "Partial Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 307 -> "Temporary Redirect";
            case 401 -> "Unauthorized";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 416 -> "Range Not Satisfiable";
            case 500 -> "Internal Server Error";
            default -> "Status";
        };
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

    private static final class HttpRequest {
        final String method;
        final String target;
        final String version;
        final Map<String, String> headers;

        HttpRequest(
                final String method,
                final String target,
                final String version,
                final Map<String, String> headers) {
            this.method = method;
            this.target = target;
            this.version = version;
            this.headers = headers;
        }
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
