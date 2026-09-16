package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.serverstart.Environment;

public class ApiChallengeRouteCompatibilityTest {

    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp() {
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    @ParameterizedTest(name = "API Challenges routes are available under {0}")
    @MethodSource("apiRoutePrefixes")
    void canonicalAndLegacyApiRoutesAreAvailable(final String prefix) {
        assertStatus(204, path(prefix, "/heartbeat"), "get");
        assertStatus(200, path(prefix, "/challenges"), "get");
        assertStatus(200, path(prefix, "/todos"), "get");
        assertStatus(200, path(prefix, "/todos"), "options");

        HttpResponseDetails exportResponse =
                http.send(path(prefix, "/todos/export?format=csv"), "get");
        Assertions.assertEquals(200, exportResponse.statusCode);
        Assertions.assertEquals(
                "attachment; filename=\"todos.csv\"",
                exportResponse.getHeader("Content-Disposition"));

        http.clearHeaders();
        HttpResponseDetails challengerResponse = http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        Assertions.assertNotNull(challengerResponse.getHeader("X-CHALLENGER"));
        Assertions.assertEquals(
                path(prefix, "/challenger/" + challengerResponse.getHeader("X-CHALLENGER")),
                challengerResponse.getHeader("Location"));

        http.clearHeaders();
        http.setBasicAuth("admin", "password");
        HttpResponseDetails tokenResponse = http.send(path(prefix, "/secret/token"), "get");
        Assertions.assertEquals(200, tokenResponse.statusCode);
        Assertions.assertEquals(
                AuthRoutes.READ_ONLY_AUTH_TOKEN, tokenResponse.getHeader("X-AUTH-TOKEN"));

        http.clearHeaders();
        http.setHeader("X-AUTH-TOKEN", AuthRoutes.READ_ONLY_AUTH_TOKEN);
        HttpResponseDetails noteResponse = http.send(path(prefix, "/secret/note"), "get");
        Assertions.assertEquals(200, noteResponse.statusCode);
        Assertions.assertTrue(noteResponse.body.contains(AuthRoutes.READ_ONLY_SECRET_NOTE));
    }

    @ParameterizedTest(name = "secret note 401 responses challenge Bearer auth under {0}")
    @MethodSource("apiRoutePrefixes")
    void secretNoteUnauthorizedResponsesIncludeBearerChallenge(final String prefix) {
        http.clearHeaders();
        HttpResponseDetails getResponse = http.send(path(prefix, "/secret/note"), "get");
        Assertions.assertEquals(401, getResponse.statusCode);
        assertBearerAuthenticationChallenge(getResponse);

        http.clearHeaders();
        http.setHeader("Content-Type", "application/json");
        HttpResponseDetails postResponse = http.send(path(prefix, "/secret/note"), "post");
        Assertions.assertEquals(401, postResponse.statusCode);
        assertBearerAuthenticationChallenge(postResponse);
    }

    @ParameterizedTest(name = "generated secret entity route {1} is not public under {0}")
    @MethodSource("generatedSecretEntityRoutes")
    void generatedSecretEntityRoutesAreNotPublic(final String prefix, final String route) {
        http.clearHeaders();

        HttpResponseDetails response = http.send(path(prefix, route), "get");

        Assertions.assertEquals(404, response.statusCode);
    }

    @ParameterizedTest(name = "challenger database export only exposes todos under {0}")
    @MethodSource("apiRoutePrefixes")
    void challengerDatabaseExportDoesNotExposeHiddenSecretEntities(final String prefix) {
        http.clearHeaders();
        HttpResponseDetails challengerResponse = http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);
        Assertions.assertEquals(
                path(prefix, "/challenger/" + challengerId),
                challengerResponse.getHeader("Location"));

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challengerId);
        HttpResponseDetails databaseResponse =
                http.send(path(prefix, "/challenger/database/" + challengerId), "get");

        Assertions.assertEquals(200, databaseResponse.statusCode);
        final JsonObject databaseExport =
                JsonParser.parseString(databaseResponse.body).getAsJsonObject();
        Assertions.assertTrue(databaseExport.has("todos"));
        Assertions.assertFalse(databaseExport.has(SecretThingifier.TOKEN_COLLECTION));
        Assertions.assertFalse(databaseExport.has(SecretThingifier.NOTE_COLLECTION));
    }

    @ParameterizedTest(name = "challenge completion works under {0}")
    @MethodSource("apiRoutePrefixes")
    void challengeCompletionWorksThroughCanonicalAndLegacyRoutes(final String prefix) {
        http.clearHeaders();
        HttpResponseDetails challengerResponse = http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challengerId);
        Assertions.assertEquals(200, http.send(path(prefix, "/todos"), "get").statusCode);
        Assertions.assertEquals(204, http.send(path(prefix, "/heartbeat"), "get").statusCode);

        ChallengerAuthData challenger =
                ChallengeMain.getChallenger().getChallengers().getChallenger(challengerId);
        Assertions.assertNotNull(challenger);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_HEARTBEAT_204));
    }

    @ParameterizedTest(name = "Thingifier afterResponse callbacks run under {0}")
    @MethodSource("apiRoutePrefixes")
    void thingifierRouteCallbacksRunThroughCanonicalAndLegacyAliases(final String prefix) {
        http.clearHeaders();
        HttpResponseDetails challengerResponse = http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);

        Map<String, String> jsonHeaders = headersFor(challengerId, "application/json");
        HttpResponseDetails createdDoneTodo =
                http.send(
                        path(prefix, "/todos"),
                        "post",
                        jsonHeaders,
                        "{\"title\":\"alias callback done\",\"doneStatus\":true,\"description\":\"\"}");
        Assertions.assertEquals(201, createdDoneTodo.statusCode);

        HttpResponseDetails createdNotDoneTodo =
                http.send(
                        path(prefix, "/todos"),
                        "post",
                        jsonHeaders,
                        "{\"title\":\"alias callback not done\",\"doneStatus\":false,\"description\":\"\"}");
        Assertions.assertEquals(201, createdNotDoneTodo.statusCode);

        Map<String, String> xmlAcceptHeaders = headersFor(challengerId, "application/json");
        xmlAcceptHeaders.put("Accept", "text/xml");
        Assertions.assertEquals(
                200, http.send(path(prefix, "/todos"), "get", xmlAcceptHeaders, "").statusCode);

        Map<String, String> vendorXmlHeaders =
                headersFor(challengerId, "application/vnd.apichallenges.todo+xml");
        vendorXmlHeaders.put("Accept", "application/json");
        Assertions.assertEquals(
                201,
                http.send(
                                path(prefix, "/todos"),
                                "post",
                                vendorXmlHeaders,
                                "<todo><title>alias vendor xml</title><doneStatus>true</doneStatus></todo>")
                        .statusCode);

        Map<String, String> structuredQueryHeaders =
                headersFor(challengerId, "application/vnd.thingifier.query+json");
        structuredQueryHeaders.put("Accept", "application/json");
        Assertions.assertEquals(
                200,
                http.send(
                                path(prefix, "/todos"),
                                "query",
                                structuredQueryHeaders,
                                "{\"filter\":{\"doneStatus\":true}}")
                        .statusCode);

        Map<String, String> jsonPatchHeaders =
                headersFor(challengerId, "application/json-patch+json");
        Assertions.assertEquals(
                200,
                http.send(
                                path(
                                        prefix,
                                        "/todos/"
                                                + lastPathSegment(
                                                        createdDoneTodo.getHeader("Location"))),
                                "patch",
                                jsonPatchHeaders,
                                "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"alias patched\"}]")
                        .statusCode);

        Map<String, String> basicAuthHeaders = headersFor(challengerId, "application/json");
        basicAuthHeaders.put("Authorization", "basic YWRtaW46cGFzc3dvcmQ=");
        Assertions.assertEquals(
                200,
                http.send(path(prefix, "/secret/token"), "get", basicAuthHeaders, "").statusCode);

        ChallengerAuthData challenger = challengerFor(challengerId);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_TODOS));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_ACCEPT_TEXT_XML));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_CREATE_VENDOR_XML));
        Assertions.assertTrue(
                challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.PATCH_TODOS_JSON_PATCH_200));
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_SECRET_TOKEN_200));
    }

    @ParameterizedTest(name = "created todo Location uses active route prefix under {0}")
    @MethodSource("apiRoutePrefixes")
    void createdTodoLocationHeaderUsesActiveRoutePrefix(final String prefix) {
        http.clearHeaders();
        HttpResponseDetails challengerResponse = http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);

        HttpResponseDetails created =
                http.send(
                        path(prefix, "/todos"),
                        "post",
                        Map.of("X-CHALLENGER", challengerId, "Content-Type", "application/json"),
                        "{\"title\":\"mounted route todo\",\"doneStatus\":false,\"description\":\"\"}");

        Assertions.assertEquals(201, created.statusCode);
        Assertions.assertTrue(
                created.getHeader("Location").startsWith(path(prefix, "/todos/")),
                created.getHeader("Location"));
    }

    @ParameterizedTest(name = "challenger API Locations use active route prefix under {0}")
    @MethodSource("apiRoutePrefixes")
    void challengerLocationHeadersUseApiRoutes(final String prefix) {
        http.clearHeaders();
        HttpResponseDetails challengerResponse = http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);
        Assertions.assertEquals(
                path(prefix, "/challenger/" + challengerId),
                challengerResponse.getHeader("Location"));

        http.clearHeaders();
        http.setHeader("X-CHALLENGER", challengerId);
        HttpResponseDetails challengesResponse = http.send(path(prefix, "/challenges"), "get");
        Assertions.assertEquals(200, challengesResponse.statusCode);
        Assertions.assertEquals(
                path(prefix, "/challenger/" + challengerId),
                challengesResponse.getHeader("Location"));
    }

    @ParameterizedTest(name = "challenger documented errors return JSON under {0}")
    @MethodSource("apiRoutePrefixes")
    void challengerDocumentedErrorsReturnJson(final String prefix) {
        http.clearHeaders();
        http.setHeader("Accept", "*/*");
        assertJsonError(
                http.send(
                        path(prefix, "/challenger/b593b008-7e4b-4225-8dde-9d0be805d80d"),
                        "put",
                        Map.of("Accept", "*/*"),
                        ""),
                400);

        http.clearHeaders();
        http.setHeader("Accept", "*/*");
        assertJsonError(
                http.send(
                        path(prefix, "/challenger/database/S3PuZh"),
                        "put",
                        Map.of("Accept", "*/*"),
                        ""),
                400);

        http.clearHeaders();
        http.setHeader("Accept", "*/*");
        assertJsonError(
                http.send(
                        path(prefix, "/challenger/database/11111111-2222-4333-8444-555555555555"),
                        "put",
                        Map.of("Accept", "*/*"),
                        ""),
                404);
    }

    @ParameterizedTest(name = "empty API 404 fallbacks return JSON under {0}")
    @MethodSource("apiRoutePrefixes")
    void emptyApi404FallbacksReturnJson(final String prefix) {
        http.clearHeaders();
        http.setHeader("Accept", "*/*");

        assertJsonError(http.send(path(prefix, "/challenger/database/extra/path"), "get"), 404);
    }

    @ParameterizedTest(name = "todo instance HEAD responses do not write a body under {0}")
    @MethodSource("apiRoutePrefixes")
    void todoInstanceHeadResponsesDoNotRaiseServerErrors(final String prefix) {
        final String challengerId = createChallengerId(prefix);
        final Map<String, String> headers = Map.of("X-CHALLENGER", challengerId);

        assertHeadResponseWithoutBody(
                http.send(path(prefix, "/todos/3"), "head", headers, ""), 200);
        assertHeadResponseWithoutBody(
                http.send(path(prefix, "/todos/26"), "head", headers, ""), 404);
    }

    @ParameterizedTest(name = "docs compatibility route {0} redirects to {1}")
    @MethodSource("legacyDocsRedirectRoutes")
    void legacyDocsCompatibilityRoutesRedirectToCanonicalDocs(
            final String docsPath, final String canonicalPath) {
        HttpResponseDetails response = http.send(docsPath, "get");

        Assertions.assertEquals(301, response.statusCode);
        Assertions.assertEquals(canonicalPath, response.getHeader("Location"));
    }

    @ParameterizedTest(name = "openapi route {0} documents {1}")
    @MethodSource("openApiDocumentationRoutes")
    void openApiDocumentsRoutesForCanonicalAndLegacyDocs(
            final String openApiPath, final String documentedRoute) {
        HttpResponseDetails response = http.send(openApiPath, "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"" + documentedRoute + "\""));
    }

    @Test
    void canonicalOpenApiOptionsSummariesUseCanonicalPaths() {
        final JsonObject paths = canonicalOpenApiPaths();

        Assertions.assertEquals(
                "show all Options for endpoint of /api/todos",
                operationSummary(paths, "/api/todos", "options"));
        Assertions.assertEquals(
                "show all Options for endpoint of /api/todos/:id",
                operationSummary(paths, "/api/todos/{id}", "options"));
        assertResponseStatus(paths, "/api/todos", "options", "200");
        assertResponseStatus(paths, "/api/todos/{id}", "options", "200");
    }

    @Test
    void canonicalOpenApiDocumentsHandWrittenRouteResponses() {
        final JsonObject paths = canonicalOpenApiPaths();

        assertResponseHasJsonContent(paths, "/api/challenges", "get", "200");
        assertResponseStatus(paths, "/api/challenger", "post", "200");
        assertResponseStatus(paths, "/api/challenger", "post", "201");
        assertResponseHasJsonContent(paths, "/api/challenger", "post", "404");
        assertResponseHasJsonContent(paths, "/api/challenger/{guid}", "get", "200");
        assertResponseHasJsonContent(paths, "/api/challenger/{guid}", "get", "404");
        assertResponseHasJsonContent(paths, "/api/challenger/{guid}", "put", "200");
        assertResponseStatus(paths, "/api/challenger/{guid}", "put", "201");
        assertResponseHasJsonContent(paths, "/api/challenger/{guid}", "put", "400");
        assertResponseHasJsonContent(paths, "/api/challenger/{guid}", "put", "409");
        assertResponseHasJsonContent(paths, "/api/challenger/database/{guid}", "get", "200");
        assertResponseHasJsonContent(paths, "/api/challenger/database/{guid}", "get", "400");
        assertResponseHasJsonContent(paths, "/api/challenger/database/{guid}", "get", "404");
        assertResponseStatus(paths, "/api/challenger/database/{guid}", "put", "204");
        assertResponseHasJsonContent(paths, "/api/challenger/database/{guid}", "put", "400");
        assertResponseHasJsonContent(paths, "/api/challenger/database/{guid}", "put", "404");
        assertChallengerSchemaKeepsUuidFormat(paths, "/api/challenger/{guid}", "get");
        assertResponseHasJsonContent(paths, "/api/todos/export", "get", "200");
        Assertions.assertTrue(
                operationResponse(paths, "/api/todos/export", "get", "200")
                        .getAsJsonObject("content")
                        .has("text/csv"));
        assertResponseHasJsonContent(paths, "/api/todos/export", "get", "400");
    }

    @Test
    void canonicalOpenApiSecretSummariesUseCanonicalPaths() {
        final JsonObject paths = canonicalOpenApiPaths();

        Assertions.assertEquals(
                "GET /api/secret/token with basic auth to get an X-AUTH-TOKEN header and token response body for access to /api/secret/note.",
                operationSummary(paths, "/api/secret/token", "get"));
        Assertions.assertFalse(paths.getAsJsonObject("/api/secret/token").has("post"));
        Assertions.assertEquals(
                "GET /api/secret/note with X-AUTH-TOKEN to return the secret note for the user.",
                operationSummary(paths, "/api/secret/note", "get"));
        Assertions.assertEquals(
                "POST /api/secret/note with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note.",
                operationSummary(paths, "/api/secret/note", "post"));
    }

    @Test
    void canonicalOpenApiTodoExportDocumentsFormatAsQueryParameter() {
        final JsonObject get =
                canonicalOpenApiPaths().getAsJsonObject("/api/todos/export").getAsJsonObject("get");

        final JsonObject formatParam = operationParameter(get, "format");

        Assertions.assertEquals("query", formatParam.get("in").getAsString());
        Assertions.assertFalse(formatParam.get("required").getAsBoolean());
        Assertions.assertEquals(
                "string", formatParam.getAsJsonObject("schema").get("type").getAsString());
    }

    @Test
    void canonicalOpenApiSecretResponsesUseSingleEntitySchemas() {
        final JsonObject paths = canonicalOpenApiPaths();

        assertJsonResponseSchemaRef(paths, "/api/secret/token", "get", SecretThingifier.TOKEN_VIEW);
        assertJsonResponseSchemaRef(paths, "/api/secret/note", "get", SecretThingifier.NOTE_VIEW);
        assertJsonResponseSchemaRef(paths, "/api/secret/note", "post", SecretThingifier.NOTE_VIEW);
    }

    @Test
    void canonicalOpenApiSecretNoteDocumentsBearerAndApiKeySecurity() {
        final JsonObject openApi = canonicalOpenApi();
        final JsonObject securitySchemes =
                openApi.getAsJsonObject("components").getAsJsonObject("securitySchemes");
        final JsonObject bearerScheme =
                securitySchemes.getAsJsonObject(SecretThingifier.TOKEN_SCHEME);
        final JsonObject apiKeyScheme =
                securitySchemes.getAsJsonObject(SecretThingifier.API_KEY_SCHEME);
        final JsonObject secretNotePath =
                openApi.getAsJsonObject("paths").getAsJsonObject("/api/secret/note");

        Assertions.assertEquals("http", bearerScheme.get("type").getAsString());
        Assertions.assertEquals("bearer", bearerScheme.get("scheme").getAsString());
        Assertions.assertEquals("apiKey", apiKeyScheme.get("type").getAsString());
        Assertions.assertEquals("header", apiKeyScheme.get("in").getAsString());
        Assertions.assertEquals("X-AUTH-TOKEN", apiKeyScheme.get("name").getAsString());
        assertSecurityAlternatives(
                secretNotePath.getAsJsonObject("get"),
                SecretThingifier.TOKEN_SCHEME,
                SecretThingifier.API_KEY_SCHEME);
        assertSecurityAlternatives(
                secretNotePath.getAsJsonObject("post"),
                SecretThingifier.TOKEN_SCHEME,
                SecretThingifier.API_KEY_SCHEME);
    }

    private static Stream<Arguments> apiRoutePrefixes() {
        return Stream.of(Arguments.of("/api"), Arguments.of(""));
    }

    private static Stream<Arguments> openApiDocumentationRoutes() {
        return Stream.of(Arguments.of("/api/docs/openapi.json", "/api/todos"));
    }

    private static Stream<Arguments> generatedSecretEntityRoutes() {
        return Stream.of("/api", "")
                .flatMap(
                        prefix ->
                                Stream.of(
                                        Arguments.of(prefix, "/secrettokens"),
                                        Arguments.of(prefix, "/secrettokens/token"),
                                        Arguments.of(prefix, "/secretnotes"),
                                        Arguments.of(prefix, "/secretnotes/note")));
    }

    private static Stream<Arguments> legacyDocsRedirectRoutes() {
        return Stream.of(
                Arguments.of("/docs", "/api/docs"),
                Arguments.of("/docs/swagger-ui", "/api/docs/swagger-ui"),
                Arguments.of("/docs/openapi.json", "/api/docs/openapi.json"),
                Arguments.of(
                        "/docs/openapi-3.2.json?permissive",
                        "/api/docs/openapi-3.2.json?permissive"),
                Arguments.of("/docs/swagger?download", "/api/docs/swagger?download"));
    }

    private void assertStatus(final int statusCode, final String route, final String verb) {
        http.clearHeaders();
        Assertions.assertEquals(statusCode, http.send(route, verb).statusCode, route);
    }

    private JsonObject canonicalOpenApiPaths() {
        return canonicalOpenApi().getAsJsonObject("paths");
    }

    private JsonObject canonicalOpenApi() {
        final HttpResponseDetails response = http.send("/api/docs/openapi.json", "get");

        Assertions.assertEquals(200, response.statusCode);
        return JsonParser.parseString(response.body).getAsJsonObject();
    }

    private String operationSummary(
            final JsonObject paths, final String path, final String operation) {
        return paths.getAsJsonObject(path).getAsJsonObject(operation).get("summary").getAsString();
    }

    private void assertResponseStatus(
            final JsonObject paths,
            final String path,
            final String operation,
            final String status) {
        Assertions.assertTrue(
                paths.getAsJsonObject(path)
                        .getAsJsonObject(operation)
                        .getAsJsonObject("responses")
                        .has(status),
                path + " " + operation + " " + status);
    }

    private void assertResponseHasJsonContent(
            final JsonObject paths,
            final String path,
            final String operation,
            final String status) {
        final JsonObject response = operationResponse(paths, path, operation, status);
        Assertions.assertTrue(response.has("content"), path + " " + operation + " " + status);
        Assertions.assertTrue(
                response.getAsJsonObject("content").has("application/json"),
                path + " " + operation + " " + status);
    }

    private JsonObject operationResponse(
            final JsonObject paths,
            final String path,
            final String operation,
            final String status) {
        return paths.getAsJsonObject(path)
                .getAsJsonObject(operation)
                .getAsJsonObject("responses")
                .getAsJsonObject(status);
    }

    private JsonObject operationParameter(final JsonObject operation, final String parameterName) {
        final JsonArray parameters = operation.getAsJsonArray("parameters");
        for (int index = 0; index < parameters.size(); index++) {
            JsonObject parameter = parameters.get(index).getAsJsonObject();
            if (parameterName.equals(parameter.get("name").getAsString())) {
                return parameter;
            }
        }
        throw new AssertionError("OpenAPI parameter not found: " + parameterName);
    }

    private void assertJsonResponseSchemaRef(
            final JsonObject paths,
            final String path,
            final String operation,
            final String schemaName) {
        final JsonObject schema =
                paths.getAsJsonObject(path)
                        .getAsJsonObject(operation)
                        .getAsJsonObject("responses")
                        .getAsJsonObject("200")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/json")
                        .getAsJsonObject("schema");

        Assertions.assertEquals(
                "#/components/schemas/" + schemaName, schema.get("$ref").getAsString());
    }

    private void assertSecurityAlternatives(
            final JsonObject operation, final String... expectedSchemeNames) {
        final JsonArray security = operation.getAsJsonArray("security");

        Assertions.assertEquals(expectedSchemeNames.length, security.size());
        for (int index = 0; index < expectedSchemeNames.length; index++) {
            Assertions.assertTrue(
                    security.get(index).getAsJsonObject().has(expectedSchemeNames[index]),
                    security.toString());
        }
    }

    private void assertBearerAuthenticationChallenge(final HttpResponseDetails response) {
        Assertions.assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
    }

    private void assertJsonError(final HttpResponseDetails response, final int expectedStatus) {
        Assertions.assertEquals(expectedStatus, response.statusCode);
        Assertions.assertTrue(
                response.getHeader("Content-Type").contains("application/json"),
                response.getHeader("Content-Type"));
        Assertions.assertTrue(response.body.contains("\"errorMessages\""), response.body);
    }

    private void assertHeadResponseWithoutBody(
            final HttpResponseDetails response, final int expectedStatus) {
        Assertions.assertEquals(expectedStatus, response.statusCode);
        Assertions.assertEquals("", response.body);
    }

    private String createChallengerId(final String prefix) {
        http.clearHeaders();
        final HttpResponseDetails challengerResponse =
                http.send(path(prefix, "/challenger"), "post");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        final String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);
        return challengerId;
    }

    private void assertChallengerSchemaKeepsUuidFormat(
            final JsonObject paths, final String path, final String operation) {
        final JsonObject schema =
                operationResponse(paths, path, operation, "200")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/json")
                        .getAsJsonObject("schema");

        Assertions.assertEquals(
                "uuid",
                schema.getAsJsonObject("properties")
                        .getAsJsonObject("xChallenger")
                        .get("format")
                        .getAsString());
        Assertions.assertEquals(Challengers.SINGLE_PLAYER_GUID.length(), 36);
    }

    private Map<String, String> headersFor(final String challengerId, final String contentType) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("X-CHALLENGER", challengerId);
        headers.put("Content-Type", contentType);
        return headers;
    }

    private ChallengerAuthData challengerFor(final String challengerId) {
        ChallengerAuthData challenger =
                ChallengeMain.getChallenger().getChallengers().getChallenger(challengerId);
        Assertions.assertNotNull(challenger);
        return challenger;
    }

    private String lastPathSegment(final String location) {
        Assertions.assertNotNull(location);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private static String path(final String prefix, final String route) {
        return prefix + route;
    }
}
