package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiFinalResponse;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;

public final class ChallengerThingifierRouteChallengeCompletion {
    private static final String X_CHALLENGER = "X-CHALLENGER";

    private final Challengers challengers;
    private final TodoReadRouteChallengeCompletion readChallenges;
    private final TodoQueryRouteChallengeCompletion queryChallenges;
    private final TodoCreateRouteChallengeCompletion createChallenges;
    private final TodoUpdateRouteChallengeCompletion updateChallenges;
    private final TodoDeleteRouteChallengeCompletion deleteChallenges;
    private final SecretRouteChallengeCompletion secretChallenges;

    public ChallengerThingifierRouteChallengeCompletion(
            final Thingifier thingifier, final Challengers challengers) {
        this.challengers = challengers;
        this.readChallenges = new TodoReadRouteChallengeCompletion(thingifier, challengers);
        this.queryChallenges = new TodoQueryRouteChallengeCompletion(thingifier, challengers);
        this.createChallenges = new TodoCreateRouteChallengeCompletion(thingifier, challengers);
        this.updateChallenges = new TodoUpdateRouteChallengeCompletion(challengers);
        this.deleteChallenges = new TodoDeleteRouteChallengeCompletion(thingifier, challengers);
        this.secretChallenges = new SecretRouteChallengeCompletion(challengers);
    }

    public void getTodos(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, readChallenges::getTodos);
    }

    public void headTodos(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, readChallenges::headTodos);
    }

    public void getTodo(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, readChallenges::getTodo);
    }

    public void postTodos(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, createChallenges::postTodos);
    }

    public void postTodo(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, createChallenges::postTodo);
    }

    public void putTodos(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, updateChallenges::putTodos);
    }

    public void putTodo(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, updateChallenges::putTodo);
    }

    public void patchTodo(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, updateChallenges::patchTodo);
    }

    public void deleteTodo(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, deleteChallenges::deleteTodo);
    }

    public void queryTodos(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, queryChallenges::queryTodos);
    }

    public void getSecretToken(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, secretChallenges::getSecretToken);
    }

    public void getSecretNote(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, secretChallenges::getSecretNote);
    }

    public void postSecretNote(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        completeRoute(context, response, secretChallenges::postSecretNote);
    }

    private void completeRoute(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response,
            final RouteChallengeCompletion completion) {
        final ChallengerAuthData challenger = challengerFor(context);
        if (challenger == null) {
            return;
        }

        completion.complete(new RouteChallengerApiExchange(context, response), challenger);
    }

    private ChallengerAuthData challengerFor(final ThingifierApiOperationContext context) {
        final Object principal = context.authenticatedPrincipal();
        if (principal instanceof ChallengerAuthData challenger) {
            return challenger;
        }

        ChallengerAuthData challenger =
                challengers.getChallenger(context.requestHeaders().get(X_CHALLENGER));
        if (challenger != null) {
            return challenger;
        }

        return challengers.getChallenger(context.dataScopeName());
    }

    @FunctionalInterface
    private interface RouteChallengeCompletion {
        void complete(ChallengerApiExchange exchange, ChallengerAuthData challenger);
    }
}
