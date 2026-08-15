package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
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

    @ParameterizedTest(name = "docs compatibility route {0} is available")
    @ValueSource(strings = {"/docs", "/docs/swagger-ui", "/docs/openapi.json"})
    void legacyDocsCompatibilityRoutesRemainAvailable(final String docsPath) {
        HttpResponseDetails response = http.send(docsPath, "get");

        Assertions.assertEquals(200, response.statusCode);
    }

    @ParameterizedTest(name = "openapi route {0} documents {1}")
    @MethodSource("openApiDocumentationRoutes")
    void openApiDocumentsRoutesForCanonicalAndLegacyDocs(
            final String openApiPath, final String documentedRoute) {
        HttpResponseDetails response = http.send(openApiPath, "get");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains("\"" + documentedRoute + "\""));
    }

    private static Stream<Arguments> apiRoutePrefixes() {
        return Stream.of(Arguments.of("/api"), Arguments.of(""));
    }

    private static Stream<Arguments> openApiDocumentationRoutes() {
        return Stream.of(
                Arguments.of("/api/docs/openapi.json", "/api/todos"),
                Arguments.of("/docs/openapi.json", "/todos"));
    }

    private void assertStatus(final int statusCode, final String route, final String verb) {
        http.clearHeaders();
        Assertions.assertEquals(statusCode, http.send(route, verb).statusCode, route);
    }

    private static String path(final String prefix, final String route) {
        return prefix + route;
    }
}
