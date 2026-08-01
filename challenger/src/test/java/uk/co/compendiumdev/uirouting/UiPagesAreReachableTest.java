package uk.co.compendiumdev.uirouting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.serverstart.Environment;

public class UiPagesAreReachableTest {

    /*
           Check UI routing without spinning up a browser test tool
    */

    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp() {
        // this uses the Environment to startup the server app to
        // issue http tests and test the server routing
        http = new HttpMessageSender(Environment.getBaseUri());

        // Basic Browser Headers
        http.clearHeaders();
        http.setHeader("ContentType", "text/html; charset=utf-8");
        http.setHeader(
                "Accept",
                "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");
    }

    @Test
    void noProcessingWhenNoBasicAuth() {

        final HttpResponseDetails response = http.send("/", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='viewport' content='width=device-width, initial-scale=1'>"));
        Assertions.assertTrue(
                response.body.contains("<meta property='og:type' content='website'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:url' content='https://apichallenges.eviltester.com'>"));
        Assertions.assertTrue(response.body.contains("application/ld+json"));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Organization\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"WebSite\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"WebPage\""));
        Assertions.assertTrue(
                response.body.contains(
                        "/images/hero/apichallenges-whole-site-gauntlet-1600x720.jpg"));
        assertContainsHeaderAndFooter(response);
    }

    @Test
    void receive404onMissingPage() {

        final HttpResponseDetails response = http.send("/bob", "get");

        Assertions.assertEquals(404, response.statusCode);
    }

    @Test
    void simulated404PageExistsAndReportsAs404() {

        // we currently don't have 404 because of the way the app is constructed
        // instead we should trap a 404 response and return a 307 redirecting to
        // 404 page with the original url appended to allow javascript to render
        // as if it was a 404 page

        final HttpResponseDetails response = http.send("/gui/404", "get");

        Assertions.assertEquals(404, response.statusCode);
        assertContainsHeaderAndFooter(response);
        Assertions.assertTrue(response.body.contains("<h1>Page Not Found</h1>"));
    }

    @Test
    void simulated404PageExistsAndReportsAs404WithPath() {

        // we currently don't have 404 because of the way the app is constructed
        // instead we should trap a 404 response and return a 307 redirecting to
        // 404 page with the original url appended to allow javascript to render
        // as if it was a 404 page

        final HttpResponseDetails response = http.send("/gui/404/bob/dobbs", "get");

        Assertions.assertEquals(404, response.statusCode);
        assertContainsHeaderAndFooter(response);
        Assertions.assertTrue(response.body.contains("<h1>Page Not Found</h1>"));
    }

    static Stream<Arguments> simplePageRoutingStatus() {
        List<Arguments> args = new ArrayList<>();

        // home page
        args.add(Arguments.of(200, "API Challenges Tutorials and Testing Practice", ""));
        args.add(Arguments.of(200, "API Challenges Tutorials and Testing Practice", "/"));
        // entities
        args.add(Arguments.of(200, "Entities Menu", "/gui/entities"));
        args.add(Arguments.of(200, "todo Instances", "gui/instances?entity=todo"));

        // Challenges
        args.add(Arguments.of(200, "API Challenges - Improve your API Skills", "/gui/challenges"));
        args.add(Arguments.of(200, "API Challenges Client", "/apichallenges/client"));
        args.add(Arguments.of(200, "Simple API Client", "/simpleapi/client"));
        args.add(Arguments.of(200, "Buggy API Client", "/shop/client"));
        args.add(
                Arguments.of(
                        200,
                        "API Challenges - Improve your API Skills",
                        "/gui/challenges/unkownchallenger"));

        // Additional Pages
        args.add(
                Arguments.of(
                        200, "Learning Utilities and Resources | API Challenges", "/learning"));
        args.add(
                Arguments.of(
                        200, "Multi-User Instructions | API Challenges Guide", "/gui/multiuser"));
        args.add(Arguments.of(200, "API Challenges API Documentation | API Challenges", "/docs"));
        args.add(
                Arguments.of(
                        200,
                        "HTTP Mirror Mode | API Challenges Practice Mode",
                        "/practice-modes/mirror"));
        args.add(
                Arguments.of(
                        200,
                        "Simulation Mode | API Challenges Practice Mode",
                        "/practice-modes/simulation"));
        return args.stream();
    }

    @ParameterizedTest(name = "simple known page routing expected {0} for {1} {2}")
    @MethodSource("simplePageRoutingStatus")
    void simplePageRoutingTest(int statusCode, String title, String url) {
        final HttpResponseDetails response = http.send(url, "get");

        Assertions.assertEquals(statusCode, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(String.format("<title>%s</title>", title)),
                String.format("Title not found %s", title));
        assertContainsHeaderAndFooter(response);
    }

    @Test
    void restrictedApiClientPagesUseAdhocLiveRequestWidgets() {
        assertClientPage(
                "/apichallenges/client",
                "/todos",
                "/todos||/todo||/challenges||/challenger||/secret||/heartbeat",
                "true");
        assertClientPage("/simpleapi/client", "/simpleapi/items", "/simpleapi", "false");
        assertClientPage("/shop/client", "/shop/products", "/shop", "false");
    }

    private void assertClientPage(
            final String path,
            final String defaultPath,
            final String allowedPathPrefixes,
            final String useChallenger) {
        final HttpResponseDetails response = http.send(path, "get");

        Assertions.assertEquals(200, response.statusCode);
        assertBodyContainsVersionedScript(response, "/js/api-live-request.js");
        Assertions.assertTrue(response.body.contains("class=\"api-live-request\""));
        Assertions.assertTrue(response.body.contains("data-method=\"GET\""));
        Assertions.assertTrue(response.body.contains("data-path=\"" + defaultPath + "\""));
        Assertions.assertTrue(response.body.contains("data-edit-mode=\"adhoc\""));
        Assertions.assertTrue(
                response.body.contains(
                        "data-allowed-path-prefixes=\"" + allowedPathPrefixes + "\""));
        Assertions.assertTrue(
                response.body.contains("data-use-challenger=\"" + useChallenger + "\""));
    }

    private void assertContainsHeaderAndFooter(HttpResponseDetails response) {

        if (!response.body.contains("<div class=\"css-menu\">")) {
            Assertions.fail("Page did not contain header menu");
        }
        if (!response.body.contains("<div class='footer'>")) {
            Assertions.fail("Page did not contain footer");
        }
        if (!response.body.contains("Copyright Compendium Developments")) {
            Assertions.fail("Page did not contain full page");
        }
    }

    private void assertCacheControl(final HttpResponseDetails response, final String expected) {
        Assertions.assertEquals(expected, response.getHeader("Cache-Control"));
    }

    private void assertBodyContainsVersionedScript(
            final HttpResponseDetails response, final String scriptPath) {
        Assertions.assertTrue(
                response.body.matches(
                        "(?s).*src=['\"]" + Pattern.quote(scriptPath) + "\\?v=[^'\"]+['\"].*"),
                "Expected versioned script " + scriptPath);
    }

    private void assertBodyContainsVersionedStylesheet(
            final HttpResponseDetails response, final String stylesheetPath) {
        Assertions.assertTrue(
                response.body.matches(
                        "(?s).*href=['\"]" + Pattern.quote(stylesheetPath) + "\\?v=[^'\"]+['\"].*"),
                "Expected versioned stylesheet " + stylesheetPath);
    }

    private void assertOpenApiVersion(final String body, final String expectedVersion) {
        final Matcher matcher = Pattern.compile("\"openapi\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        Assertions.assertTrue(matcher.find(), "Expected OpenAPI version in response");
        if (expectedVersion.endsWith(".")) {
            Assertions.assertTrue(
                    matcher.group(1).startsWith(expectedVersion),
                    "Expected OpenAPI version to start with " + expectedVersion);
        } else {
            Assertions.assertEquals(expectedVersion, matcher.group(1));
        }
    }

    private int countOccurrences(final String body, final String value) {
        int count = 0;
        int index = 0;
        while ((index = body.indexOf(value, index)) >= 0) {
            count++;
            index += value.length();
        }
        return count;
    }

    private String challengeIdFor(final String challengeName) {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        for (ChallengeDefinitionData challenge : new ChallengeDefinitions(config).getChallenges()) {
            if (challenge.name.equals(challengeName)) {
                return challenge.id;
            }
        }
        Assertions.fail("Could not find challenge " + challengeName);
        return "";
    }

    private String normalizedChallengeId(final String challengeId) {
        return challengeId.replaceFirst("^0+(?!$)", "");
    }

    private void assertChallengeStatus(
            final HttpMessageSender statusHttp,
            final String challengeId,
            final boolean expectedStatus) {

        final HttpResponseDetails response =
                statusHttp.send("/gui/challenge-status/" + challengeId, "get");
        Assertions.assertEquals(200, response.statusCode);
        final JsonObject json = JsonParser.parseString(response.body).getAsJsonObject();
        Assertions.assertTrue(json.get("known").getAsBoolean());
        Assertions.assertEquals(normalizedChallengeId(challengeId), json.get("id").getAsString());
        Assertions.assertEquals(expectedStatus, json.get("status").getAsBoolean());
    }

    private void assertOpenApiFilePageLinks(
            final String body, final String docsPrefix, final String oldSwaggerPath) {

        Assertions.assertTrue(body.contains("currently returns OpenAPI v 3.1"));
        Assertions.assertFalse(body.contains("Download Normal OpenAPI File"));
        Assertions.assertFalse(body.contains("Download Permissive OpenAPI File"));
        Assertions.assertFalse(body.contains("href=\"" + oldSwaggerPath + "\""));
        Assertions.assertFalse(body.contains("href='" + oldSwaggerPath + "'"));
        Assertions.assertFalse(body.contains("href=\"" + oldSwaggerPath + "?permissive\""));
        Assertions.assertFalse(body.contains("href='" + oldSwaggerPath + "?permissive'"));

        for (final String version : List.of("3.0", "3.1", "3.2")) {
            final String openApiJsonPath = docsPrefix + "/docs/openapi-" + version + ".json";
            final String normalizedBody = body.replace("&amp;", "&");
            Assertions.assertTrue(body.contains("OpenAPI v " + version + " JSON"));
            Assertions.assertTrue(body.contains(openApiJsonPath));
            Assertions.assertTrue(body.contains(openApiJsonPath + "?download"));
            Assertions.assertTrue(body.contains(openApiJsonPath + "?permissive"));
            Assertions.assertTrue(
                    body.contains(openApiJsonPath + "?permissive&amp;download")
                            || body.contains(openApiJsonPath + "?permissive&download"));
            Assertions.assertTrue(
                    countOccurrences(normalizedBody, openApiJsonPath + "?download") >= 2);
            Assertions.assertTrue(
                    countOccurrences(normalizedBody, openApiJsonPath + "?permissive&download")
                            >= 2);
        }
    }

    @Test
    void canDownloadSwaggerFile() {

        // we currently don't have 404 because of the way the app is constructed
        // instead we should trap a 404 response and return a 307 redirecting to
        // 404 page with the original url appended to allow javascript to render
        // as if it was a 404 page

        final HttpResponseDetails response = http.send("/docs/swagger", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals(
                "attachment; filename=\"API-Challenges-Simple-Todo-List-swagger.json\"",
                response.getHeader("Content-Disposition"));
        assertOpenApiVersion(response.body, "3.1.0");
        Assertions.assertTrue(
                response.body.contains("\"title\" : \"API Challenges Simple Todo List\""));
    }

    @Test
    void canFetchDefaultOpenApiJsonForSwaggerUi() {

        final HttpResponseDetails response = http.send("/docs/openapi.json", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNotNull(response.getHeader("Content-Type"));
        Assertions.assertTrue(response.getHeader("Content-Type").contains("application/json"));
        assertOpenApiVersion(response.body, "3.1.0");
        Assertions.assertTrue(
                response.body.contains("\"title\" : \"API Challenges Simple Todo List\""));
        Assertions.assertTrue(
                response.body.indexOf("\"url\" : \"http://localhost:4567\"")
                        < response.body.indexOf(
                                "\"url\" : \"https://apichallenges.eviltester.com\""));
    }

    @Test
    void canFetchOpenApiJsonForSwaggerUiBehindHttpsProxy() {

        final HttpMessageSender proxyHttp = new HttpMessageSender(Environment.getBaseUri());
        proxyHttp.clearHeaders();
        proxyHttp.setHeader("X-Forwarded-Proto", "https");
        proxyHttp.setHeader("X-Forwarded-Host", "apichallenges.eviltester.com");

        final HttpResponseDetails response = proxyHttp.send("/docs/openapi.json", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNotNull(response.getHeader("Content-Type"));
        Assertions.assertTrue(response.getHeader("Content-Type").contains("application/json"));
        assertOpenApiVersion(response.body, "3.1.0");
        Assertions.assertTrue(
                response.body.indexOf("\"url\" : \"https://apichallenges.eviltester.com\"")
                        < response.body.indexOf("\"url\" : \"http://localhost:4567\""));
    }

    static Stream<Arguments> versionedOpenApiJsonRoutes() {
        List<Arguments> args = new ArrayList<>();
        for (String prefix : List.of("", "/simpleapi", "/sim", "/mirror", "/fromhell")) {
            args.add(Arguments.of(prefix + "/docs/openapi.json", "3.1.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.1.json", "3.1.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.2.json", "3.2.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.0.json", "3.0."));
        }
        return args.stream();
    }

    @ParameterizedTest(name = "openapi json {0} uses {1}")
    @MethodSource("versionedOpenApiJsonRoutes")
    void canFetchVersionedOpenApiJsonForSwaggerUi(
            final String openApiJsonPath, final String expectedVersion) {

        final HttpResponseDetails response = http.send(openApiJsonPath, "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNotNull(response.getHeader("Content-Type"));
        Assertions.assertTrue(response.getHeader("Content-Type").contains("application/json"));
        assertOpenApiVersion(response.body, expectedVersion);
    }

    static Stream<Arguments> thingifierBackedVersionedOpenApiJsonRoutes() {
        List<Arguments> args = new ArrayList<>();
        for (String prefix : List.of("", "/simpleapi", "/sim", "/mirror")) {
            args.add(Arguments.of(prefix + "/docs/openapi.json", "3.1.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.1.json", "3.1.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.2.json", "3.2.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.0.json", "3.0."));
        }
        return args.stream();
    }

    @ParameterizedTest(name = "openapi json {0} has permissive variant")
    @MethodSource("thingifierBackedVersionedOpenApiJsonRoutes")
    void versionedOpenApiJsonCanBeGeneratedInPermissiveForm(
            final String openApiJsonPath, final String expectedVersion) {

        final HttpResponseDetails permissiveResponse =
                http.send(openApiJsonPath + "?permissive", "get");

        Assertions.assertEquals(200, permissiveResponse.statusCode);
        assertOpenApiVersion(permissiveResponse.body, expectedVersion);
    }

    @ParameterizedTest(name = "openapi json {0} can be downloaded")
    @MethodSource("thingifierBackedVersionedOpenApiJsonRoutes")
    void versionedOpenApiJsonCanBeDownloaded(
            final String openApiJsonPath, final String expectedVersion) {

        final HttpResponseDetails downloadResponse =
                http.send(openApiJsonPath + "?download", "get");
        final HttpResponseDetails permissiveDownloadResponse =
                http.send(openApiJsonPath + "?permissive&download", "get");

        Assertions.assertEquals(200, downloadResponse.statusCode);
        Assertions.assertEquals(200, permissiveDownloadResponse.statusCode);
        Assertions.assertTrue(
                downloadResponse.getHeader("Content-Type").contains("application/json"));
        Assertions.assertTrue(
                permissiveDownloadResponse.getHeader("Content-Type").contains("application/json"));
        assertOpenApiVersion(downloadResponse.body, expectedVersion);
        assertOpenApiVersion(permissiveDownloadResponse.body, expectedVersion);

        final String filename = openApiJsonPath.substring(openApiJsonPath.lastIndexOf("/") + 1);
        Assertions.assertEquals(
                "attachment; filename=\"" + filename + "\"",
                downloadResponse.getHeader("Content-Disposition"));
        Assertions.assertEquals(
                "attachment; filename=\"permissive-" + filename + "\"",
                permissiveDownloadResponse.getHeader("Content-Disposition"));
    }

    @Test
    void openApiFilePagesUseVersionedStandardPermissiveAndDownloadLinks() {

        final HttpResponseDetails apiChallengesOpenApiPage =
                http.send("/apichallenges/openapi", "get");

        Assertions.assertEquals(200, apiChallengesOpenApiPage.statusCode);
        assertOpenApiFilePageLinks(apiChallengesOpenApiPage.body, "", "/docs/swagger");

        final HttpResponseDetails simpleApiOpenApiPage =
                http.send("/practice-modes/simpleapi-openapi", "get");

        Assertions.assertEquals(200, simpleApiOpenApiPage.statusCode);
        assertOpenApiFilePageLinks(
                simpleApiOpenApiPage.body, "/simpleapi", "/simpleapi/docs/swagger");
    }

    static Stream<Arguments> expandableThingifierBackedVersionedOpenApiJsonRoutes() {
        List<Arguments> args = new ArrayList<>();
        for (String prefix : List.of("", "/simpleapi", "/sim")) {
            args.add(Arguments.of(prefix + "/docs/openapi.json", "3.1.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.1.json", "3.1.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.2.json", "3.2.0"));
            args.add(Arguments.of(prefix + "/docs/openapi-3.0.json", "3.0."));
        }
        return args.stream();
    }

    @ParameterizedTest(name = "openapi json {0} expands in permissive form")
    @MethodSource("expandableThingifierBackedVersionedOpenApiJsonRoutes")
    void versionedOpenApiJsonPermissiveFormExpandsGeneratedSpec(
            final String openApiJsonPath, final String expectedVersion) {

        final HttpResponseDetails strictResponse = http.send(openApiJsonPath, "get");
        final HttpResponseDetails permissiveResponse =
                http.send(openApiJsonPath + "?permissive", "get");

        Assertions.assertEquals(200, strictResponse.statusCode);
        Assertions.assertEquals(200, permissiveResponse.statusCode);
        assertOpenApiVersion(strictResponse.body, expectedVersion);
        assertOpenApiVersion(permissiveResponse.body, expectedVersion);
        Assertions.assertNotEquals(strictResponse.body, permissiveResponse.body);
    }

    @Test
    void generatedOpenApiDocumentsQueryAccordingToSpecVersion() {

        HttpResponseDetails response = http.send("/docs/openapi-3.2.json", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"/todos\""));
        Assertions.assertTrue(response.body.contains("\"query\""));
        Assertions.assertFalse(response.body.contains("\"x-query-operation\""));

        response = http.send("/docs/openapi-3.1.json", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"x-query-operation\""));

        response = http.send("/sim/docs/openapi-3.2.json", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"/sim/entities\""));
        Assertions.assertTrue(response.body.contains("\"query\""));

        response = http.send("/mirror/docs/openapi-3.2.json", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"/mirror/request\""));
        Assertions.assertTrue(response.body.contains("\"query\""));
    }

    @Test
    void generatedApiDocsDocumentPatchInstanceRoutesAsSupported() {

        assertPatchRouteDocumentedAsSupported("/docs", "/todos/:id", "todo");
        assertPatchRouteDocumentedAsSupported("/simpleapi/docs", "/simpleapi/items/:id", "item");
    }

    @Test
    void generatedOpenApiDocumentsPatchForEntityInstanceRoutesOnly() {

        HttpResponseDetails response = http.send("/docs/openapi.json", "get");
        Assertions.assertEquals(200, response.statusCode);

        JsonObject todoCollection = openApiPath(response.body, "/todos");
        JsonObject todoInstance = openApiPath(response.body, "/todos/{id}");
        Assertions.assertFalse(todoCollection.has("patch"));
        Assertions.assertTrue(todoInstance.has("patch"));
        assertPatchRequestBodiesAndAcceptPatch(todoInstance);
        Assertions.assertFalse(todoCollection.toString().contains("Accept-Patch"));

        response = http.send("/simpleapi/docs/openapi.json", "get");
        Assertions.assertEquals(200, response.statusCode);

        JsonObject itemCollection = openApiPath(response.body, "/simpleapi/items");
        JsonObject itemInstance = openApiPath(response.body, "/simpleapi/items/{id}");
        Assertions.assertFalse(itemCollection.has("patch"));
        Assertions.assertTrue(itemInstance.has("patch"));
        assertPatchRequestBodiesAndAcceptPatch(itemInstance);
        Assertions.assertFalse(itemCollection.toString().contains("Accept-Patch"));
    }

    private JsonObject openApiPath(final String body, final String path) {
        final JsonObject paths =
                JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("paths");
        Assertions.assertTrue(paths.has(path), "Expected OpenAPI path " + path);
        return paths.getAsJsonObject(path);
    }

    private void assertPatchRequestBodiesAndAcceptPatch(final JsonObject instancePath) {
        final JsonObject patchOperation = instancePath.getAsJsonObject("patch");
        final String expectedDocumentationPrefix = "patch a specific instance of ";
        Assertions.assertTrue(patchOperation.has("summary"));
        Assertions.assertTrue(patchOperation.has("description"));
        Assertions.assertTrue(
                patchOperation
                        .get("summary")
                        .getAsString()
                        .startsWith(expectedDocumentationPrefix));
        Assertions.assertTrue(
                patchOperation
                        .get("description")
                        .getAsString()
                        .startsWith(expectedDocumentationPrefix));
        Assertions.assertNotEquals(
                "method not allowed", patchOperation.get("summary").getAsString());
        Assertions.assertNotEquals(
                "method not allowed", patchOperation.get("description").getAsString());
        final JsonObject content =
                patchOperation.getAsJsonObject("requestBody").getAsJsonObject("content");

        Assertions.assertTrue(content.has("application/json"));
        Assertions.assertTrue(content.has("application/merge-patch+json"));
        Assertions.assertTrue(content.has("application/json-patch+json"));
        Assertions.assertTrue(instancePath.toString().contains("Accept-Patch"));
    }

    private void assertPatchRouteDocumentedAsSupported(
            final String docsPath, final String routePath, final String entityName) {

        final HttpResponseDetails response = http.send(docsPath, "get");
        final String expectedDocumentation =
                "patch a specific instance of "
                        + entityName
                        + " with a body containing the patch details";

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<strong>PATCH " + routePath + "</strong>"));
        Assertions.assertTrue(response.body.contains(expectedDocumentation));
        Assertions.assertFalse(
                response.body.contains(
                        "<strong>PATCH "
                                + routePath
                                + "</strong><ul><li class='normal'>method not allowed</li></ul>"));
    }

    @Test
    void simulationModePageUsesLocalOriginByDefault() {

        final HttpResponseDetails response = http.send("/practice-modes/simulation", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("GET http://localhost:4567/sim/entities"));
    }

    @Test
    void simulationModePageUsesForwardedHttpsOriginBehindProxy() {

        final HttpMessageSender proxyHttp = new HttpMessageSender(Environment.getBaseUri());
        proxyHttp.clearHeaders();
        proxyHttp.setHeader("ContentType", "text/html; charset=utf-8");
        proxyHttp.setHeader(
                "Accept",
                "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");
        proxyHttp.setHeader("X-Forwarded-Proto", "https");
        proxyHttp.setHeader("X-Forwarded-Host", "apichallenges.eviltester.com");

        final HttpResponseDetails response = proxyHttp.send("/practice-modes/simulation", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains("GET https://apichallenges.eviltester.com/sim/entities"));
        Assertions.assertFalse(
                response.body.contains("GET http://apichallenges.eviltester.com/sim/entities"));
    }

    @Test
    void learningZonePageIncludesHeroAndSocialCard() {

        final HttpResponseDetails response = http.send("/learning", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>Learning API Testing</h1>"));
        Assertions.assertTrue(
                response.body.contains("/images/hero/learning-zone-api-testing-path-1600x720.jpg"));
        Assertions.assertTrue(
                response.body.contains("content-hero-figure learning-zone-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/learning-zone-api-testing-path-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/learning-zone-api-testing-path-1600x720.jpg'>"));
        final int learningTitle = response.body.indexOf("<h1>Learning API Testing</h1>");
        final int learningHero =
                response.body.indexOf("content-hero-figure learning-zone-hero-image");
        final int learningToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(learningTitle < learningHero);
        Assertions.assertTrue(learningHero < learningToc);
    }

    @Test
    void apiChallengesPageIncludesHeroAndSocialCard() {

        final HttpResponseDetails response = http.send("/apichallenges", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>API Challenges</h1>"));
        Assertions.assertTrue(
                response.body.contains(
                        "/images/hero/api-challenges-api-session-progress-1600x720.jpg"));
        Assertions.assertTrue(
                response.body.contains("content-hero-figure api-challenges-api-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/api-challenges-api-session-progress-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/api-challenges-api-session-progress-1600x720.jpg'>"));
        final int apiChallengesTitle = response.body.indexOf("<h1>API Challenges</h1>");
        final int apiChallengesHero =
                response.body.indexOf("content-hero-figure api-challenges-api-hero-image");
        final int apiChallengesToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(apiChallengesTitle < apiChallengesHero);
        Assertions.assertTrue(apiChallengesHero < apiChallengesToc);
    }

    @Test
    void simpleApiPracticeModePageIncludesHeroAndSocialCard() {

        final HttpResponseDetails response = http.send("/practice-modes/simpleapi", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>Simple API</h1>"));
        Assertions.assertTrue(
                response.body.contains("/images/hero/simple-api-no-auth-practice-1600x720.jpg"));
        Assertions.assertTrue(response.body.contains("content-hero-figure simple-api-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/simple-api-no-auth-practice-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/simple-api-no-auth-practice-1600x720.jpg'>"));
        final int simpleApiTitle = response.body.indexOf("<h1>Simple API</h1>");
        final int simpleApiHero =
                response.body.indexOf("content-hero-figure simple-api-hero-image");
        final int simpleApiToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(simpleApiTitle < simpleApiHero);
        Assertions.assertTrue(simpleApiHero < simpleApiToc);
    }

    @Test
    void simulationModePageIncludesHeroAndSocialCard() {

        final HttpResponseDetails response = http.send("/practice-modes/simulation", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>Simulation Mode</h1>"));
        Assertions.assertTrue(
                response.body.contains("/images/hero/api-simulator-browser-requests-1600x720.jpg"));
        Assertions.assertTrue(response.body.contains("content-hero-figure simulator-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/api-simulator-browser-requests-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/api-simulator-browser-requests-1600x720.jpg'>"));
        final int simulationTitle = response.body.indexOf("<h1>Simulation Mode</h1>");
        final int simulationHero =
                response.body.indexOf("content-hero-figure simulator-hero-image");
        final int simulationToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(simulationTitle < simulationHero);
        Assertions.assertTrue(simulationHero < simulationToc);
    }

    @Test
    void simulationModePageIncludesLiveRequestWidgetsForEachStep() {

        final HttpResponseDetails response = http.send("/practice-modes/simulation", "get");

        Assertions.assertEquals(200, response.statusCode);
        assertBodyContainsVersionedScript(response, "/js/api-live-request.js");
        Assertions.assertEquals(
                14, response.body.split("class=\"sim-live-request\"", -1).length - 1);
        Assertions.assertTrue(
                response.body.contains(
                        "class=\"sim-live-request\" data-method=\"GET\""
                                + " data-path=\"/sim/entities\""));
        Assertions.assertTrue(
                response.body.contains(
                        "class=\"sim-live-request\" data-method=\"POST\""
                                + " data-path=\"/sim/entities\""));
        Assertions.assertTrue(
                response.body.contains("data-body=\"{&quot;name&quot;: &quot;bob&quot;}\""));
        Assertions.assertTrue(
                response.body.contains(
                        "class=\"sim-live-request\" data-method=\"HEAD\""
                                + " data-path=\"/sim/entities\""));
        Assertions.assertTrue(
                response.body.contains(
                        "class=\"sim-live-request\" data-method=\"PATCH\""
                                + " data-path=\"/sim/entities\""));
        Assertions.assertTrue(response.body.contains("data-editable=\"true\""));
    }

    @Test
    void mirrorModePageIncludesHeroAndSocialCard() {

        final HttpResponseDetails response = http.send("/practice-modes/mirror", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>Mirror Mode</h1>"));
        Assertions.assertTrue(
                response.body.contains("/images/hero/http-mirror-client-evidence-1600x720.jpg"));
        Assertions.assertTrue(response.body.contains("content-hero-figure mirror-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/http-mirror-client-evidence-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/http-mirror-client-evidence-1600x720.jpg'>"));
        final int mirrorTitle = response.body.indexOf("<h1>Mirror Mode</h1>");
        final int mirrorHero = response.body.indexOf("content-hero-figure mirror-hero-image");
        final int mirrorToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(mirrorTitle < mirrorHero);
        Assertions.assertTrue(mirrorHero < mirrorToc);
    }

    @Test
    void buggyApiPracticeModePageIncludesHeroAndSocialCard() {

        final HttpResponseDetails response = http.send("/practice-modes/shoppingcart", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>Buggy API</h1>"));
        Assertions.assertTrue(
                response.body.contains("/images/hero/buggy-api-shopping-cart-1600x720.jpg"));
        Assertions.assertTrue(response.body.contains("content-hero-figure buggy-api-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/buggy-api-shopping-cart-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/buggy-api-shopping-cart-1600x720.jpg'>"));
        final int buggyApiTitle = response.body.indexOf("<h1>Buggy API</h1>");
        final int buggyApiHero = response.body.indexOf("content-hero-figure buggy-api-hero-image");
        final int buggyApiToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(buggyApiTitle < buggyApiHero);
        Assertions.assertTrue(buggyApiHero < buggyApiToc);
    }

    @Test
    void fromHellPracticeModePageIncludesCollapsedLiveRequestWidgets() {

        final HttpResponseDetails response = http.send("/practice-modes/fromhell", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("<h1>API From Hell</h1>"));
        Assertions.assertTrue(
                response.body.contains("/images/hero/api-from-hell-burning-1200x630.jpg"));
        Assertions.assertTrue(response.body.contains("content-hero-figure fromhell-hero-image"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/api-from-hell-burning-1200x630.jpg'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/api-from-hell-burning-1200x630.jpg'>"));
        final int fromHellTitle = response.body.indexOf("<h1>API From Hell</h1>");
        final int fromHellHero = response.body.indexOf("content-hero-figure fromhell-hero-image");
        final int fromHellToc = response.body.indexOf("<div id='toc'>");
        Assertions.assertTrue(fromHellTitle < fromHellHero);
        Assertions.assertTrue(fromHellHero < fromHellToc);
        Assertions.assertTrue(
                response.body.contains(
                        "<a href=\"/fromhell/docs/openapi-3.1.json?download\">download</a>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<a href=\"/fromhell/docs/openapi-3.1.json\">OpenAPI 3.1 JSON</a>"));
        assertBodyContainsVersionedScript(response, "/js/api-live-request.js");
        Assertions.assertTrue(
                response.body.contains(
                        "respond with <code>405 Method Not Allowed</code> and an"
                                + " <code>Allow</code> header"));
        Assertions.assertEquals(
                77, response.body.split("class=\"sim-live-request\"", -1).length - 1);
        Assertions.assertEquals(
                77, response.body.split("class=\"sim-live-request-details\"", -1).length - 1);
        Assertions.assertTrue(
                response.body.contains(
                        "<summary>Try it now</summary><div class=\"sim-live-request\""
                                + " data-method=\"GET\" data-path=\"/fromhell/status\""));
        Assertions.assertTrue(response.body.contains("<h2>Additional Content Formats</h2>"));
        Assertions.assertTrue(response.body.contains("<h3>Good CSV</h3>"));
        Assertions.assertTrue(response.body.contains("data-path=\"/fromhell/good/csv\""));
        Assertions.assertTrue(response.body.contains("data-path=\"/fromhell/good/octet-stream\""));
        Assertions.assertTrue(response.body.contains("<h2>Missing Content-Type</h2>"));
        Assertions.assertTrue(
                response.body.contains("data-path=\"/fromhell/missing-content-type/xml\""));
        Assertions.assertFalse(response.body.contains("TODO: expand to cover more formats"));
        Assertions.assertTrue(response.body.contains("<h3>Trailing Comma In Array</h3>"));
        Assertions.assertTrue(
                response.body.contains(
                        "data-path=\"/fromhell/malformed/json/trailing-comma-array\""));
        Assertions.assertTrue(response.body.contains("<h2>Problematic JSON</h2>"));
        Assertions.assertTrue(response.body.contains("<h3>Duplicate Keys</h3>"));
        Assertions.assertTrue(
                response.body.contains("data-path=\"/fromhell/problematic/json/duplicate-keys\""));
        Assertions.assertTrue(
                response.body.contains("data-path=\"/fromhell/problematic/json/ndjson\""));
        Assertions.assertTrue(response.body.contains("<h3>Undefined Entity</h3>"));
        Assertions.assertTrue(
                response.body.contains("data-path=\"/fromhell/malformed/xml/undefined-entity\""));
        Assertions.assertTrue(response.body.contains("<h2>Problematic XML</h2>"));
        Assertions.assertTrue(response.body.contains("<h3>Attributes Vs Elements</h3>"));
        Assertions.assertTrue(
                response.body.contains(
                        "data-path=\"/fromhell/problematic/xml/attributes-vs-elements\""));
        Assertions.assertTrue(
                response.body.contains("data-path=\"/fromhell/problematic/xml/bom-prefix\""));
        Assertions.assertTrue(
                response.body.contains("data-path=\"/fromhell/mismatch/content-type/xml-json\""));
        Assertions.assertTrue(response.body.contains("<h2>Status Code Semantic Mismatches</h2>"));
        Assertions.assertTrue(
                response.body.contains(
                        "data-method=\"POST\""
                                + " data-path=\"/fromhell/status-code/201-no-location\""));
        Assertions.assertTrue(
                response.body.contains(
                        "data-method=\"DELETE\""
                                + " data-path=\"/fromhell/status-code/204-with-body\""));
        Assertions.assertTrue(
                response.body.contains(
                        "Different deployment mechanisms can produce different observable"
                                + " results"));
        Assertions.assertTrue(
                response.body.contains(
                        "use a proxy between the API and your API client so you can"));
        Assertions.assertTrue(
                response.body.contains(
                        "<code>304</code> is a cache validation response that should not include"
                                + " content"));
        Assertions.assertTrue(response.body.contains("Content-Range: bytes 0-99/200"));
        Assertions.assertTrue(response.body.contains("Content-Range: bytes */200"));
        Assertions.assertTrue(response.body.contains("WWW-Authenticate: Basic realm"));
        Assertions.assertTrue(response.body.contains("Allow: GET, HEAD, OPTIONS"));
        Assertions.assertFalse(
                response.body.contains("<details class=\"sim-live-request-details\" open"));
    }

    @Test
    void staticAssetsAreServedBeforeGenericFallbackRoutes() {

        HttpResponseDetails response = http.send("/css/default.css", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("text/css"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(response.body.contains(".rootmenu"));

        response = http.send("/css/content.css", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("text/css"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(response.body.contains("div.main-text-content pre"));
        Assertions.assertTrue(response.body.contains("white-space: pre-wrap"));
        Assertions.assertTrue(response.body.contains(".sim-live-pretty-print"));
        Assertions.assertTrue(response.body.contains(".sim-live-edit-actions"));
        Assertions.assertTrue(response.body.contains(".sim-live-execute-row"));
        Assertions.assertTrue(response.body.contains(".sim-live-challenge-feedback"));
        Assertions.assertTrue(response.body.contains(".solution-challenge-completed"));
        Assertions.assertTrue(response.body.contains(".sim-live-fireworks"));
        Assertions.assertTrue(response.body.contains("@keyframes sim-live-firework-spark"));
        Assertions.assertTrue(response.body.contains("@keyframes sim-live-firework-ring"));
        Assertions.assertTrue(response.body.contains("@keyframes sim-live-firework-confetti"));
        Assertions.assertTrue(response.body.contains(".sim-live-command-actions"));
        Assertions.assertTrue(response.body.contains(".sim-live-curl-exe-toggle"));
        Assertions.assertTrue(response.body.contains(".sim-live-request-details"));
        Assertions.assertTrue(response.body.contains(".sim-live-request-details .sim-live-title"));
        Assertions.assertTrue(response.body.contains(".sim-live-validation"));
        Assertions.assertTrue(response.body.contains(".sim-live-edit-query"));
        Assertions.assertTrue(response.body.contains("background: #16803a"));

        response = http.send("/css/theme-experiments.css", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("text/css"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(
                response.body.contains("html[data-theme=\"dark-lab\"] .sim-live-status"));
        Assertions.assertTrue(
                response.body.contains("html[data-theme=\"dark-lab\"] .sim-live-execute"));
        Assertions.assertTrue(response.body.contains("background: #22c55e"));
        Assertions.assertTrue(response.body.contains("border-left-color: var(--accent)"));
        Assertions.assertTrue(response.body.contains("color: var(--text)"));

        response = http.send("/js/toc.js", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("javascript"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(response.body.contains("htmlTableOfContents"));

        response = http.send("/js/api-live-request.js", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("javascript"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(response.body.contains("buildCurlCommand"));
        Assertions.assertTrue(response.body.contains("api-live-request"));
        Assertions.assertTrue(response.body.contains("currentChallenger"));
        Assertions.assertTrue(response.body.contains("X-Auth-Token"));
        Assertions.assertTrue(response.body.contains("firstTodoId"));
        Assertions.assertTrue(response.body.contains("updateRenderedWidgetsFromSession"));
        Assertions.assertTrue(response.body.contains("formatRequestBody"));
        Assertions.assertTrue(response.body.contains("JSON.stringify(JSON.parse(request.body)"));
        Assertions.assertTrue(response.body.contains("formatXml"));
        Assertions.assertTrue(response.body.contains("DOMParser"));
        Assertions.assertTrue(response.body.contains("Pretty print body"));
        Assertions.assertTrue(response.body.contains("checkChallengePassed"));
        Assertions.assertTrue(response.body.contains("Challenge Not Passed Yet"));
        Assertions.assertTrue(response.body.contains("updateChallengeCompletedBanners"));
        Assertions.assertTrue(response.body.contains("BROWSER_UNSUPPORTED_METHODS"));
        Assertions.assertTrue(
                response.body.contains("BROWSER_UNSUPPORTED_METHOD_OVERRIDE_HEADERS"));
        Assertions.assertTrue(response.body.contains("x-http-method-override"));
        Assertions.assertTrue(response.body.contains("unsupportedMethodOverrideHeader"));
        Assertions.assertTrue(response.body.contains("X-API-Challenges-Live-Widget"));
        Assertions.assertTrue(response.body.contains("browserRequestHeaders"));
        Assertions.assertTrue(response.body.contains("Use the cURL or wget tabs"));
        Assertions.assertTrue(response.body.contains("oversizedChallengerValue"));
        Assertions.assertTrue(response.body.contains("101 - prefix.length"));
        Assertions.assertTrue(response.body.contains("autoCreateFirstTodo"));
        Assertions.assertTrue(response.body.contains("lastCreatedTodoId"));
        Assertions.assertTrue(response.body.contains("USE_CURL_EXE"));
        Assertions.assertTrue(response.body.contains("navigator.userAgentData.platform"));
        Assertions.assertTrue(response.body.contains("isWindowsPlatform"));
        Assertions.assertTrue(response.body.contains("curl.exe"));
        Assertions.assertTrue(response.body.contains("sim-live-curl-exe-checkbox"));
        Assertions.assertTrue(response.body.contains("refreshCurlCommands"));
        Assertions.assertTrue(response.body.contains("dataset.editMode"));
        Assertions.assertTrue(response.body.contains("allowedPathPrefixes"));
        Assertions.assertTrue(response.body.contains("ApiChallengesLiveRequest"));

        response = http.send("/js/api-docs-live-request.js", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("javascript"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(response.body.contains("'/docs'"));
        Assertions.assertTrue(response.body.contains("'/simpleapi/docs'"));
        Assertions.assertTrue(response.body.contains("'/shop/docs'"));
        Assertions.assertFalse(response.body.contains("'/sim/docs'"));
        Assertions.assertFalse(response.body.contains("'/mirror/docs'"));
        Assertions.assertFalse(response.body.contains("'/fromhell/docs'"));

        response = http.send("/favicon/site.webmanifest", "get");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("manifest+json"));
        assertCacheControl(response, "public, max-age=31536000, immutable");
        Assertions.assertTrue(response.body.contains("icons"));
    }

    @Test
    void docsAndDynamicRoutesDeclareCdnCachePolicy() {

        HttpResponseDetails response = http.send("/learning", "get");
        Assertions.assertEquals(200, response.statusCode);
        assertCacheControl(
                response,
                "public, max-age=300, s-maxage=86400, stale-while-revalidate=604800, stale-if-error=604800");

        response = http.send("/learning", "head");
        Assertions.assertEquals(200, response.statusCode);
        assertCacheControl(
                response,
                "public, max-age=300, s-maxage=86400, stale-while-revalidate=604800, stale-if-error=604800");

        response = http.send("/docs/swagger-ui", "get");
        Assertions.assertEquals(200, response.statusCode);
        assertCacheControl(
                response,
                "public, max-age=300, s-maxage=86400, stale-while-revalidate=604800, stale-if-error=604800");

        response = http.send("/docs/openapi.json", "get");
        Assertions.assertEquals(200, response.statusCode);
        assertCacheControl(
                response,
                "public, max-age=300, s-maxage=86400, stale-while-revalidate=604800, stale-if-error=604800");

        for (final String dynamicPath :
                List.of(
                        "/gui/challenges",
                        "/todos",
                        "/challenges",
                        "/simpleapi/items",
                        "/mirror/request")) {
            response = http.send(dynamicPath, "get");
            Assertions.assertEquals(200, response.statusCode, dynamicPath);
            assertCacheControl(response, "no-store");
        }
    }

    static Stream<Arguments> swaggerUiPageRoutes() {
        List<Arguments> args = new ArrayList<>();
        args.add(
                Arguments.of(
                        "/docs/swagger-ui", "/docs/openapi.json", "API Challenges - Swagger UI"));
        args.add(
                Arguments.of(
                        "/simpleapi/docs/swagger-ui",
                        "/simpleapi/docs/openapi.json",
                        "Simple API - Swagger UI"));
        args.add(
                Arguments.of(
                        "/sim/docs/swagger-ui",
                        "/sim/docs/openapi.json",
                        "API Simulator - Swagger UI"));
        args.add(
                Arguments.of(
                        "/shop/docs/swagger-ui",
                        "/shop/docs/openapi.json",
                        "Buggy API - Swagger UI"));
        args.add(
                Arguments.of(
                        "/mirror/docs/swagger-ui",
                        "/mirror/docs/openapi.json",
                        "Mirror Mode API Documentation | API Challenges Swagger UI"));
        args.add(
                Arguments.of(
                        "/fromhell/docs/swagger-ui",
                        "/fromhell/docs/openapi.json",
                        "API From Hell - Swagger UI"));
        return args.stream();
    }

    @ParameterizedTest(name = "swagger ui page {0} references {1}")
    @MethodSource("swaggerUiPageRoutes")
    void swaggerUiPagesRenderAndReferenceMatchingOpenApiJson(
            final String swaggerUiPath, final String openApiJsonPath, final String pageTitle) {

        final HttpResponseDetails response = http.send(swaggerUiPath, "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertNotNull(response.getHeader("Content-Type"));
        Assertions.assertTrue(response.getHeader("Content-Type").contains("text/html"));
        Assertions.assertEquals("noindex, follow", response.getHeader("X-Robots-Tag"));
        assertContainsHeaderAndFooter(response);
        Assertions.assertTrue(
                response.body.contains("https://unpkg.com/swagger-ui-dist/swagger-ui.css"));
        Assertions.assertTrue(
                response.body.contains("https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js"));
        Assertions.assertTrue(
                response.body.contains(
                        "https://unpkg.com/swagger-ui-dist/swagger-ui-standalone-preset.js"));
        Assertions.assertTrue(response.body.contains("color-scheme:light"));
        Assertions.assertTrue(response.body.contains("syntaxHighlight: {activated: false}"));
        Assertions.assertTrue(response.body.contains("SwaggerUIBundle"));
        Assertions.assertTrue(response.body.contains("url: \"" + openApiJsonPath + "\""));
        Assertions.assertTrue(response.body.contains("openapi-3.1.json"));
        Assertions.assertTrue(response.body.contains("openapi-3.2.json"));
        Assertions.assertTrue(response.body.contains("openapi-3.0.json"));
        Assertions.assertTrue(
                response.body.contains("\"urls.primaryName\": \"OpenAPI 3.1 default\""));
        Assertions.assertFalse(response.body.contains("Download OpenAPI JSON"));
        Assertions.assertFalse(response.body.contains(".swagger-ui .topbar{display:none;}"));
        Assertions.assertTrue(response.body.contains("<h1>" + pageTitle + "</h1>"));
        Assertions.assertTrue(response.body.contains("<title>" + pageTitle + "</title>"));
    }

    @Test
    void challengerMenuContainsSwaggerUiLinksOnlyForSuitableModes() {

        final HttpResponseDetails response = http.send("/", "get");

        Assertions.assertEquals(200, response.statusCode);
        assertBodyContainsVersionedStylesheet(response, "/css/default.css");
        assertBodyContainsVersionedStylesheet(response, "/css/content.css");
        assertBodyContainsVersionedStylesheet(response, "/css/theme-experiments.css");
        assertBodyContainsVersionedScript(response, "/js/theme-switcher.js");
        Assertions.assertTrue(response.body.contains("href=\"/docs/swagger-ui\""));
        Assertions.assertTrue(response.body.contains("href=\"/apichallenges/client\""));
        Assertions.assertTrue(response.body.contains("href=\"/simpleapi/docs/swagger-ui\""));
        Assertions.assertTrue(response.body.contains("href=\"/simpleapi/client\""));
        Assertions.assertTrue(response.body.contains("href=\"/shop/docs/swagger-ui\""));
        Assertions.assertTrue(response.body.contains("href=\"/shop/client\""));
        Assertions.assertTrue(response.body.contains("href=\"/shop/gui/entities\""));
        Assertions.assertTrue(response.body.contains("href=\"/sim/docs/swagger-ui\""));
        Assertions.assertTrue(response.body.contains("href=\"/sim/docs/openapi.json?download\""));
        Assertions.assertTrue(
                response.body.contains("href=\"/mirror/docs/openapi.json?download\""));
        Assertions.assertFalse(response.body.contains("href=\"/sim/docs/swagger\""));
        Assertions.assertFalse(response.body.contains("href=\"/mirror/docs/swagger\""));
        Assertions.assertFalse(response.body.contains("href=\"/fromhell/docs/swagger-ui\""));
        Assertions.assertFalse(response.body.contains("href=\"/mirror/docs/swagger-ui\""));
        Assertions.assertFalse(response.body.contains("href=\"/sim/client\""));
        Assertions.assertFalse(response.body.contains("href=\"/mirror/client\""));
        Assertions.assertFalse(response.body.contains("href=\"/fromhell/client\""));

        final HttpResponseDetails practiceModeResponse =
                http.send("/practice-modes/simulation", "get");
        Assertions.assertEquals(200, practiceModeResponse.statusCode);
        Assertions.assertTrue(
                practiceModeResponse.body.contains("href=\"/practice-modes/fromhell\""));
        Assertions.assertFalse(
                practiceModeResponse.body.contains("href=\"/fromhell/docs/swagger-ui\""));
    }

    @Test
    void mirrorPracticeModePageDoesNotAdvertiseSwaggerUi() {

        final HttpResponseDetails response = http.send("/practice-modes/mirror", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertFalse(response.body.contains("href=\"/mirror/docs/swagger-ui\""));
        Assertions.assertTrue(response.body.contains("OpenAPI 3.2 JSON"));
        Assertions.assertTrue(response.body.contains("/mirror/docs/openapi-3.2.json?download"));
    }

    @Test
    void docsPagesRenderPerApiSeoMetadata() {

        final HttpResponseDetails docsResponse = http.send("/docs", "get");
        Assertions.assertEquals(200, docsResponse.statusCode);
        assertBodyContainsVersionedScript(docsResponse, "/js/api-live-request.js");
        assertBodyContainsVersionedScript(docsResponse, "/js/api-docs-live-request.js");
        Assertions.assertTrue(docsResponse.body.contains("Open Swagger UI"));
        Assertions.assertTrue(docsResponse.body.contains("href='/docs/swagger-ui'"));
        Assertions.assertTrue(
                docsResponse.body.contains(
                        "<title>API Challenges API Documentation | API Challenges</title>"));
        Assertions.assertTrue(
                docsResponse.body.contains(
                        "<meta name='description' content='Explore API Challenges endpoint documentation with request formats, payload examples, and expected responses for practical API testing.'>"));
        Assertions.assertTrue(
                docsResponse.body.contains("<meta name='robots' content='index,follow'>"));
        Assertions.assertTrue(
                docsResponse.body.contains(
                        "<meta property='og:url' content='https://apichallenges.eviltester.com/docs'>"));
        Assertions.assertTrue(
                docsResponse.body.contains(
                        "<meta name='twitter:title' content='API Challenges API Documentation | API Challenges'>"));
        Assertions.assertTrue(
                docsResponse.body.contains(
                        "<link rel='canonical' href='https://apichallenges.eviltester.com/docs'>"));

        final HttpResponseDetails simpleApiDocsResponse = http.send("/simpleapi/docs", "get");
        Assertions.assertEquals(200, simpleApiDocsResponse.statusCode);
        assertBodyContainsVersionedScript(simpleApiDocsResponse, "/js/api-live-request.js");
        assertBodyContainsVersionedScript(simpleApiDocsResponse, "/js/api-docs-live-request.js");
        Assertions.assertTrue(
                simpleApiDocsResponse.body.contains(
                        "<title>Simple API Documentation | API Challenges</title>"));
        Assertions.assertTrue(
                simpleApiDocsResponse.body.contains("<meta name='robots' content='index,follow'>"));
        Assertions.assertTrue(
                simpleApiDocsResponse.body.contains(
                        "<meta property='og:url' content='https://apichallenges.eviltester.com/simpleapi/docs'>"));

        final HttpResponseDetails shopDocsResponse = http.send("/shop/docs", "get");
        Assertions.assertEquals(200, shopDocsResponse.statusCode);
        assertBodyContainsVersionedScript(shopDocsResponse, "/js/api-live-request.js");
        assertBodyContainsVersionedScript(shopDocsResponse, "/js/api-docs-live-request.js");
        Assertions.assertTrue(
                shopDocsResponse.body.contains(
                        "<title>Buggy API Documentation | API Challenges</title>"));
        Assertions.assertTrue(
                shopDocsResponse.body.contains("<meta name='robots' content='index,follow'>"));
        Assertions.assertTrue(
                shopDocsResponse.body.contains(
                        "<meta property='og:url' content='https://apichallenges.eviltester.com/shop/docs'>"));

        final HttpResponseDetails simDocsResponse = http.send("/sim/docs", "get");
        Assertions.assertEquals(200, simDocsResponse.statusCode);
        Assertions.assertTrue(
                simDocsResponse.body.contains(
                        "<title>Simulation Mode API Documentation | API Challenges</title>"));
        Assertions.assertTrue(
                simDocsResponse.body.contains("<meta name='robots' content='noindex,follow'>"));

        final HttpResponseDetails mirrorDocsResponse = http.send("/mirror/docs", "get");
        Assertions.assertEquals(200, mirrorDocsResponse.statusCode);
        Assertions.assertTrue(
                mirrorDocsResponse.body.contains(
                        "<title>Mirror Mode API Documentation | API Challenges</title>"));
        Assertions.assertTrue(
                mirrorDocsResponse.body.contains("<meta name='robots' content='noindex,follow'>"));
        Assertions.assertFalse(mirrorDocsResponse.body.contains("href='/mirror/docs/swagger-ui'"));
        Assertions.assertFalse(mirrorDocsResponse.body.contains("Open Swagger UI"));
        Assertions.assertTrue(mirrorDocsResponse.body.contains("<li>OpenAPI v 3.2 JSON"));
        Assertions.assertTrue(
                mirrorDocsResponse.body.contains("href='/mirror/docs/openapi-3.2.json?download'"));
    }

    @Test
    void markdownPageWithMetadataOverridesRendersExpectedSeoAndSocialTags() {

        final HttpResponseDetails response = http.send("/seo-metadata-test-page", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "<title>Open Graph Metadata Test Page for Validation | API Challenges</title>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='description' content='Search snippet with Alan&#39;s &quot;special&quot; chars &amp; context.'>"));
        Assertions.assertTrue(
                response.body.contains("<meta name='robots' content='noindex,nofollow'>"));
        Assertions.assertTrue(
                response.body.contains("<meta property='og:type' content='article'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:url' content='https://apichallenges.eviltester.com/seo-metadata-test-page'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/social/apichallenges-og-1200x630.png'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image:alt' content='OG preview image for API Challenges metadata tests'>"));
        Assertions.assertTrue(
                response.body.contains("<meta name='twitter:card' content='summary'>"));
        Assertions.assertTrue(
                response.body.contains("<meta name='twitter:site' content='@apichallenges'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/social/apichallenges-og-1200x630.png'>"));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Article\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"description\":\"Search snippet with Alan's \\\"special\\\" chars & context.\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"url\":\"https://apichallenges.eviltester.com/seo-metadata-test-page\""));
        Assertions.assertTrue(response.body.contains("\"dateModified\":\"2026-02-18\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"HowTo\""));
        Assertions.assertTrue(response.body.contains("\"name\":\"Open the metadata test page\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"VideoObject\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"contentUrl\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\""));
        Assertions.assertFalse(response.body.contains("\"@type\":\"BreadcrumbList\""));
    }

    @Test
    void openApiAndSwaggerReferencePagesAreSplitByConcept() {

        final HttpResponseDetails openApiResponse = http.send("/tutorials/openapi", "get");

        Assertions.assertEquals(200, openApiResponse.statusCode);
        Assertions.assertTrue(openApiResponse.body.contains("<h1>Introduction to OpenAPI</h1>"));
        Assertions.assertTrue(
                openApiResponse.body.contains("OpenAPI is a standard specification format"));
        Assertions.assertTrue(openApiResponse.body.contains("Swagger is one family of tools"));
        Assertions.assertTrue(openApiResponse.body.contains("href=\"/tutorials/swagger\""));
        Assertions.assertTrue(openApiResponse.body.contains("href=\"/tutorials/openapi\""));
        Assertions.assertFalse(openApiResponse.body.contains("OpenAPI / Swagger"));

        final HttpResponseDetails swaggerResponse = http.send("/tutorials/swagger", "get");

        Assertions.assertEquals(200, swaggerResponse.statusCode);
        Assertions.assertTrue(swaggerResponse.body.contains("<h1>Introduction to Swagger</h1>"));
        Assertions.assertTrue(
                swaggerResponse.body.contains(
                        "OpenAPI is the standard specification. Swagger is tooling"));
        Assertions.assertTrue(swaggerResponse.body.contains("Swagger UI"));
        Assertions.assertTrue(swaggerResponse.body.contains("href=\"/tutorials/openapi\""));
        Assertions.assertTrue(swaggerResponse.body.contains("href=\"/tutorials/swagger\""));
        Assertions.assertFalse(swaggerResponse.body.contains("OpenAPI / Swagger"));
    }

    @Test
    void markdownPageWithNoOptionalMetadataUsesFallbackDefaults() {

        final HttpResponseDetails response = http.send("/", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='description' content='A practice API application with tutorials for HTTP and REST APIs. Guided exercises and gamification hands on learning path.'>"));
        Assertions.assertTrue(
                response.body.contains("<meta name='robots' content='index,follow'>"));
        Assertions.assertTrue(
                response.body.contains("<meta property='og:type' content='website'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:url' content='https://apichallenges.eviltester.com'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta property='og:image' content='https://apichallenges.eviltester.com/images/hero/apichallenges-whole-site-gauntlet-1600x720.jpg'>"));
        Assertions.assertTrue(
                response.body.contains("<meta name='twitter:card' content='summary_large_image'>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<meta name='twitter:image' content='https://apichallenges.eviltester.com/images/hero/apichallenges-whole-site-gauntlet-1600x720.jpg'>"));
        Assertions.assertTrue(response.body.contains("\"@type\":\"WebPage\""));
    }

    @Test
    void markdownContentPageDefaultsToArticleSchema() {

        final HttpResponseDetails response = http.send("/learning", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"@type\":\"Article\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"url\":\"https://apichallenges.eviltester.com/learning\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"mainEntityOfPage\":\"https://apichallenges.eviltester.com/learning\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Person\""));
        Assertions.assertTrue(response.body.contains("\"name\":\"Alan Richardson\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"jobTitle\":\"Software Testing and Development Consultant\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"Organization\""));
        Assertions.assertTrue(response.body.contains("\"name\":\"eviltester.com\""));
        Assertions.assertTrue(
                response.body.contains("\"legalName\":\"Compendium Developments Ltd\""));
        Assertions.assertTrue(response.body.contains("\"dateModified\":\"2026-02-18\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertFalse(response.body.contains("<aside class='next-challenge-cta'"));
        Assertions.assertTrue(response.body.contains("<aside class='author-bio-snippet'"));
        Assertions.assertTrue(response.body.contains("href='/author/alan-richardson'"));
    }

    @Test
    void authorBioPageIsReachable() {

        final HttpResponseDetails response = http.send("/author/alan-richardson", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "<title>Alan Richardson Author Profile and API Testing Credentials</title>"));
        Assertions.assertTrue(response.body.contains("<h1>About Alan Richardson</h1>"));
        Assertions.assertFalse(response.body.contains("<aside class='author-bio-snippet'"));
    }

    @Test
    void solutionPageEmitsHowToVideoAndBreadcrumbSchemasWithExplicitHowToSteps() {

        final HttpResponseDetails response =
                http.send("/apichallenges/solutions/get/get-todos-200", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"@type\":\"HowTo\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"HowToStep\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"VideoObject\""));
        Assertions.assertTrue(
                response.body.contains(
                        "\"contentUrl\":\"https://www.youtube.com/watch?v=OpisB0UZq0c\""));
        Assertions.assertTrue(response.body.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertTrue(response.body.contains("<aside class='next-challenge-cta'"));
        Assertions.assertTrue(response.body.contains("class='next-challenge-cta-link'"));
        Assertions.assertTrue(response.body.contains("Try the next challenge walkthrough"));
    }

    @Test
    void generatedTocDoesNotPreventFollowingParagraphMarkdownRendering() {

        final HttpResponseDetails response =
                http.send("/apichallenges/solutions/patch/patch-todos-id-200-merge-patch", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "<p>Use <code>PATCH</code> with <code>Content-Type:"
                                + " application/merge-patch+json</code>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<a href=\"https://www.rfc-editor.org/rfc/rfc7396\">JSON Merge"
                                + " Patch</a> document.</p>"));
        Assertions.assertFalse(response.body.contains("[JSON Merge Patch](https://"));
    }

    @Test
    void statusCodeSolutionsLinkToHeaderTooLargeSolutionBeforeMethodOverride() {

        HttpResponseDetails response =
                http.send("/apichallenges/solutions/status-codes/get-heartbeat-204", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "href='/apichallenges/solutions/status-codes/x-challenger-too-long-431'"));

        response =
                http.send("/apichallenges/solutions/status-codes/x-challenger-too-long-431", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "href='/apichallenges/solutions/method-override/post-heartbeat-as-delete-405'"));
    }

    @Test
    void authorizationSolutionsIncludePostSecretNoteFailuresInNextWalkthroughOrder() {

        HttpResponseDetails response =
                http.send("/apichallenges/solutions/authorization/get-secret-note-200", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "href='/apichallenges/solutions/authorization/post-secret-note-200'"));

        response = http.send("/apichallenges/solutions/authorization/post-secret-note-200", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "href='/apichallenges/solutions/authorization/post-secret-note-401'"));

        response = http.send("/apichallenges/solutions/authorization/post-secret-note-401", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "href='/apichallenges/solutions/authorization/post-secret-note-403'"));

        response = http.send("/apichallenges/solutions/authorization/post-secret-note-403", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains(
                        "href='/apichallenges/solutions/authorization/get-secret-note-bearer'"));
    }

    @Test
    void activeSolutionPagesIncludeApiLiveRequestWidgets() {

        final HttpResponseDetails indexResponse = http.send("/apichallenges/solutions", "get");
        Assertions.assertEquals(200, indexResponse.statusCode);

        final Pattern solutionLinkPattern =
                Pattern.compile("href=\"(/apichallenges/solutions/[^\"]+)\"");
        final Matcher solutionLinkMatcher = solutionLinkPattern.matcher(indexResponse.body);
        final Set<String> solutionLinks = new LinkedHashSet<>();
        while (solutionLinkMatcher.find()) {
            solutionLinks.add(solutionLinkMatcher.group(1));
        }

        Assertions.assertFalse(solutionLinks.isEmpty());

        for (final String solutionLink : solutionLinks) {
            final HttpResponseDetails response = http.send(solutionLink, "get");

            Assertions.assertEquals(200, response.statusCode, solutionLink);
            Assertions.assertTrue(
                    response.body.matches(
                            "(?s).*src='"
                                    + Pattern.quote("/js/api-live-request.js")
                                    + "\\?v=[^']+'.*"),
                    solutionLink);
            Assertions.assertTrue(
                    response.body.contains("class=\"api-live-request\""), solutionLink);
        }
    }

    @Test
    void solutionLiveRequestMacroRendersEscapedRequestAttributes() {

        final HttpResponseDetails response =
                http.send("/apichallenges/solutions/post-create/post-todos-201", "get");

        Assertions.assertEquals(200, response.statusCode);
        assertBodyContainsVersionedScript(response, "/js/api-live-request.js");
        Assertions.assertTrue(
                response.body.contains(
                        "<details class=\"sim-live-request-details\" open><summary>POST /todos to"
                                + " create a todo</summary>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<aside class=\"solution-challenge-completed\" data-challenge-id=\""));
        Assertions.assertTrue(
                response.body.indexOf("solution-challenge-completed")
                        < response.body.indexOf("<h1"));
        Assertions.assertTrue(
                response.body.matches(
                        "(?s).*class=\"api-live-request\" data-method=\"POST\""
                                + " data-path=\"/todos\" data-editable=\"true\""
                                + " data-edit-mode=\"fixed\" data-allowed-path-prefixes=\""
                                + Pattern.quote(
                                        "/todos||/todo||/challenges||/challenger||/secret||/heartbeat")
                                + "\"[^>]* data-expected-status=\"201\".*"));
        Assertions.assertTrue(
                response.body.contains("<summary>Experiment with this endpoint</summary>"));
        Assertions.assertTrue(response.body.contains("data-edit-mode=\"adhoc\""));
        Assertions.assertTrue(response.body.contains("data-challenge-id=\""));
        Assertions.assertTrue(
                response.body.contains(
                        "data-headers=\"Content-Type: application/json||Accept:"
                                + " application/json\""));
        Assertions.assertTrue(
                response.body.contains(
                        "data-body=\"{&quot;title&quot;:&quot;solution widget todo&quot;"));
    }

    @Test
    void deleteSolutionUsesCollapsedHelpersAndOpenChallengeRequestWithoutAutoCreate() {

        final HttpResponseDetails response =
                http.send("/apichallenges/solutions/delete/delete-todos-id-204", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals(4, countOccurrences(response.body, "class=\"api-live-request\""));
        Assertions.assertTrue(
                response.body.contains(
                        "<summary>GET /todos to see what todos are available now</summary>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<summary>POST /todos to create a todo item for deletion</summary>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<details class=\"sim-live-request-details\" open><summary>DELETE"
                                + " /todos/{id} to delete a specific todo</summary>"));
        Assertions.assertTrue(
                response.body.contains(
                        "class=\"api-live-request\" data-method=\"DELETE\""
                                + " data-path=\"/todos/{{firstTodoId}}\""));
        Assertions.assertTrue(response.body.contains("data-auto-create-first-todo=\"false\""));
        Assertions.assertTrue(response.body.contains("data-refresh-after-execute=\"false\""));
        Assertions.assertTrue(
                response.body.contains("<summary>Experiment with this endpoint</summary>"));
        Assertions.assertEquals(2, countOccurrences(response.body, "data-challenge-id=\""));
    }

    @Test
    void solutionLiveRequestSupportsNoChallengerAndAuthTokenFlows() {

        HttpResponseDetails response =
                http.send("/apichallenges/solutions/create-session/post-challenger-201", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("class=\"api-live-request\""));
        Assertions.assertTrue(response.body.contains("data-use-challenger=\"false\""));

        response = http.send("/apichallenges/solutions/authorization/get-secret-note-200", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("X-AUTH-TOKEN: {{authToken}}"));
    }

    @Test
    void challengeProgressPagesSetCurrentChallengerCookiesForSolutionWidgets() {

        final HttpMessageSender apiHttp = new HttpMessageSender(Environment.getBaseUri());
        apiHttp.clearHeaders();
        apiHttp.setHeader("Accept", "application/json");
        final HttpResponseDetails challengerResponse = apiHttp.post("/challenger", "");
        final String challengerId = challengerResponse.getHeader("X-CHALLENGER");

        final HttpMessageSender guiHttp = new HttpMessageSender(Environment.getBaseUri());
        guiHttp.clearHeaders();
        guiHttp.setHeader(
                "Accept",
                "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");
        final HttpResponseDetails response = guiHttp.send("/gui/challenges/" + challengerId, "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains("Progress For Challenger ID " + challengerId + " - Active"));
        Assertions.assertTrue(
                response.body.contains(
                        "setCookie('X-THINGIFIER-DATABASE-NAME','" + challengerId + "',365);"));
        Assertions.assertTrue(
                response.body.contains("setCookie('X-CHALLENGER','" + challengerId + "',365);"));
        Assertions.assertTrue(response.body.contains("<script>showAchievements()</script>"));
        assertBodyContainsVersionedScript(response, "/js/api-live-request.js");
        Assertions.assertTrue(response.body.contains("<summary>Solve Now</summary>"));
        Assertions.assertTrue(response.body.contains("data-edit-mode=\"adhoc\""));
        Assertions.assertTrue(response.body.contains("data-path=\"/challenger\""));
        Assertions.assertTrue(response.body.contains("data-challenge-id=\""));

        final int achievements = response.body.indexOf("<script>showAchievements()</script>");
        final int gettingStarted =
                response.body.indexOf("<h2 id='gettingstarted'>Getting Started</h2>");
        Assertions.assertTrue(achievements > 0);
        Assertions.assertTrue(gettingStarted > achievements);
    }

    @Test
    void challengeStatusEndpointReportsCompletionWithoutCompletingChallengesListChallenge() {

        final String getTodosChallengeId = challengeIdFor("GET /todos (200)");
        final String getChallengesChallengeId = challengeIdFor("GET /challenges (200)");

        final HttpMessageSender apiHttp = new HttpMessageSender(Environment.getBaseUri());
        apiHttp.clearHeaders();
        apiHttp.setHeader("Accept", "application/json");
        final HttpResponseDetails challengerResponse = apiHttp.post("/challenger", "");
        final String challengerId = challengerResponse.getHeader("X-CHALLENGER");

        final HttpMessageSender statusHttp = new HttpMessageSender(Environment.getBaseUri());
        statusHttp.clearHeaders();
        statusHttp.setHeader("Accept", "application/json");
        statusHttp.setHeader("X-CHALLENGER", challengerId);

        assertChallengeStatus(statusHttp, getTodosChallengeId, false);
        assertChallengeStatus(statusHttp, normalizedChallengeId(getTodosChallengeId), false);

        apiHttp.setHeader("X-CHALLENGER", challengerId);
        final HttpResponseDetails todosResponse = apiHttp.send("/todos", "get");

        Assertions.assertEquals(200, todosResponse.statusCode);
        Assertions.assertEquals(challengerId, todosResponse.getHeader("X-CHALLENGER"));

        assertChallengeStatus(statusHttp, getTodosChallengeId, true);
        assertChallengeStatus(statusHttp, getChallengesChallengeId, false);
    }

    @Test
    void unknownChallengeProgressPageIncludesLocalAutoRestoreHooks() {

        final String challengerId = "11111111-2222-4333-8444-555555555555";
        final HttpMessageSender guiHttp = new HttpMessageSender(Environment.getBaseUri());
        guiHttp.clearHeaders();
        guiHttp.setHeader(
                "Accept",
                "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");

        final HttpResponseDetails response = guiHttp.send("/gui/challenges/" + challengerId, "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("Unknown Challenger ID"));
        Assertions.assertTrue(
                response.body.contains("<script>document.challengerData={};</script>"));
        Assertions.assertTrue(response.body.contains("<script>document.databaseData={};</script>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<button onclick=inputChallengeGuid()>Input Challenger GUID</button>"));
        Assertions.assertTrue(
                response.body.contains("<a href='#gettingstarted'>Create Challenger</a>"));
        Assertions.assertTrue(
                response.body.contains("<h2 id='gettingstarted'>Getting Started</h2>"));
        assertBodyContainsVersionedScript(response, "/js/challengerui.js");
        Assertions.assertTrue(response.body.contains("<script>showCurrentStatus()</script>"));
        Assertions.assertTrue(response.body.contains("<script>showAchievements()</script>"));
        Assertions.assertTrue(response.body.contains("<script>displayLocalGuids()</script>"));

        final int achievements = response.body.indexOf("<script>showAchievements()</script>");
        final int gettingStarted =
                response.body.indexOf("<h2 id='gettingstarted'>Getting Started</h2>");
        Assertions.assertTrue(achievements > 0);
        Assertions.assertTrue(gettingStarted > achievements);
    }

    @Test
    void challengesPageRedirectsFromCurrentChallengerCookie() {

        final String challengerId = "11111111-2222-4333-8444-555555555555";
        final HttpMessageSender cookieHttp = new HttpMessageSender(Environment.getBaseUri());
        cookieHttp.clearHeaders();
        cookieHttp.setHeader(
                "Accept",
                "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, */*;q=0.8");
        cookieHttp.setHeader("Cookie", "X-CHALLENGER=" + challengerId);

        final HttpResponseDetails response = cookieHttp.send("/gui/challenges", "get");

        Assertions.assertEquals(302, response.statusCode);
        Assertions.assertEquals("/gui/challenges/" + challengerId, response.getHeader("Location"));
    }

    @Test
    void articleSchemaIncludesDatePublishedAndDateModifiedWhenDateAndLastmodExist() {

        final HttpResponseDetails response =
                http.send("/apichallenges/solutions/authentication/post-secret-201", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"datePublished\":\"2021-07-24T08:30:00Z\""));
        Assertions.assertTrue(response.body.contains("\"dateModified\":\"2026-02-18\""));
    }

    @Test
    void sitemapUsesFixedLastmodForPhaseOneUrls() {

        final HttpResponseDetails response = http.send("/sitemap.xml", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains("<loc>https://apichallenges.eviltester.com</loc>"));
        Assertions.assertTrue(
                response.body.contains("<loc>https://apichallenges.eviltester.com/docs</loc>"));
        Assertions.assertFalse(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/docs/swagger-ui</loc>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/gui/challenges</loc>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/tutorials/openapi</loc>"));
        Assertions.assertTrue(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/tutorials/swagger</loc>"));
        Assertions.assertFalse(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/tutorials/openapi-swagger</loc>"));
        Assertions.assertTrue(response.body.contains("<lastmod>2026-02-18</lastmod>"));
    }

    @Test
    void sitemapExcludesNoindexAndExplicitlyExcludedContentPages() {

        final HttpResponseDetails response = http.send("/sitemap.xml", "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertFalse(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/seo-metadata-test-page</loc>"));
        Assertions.assertFalse(
                response.body.contains(
                        "<loc>https://apichallenges.eviltester.com/practice-modes/shoppingcart-bugs</loc>"));
    }

    @Test
    void headRequestsToExistingContentPagesReturn200() {

        HttpResponseDetails response = http.send("/", "head");
        Assertions.assertEquals(200, response.statusCode);

        response = http.send("/learning", "head");
        Assertions.assertEquals(200, response.statusCode);

        response = http.send("/tutorials/openapi", "head");
        Assertions.assertEquals(200, response.statusCode);

        response = http.send("/tutorials/swagger", "head");
        Assertions.assertEquals(200, response.statusCode);
    }

    static Stream<Arguments> legacyUrlRedirects() {
        List<Arguments> args = new ArrayList<>();
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/method-overrides/all-method-overrides",
                        "/apichallenges/solutions/method-override/post-heartbeat-as-delete-405"));
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/method-override/all-method-overrides",
                        "/apichallenges/solutions/method-override/post-heartbeat-as-delete-405"));
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/status-codes/status-codes-405-500-501-204",
                        "/apichallenges/solutions/status-codes/delete-heartbeat-405"));
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/manage-session/save-restore-session",
                        "/apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200"));
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/authorization/post-secret-note-401-403",
                        "/apichallenges/solutions/authorization/post-secret-note-401"));
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/authorization/get-post-secret-note-bearer",
                        "/apichallenges/solutions/authorization/get-secret-note-bearer"));
        args.add(Arguments.of("/tools/clients/soapyi", "/tools/clients/soapui"));
        args.add(Arguments.of("/tutorials/openapi-swagger", "/tutorials/openapi"));
        args.add(
                Arguments.of(
                        "/apichallenges/solutions/query/query-todos-200-filter",
                        "/apichallenges/solutions/query/query-todos-200"));
        return args.stream();
    }

    @ParameterizedTest(name = "legacy url {0} redirects to {1}")
    @MethodSource("legacyUrlRedirects")
    void legacyUrlsRedirectToCanonicalContent(String legacyUrl, String canonicalUrl) {
        final HttpResponseDetails response = http.send(legacyUrl, "get");

        Assertions.assertEquals(301, response.statusCode);
        Assertions.assertEquals(canonicalUrl, response.getHeader("Location"));
    }
}
