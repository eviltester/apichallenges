package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;

final class TodoQueryRouteChallengeCompletion {
    private final Challengers challengers;
    private final TodoQueryFilters filters;

    TodoQueryRouteChallengeCompletion(final Thingifier thingifier, final Challengers challengers) {
        this.challengers = challengers;
        this.filters = new TodoQueryFilters(new TodoRepository(thingifier));
    }

    void queryTodos(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() != 200) {
            return;
        }

        TodoJsonResponse response = TodoJsonResponse.from(exchange);

        if (ApiChallengeMediaTypes.requestContentTypeIs(
                        exchange, "application/x-www-form-urlencoded")
                && TodoQueryBodies.formBodyContainsDoneStatusTrue(exchange)
                && filters.hasDoneAndNotDoneTodos(exchange)) {
            challengers.pass(challenger, CHALLENGE.QUERY_TODOS_FILTERED);
        }

        if (ApiChallengeMediaTypes.requestContentTypeIs(exchange, "application/jsonpath")
                && TodoQueryBodies.jsonPathBodyTargetsDoneStatusTrue(exchange)
                && response.allDoneStatusTrue()) {
            challengers.pass(challenger, CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED);
        }

        if (ApiChallengeMediaTypes.requestContentTypeIs(
                        exchange, ApiChallengeMediaTypes.STRUCTURED_JSON_QUERY)
                && TodoQueryBodies.structuredJsonBodyTargetsDoneStatusTrue(exchange)
                && response.allDoneStatusTrue()) {
            challengers.pass(challenger, CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED);
        }
    }
}
