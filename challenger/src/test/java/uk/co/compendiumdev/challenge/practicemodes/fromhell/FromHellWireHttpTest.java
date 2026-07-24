package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.serverstart.Environment;

public class FromHellWireHttpTest {

    private static URI baseUri;

    @BeforeAll
    public static void startServer() {
        baseUri = URI.create(Environment.getBaseUri());
    }

    public static Stream<Arguments> missingSemanticHeaderExpectations() {
        return Stream.of(
                Arguments.of("POST", "/fromhell/status-code/201-no-location", 201, "Location"),
                Arguments.of("GET", "/fromhell/status-code/301-no-location", 301, "Location"),
                Arguments.of("GET", "/fromhell/status-code/302-no-location", 302, "Location"),
                Arguments.of("GET", "/fromhell/status-code/307-no-location", 307, "Location"),
                Arguments.of(
                        "GET", "/fromhell/status-code/206-no-content-range", 206, "Content-Range"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/401-no-www-authenticate",
                        401,
                        "WWW-Authenticate"),
                Arguments.of("POST", "/fromhell/status-code/405-no-allow", 405, "Allow"),
                Arguments.of(
                        "GET", "/fromhell/status-code/416-no-content-range", 416, "Content-Range"));
    }

    @ParameterizedTest(name = "{0} {1} has no {3}")
    @MethodSource("missingSemanticHeaderExpectations")
    public void semanticMismatchResponsesReallyOmitExpectedHeadersOnTheWire(
            final String method, final String path, final int status, final String missingHeader)
            throws IOException {

        final RawHttpResponse response = send(method, path);

        Assertions.assertEquals(status, response.statusCode());
        Assertions.assertNull(response.header(missingHeader));
        Assertions.assertFalse(response.bodyText().isEmpty());
    }

    @Test
    public void versionResponseReallyHasNoContentTypeHeaderOnTheWire() throws IOException {
        final RawHttpResponse response = send("GET", "/fromhell/version");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNull(response.header("Content-Type"));
        Assertions.assertEquals("{\"version\":\"6\"}", response.bodyText());
    }

    @Test
    public void wrongMethodOnKnownPathReallyReturnsMethodNotAllowedWithAllowHeaderOnTheWire()
            throws IOException {

        final RawHttpResponse response = send("POST", "/fromhell/version");

        Assertions.assertEquals(405, response.statusCode());
        Assertions.assertEquals("GET, HEAD, OPTIONS", response.header("Allow"));
        Assertions.assertEquals("Method Not Allowed", response.bodyText());
    }

    @Test
    public void unknownFromHellPathReallyReturnsNotFoundOnTheWire() throws IOException {
        final RawHttpResponse response = send("GET", "/fromhell/not-here");

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    public void noContentEndpointSendsTheCataloguedBodyOnTheWire() throws IOException {
        final FromHellEndpoint endpoint =
                FromHellCatalog.loadDefault().endpoint("DELETE", "/status-code/204-with-body");

        final RawHttpResponse response = send("DELETE", "/fromhell/status-code/204-with-body");

        Assertions.assertFalse(endpoint.body().isEmpty());
        Assertions.assertEquals(204, response.statusCode());
        Assertions.assertEquals("application/json", response.header("Content-Type"));
        Assertions.assertEquals(endpoint.body(), response.bodyText());
        Assertions.assertEquals(
                String.valueOf(response.bodyBytes().length), response.header("Content-Length"));
    }

    @Test
    public void resetContentEndpointActuallySendsTheCataloguedBodyOnTheWire() throws IOException {
        final FromHellEndpoint endpoint =
                FromHellCatalog.loadDefault().endpoint("POST", "/status-code/205-with-body");

        final RawHttpResponse response = send("POST", "/fromhell/status-code/205-with-body");

        Assertions.assertEquals(205, response.statusCode());
        Assertions.assertEquals("application/json", response.header("Content-Type"));
        Assertions.assertEquals(endpoint.body(), response.bodyText());
        Assertions.assertEquals(
                String.valueOf(response.bodyBytes().length), response.header("Content-Length"));
    }

    @Test
    public void notModifiedEndpointSendsTheCataloguedBodyOnTheWire() throws IOException {
        final FromHellEndpoint endpoint =
                FromHellCatalog.loadDefault().endpoint("GET", "/status-code/304-with-body");

        final RawHttpResponse response = send("GET", "/fromhell/status-code/304-with-body");

        Assertions.assertFalse(endpoint.body().isEmpty());
        Assertions.assertEquals(304, response.statusCode());
        Assertions.assertEquals("application/json", response.header("Content-Type"));
        Assertions.assertEquals(
                String.valueOf(endpoint.body().getBytes(StandardCharsets.UTF_8).length),
                response.header("Content-Length"));
        Assertions.assertEquals(endpoint.body(), response.bodyText());
    }

    private RawHttpResponse send(final String method, final String path) throws IOException {
        final int port = baseUri.getPort() == -1 ? 80 : baseUri.getPort();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(baseUri.getHost(), port), 5000);
            socket.setSoTimeout(5000);
            socket.getOutputStream()
                    .write(
                            rawRequest(method, path, baseUri.getHost(), port)
                                    .getBytes(StandardCharsets.ISO_8859_1));
            socket.getOutputStream().flush();
            return RawHttpResponse.parse(readAll(socket));
        }
    }

    private String rawRequest(
            final String method, final String path, final String host, final int port) {
        final StringBuilder request = new StringBuilder();
        request.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
        request.append("Host: ").append(host).append(":").append(port).append("\r\n");
        request.append("User-Agent: api-from-hell-wire-test\r\n");
        request.append("Accept: */*\r\n");
        request.append("Connection: close\r\n");
        if (methodAllowsRequestBody(method)) {
            request.append("Content-Length: 0\r\n");
        }
        request.append("\r\n");
        return request.toString();
    }

    private boolean methodAllowsRequestBody(final String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private byte[] readAll(final Socket socket) throws IOException {
        final ByteArrayOutputStream response = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int read;
        while ((read = socket.getInputStream().read(buffer)) != -1) {
            response.write(buffer, 0, read);
        }
        return response.toByteArray();
    }

    private static final class RawHttpResponse {
        private final String statusLine;
        private final Map<String, String> headers;
        private final byte[] body;

        private RawHttpResponse(
                final String statusLine, final Map<String, String> headers, final byte[] body) {
            this.statusLine = statusLine;
            this.headers = headers;
            this.body = body.clone();
        }

        static RawHttpResponse parse(final byte[] responseBytes) {
            final int bodyStart = findHeaderEnd(responseBytes);
            final String headerText =
                    new String(responseBytes, 0, bodyStart, StandardCharsets.ISO_8859_1);
            final String[] headerLines = headerText.split("\r\n");
            final Map<String, String> headers = new LinkedHashMap<>();
            for (int index = 1; index < headerLines.length; index++) {
                final int separator = headerLines[index].indexOf(':');
                if (separator > 0) {
                    headers.put(
                            headerLines[index].substring(0, separator).toLowerCase(Locale.ROOT),
                            headerLines[index].substring(separator + 1).trim());
                }
            }

            final int bodyOffset = bodyStart + 4;
            final byte[] body = new byte[responseBytes.length - bodyOffset];
            System.arraycopy(responseBytes, bodyOffset, body, 0, body.length);
            return new RawHttpResponse(headerLines[0], headers, body);
        }

        int statusCode() {
            return Integer.parseInt(statusLine.split(" ")[1]);
        }

        String header(final String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }

        byte[] bodyBytes() {
            return body.clone();
        }

        String bodyText() {
            return new String(body, StandardCharsets.UTF_8);
        }

        private static int findHeaderEnd(final byte[] responseBytes) {
            for (int index = 0; index < responseBytes.length - 3; index++) {
                if (responseBytes[index] == '\r'
                        && responseBytes[index + 1] == '\n'
                        && responseBytes[index + 2] == '\r'
                        && responseBytes[index + 3] == '\n') {
                    return index;
                }
            }
            throw new IllegalStateException(
                    "Raw HTTP response did not contain a header terminator");
        }
    }
}
