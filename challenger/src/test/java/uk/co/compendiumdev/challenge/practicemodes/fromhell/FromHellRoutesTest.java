package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.serverstart.Environment;

public class FromHellRoutesTest {

    private static HttpMessageSender http;

    @BeforeAll
    public static void createHttp() {
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    public static Stream<Arguments> endpointExpectations() {
        return Stream.of(
                Arguments.of(
                        "GET",
                        "/fromhell/status",
                        200,
                        "text/markdown",
                        "# Mock API From Hell",
                        "An API designed to test and evaluate REST Clients."),
                Arguments.of("GET", "/fromhell/version", 200, "", "{\"version\":\"6\"}", ""),
                Arguments.of(
                        "GET",
                        "/fromhell/good/json",
                        200,
                        "application/json",
                        "[{\"id\":\"57ab8bfa",
                        "\"username\":\"Damaris76\""),
                Arguments.of(
                        "GET",
                        "/fromhell/good/xml",
                        200,
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "</root>"),
                Arguments.of(
                        "GET",
                        "/fromhell/malformed/json",
                        200,
                        "application/json",
                        "[{\"id\":\"57ab8bfa",
                        "\"Maverick.Corkery74\"}"),
                Arguments.of(
                        "GET",
                        "/fromhell/malformed/xml",
                        200,
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<root>"),
                Arguments.of(
                        "GET",
                        "/fromhell/mismatch/content-type/json-xml",
                        200,
                        "application/json",
                        "<?xml version=\"1.0\"",
                        "</root>"),
                Arguments.of(
                        "GET",
                        "/fromhell/mismatch/content-type/xml-json",
                        200,
                        "application/xml",
                        "[{\"id\":\"57ab8bfa",
                        "\"username\":\"Damaris76\""),
                Arguments.of(
                        "POST",
                        "/fromhell/status-code/201-no-location",
                        201,
                        "application/json",
                        "{\"id\":123",
                        "created without a Location header"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/301-no-location",
                        301,
                        "text/plain",
                        "Moved permanently",
                        "no Location header"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/302-no-location",
                        302,
                        "text/plain",
                        "Found somewhere else",
                        "no Location header"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/307-no-location",
                        307,
                        "text/plain",
                        "Temporary redirect",
                        "no Location header"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/206-no-content-range",
                        206,
                        "application/json",
                        "{\"items\"",
                        "without Content-Range"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/401-no-www-authenticate",
                        401,
                        "application/json",
                        "{\"error\"",
                        "no challenge header"),
                Arguments.of(
                        "POST",
                        "/fromhell/status-code/405-no-allow",
                        405,
                        "application/json",
                        "{\"error\"",
                        "no Allow header"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/416-no-content-range",
                        416,
                        "application/json",
                        "{\"error\"",
                        "no Content-Range"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/200-error-body",
                        200,
                        "application/json",
                        "{\"error\"",
                        "NOT_OK"),
                Arguments.of(
                        "GET",
                        "/fromhell/status-code/500-success-body",
                        500,
                        "application/json",
                        "{\"success\"",
                        "status is 500"));
    }

    public static Stream<Arguments> expandedMalformedEndpointExpectations() {
        return Stream.of(
                Arguments.of(
                        "/fromhell/malformed/json/trailing-comma-array",
                        "application/json",
                        "[{\"id\":1",
                        ",]"),
                Arguments.of(
                        "/fromhell/malformed/json/trailing-comma-object",
                        "application/json",
                        "{\"id\":1",
                        ",}"),
                Arguments.of(
                        "/fromhell/malformed/json/unquoted-key",
                        "application/json",
                        "{id:1",
                        "\"name\""),
                Arguments.of(
                        "/fromhell/malformed/json/single-quoted-string",
                        "application/json",
                        "{'id'",
                        "'name'"),
                Arguments.of(
                        "/fromhell/malformed/json/bad-escape",
                        "application/json",
                        "{\"path\"",
                        "\\q"),
                Arguments.of(
                        "/fromhell/malformed/json/truncated-string",
                        "application/json",
                        "{\"message\"",
                        "unterminated}"),
                Arguments.of(
                        "/fromhell/malformed/json/extra-data-after-document",
                        "application/json",
                        "{\"ok\"",
                        "} {"),
                Arguments.of(
                        "/fromhell/malformed/json/missing-colon",
                        "application/json",
                        "{\"message\"",
                        "\"hello\""),
                Arguments.of(
                        "/fromhell/malformed/json/leading-zero-number",
                        "application/json",
                        "{\"count\"",
                        "01"),
                Arguments.of(
                        "/fromhell/malformed/json/nan-value",
                        "application/json",
                        "{\"value\"",
                        "NaN"),
                Arguments.of(
                        "/fromhell/malformed/json/unclosed-object",
                        "application/json",
                        "{\"id\"",
                        "\"open\""),
                Arguments.of(
                        "/fromhell/malformed/json/control-character",
                        "application/json",
                        "{\"message\"",
                        "control"),
                Arguments.of(
                        "/fromhell/malformed/xml/mismatched-tag",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "</thing>"),
                Arguments.of(
                        "/fromhell/malformed/xml/unescaped-ampersand",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "Tom & Jerry"),
                Arguments.of(
                        "/fromhell/malformed/xml/duplicate-attribute",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "id=\"1\" id=\"2\""),
                Arguments.of(
                        "/fromhell/malformed/xml/undefined-entity",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "&unknown;"),
                Arguments.of(
                        "/fromhell/malformed/xml/multiple-root-elements",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<two>"),
                Arguments.of(
                        "/fromhell/malformed/xml/missing-attribute-quote",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "id=1"),
                Arguments.of(
                        "/fromhell/malformed/xml/unclosed-cdata",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<![CDATA["),
                Arguments.of(
                        "/fromhell/malformed/xml/invalid-character",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "char</root>"),
                Arguments.of(
                        "/fromhell/malformed/xml/truncated-document",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<item>one"),
                Arguments.of(
                        "/fromhell/malformed/xml/bad-processing-instruction",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<?processing instruction"));
    }

    public static Stream<Arguments> additionalFormatEndpointExpectations() {
        return Stream.of(
                Arguments.of(
                        "/fromhell/good/text",
                        "text/plain",
                        "API From Hell plain text response.",
                        "Line two"),
                Arguments.of(
                        "/fromhell/good/html",
                        "text/html",
                        "<!doctype html>",
                        "<h1>HTML response</h1>"),
                Arguments.of("/fromhell/good/csv", "text/csv", "id,name,active", "2,Bob,false"),
                Arguments.of(
                        "/fromhell/good/yaml",
                        "application/yaml",
                        "name: API From Hell",
                        "  - id: 1"),
                Arguments.of(
                        "/fromhell/good/form-urlencoded",
                        "application/x-www-form-urlencoded",
                        "name=Alice+Example",
                        "active=true"),
                Arguments.of(
                        "/fromhell/good/octet-stream",
                        "application/octet-stream",
                        "API-FROM-HELL-BYTES:",
                        "END"));
    }

    public static Stream<Arguments> missingContentTypeEndpointExpectations() {
        return Stream.of(
                Arguments.of(
                        "/fromhell/missing-content-type/json",
                        "{\"message\"",
                        "without content type"),
                Arguments.of(
                        "/fromhell/missing-content-type/xml",
                        "<?xml version=\"1.0\"",
                        "xml body without content type"),
                Arguments.of(
                        "/fromhell/missing-content-type/html",
                        "<!doctype html>",
                        "HTML without content type"));
    }

    public static Stream<Arguments> problematicJsonEndpointExpectations() {
        return Stream.of(
                Arguments.of(
                        "/fromhell/problematic/json/duplicate-keys",
                        "application/json",
                        "{\"id\":1",
                        "\"id\":2"),
                Arguments.of(
                        "/fromhell/problematic/json/large-integer",
                        "application/json",
                        "{\"safeLimit\"",
                        "9007199254740993"),
                Arguments.of(
                        "/fromhell/problematic/json/high-precision-decimal",
                        "application/json",
                        "{\"amount\"",
                        "0.12345678901234567890123456789"),
                Arguments.of(
                        "/fromhell/problematic/json/exponent-number",
                        "application/json",
                        "{\"small\"",
                        "6.022e23"),
                Arguments.of(
                        "/fromhell/problematic/json/null-vs-missing",
                        "application/json",
                        "{\"items\"",
                        "\"nickname\":null"),
                Arguments.of(
                        "/fromhell/problematic/json/escaped-unicode",
                        "application/json",
                        "{\"normal\"",
                        "confusable"),
                Arguments.of(
                        "/fromhell/problematic/json/empty-object",
                        "application/json",
                        "{}",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/json/top-level-string",
                        "application/json",
                        "\"hello\"",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/json/top-level-number",
                        "application/json",
                        "42",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/json/top-level-boolean",
                        "application/json",
                        "true",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/json/top-level-null",
                        "application/json",
                        "null",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/json/empty-body",
                        "application/json",
                        "",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/json/bom-prefix",
                        "application/json",
                        "\ufeff{\"message\"",
                        "bom prefix"),
                Arguments.of(
                        "/fromhell/problematic/json/ndjson",
                        "application/x-ndjson",
                        "{\"id\":1",
                        "{\"id\":2"));
    }

    public static Stream<Arguments> problematicXmlEndpointExpectations() {
        return Stream.of(
                Arguments.of(
                        "/fromhell/problematic/xml/attributes-vs-elements",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "id=\"123\""),
                Arguments.of(
                        "/fromhell/problematic/xml/empty-vs-missing-vs-nil",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "xsi:nil=\"true\""),
                Arguments.of(
                        "/fromhell/problematic/xml/whitespace-text",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "  padded value  "),
                Arguments.of(
                        "/fromhell/problematic/xml/cdata-content",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<![CDATA["),
                Arguments.of(
                        "/fromhell/problematic/xml/mixed-content",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "Hello <em>important</em> world"),
                Arguments.of(
                        "/fromhell/problematic/xml/processing-instruction",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<?client-hint"),
                Arguments.of(
                        "/fromhell/problematic/xml/comments",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<!-- hidden note"),
                Arguments.of(
                        "/fromhell/problematic/xml/encoding-mismatch",
                        "application/xml; charset=UTF-8",
                        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"",
                        "caf&#233;"),
                Arguments.of(
                        "/fromhell/problematic/xml/doctype-dtd",
                        "application/xml",
                        "<?xml version=\"1.0\"",
                        "<!DOCTYPE note"),
                Arguments.of(
                        "/fromhell/problematic/xml/empty-body",
                        "application/xml",
                        "",
                        ""),
                Arguments.of(
                        "/fromhell/problematic/xml/bom-prefix",
                        "application/xml",
                        "\ufeff<?xml version=\"1.0\"",
                        "bom prefix"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpointExpectations")
    public void fromHellEndpointsReturnCataloguedResponses(
            final String method,
            final String path,
            final int statusCode,
            final String contentType,
            final String expectedStart,
            final String expectedContent) {

        final HttpResponseDetails response = send(path, method);

        Assertions.assertEquals(statusCode, response.statusCode);
        Assertions.assertTrue(response.body.startsWith(expectedStart));
        if (!expectedContent.isEmpty()) {
            Assertions.assertTrue(response.body.contains(expectedContent));
        }
        if (!contentType.isEmpty()) {
            Assertions.assertTrue(response.getHeader("Content-Type").contains(contentType));
        } else {
            Assertions.assertNull(response.getHeader("Content-Type"));
        }
        Assertions.assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("expandedMalformedEndpointExpectations")
    public void expandedMalformedEndpointsReturnCataloguedResponses(
            final String path,
            final String contentType,
            final String expectedStart,
            final String expectedContent) {

        final HttpResponseDetails response = send(path, "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains(contentType));
        Assertions.assertTrue(response.body.startsWith(expectedStart));
        Assertions.assertTrue(response.body.contains(expectedContent));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("additionalFormatEndpointExpectations")
    public void additionalFormatEndpointsReturnCataloguedResponses(
            final String path,
            final String contentType,
            final String expectedStart,
            final String expectedContent) {

        final HttpResponseDetails response = send(path, "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains(contentType));
        Assertions.assertTrue(response.body.startsWith(expectedStart));
        Assertions.assertTrue(response.body.contains(expectedContent));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingContentTypeEndpointExpectations")
    public void missingContentTypeEndpointsReturnCataloguedResponses(
            final String path, final String expectedStart, final String expectedContent) {

        final HttpResponseDetails response = send(path, "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNull(response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.startsWith(expectedStart));
        Assertions.assertTrue(response.body.contains(expectedContent));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("problematicJsonEndpointExpectations")
    public void problematicJsonEndpointsReturnCataloguedResponses(
            final String path,
            final String contentType,
            final String expectedStart,
            final String expectedContent) {

        final HttpResponseDetails response = send(path, "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains(contentType));
        Assertions.assertTrue(response.body.startsWith(expectedStart));
        if (!expectedContent.isEmpty()) {
            Assertions.assertTrue(response.body.contains(expectedContent));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("problematicXmlEndpointExpectations")
    public void problematicXmlEndpointsReturnCataloguedResponses(
            final String path,
            final String contentType,
            final String expectedStart,
            final String expectedContent) {

        final HttpResponseDetails response = send(path, "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains(contentType));
        Assertions.assertTrue(response.body.startsWith(expectedStart));
        if (!expectedContent.isEmpty()) {
            Assertions.assertTrue(response.body.contains(expectedContent));
        }
    }

    public static Stream<Arguments> bodyForbiddenStatusExpectations() {
        return Stream.of(
                Arguments.of(
                        "POST",
                        "/fromhell/status-code/205-with-body",
                        205,
                        "This body should not be sent with 205 Reset Content"),
                Arguments.of("GET", "/fromhell/status-code/304-with-body", 304, ""));
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("bodyForbiddenStatusExpectations")
    public void bodyForbiddenStatusEndpointsExposeObservedHttpStackBehaviour(
            final String method,
            final String path,
            final int statusCode,
            final String expectedBodyContent) {

        final HttpResponseDetails response = send(path, method);

        Assertions.assertEquals(statusCode, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("Content-Type"));
        if (expectedBodyContent.isEmpty()) {
            Assertions.assertEquals("", response.body);
        } else {
            Assertions.assertTrue(response.body.contains(expectedBodyContent));
        }
    }

    @Test
    public void strictJavaHttpClientRejectsNoContentWithContentLengthResponse() {
        final RuntimeException thrown =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> send("/fromhell/status-code/204-with-body", "DELETE"));

        Assertions.assertTrue(thrown.getMessage().contains("unexpected content length header"));
    }

    @Test
    public void knownPathWithWrongMethodReturnsMethodNotAllowed() {
        final HttpResponseDetails response = http.post("/fromhell/version", "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertEquals("GET, HEAD, OPTIONS", response.getHeader("Allow"));
        Assertions.assertTrue(response.body.contains("Method Not Allowed"));
    }

    @Test
    public void problematicXmlKnownPathWithWrongMethodReturnsMethodNotAllowed() {
        final HttpResponseDetails response =
                http.post("/fromhell/problematic/xml/attributes-vs-elements", "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertEquals("GET, HEAD, OPTIONS", response.getHeader("Allow"));
        Assertions.assertTrue(response.body.contains("Method Not Allowed"));
    }

    @Test
    public void additionalFormatKnownPathWithWrongMethodReturnsMethodNotAllowed() {
        final HttpResponseDetails response = http.post("/fromhell/good/csv", "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertEquals("GET, HEAD, OPTIONS", response.getHeader("Allow"));
        Assertions.assertTrue(response.body.contains("Method Not Allowed"));
    }

    @Test
    public void postOnlyKnownPathWithWrongMethodReturnsMethodNotAllowed() {
        final HttpResponseDetails response = http.get("/fromhell/status-code/201-no-location");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertEquals("POST, OPTIONS", response.getHeader("Allow"));
    }

    @Test
    public void optionsForKnownPathReturnsAllowHeader() {
        final HttpResponseDetails response = http.options("/fromhell/good/json");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertEquals("GET, HEAD, OPTIONS", response.getHeader("Allow"));
    }

    @Test
    public void headForGetEndpointReturnsNoBody() {
        final HttpResponseDetails response = http.head("/fromhell/good/json");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("", response.body);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("application/json"));
    }

    @Test
    public void unknownFromHellPathStillReturnsNotFound() {
        final HttpResponseDetails response = http.get("/fromhell/not-here");

        Assertions.assertEquals(404, response.statusCode);
    }

    @Test
    public void deliberateMethodNotAllowedEndpointDoesNotReturnAllowHeader() {
        final HttpResponseDetails response = http.post("/fromhell/status-code/405-no-allow", "");

        Assertions.assertEquals(405, response.statusCode);
        Assertions.assertNull(response.getHeader("Allow"));
        Assertions.assertTrue(response.body.contains("no Allow header"));
    }

    @Test
    public void malformedJsonEndpointReallyReturnsUnterminatedJsonArray() {
        final HttpResponseDetails response = http.send("/fromhell/malformed/json", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.startsWith("[{"));
        Assertions.assertFalse(response.body.endsWith("]"));
    }

    @Test
    public void fromHellOpenApiDocumentsEveryEndpoint() {
        final HttpResponseDetails response = http.send("/fromhell/docs/openapi.json", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("application/json"));
        Assertions.assertTrue(response.body.contains("\"title\": \"API From Hell\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/status\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/version\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/good/json\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/malformed/xml\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/status-code/201-no-location\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/status-code/204-with-body\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/status-code/500-success-body\""));
        for (FromHellEndpoint endpoint : FromHellCatalog.loadDefault().endpoints()) {
            Assertions.assertTrue(
                    response.body.contains("\"/fromhell" + endpoint.path() + "\""),
                    endpoint.path());
            Assertions.assertTrue(
                    response.body.contains("\"summary\": \"" + endpoint.label() + "\""),
                    endpoint.label());
        }
        Assertions.assertTrue(
                response.body.contains(
                        "\"summary\": \"Malformed JSON - Missing Collection Terminator\""));
        Assertions.assertTrue(
                response.body.contains("\"summary\": \"Good CSV - Valid CSV And Content-Type\""));
        Assertions.assertTrue(
                response.body.contains("\"summary\": \"Missing Content-Type - XML Body\""));
        Assertions.assertTrue(
                response.body.contains("\"summary\": \"Problematic JSON - Duplicate Keys\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"summary\": \"Problematic JSON - Empty Body With JSON Content-Type\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"summary\": \"Problematic XML - Attributes Vs Elements\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"summary\": \"Problematic XML - Empty Body With XML Content-Type\""));
        Assertions.assertTrue(
                response.body.contains("\"summary\": \"416 Missing Content-Range Header\""));
        Assertions.assertTrue(response.body.contains("\"post\""));
        Assertions.assertTrue(response.body.contains("\"delete\""));
        Assertions.assertTrue(response.body.contains("\"application/json\""));
        Assertions.assertTrue(response.body.contains("\"application/xml\""));
    }

    @Test
    public void fromHellOpenApiUsesForwardedOriginFirst() {
        final HttpMessageSender proxyHttp = new HttpMessageSender(Environment.getBaseUri());
        proxyHttp.clearHeaders();
        proxyHttp.setHeader("X-Forwarded-Proto", "https");
        proxyHttp.setHeader("X-Forwarded-Host", "apichallenges.eviltester.com");

        final HttpResponseDetails response = proxyHttp.send("/fromhell/docs/openapi.json", "get");

        Assertions.assertEquals(200, response.statusCode);
        final int firstServerIndex =
                response.body.indexOf("\"url\": \"https://apichallenges.eviltester.com\"");
        final int localServerIndex = response.body.indexOf("\"url\": \"http://localhost:4567\"");
        Assertions.assertTrue(firstServerIndex > 0);
        Assertions.assertTrue(localServerIndex > firstServerIndex);
    }

    @Test
    public void fromHellSwaggerDownloadIsAttachment() {
        final HttpResponseDetails response = http.send("/fromhell/docs/swagger", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/octet-stream", response.getHeader("Content-Type"));
        Assertions.assertTrue(
                response.getHeader("Content-Disposition")
                        .contains("filename=\"api-from-hell-openapi.json\""));
        Assertions.assertTrue(response.body.contains("\"/fromhell/good/json\""));
    }

    @Test
    public void fromHellSwaggerUiUsesPackagedOpenApi() {
        final HttpResponseDetails response = http.send("/fromhell/docs/swagger-ui", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>API From Hell - Swagger UI</h1>"));
        Assertions.assertTrue(response.body.contains("url: \"/fromhell/docs/openapi.json\""));
        Assertions.assertTrue(
                response.body.contains("https://unpkg.com/swagger-ui-dist/swagger-ui.css"));
    }

    private HttpResponseDetails send(final String path, final String method) {
        switch (method) {
            case "GET":
                return http.get(path);
            case "HEAD":
                return http.head(path);
            case "OPTIONS":
                return http.options(path);
            case "POST":
                return http.post(path, "");
            case "PUT":
                return http.put(path, "");
            case "PATCH":
                return http.patch(path, "");
            case "DELETE":
                return http.delete(path);
            case "TRACE":
                return http.trace(path);
            default:
                return http.send(path, method);
        }
    }
}
