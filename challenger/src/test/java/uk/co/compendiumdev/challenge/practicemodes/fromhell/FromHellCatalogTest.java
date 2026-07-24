package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FromHellCatalogTest {

    private static final String[] MALFORMED_JSON_PATHS = {
        "/malformed/json/trailing-comma-array",
        "/malformed/json/trailing-comma-object",
        "/malformed/json/unquoted-key",
        "/malformed/json/single-quoted-string",
        "/malformed/json/bad-escape",
        "/malformed/json/truncated-string",
        "/malformed/json/extra-data-after-document",
        "/malformed/json/missing-colon",
        "/malformed/json/leading-zero-number",
        "/malformed/json/nan-value",
        "/malformed/json/unclosed-object",
        "/malformed/json/control-character"
    };

    private static final String[] MALFORMED_XML_PATHS = {
        "/malformed/xml/mismatched-tag",
        "/malformed/xml/unescaped-ampersand",
        "/malformed/xml/duplicate-attribute",
        "/malformed/xml/undefined-entity",
        "/malformed/xml/multiple-root-elements",
        "/malformed/xml/missing-attribute-quote",
        "/malformed/xml/unclosed-cdata",
        "/malformed/xml/invalid-character",
        "/malformed/xml/truncated-document",
        "/malformed/xml/bad-processing-instruction"
    };

    private static final String[] PROBLEMATIC_JSON_PATHS = {
        "/problematic/json/duplicate-keys",
        "/problematic/json/large-integer",
        "/problematic/json/high-precision-decimal",
        "/problematic/json/exponent-number",
        "/problematic/json/null-vs-missing",
        "/problematic/json/escaped-unicode",
        "/problematic/json/empty-object",
        "/problematic/json/top-level-string",
        "/problematic/json/top-level-number",
        "/problematic/json/top-level-boolean",
        "/problematic/json/top-level-null",
        "/problematic/json/empty-body",
        "/problematic/json/bom-prefix",
        "/problematic/json/ndjson"
    };

    private static final String[] PROBLEMATIC_XML_PATHS = {
        "/problematic/xml/attributes-vs-elements",
        "/problematic/xml/empty-vs-missing-vs-nil",
        "/problematic/xml/whitespace-text",
        "/problematic/xml/cdata-content",
        "/problematic/xml/mixed-content",
        "/problematic/xml/processing-instruction",
        "/problematic/xml/comments",
        "/problematic/xml/encoding-mismatch",
        "/problematic/xml/doctype-dtd",
        "/problematic/xml/empty-body",
        "/problematic/xml/bom-prefix"
    };

    private static final String[] ADDITIONAL_FORMAT_PATHS = {
        "/good/text",
        "/good/html",
        "/good/csv",
        "/good/yaml",
        "/good/form-urlencoded",
        "/good/octet-stream"
    };

    private static final String[] MISSING_CONTENT_TYPE_PATHS = {
        "/missing-content-type/json", "/missing-content-type/xml", "/missing-content-type/html"
    };

    @Test
    public void catalogLoadsExpectedEndpoints() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        Assertions.assertEquals("API From Hell", catalog.name());
        Assertions.assertEquals(77, catalog.endpoints().size());

        final Set<String> paths =
                catalog.endpoints().stream()
                        .map(FromHellEndpoint::path)
                        .collect(Collectors.toSet());
        Assertions.assertEquals(77, paths.size());
        Assertions.assertTrue(paths.contains("/status"));
        Assertions.assertTrue(paths.contains("/version"));
        Assertions.assertTrue(paths.contains("/good/json"));
        Assertions.assertTrue(paths.contains("/good/xml"));
        Assertions.assertTrue(paths.contains("/malformed/json"));
        Assertions.assertTrue(paths.contains("/malformed/xml"));
        Assertions.assertTrue(paths.contains("/mismatch/content-type/json-xml"));
        Assertions.assertTrue(paths.contains("/mismatch/content-type/xml-json"));
        Assertions.assertTrue(paths.contains("/status-code/201-no-location"));
        Assertions.assertTrue(paths.contains("/status-code/204-with-body"));
        Assertions.assertTrue(paths.contains("/status-code/405-no-allow"));
        Assertions.assertTrue(paths.contains("/status-code/500-success-body"));
        for (String path : MALFORMED_JSON_PATHS) {
            Assertions.assertTrue(paths.contains(path), path);
        }
        for (String path : MALFORMED_XML_PATHS) {
            Assertions.assertTrue(paths.contains(path), path);
        }
        for (String path : PROBLEMATIC_JSON_PATHS) {
            Assertions.assertTrue(paths.contains(path), path);
        }
        for (String path : PROBLEMATIC_XML_PATHS) {
            Assertions.assertTrue(paths.contains(path), path);
        }
        for (String path : ADDITIONAL_FORMAT_PATHS) {
            Assertions.assertTrue(paths.contains(path), path);
        }
        for (String path : MISSING_CONTENT_TYPE_PATHS) {
            Assertions.assertTrue(paths.contains(path), path);
        }
    }

    @Test
    public void catalogPreservesResponseQuirks() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        final FromHellEndpoint malformedJson = catalog.endpoint("/malformed/json");
        Assertions.assertEquals("application/json", malformedJson.contentType());
        Assertions.assertTrue(malformedJson.body().startsWith("[{"));
        Assertions.assertFalse(malformedJson.body().endsWith("]"));

        final FromHellEndpoint malformedXml = catalog.endpoint("/malformed/xml");
        Assertions.assertEquals("application/xml", malformedXml.contentType());
        Assertions.assertTrue(malformedXml.body().contains("<root>"));
        Assertions.assertFalse(malformedXml.body().contains("</root>"));

        final FromHellEndpoint jsonHeaderXmlBody =
                catalog.endpoint("/mismatch/content-type/json-xml");
        Assertions.assertEquals("application/json", jsonHeaderXmlBody.contentType());
        Assertions.assertTrue(jsonHeaderXmlBody.body().startsWith("<?xml"));

        final FromHellEndpoint xmlHeaderJsonBody =
                catalog.endpoint("/mismatch/content-type/xml-json");
        Assertions.assertEquals("application/xml", xmlHeaderJsonBody.contentType());
        Assertions.assertTrue(xmlHeaderJsonBody.body().startsWith("[{"));

        final FromHellEndpoint version = catalog.endpoint("/version");
        Assertions.assertTrue(version.contentType().isEmpty());
        Assertions.assertEquals("{\"version\":\"6\"}", version.body());

        final FromHellEndpoint createdWithoutLocation =
                catalog.endpoint("POST", "/status-code/201-no-location");
        Assertions.assertEquals(201, createdWithoutLocation.statusCode());
        Assertions.assertTrue(createdWithoutLocation.getHeader("Location").isEmpty());
    }

    @Test
    public void catalogPreservesExpandedMalformedJsonQuirks() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        for (String path : MALFORMED_JSON_PATHS) {
            Assertions.assertEquals("application/json", catalog.endpoint(path).contentType(), path);
        }
        Assertions.assertTrue(
                catalog.endpoint("/malformed/json/trailing-comma-array").body().endsWith(",]"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/json/trailing-comma-object").body().endsWith(",}"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/json/bad-escape").body().contains("\\q"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/json/truncated-string")
                        .body()
                        .endsWith("unterminated}"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/json/extra-data-after-document")
                        .body()
                        .contains("} {"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/json/control-character").body().contains("\u0001"));
    }

    @Test
    public void catalogPreservesProblematicJsonQuirks() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        for (String path : PROBLEMATIC_JSON_PATHS) {
            final String expectedType =
                    path.equals("/problematic/json/ndjson")
                            ? "application/x-ndjson"
                            : "application/json";
            Assertions.assertEquals(expectedType, catalog.endpoint(path).contentType(), path);
        }

        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/duplicate-keys")
                        .body()
                        .contains("\"id\":1,\"id\":2"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/large-integer")
                        .body()
                        .contains("9007199254740993"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/high-precision-decimal")
                        .body()
                        .contains("0.12345678901234567890123456789"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/exponent-number").body().contains("6.022e23"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/null-vs-missing")
                        .body()
                        .contains("\"nickname\":null"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/escaped-unicode").body().contains("zeroWidth"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/escaped-unicode")
                        .body()
                        .contains("confusable"));
        Assertions.assertEquals("{}", catalog.endpoint("/problematic/json/empty-object").body());
        Assertions.assertEquals(
                "\"hello\"", catalog.endpoint("/problematic/json/top-level-string").body());
        Assertions.assertEquals(
                "42", catalog.endpoint("/problematic/json/top-level-number").body());
        Assertions.assertEquals(
                "true", catalog.endpoint("/problematic/json/top-level-boolean").body());
        Assertions.assertEquals(
                "null", catalog.endpoint("/problematic/json/top-level-null").body());
        Assertions.assertEquals("", catalog.endpoint("/problematic/json/empty-body").body());
        Assertions.assertTrue(
                catalog.endpoint("/problematic/json/bom-prefix").body().startsWith("\ufeff"));
        Assertions.assertTrue(catalog.endpoint("/problematic/json/ndjson").body().contains("\n"));
    }

    @Test
    public void catalogPreservesExpandedMalformedXmlQuirks() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        for (String path : MALFORMED_XML_PATHS) {
            Assertions.assertEquals("application/xml", catalog.endpoint(path).contentType(), path);
        }
        Assertions.assertTrue(
                catalog.endpoint("/malformed/xml/mismatched-tag").body().contains("</thing>"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/xml/unescaped-ampersand")
                        .body()
                        .contains("Tom & Jerry"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/xml/duplicate-attribute")
                        .body()
                        .contains("id=\"1\" id=\"2\""));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/xml/missing-attribute-quote").body().contains("id=1"));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/xml/unclosed-cdata").body().contains("<![CDATA["));
        Assertions.assertTrue(
                catalog.endpoint("/malformed/xml/invalid-character").body().contains("\u0001"));
    }

    @Test
    public void catalogPreservesAdditionalContentFormatQuirks() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        Assertions.assertEquals("text/plain", catalog.endpoint("/good/text").contentType());
        Assertions.assertTrue(catalog.endpoint("/good/text").body().contains("plain text"));
        Assertions.assertEquals("text/html", catalog.endpoint("/good/html").contentType());
        Assertions.assertTrue(catalog.endpoint("/good/html").body().contains("<h1>HTML response"));
        Assertions.assertEquals("text/csv", catalog.endpoint("/good/csv").contentType());
        Assertions.assertTrue(catalog.endpoint("/good/csv").body().contains("id,name,active"));
        Assertions.assertEquals("application/yaml", catalog.endpoint("/good/yaml").contentType());
        Assertions.assertTrue(catalog.endpoint("/good/yaml").body().contains("  - id: 1"));
        Assertions.assertEquals(
                "application/x-www-form-urlencoded",
                catalog.endpoint("/good/form-urlencoded").contentType());
        Assertions.assertTrue(
                catalog.endpoint("/good/form-urlencoded").body().contains("name=Alice+Example"));
        Assertions.assertEquals(
                "application/octet-stream", catalog.endpoint("/good/octet-stream").contentType());
        Assertions.assertTrue(catalog.endpoint("/good/octet-stream").body().contains("\u0000"));

        for (String path : MISSING_CONTENT_TYPE_PATHS) {
            Assertions.assertTrue(catalog.endpoint(path).contentType().isEmpty(), path);
        }
        Assertions.assertTrue(
                catalog.endpoint("/missing-content-type/json").body().startsWith("{"));
        Assertions.assertTrue(
                catalog.endpoint("/missing-content-type/xml").body().startsWith("<?xml"));
        Assertions.assertTrue(
                catalog.endpoint("/missing-content-type/html").body().startsWith("<!doctype"));
    }

    @Test
    public void catalogPreservesProblematicXmlQuirks() {
        final FromHellCatalog catalog = FromHellCatalog.loadDefault();

        for (String path : PROBLEMATIC_XML_PATHS) {
            Assertions.assertTrue(
                    catalog.endpoint(path).contentType().contains("application/xml"), path);
        }

        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/attributes-vs-elements")
                        .body()
                        .contains("id=\"123\""));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/attributes-vs-elements")
                        .body()
                        .contains("<id>456</id>"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/empty-vs-missing-vs-nil")
                        .body()
                        .contains("xsi:nil=\"true\""));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/whitespace-text")
                        .body()
                        .contains(">  padded value  <"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/cdata-content").body().contains("<![CDATA["));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/mixed-content")
                        .body()
                        .contains("Hello <em>important</em> world"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/processing-instruction")
                        .body()
                        .contains("<?client-hint"));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/comments").body().contains("<!-- hidden note"));
        Assertions.assertEquals(
                "application/xml; charset=UTF-8",
                catalog.endpoint("/problematic/xml/encoding-mismatch").contentType());
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/encoding-mismatch")
                        .body()
                        .contains("encoding=\"ISO-8859-1\""));
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/doctype-dtd").body().contains("<!DOCTYPE note"));
        Assertions.assertEquals("", catalog.endpoint("/problematic/xml/empty-body").body());
        Assertions.assertTrue(
                catalog.endpoint("/problematic/xml/bom-prefix").body().startsWith("\ufeff"));
    }

    @Test
    public void catalogRejectsDuplicateMethodAndPath() {
        final String duplicateCatalog =
                "{\"name\":\"x\",\"description\":\"x\",\"endpoints\":["
                        + "{\"method\":\"GET\",\"path\":\"/same\"},"
                        + "{\"method\":\"GET\",\"path\":\"/same\"}]}";

        Assertions.assertThrows(
                IllegalStateException.class,
                () ->
                        FromHellCatalog.loadFrom(
                                new ByteArrayInputStream(
                                        duplicateCatalog.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void catalogAllowsSamePathWithDifferentMethods() {
        final String duplicatePathCatalog =
                "{\"name\":\"x\",\"description\":\"x\",\"endpoints\":["
                        + "{\"method\":\"GET\",\"path\":\"/same\"},"
                        + "{\"method\":\"POST\",\"path\":\"/same\"}]}";

        final FromHellCatalog catalog =
                FromHellCatalog.loadFrom(
                        new ByteArrayInputStream(
                                duplicatePathCatalog.getBytes(StandardCharsets.UTF_8)));

        Assertions.assertEquals(2, catalog.endpoints().size());
        Assertions.assertEquals(1, catalog.paths().size());
        Assertions.assertNotNull(catalog.endpoint("GET", "/same"));
        Assertions.assertNotNull(catalog.endpoint("POST", "/same"));
    }
}
