package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;

class TodoReadRouteChallengeCompletionTest {

    @Test
    void getTodosCompletesBaseAndAcceptHeaderChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoReadRouteChallengeCompletion completion =
                    new TodoReadRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.getTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Accept", "text/xml")
                            .withResponseContentType(ApiChallengeMediaTypes.TEXT_XML),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_ACCEPT_TEXT_XML));
        }
    }

    @Test
    void getTodosCompletesUnsupportedAcceptChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoReadRouteChallengeCompletion completion =
                    new TodoReadRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.getTodos(
                    fixture.exchange()
                            .withStatusCode(406)
                            .withRequestHeader("Accept", "application/json;q=0"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_ACCEPT_Q_REJECTS_ALL_406));
            Assertions.assertTrue(
                    fixture.completed(CHALLENGE.GET_UNSUPPORTED_STRUCTURED_JSON_ACCEPT_406));
        }
    }

    @Test
    void getTodosCompletesFilterSortAndPaginationChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            fixture.addTodo("already done", "true");
            TodoReadRouteChallengeCompletion completion =
                    new TodoReadRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.getTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withQuery("doneStatus=false&_sortBy=-id&_limit=2&_offset=1")
                            .withResponseBody(fixture.todosJsonWithDoneStatus(false, 9, 8)),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS_FILTERED));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS_SORTED_DESCENDING));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS_FILTERED_AND_SORTED));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS_PAGINATED_FILTERED));
        }
    }

    @Test
    void getTodosCompletesSpecificFilterChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoReadRouteChallengeCompletion completion =
                    new TodoReadRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.getTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withQuery("id%3E1")
                            .withResponseBody(fixture.todosJson(2, 3)),
                    fixture.challenger);
            completion.getTodos(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withQuery("description%7E=.%2Afixture")
                            .withResponseBody(
                                    fixture.todosJsonWithDescriptions(
                                            "alpha fixture", "beta fixture")),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN));
            Assertions.assertTrue(
                    fixture.completed(CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX));
        }
    }

    @Test
    void getTodosCompletesLimitTooHighChallenge() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoReadRouteChallengeCompletion completion =
                    new TodoReadRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.getTodos(
                    fixture.exchange().withStatusCode(400).withQuery("_limit=21"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH));
        }
    }

    @Test
    void headAndInstanceReadCompleteSimpleStatusChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            TodoReadRouteChallengeCompletion completion =
                    new TodoReadRouteChallengeCompletion(fixture.thingifier, fixture.challengers);

            completion.headTodos(fixture.exchange().withStatusCode(200), fixture.challenger);
            completion.getTodo(fixture.exchange().withStatusCode(200), fixture.challenger);
            completion.getTodo(fixture.exchange().withStatusCode(404), fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_HEAD_TODOS));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODO));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_TODO_404));
        }
    }
}
