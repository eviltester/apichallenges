package uk.co.compendiumdev.challenger.http.completechallenges;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.serverstart.Environment;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class SinglePlayerPaginationSolutionTest {

    private ChallengerAuthData challenger;
    private EntityDefinition todos;
    private ThingStore repository;
    private HttpMessageSender http;

    @BeforeEach
    public void startSinglePlayerModeWithShortTodoList() {
        Environment.stop();
        http = new HttpMessageSender(Environment.getBaseUri(true, false));

        challenger = ChallengeMain.getChallenger().getChallengers().SINGLE_PLAYER;
        todos =
                ChallengeMain.getChallenger()
                        .getThingifier()
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("todo");
        repository =
                ChallengeMain.getChallenger()
                        .getThingifier()
                        .getStore(Challengers.SINGLE_PLAYER_GUID);

        for (EntityInstance todo : new ArrayList<>(repository.entityQueries().list(todos))) {
            repository.entities().delete(todo);
        }

        createTodo("single player pagination 1");
        createTodo("single player pagination 2");
    }

    @AfterEach
    public void stopSinglePlayerMode() {
        Environment.stop();
    }

    @Test
    public void solutionRequestsCompletePaginationChallengesWithoutHeaderInSinglePlayerMode() {
        Map<String, String> headers = Map.of("Accept", "application/json");

        assertTodoPageSize("/todos?_limit=8", 2, headers);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT));

        assertTodoPageSize("/todos?_limit=5&_offset=5", 0, headers);
        Assertions.assertTrue(
                challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET));

        HttpResponseDetails tooHighResponse = http.send("/todos?_limit=21", "GET", headers, "");
        Assertions.assertEquals(400, tooHighResponse.statusCode);
        Assertions.assertTrue(
                challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH));

        assertTodoPageSize("/todos?_sortBy=-id&_limit=5&_offset=5", 0, headers);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_SORTED));

        assertTodoPageSize("/todos?doneStatus=false&_limit=2&_offset=1", 1, headers);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_FILTERED));
    }

    @Test
    public void writeRequestsWithoutHeaderStillUseSinglePlayerDataScope() {
        final String title = "single player write without x-challenger";
        final Map<String, String> headers =
                Map.of("Accept", "application/json", "Content-Type", "application/json");

        final HttpResponseDetails response =
                http.send(
                        "/todos",
                        "POST",
                        headers,
                        "{\"title\":\"" + title + "\",\"doneStatus\":false}");

        Assertions.assertEquals(201, response.statusCode);
        Assertions.assertTrue(containsTodoTitled(title));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "PATCH"})
    public void amendRequestsWithoutHeaderStillUseSinglePlayerDataScope(final String verb) {
        final EntityInstance todo = firstTodo();
        final String title = "single player " + verb.toLowerCase() + " without x-challenger";
        final Map<String, String> headers =
                Map.of("Accept", "application/json", "Content-Type", "application/json");
        final String body =
                "PUT".equals(verb)
                        ? "{\"title\":\"" + title + "\",\"doneStatus\":false}"
                        : "{\"title\":\"" + title + "\"}";

        final HttpResponseDetails response =
                http.send("/todos/" + todo.getPrimaryKeyValue(), verb, headers, body);

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(containsTodoTitled(title));
    }

    @Test
    public void deleteRequestsWithoutHeaderStillUseSinglePlayerDataScope() {
        final EntityInstance todo = firstTodo();

        final HttpResponseDetails response =
                http.send(
                        "/todos/" + todo.getPrimaryKeyValue(),
                        "DELETE",
                        Map.of("Accept", "application/json"),
                        "");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertFalse(containsTodoWithId(todo.getPrimaryKeyValue()));
    }

    private void createTodo(final String title) {
        repository
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(todos)
                                .withField("title", title)
                                .withField("doneStatus", "false"));
    }

    private void assertTodoPageSize(
            final String path, final int expectedSize, final Map<String, String> headers) {
        HttpResponseDetails response = http.send(path, "GET", headers, "");

        Assertions.assertEquals(200, response.statusCode);
        Todos returnedTodos = new Gson().fromJson(response.body, Todos.class);
        Assertions.assertEquals(expectedSize, returnedTodos.todos.size());
    }

    private EntityInstance firstTodo() {
        return new ArrayList<>(repository.entityQueries().list(todos)).get(0);
    }

    private boolean containsTodoTitled(final String title) {
        return repository.entityQueries().list(todos).stream()
                .anyMatch(todo -> title.equals(todo.getFieldValue("title").asString()));
    }

    private boolean containsTodoWithId(final String id) {
        return repository.entityQueries().list(todos).stream()
                .anyMatch(todo -> id.equals(todo.getPrimaryKeyValue()));
    }
}
