package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;

final class TodoDeleteRouteChallengeCompletion {
    private final Challengers challengers;
    private final TodoRepository todos;

    TodoDeleteRouteChallengeCompletion(final Thingifier thingifier, final Challengers challengers) {
        this.challengers = challengers;
        this.todos = new TodoRepository(thingifier);
    }

    void deleteTodo(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() != 204) {
            return;
        }
        challengers.pass(challenger, CHALLENGE.DELETE_A_TODO);
        if (todos.count(exchange) == 0) {
            challengers.pass(challenger, CHALLENGE.DELETE_ALL_TODOS);
        }
    }
}
