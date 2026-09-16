package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.serverstart.Environment;

public class MultiPlayerScopedSessionTest {
    private static HttpMessageSender http;

    @BeforeAll
    static void startMultiPlayerApi() {
        Environment.stop();
        http = new HttpMessageSender(Environment.getBaseUri(false));
    }

    @AfterAll
    static void stopMultiPlayerApi() {
        Environment.stop();
    }

    @ParameterizedTest(name = "valid X-CHALLENGER selects scoped todos under {0}")
    @MethodSource("apiRoutePrefixes")
    void readRequestsWithValidChallengerHeaderUseTheChallengerDataScope(final String prefix) {
        final String scopedOnlyTitle =
                "scoped-session-" + UUID.randomUUID().toString().substring(0, 8);

        final Todos defaultTodosBefore = getTodos(prefix, Map.of());
        Assertions.assertFalse(containsTodoTitled(defaultTodosBefore, scopedOnlyTitle));

        final HttpResponseDetails challengerResponse =
                http.send(path(prefix, "/challenger"), "POST");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        final String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);

        final Map<String, String> createHeaders = new HashMap<>();
        createHeaders.put("X-CHALLENGER", challengerId);
        createHeaders.put("Content-Type", "application/json");

        final HttpResponseDetails createScopedTodoResponse =
                http.send(
                        path(prefix, "/todos"),
                        "POST",
                        createHeaders,
                        "{\"title\":\"" + scopedOnlyTitle + "\",\"doneStatus\":false}");
        Assertions.assertEquals(201, createScopedTodoResponse.statusCode);

        final Map<String, String> readHeaders = Map.of("X-CHALLENGER", challengerId);
        final HttpResponseDetails scopedRead =
                http.send(path(prefix, "/todos"), "GET", readHeaders, "");
        Assertions.assertEquals(200, scopedRead.statusCode);
        Assertions.assertEquals(challengerId, scopedRead.getHeader("X-CHALLENGER"));
        Assertions.assertTrue(
                containsTodoTitled(todosFrom(scopedRead), scopedOnlyTitle),
                "Expected valid X-CHALLENGER to select the challenger data scope");

        final Todos defaultTodosAfter = getTodos(prefix, Map.of());
        Assertions.assertEquals(defaultTodosBefore.todos.size(), defaultTodosAfter.todos.size());
        Assertions.assertFalse(containsTodoTitled(defaultTodosAfter, scopedOnlyTitle));
    }

    private static Stream<Arguments> apiRoutePrefixes() {
        return Stream.of(Arguments.of(""), Arguments.of("/api"));
    }

    private Todos getTodos(final String prefix, final Map<String, String> headers) {
        final HttpResponseDetails response = http.send(path(prefix, "/todos"), "GET", headers, "");
        Assertions.assertEquals(200, response.statusCode);
        return todosFrom(response);
    }

    private Todos todosFrom(final HttpResponseDetails response) {
        return new Gson().fromJson(response.body, Todos.class);
    }

    private boolean containsTodoTitled(final Todos todos, final String title) {
        return todos.todos.stream().anyMatch(todo -> title.equals(todo.title));
    }

    private static String path(final String prefix, final String route) {
        return prefix + route;
    }
}
