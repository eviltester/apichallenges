package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;

class TodoUpdateRouteChallengeCompletionTest {

    @Test
    void putTodosCompletesCollectionPutChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoUpdateRouteChallengeCompletion completion =
                    new TodoUpdateRouteChallengeCompletion(fixture.challengers);

            completion.putTodos(
                    fixture.exchange().withStatusCode(200).withRequestBody("{\"id\":1}"),
                    fixture.challenger);
            completion.putTodos(
                    fixture.exchange().withStatusCode(422).withRequestBody("{\"title\":\"x\"}"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_BODY_ID_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_NO_ID_422));
        }
    }

    @Test
    void putTodoCompletesSuccessfulFullPartialAndMissingBodyIdChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoUpdateRouteChallengeCompletion completion =
                    new TodoUpdateRouteChallengeCompletion(fixture.challengers);

            completion.putTodo(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestBody(
                                    "{\"title\":\"x\",\"doneStatus\":false,\"description\":\"\"}"),
                    fixture.challenger);
            completion.putTodo(
                    fixture.exchange().withStatusCode(200).withRequestBody("{\"title\":\"x\"}"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_FULL_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_PARTIAL_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_ID_NO_BODY_ID_200));
        }
    }

    @Test
    void putTodoCompletesValidationFailureChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoUpdateRouteChallengeCompletion completion =
                    new TodoUpdateRouteChallengeCompletion(fixture.challengers);

            completion.putTodo(
                    fixture.exchange()
                            .withStatusCode(422)
                            .withResponseBody("Cannot create todo with PUT due to Auto fields id"),
                    fixture.challenger);
            completion.putTodo(
                    fixture.exchange()
                            .withStatusCode(422)
                            .withResponseBody("title : field is mandatory"),
                    fixture.challenger);
            completion.putTodo(
                    fixture.exchange()
                            .withStatusCode(422)
                            .withResponseBody("Can not amend id from 1 to 2"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_422));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_MISSING_TITLE_422));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PUT_TODOS_422_NO_AMEND_ID));
        }
    }

    @Test
    void patchTodoCompletesPatchContentTypeChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoUpdateRouteChallengeCompletion completion =
                    new TodoUpdateRouteChallengeCompletion(fixture.challengers);

            completion.patchTodo(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Content-Type", "application/json"),
                    fixture.challenger);
            completion.patchTodo(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Content-Type", "application/merge-patch+json"),
                    fixture.challenger);
            completion.patchTodo(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Content-Type", "application/json-patch+json"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.PATCH_TODOS_PARTIAL_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PATCH_TODOS_MERGE_PATCH_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.PATCH_TODOS_JSON_PATCH_200));
        }
    }
}
