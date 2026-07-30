package uk.co.compendiumdev.challenger.restassured._03_get_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class GetTodosPagination200Test extends RestAssuredBaseTest {

    @Test
    void canGetTodosWithLimit() {

        ensureMinimumTodos(8);

        Todos todosList = getTodosFrom("/todos?_limit=8");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /todos (200) ?_limit").status);
        Assertions.assertEquals(8, todosList.todos.size());
    }

    @Test
    void canGetTodosWithLimitAndOffset() {

        ensureMinimumTodos(10);

        Todos todosList = getTodosFrom("/todos?_limit=5&_offset=5");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?_limit&_offset").status);
        Assertions.assertEquals(5, todosList.todos.size());
    }

    @Test
    void canGetTodosWithLimitTooHigh() {

        RestAssured.given()
                .header("X-CHALLENGER", xChallenger)
                .accept("application/json")
                .get(apiPath("/todos?_limit=21"))
                .then()
                .statusCode(400);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (400) ?_limit too high").status);
    }

    @Test
    void canGetTodosSortedAndPaginated() {

        ensureMinimumTodos(10);

        Todos todosList = getTodosFrom("/todos?_sortBy=-id&_limit=5&_offset=5");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?_sortBy&_limit&_offset").status);
        Assertions.assertEquals(5, todosList.todos.size());
        Assertions.assertTrue(idsAreDescending(todosList.todos));
    }

    @Test
    void canGetTodosFilteredAndPaginated() {

        ensureMinimumFalseTodos(3);

        Todos todosList = getTodosFrom("/todos?doneStatus=false&_limit=2&_offset=1");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter&_limit&_offset").status);
        Assertions.assertEquals(2, todosList.todos.size());
        Assertions.assertTrue(allDoneStatusFalse(todosList.todos));
    }

    private Todos getTodosFrom(final String path) {
        return RestAssured.given()
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

    private void ensureMinimumTodos(final int minimum) {
        TodosApi api = new TodosApi();
        List<Todo> todos = api.getTodos();

        while (todos.size() < minimum) {
            api.createTodo(
                    "pagination fixture " + todos.size(),
                    "created for pagination challenge",
                    false);
            todos = api.getTodos();
        }
    }

    private void ensureMinimumFalseTodos(final int minimum) {
        ensureMinimumTodos(minimum);

        TodosApi api = new TodosApi();
        List<Todo> todos = api.getTodos();
        for (Todo todo : todos) {
            if (countFalseTodos(todos) >= minimum) {
                return;
            }
            if (Boolean.TRUE.equals(todo.doneStatus)) {
                setTodoDoneStatus(todo.id, false);
                todos = api.getTodos();
            }
        }
    }

    private void setTodoDoneStatus(final Integer id, final boolean doneStatus) {
        RestAssured.given()
                .header("X-CHALLENGER", xChallenger)
                .accept("application/json")
                .contentType("application/json")
                .body("{\"doneStatus\":" + doneStatus + "}")
                .post(apiPath("/todos/" + id))
                .then()
                .statusCode(200);
    }

    private int countFalseTodos(final List<Todo> todos) {
        int count = 0;
        for (Todo todo : todos) {
            if (Boolean.FALSE.equals(todo.doneStatus)) {
                count++;
            }
        }
        return count;
    }

    private boolean idsAreDescending(final List<Todo> todos) {
        for (int index = 1; index < todos.size(); index++) {
            if (todos.get(index - 1).id < todos.get(index).id) {
                return false;
            }
        }
        return true;
    }

    private boolean allDoneStatusFalse(final List<Todo> todos) {
        for (Todo todo : todos) {
            if (Boolean.TRUE.equals(todo.doneStatus)) {
                return false;
            }
        }
        return true;
    }
}
