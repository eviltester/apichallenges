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

public class GetTodosSorted200Test extends RestAssuredBaseTest {

    @Test
    void canGetTodosSortedAscendingByTitle() {

        Todos todosList = getTodosSortedBy("title");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?_sortBy ascending").status);
        Assertions.assertTrue(titlesAreAscending(todosList.todos));
    }

    @Test
    void canGetTodosSortedDescendingById() {

        Todos todosList = getTodosSortedBy("-id");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?_sortBy descending").status);
        Assertions.assertTrue(idsAreDescending(todosList.todos));
    }

    @Test
    void canGetTodosSortedByMultipleFields() {

        Todos todosList = getTodosSortedBy("doneStatus,-id");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?_sortBy multiple").status);
        Assertions.assertTrue(doneStatusAscendingThenIdsDescending(todosList.todos));
    }

    @Test
    void canGetTodosFilteredAndSorted() {

        Todos todosList = getTodosFrom("/todos?doneStatus=false&_sortBy=-id");

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /todos (200) ?filter&_sortBy").status);
        Assertions.assertTrue(allDoneStatusFalse(todosList.todos));
        Assertions.assertTrue(idsAreDescending(todosList.todos));
    }

    private Todos getTodosSortedBy(final String sortBy) {
        return getTodosFrom("/todos?_sortBy=" + sortBy);
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

    private boolean titlesAreAscending(final List<Todo> todos) {
        for (int index = 1; index < todos.size(); index++) {
            if (todos.get(index - 1).title.compareTo(todos.get(index).title) > 0) {
                return false;
            }
        }
        return true;
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
            if (todo.doneStatus) {
                return false;
            }
        }
        return true;
    }

    private boolean doneStatusAscendingThenIdsDescending(final List<Todo> todos) {
        for (int index = 1; index < todos.size(); index++) {
            Todo previous = todos.get(index - 1);
            Todo current = todos.get(index);
            int doneStatusComparison = Boolean.compare(previous.doneStatus, current.doneStatus);
            if (doneStatusComparison > 0) {
                return false;
            }
            if (doneStatusComparison == 0 && previous.id < current.id) {
                return false;
            }
        }
        return true;
    }
}
