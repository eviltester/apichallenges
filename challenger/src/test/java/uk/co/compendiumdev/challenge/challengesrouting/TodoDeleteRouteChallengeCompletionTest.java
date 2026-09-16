package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;

class TodoDeleteRouteChallengeCompletionTest {

    @Test
    void deleteTodoCompletesSingleDeleteWhenTodosRemain() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoDeleteRouteChallengeCompletion completion =
                    new TodoDeleteRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.deleteTodo(fixture.exchange().withStatusCode(204), fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.DELETE_A_TODO));
            Assertions.assertFalse(fixture.completed(CHALLENGE.DELETE_ALL_TODOS));
        }
    }

    @Test
    void deleteTodoCompletesDeleteAllWhenRepositoryIsEmpty() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            fixture.deleteAllTodos();
            TodoDeleteRouteChallengeCompletion completion =
                    new TodoDeleteRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.deleteTodo(fixture.exchange().withStatusCode(204), fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.DELETE_A_TODO));
            Assertions.assertTrue(fixture.completed(CHALLENGE.DELETE_ALL_TODOS));
        }
    }

    @Test
    void deleteTodoDoesNotCompleteOnNonNoContentResponse() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoDeleteRouteChallengeCompletion completion =
                    new TodoDeleteRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.deleteTodo(fixture.exchange().withStatusCode(404), fixture.challenger);

            Assertions.assertFalse(fixture.completed(CHALLENGE.DELETE_A_TODO));
        }
    }
}
