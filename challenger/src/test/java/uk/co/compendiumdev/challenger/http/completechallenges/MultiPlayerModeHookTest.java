package uk.co.compendiumdev.challenger.http.completechallenges;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.serverstart.Environment;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

public class MultiPlayerModeHookTest {

    @BeforeAll
    public static void controlEnv() {
        Environment.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    public void inMultiPlayerModeRequestsWithNoXChallengerHeaderAmendmentVerbsShould401(
            String verb) {

        int expectedResponse = 401;

        // force multi-player mode for these tests
        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));

        final HttpResponseDetails response = http.send("/todos/1", verb);
        Assertions.assertEquals(expectedResponse, response.statusCode);
        Assertions.assertTrue(response.body.contains("Cannot amend details."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    public void inMultiPlayerModeRequestsToAmendDefaultDBItemsShould401(String verb) {

        int expectedResponse = 401;

        // force multi-player mode for these tests
        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));

        final Map<String, String> defaultDatabaseChallengerHeaders = new HashMap<>();
        defaultDatabaseChallengerHeaders.put("X-CHALLENGER", EntityRelModel.DEFAULT_DATABASE_NAME);

        final HttpResponseDetails response =
                http.send("/todos/1", verb, defaultDatabaseChallengerHeaders, "");
        Assertions.assertEquals(expectedResponse, response.statusCode);
        Assertions.assertTrue(response.body.contains("Cannot amend details."));
    }

    @Test
    public void inMultiPlayerModeRequestsWithNoXChallengerCanReadDefaultTodos() {

        final String scopedOnlyTitle = "scoped multiplayer todo";

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));

        final HttpResponseDetails defaultReadBefore = http.send("/todos", "GET");
        Assertions.assertEquals(200, defaultReadBefore.statusCode);
        final Todos defaultTodosBefore = new Gson().fromJson(defaultReadBefore.body, Todos.class);
        Assertions.assertFalse(containsTodoTitled(defaultTodosBefore, scopedOnlyTitle));

        final HttpResponseDetails challengerResponse = http.send("/challenger", "POST");
        Assertions.assertEquals(201, challengerResponse.statusCode);
        final String challengerId = challengerResponse.getHeader("X-CHALLENGER");
        Assertions.assertNotNull(challengerId);

        final Map<String, String> challengerHeaders = new HashMap<>();
        challengerHeaders.put("X-CHALLENGER", challengerId);
        challengerHeaders.put("Content-Type", "application/json");

        final HttpResponseDetails createScopedTodoResponse =
                http.send(
                        "/todos",
                        "POST",
                        challengerHeaders,
                        "{\"title\":\"" + scopedOnlyTitle + "\",\"doneStatus\":false}");
        Assertions.assertEquals(201, createScopedTodoResponse.statusCode);

        final HttpResponseDetails scopedRead = http.send("/todos", "GET", challengerHeaders, "");
        Assertions.assertEquals(200, scopedRead.statusCode);
        final Todos scopedTodos = new Gson().fromJson(scopedRead.body, Todos.class);
        Assertions.assertTrue(containsTodoTitled(scopedTodos, scopedOnlyTitle));

        http.clearHeaders();
        final HttpResponseDetails defaultReadAfter = http.send("/todos", "GET");
        Assertions.assertEquals(200, defaultReadAfter.statusCode);
        final Todos defaultTodosAfter = new Gson().fromJson(defaultReadAfter.body, Todos.class);
        Assertions.assertEquals(defaultTodosBefore.todos.size(), defaultTodosAfter.todos.size());
        Assertions.assertFalse(containsTodoTitled(defaultTodosAfter, scopedOnlyTitle));
    }

    private boolean containsTodoTitled(final Todos todos, final String title) {
        return todos.todos.stream().anyMatch(todo -> title.equals(todo.title));
    }

    @AfterAll
    public static void stopEnv() {
        Environment.stop();
    }
}
