package uk.co.compendiumdev.challenger.restassured._21_patch_update_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class PatchTodosJsonPatch200Test extends RestAssuredBaseTest {

    @Test
    void canPatchTodoWithJsonPatch() {

        Todo todo = new TodosApi().createTodo("patch json", "before json patch", false);
        String updatedTitle = "patched json " + System.currentTimeMillis();

        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .accept("application/json")
                        .header("Content-Type", "application/json-patch+json")
                        .body(
                                "[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\""
                                        + updatedTitle
                                        + "\"}]")
                        .patch(apiPath("/todos/" + todo.id))
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract()
                        .response();

        Todo patchedTodo = response.body().as(Todo.class);
        Assertions.assertEquals(todo.id, patchedTodo.id);
        Assertions.assertEquals(updatedTitle, patchedTodo.title);
        Assertions.assertEquals(todo.description, patchedTodo.description);
        Assertions.assertEquals(todo.doneStatus, patchedTodo.doneStatus);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("PATCH /todos/{id} (200) json-patch").status);
    }
}
