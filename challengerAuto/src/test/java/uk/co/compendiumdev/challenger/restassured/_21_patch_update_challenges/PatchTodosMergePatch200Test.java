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

public class PatchTodosMergePatch200Test extends RestAssuredBaseTest {

    @Test
    void canPatchTodoWithJsonMergePatch() {

        Todo todo = new TodosApi().createTodo("patch merge", "before merge", false);
        String updatedDescription = "patched merge " + System.currentTimeMillis();

        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .accept("application/json")
                        .header("Content-Type", "application/merge-patch+json")
                        .body("{\"description\":\"" + updatedDescription + "\"}")
                        .patch(apiPath("/todos/" + todo.id))
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract()
                        .response();

        Todo patchedTodo = response.body().as(Todo.class);
        Assertions.assertEquals(todo.id, patchedTodo.id);
        Assertions.assertEquals(todo.title, patchedTodo.title);
        Assertions.assertEquals(updatedDescription, patchedTodo.description);
        Assertions.assertEquals(todo.doneStatus, patchedTodo.doneStatus);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("PATCH /todos/{id} (200) merge-patch").status);
    }
}
