package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

class TodoCreateRouteChallengeCompletionTest {

    @Test
    void postTodosCompletesCreateAndContentNegotiationChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoCreateRouteChallengeCompletion completion =
                    new TodoCreateRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.postTodos(
                    fixture.exchange()
                            .withStatusCode(201)
                            .withRequestHeader("Content-Type", "application/json")
                            .withResponseContentType(ApiChallengeMediaTypes.APPLICATION_XML),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_CREATE_JSON));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML));
        }
    }

    @Test
    void postTodosCompletesVendorXmlAndMaxLengthChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            EntityInstance todo = fixture.addTodo("t".repeat(50), "false", "d".repeat(200));
            TodoCreateRouteChallengeCompletion completion =
                    new TodoCreateRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.postTodos(
                    fixture.exchange()
                            .withStatusCode(201)
                            .withRequestHeader(
                                    "Content-Type", ApiChallengeMediaTypes.TODO_VENDOR_XML)
                            .withResponseHeader("Location", "/todos/" + todo.getPrimaryKeyValue()),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_CREATE_VENDOR_XML));
            Assertions.assertTrue(
                    fixture.completed(CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH));
        }
    }

    @Test
    void postTodosCompletesErrorChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoCreateRouteChallengeCompletion completion =
                    new TodoCreateRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.postTodos(fixture.exchange().withStatusCode(415), fixture.challenger);
            completion.postTodos(
                    fixture.exchange()
                            .withStatusCode(409)
                            .withResponseBody("maximum limit of 20 todos"),
                    fixture.challenger);
            completion.postTodos(
                    fixture.exchange()
                            .withStatusCode(422)
                            .withResponseBody(
                                    "doneStatus must be boolean; title maximum length; "
                                            + "description maximum length; extra field"),
                    fixture.challenger);
            completion.postTodos(
                    fixture.exchange()
                            .withStatusCode(413)
                            .withResponseBody("request body too large"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS_415));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_ALL_TODOS));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS_BAD_DONE_STATUS));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH));
            Assertions.assertTrue(
                    fixture.completed(CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE));
        }
    }

    @Test
    void postTodoCompletesUpdateAndNotFoundChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoCreateRouteChallengeCompletion completion =
                    new TodoCreateRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.postTodo(fixture.exchange().withStatusCode(200), fixture.challenger);
            completion.postTodo(fixture.exchange().withStatusCode(404), fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_UPDATE_TODO));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_TODOS_404));
        }
    }
}
