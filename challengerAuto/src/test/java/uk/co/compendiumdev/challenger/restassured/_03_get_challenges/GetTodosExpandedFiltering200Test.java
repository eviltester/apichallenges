package uk.co.compendiumdev.challenger.restassured._03_get_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class GetTodosExpandedFiltering200Test extends RestAssuredBaseTest {

    @Test
    void canGetTodosFilteredByIdGreaterThan() {

        List<Todo> allTodos = ensureAtLeastTwoTodos();
        int threshold = allTodos.stream().map(todo -> todo.id).min(Integer::compareTo).get();

        Todos filteredTodos = getTodosFrom("/todos?id%3E" + threshold);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter id greater than").status);
        Assertions.assertFalse(filteredTodos.todos.isEmpty());
        Assertions.assertTrue(filteredTodos.todos.size() < allTodos.size());
        Assertions.assertTrue(allIdsGreaterThan(filteredTodos.todos, threshold));
    }

    @Test
    void canGetTodosFilteredByIdLessThan() {

        List<Todo> allTodos = ensureAtLeastTwoTodos();
        int threshold = allTodos.stream().map(todo -> todo.id).max(Integer::compareTo).get();

        Todos filteredTodos = getTodosFrom("/todos?id%3C" + threshold);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter id less than").status);
        Assertions.assertFalse(filteredTodos.todos.isEmpty());
        Assertions.assertTrue(filteredTodos.todos.size() < allTodos.size());
        Assertions.assertTrue(allIdsLessThan(filteredTodos.todos, threshold));
    }

    @Test
    void canGetTodosFilteredByIdToSingleResult() {

        List<Todo> allTodos = ensureAtLeastTwoTodos();
        Todo selectedTodo = allTodos.stream().min(Comparator.comparing(todo -> todo.id)).get();

        Todos filteredTodos = getTodosFrom("/todos?id=" + selectedTodo.id);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter id single result").status);
        Assertions.assertEquals(1, filteredTodos.todos.size());
        Assertions.assertEquals(selectedTodo.id, filteredTodos.todos.get(0).id);
    }

    @Test
    void canGetTodosFilteredByDescriptionRegex() {

        String token = "regexfixture";
        Todo todo = ensureAtLeastTwoTodos().get(0);
        updateTodoDescription(todo, token + " description");

        Todos filteredTodos = getTodosFrom("/todos?description%7E=.%2A" + token + ".%2A");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter description regex").status);
        Assertions.assertFalse(filteredTodos.todos.isEmpty());
        Assertions.assertTrue(allDescriptionsContain(filteredTodos.todos, token));
    }

    @Test
    void canGetTodosFilteredByDescriptionWildcard() {

        String token = "wildcardfixture";
        Todo todo = ensureAtLeastTwoTodos().get(0);
        updateTodoDescription(todo, token + " description");

        Todos filteredTodos = getTodosFrom("/todos?description%2A=%2A" + token + "%2A");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter description wildcard").status);
        Assertions.assertFalse(filteredTodos.todos.isEmpty());
        Assertions.assertTrue(allDescriptionsContain(filteredTodos.todos, token));
    }

    private List<Todo> ensureAtLeastTwoTodos() {
        TodosApi api = new TodosApi();
        List<Todo> todos = api.getTodos();

        while (todos.size() < 2) {
            api.createTodo("expanded filter fixture " + todos.size(), "filter fixture", false);
            todos = api.getTodos();
        }

        return todos;
    }

    private void updateTodoDescription(final Todo todo, final String description) {
        Todo payload = new Todo();
        payload.title = todo.title;
        payload.doneStatus = todo.doneStatus;
        payload.description = description;

        RestAssured.given()
                .header("X-CHALLENGER", xChallenger)
                .accept("application/json")
                .contentType("application/json")
                .body(payload)
                .post(apiPath("/todos/" + todo.id))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    private Todos getTodosFrom(final String path) {
        return RestAssured.given()
                .urlEncodingEnabled(false)
                .header("X-CHALLENGER", xChallenger)
                .accept("application/json")
                .get(apiPath(path))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .as(Todos.class);
    }

    private boolean allIdsGreaterThan(final List<Todo> todos, final int threshold) {
        for (Todo todo : todos) {
            if (todo.id <= threshold) {
                return false;
            }
        }
        return true;
    }

    private boolean allIdsLessThan(final List<Todo> todos, final int threshold) {
        for (Todo todo : todos) {
            if (todo.id >= threshold) {
                return false;
            }
        }
        return true;
    }

    private boolean allDescriptionsContain(final List<Todo> todos, final String token) {
        for (Todo todo : todos) {
            if (todo.description == null
                    || todo.description.isEmpty()
                    || !todo.description.contains(token)) {
                return false;
            }
        }
        return true;
    }
}
