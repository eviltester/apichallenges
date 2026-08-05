package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OpenApiTesterConverterJavascriptTest {

    @Test
    void converterModuleExposesProfilesAndConversionRules() throws IOException {
        final String javascript = javascript("/public/js/openapi-tester-converter.js");

        Assertions.assertTrue(
                javascript.contains("root.ApiChallengesOpenApiTesterConverter = api"));
        Assertions.assertTrue(javascript.contains("profileOptions: profileOptions"));
        Assertions.assertTrue(javascript.contains("convert: convert"));
        Assertions.assertTrue(javascript.contains("const PRACTICAL_METHODS"));
        Assertions.assertTrue(javascript.contains("'get', 'post', 'put', 'patch', 'delete'"));
        Assertions.assertTrue(javascript.contains("const OPENAPI_METHODS"));
        Assertions.assertTrue(javascript.contains("'trace'"));
        Assertions.assertTrue(javascript.contains("Tester conversion supports OpenAPI 3.x specs"));
        Assertions.assertTrue(javascript.contains("parameter.in === 'path'"));
        Assertions.assertTrue(javascript.contains("parameter.required = true"));
        Assertions.assertTrue(javascript.contains("parameter.required = false"));
        Assertions.assertTrue(javascript.contains("requestBody.required = false"));
        Assertions.assertTrue(javascript.contains("generatedTester_"));
        Assertions.assertTrue(javascript.contains("'405'"));
    }

    @Test
    void converterPageJavascriptUsesSharedModuleAndExportActions() throws IOException {
        final String javascript = javascript("/public/js/openapi-converter-page.js");

        Assertions.assertTrue(javascript.contains("window.ApiChallengesOpenApiTesterConverter"));
        Assertions.assertTrue(javascript.contains("window.ApiChallengesOpenApiTextLoader"));
        Assertions.assertTrue(javascript.contains("data-openapi-profile"));
        Assertions.assertTrue(javascript.contains("data-openapi-option"));
        Assertions.assertTrue(javascript.contains("data-openapi-verb"));
        Assertions.assertTrue(javascript.contains("data-openapi-copy-converted"));
        Assertions.assertTrue(javascript.contains("data-openapi-download-converted"));
        Assertions.assertTrue(javascript.contains("data-openapi-open-swagger"));
        Assertions.assertTrue(javascript.contains("apiChallengesConvertedOpenApiSpec"));
        Assertions.assertTrue(javascript.contains("/tools/online-clients/swagger?converted=session"));
        Assertions.assertTrue(javascript.contains("loader.fetchOpenApi(openApiUrl)"));
        Assertions.assertTrue(javascript.contains("loader.parseOpenApiText"));
        Assertions.assertTrue(javascript.contains("customOptions && profile === 'custom'"));
        Assertions.assertFalse(javascript.contains("data-openapi-example"));
        Assertions.assertFalse(javascript.contains("customOptions.open = profile === 'custom'"));
        Assertions.assertFalse(javascript.contains("function parseOpenApiText"));
    }

    @Test
    void swaggerJavascriptUsesSharedModuleAndCanRenderConvertedSessionSpec() throws IOException {
        final String javascript = javascript("/public/js/online-swagger-client.js");

        Assertions.assertTrue(javascript.contains("window.ApiChallengesOpenApiTesterConverter"));
        Assertions.assertTrue(javascript.contains("window.ApiChallengesOpenApiTextLoader"));
        Assertions.assertTrue(javascript.contains("data-openapi-profile"));
        Assertions.assertTrue(javascript.contains("data-openapi-option"));
        Assertions.assertTrue(javascript.contains("data-openapi-verb"));
        Assertions.assertTrue(javascript.contains("data-openapi-copy-converted"));
        Assertions.assertTrue(javascript.contains("data-openapi-download-converted"));
        Assertions.assertTrue(javascript.contains("apiChallengesConvertedOpenApiSpec"));
        Assertions.assertTrue(javascript.contains("converted') === 'session'"));
        Assertions.assertTrue(javascript.contains("api.convert(originalSpec, options)"));
        Assertions.assertTrue(javascript.contains("loader.fetchOpenApi(openApiUrl)"));
        Assertions.assertTrue(javascript.contains("loader.parseOpenApiText"));
        Assertions.assertTrue(javascript.contains("customOptions && profile === 'custom'"));
        Assertions.assertFalse(javascript.contains("data-openapi-example"));
        Assertions.assertFalse(javascript.contains("customOptions.open = profile === 'custom'"));
        Assertions.assertFalse(javascript.contains("function parseOpenApiText"));
    }

    @Test
    void textLoaderJavascriptExposesJsonYamlParsingAndUrlLoading() throws IOException {
        final String javascript = javascript("/public/js/openapi-text-loader.js");

        Assertions.assertTrue(javascript.contains("root.ApiChallengesOpenApiTextLoader = api"));
        Assertions.assertTrue(javascript.contains("parseOpenApiText: parseOpenApiText"));
        Assertions.assertTrue(javascript.contains("fetchOpenApi: fetchOpenApi"));
        Assertions.assertTrue(javascript.contains("JSON.parse(trimmed)"));
        Assertions.assertTrue(javascript.contains("root.jsyaml.load(trimmed)"));
        Assertions.assertTrue(javascript.contains("YAML parsing is unavailable"));
        Assertions.assertTrue(javascript.contains("The server returned HTTP"));
    }

    private String javascript(final String resourcePath) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
