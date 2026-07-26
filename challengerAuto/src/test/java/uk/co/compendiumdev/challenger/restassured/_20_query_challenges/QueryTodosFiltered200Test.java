package uk.co.compendiumdev.challenger.restassured._20_query_challenges;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class QueryTodosFiltered200Test extends RestAssuredBaseTest {

    @Test
    void canQueryFilteredTodos() throws Exception {

        TodosApi api = new TodosApi();
        api.createTodo("not done", "this todo is not done", false);
        final Todo doneTodo = api.createTodo("done", "this todo is done", true);

        final HttpRequest request =
                HttpRequest.newBuilder(URI.create(apiPath("/todos")))
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .method("QUERY", HttpRequest.BodyPublishers.ofString("doneStatus=true"))
                        .build();

        final HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .contains("application/json"));

        Todos todosList = new Gson().fromJson(response.body(), Todos.class);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("QUERY /todos (200)").status);

        boolean foundOurTodo = false;
        boolean foundAllTrue = true;
        for (Todo todo : todosList.todos) {
            if (todo.id.equals(doneTodo.id)) {
                foundOurTodo = true;
            }
            foundAllTrue = foundAllTrue && todo.doneStatus;
        }

        Assertions.assertTrue(
                foundOurTodo, "Expected to see the todo we created as 'done' in the list");
        Assertions.assertTrue(foundAllTrue, "Expected all todos returned to be 'done'");
    }
}
