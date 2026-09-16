package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;

class TodoQueryRouteChallengeCompletionTest {

    @Test
    void queryTodosCompletesFormFilteredChallengeWhenDoneAndNotDoneTodosExist() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            fixture.addTodo("already done", "true");
            TodoQueryRouteChallengeCompletion completion =
                    new TodoQueryRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.queryTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Content-Type", "application/x-www-form-urlencoded")
                            .withRequestBody("doneStatus=true"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.QUERY_TODOS_FILTERED));
        }
    }

    @Test
    void queryTodosCompletesJsonPathFilteredChallengeWhenResponseIsAllDone() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoQueryRouteChallengeCompletion completion =
                    new TodoQueryRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.queryTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Content-Type", "application/jsonpath")
                            .withRequestBody("$[?(@.doneStatus == true)]")
                            .withResponseBody(fixture.todosJsonWithDoneStatus(true, 1, 2)),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED));
        }
    }

    @Test
    void queryTodosCompletesStructuredJsonFilteredChallengeWhenResponseIsAllDone() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoQueryRouteChallengeCompletion completion =
                    new TodoQueryRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.queryTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader(
                                    "Content-Type", ApiChallengeMediaTypes.STRUCTURED_JSON_QUERY)
                            .withRequestBody("{\"filter\":{\"doneStatus\":true}}")
                            .withResponseBody(fixture.todosJsonWithDoneStatus(true, 1)),
                    fixture.challenger);

            Assertions.assertTrue(
                    fixture.completed(CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED));
        }
    }

    @Test
    void queryTodosDoesNotCompleteOnNonOkResponse() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoQueryRouteChallengeCompletion completion =
                    new TodoQueryRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.queryTodos(
                    fixture.exchange()
                            .withStatusCode(400)
                            .withRequestHeader("Content-Type", "application/jsonpath")
                            .withRequestBody("$[?(@.doneStatus == true)]")
                            .withResponseBody(fixture.todosJsonWithDoneStatus(true, 1)),
                    fixture.challenger);

            Assertions.assertFalse(fixture.completed(CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED));
        }
    }
}
