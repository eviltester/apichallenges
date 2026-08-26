package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiFinalResponse;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class ChallengerRouteResponseCallbacks {
    private static final int OK = 200;
    private static final String X_CHALLENGER = "X-CHALLENGER";

    public void configure(final Thingifier thingifier, final Challengers challengers) {
        thingifier
                .apiContract()
                .route(RoutingVerb.GET, "/todos")
                .afterResponse(
                        "complete-get-todos",
                        (context, response) ->
                                completeTodosCollectionChallenge(
                                        context, response, challengers, CHALLENGE.GET_TODOS));

        thingifier
                .apiContract()
                .route(RoutingVerb.HEAD, "/todos")
                .afterResponse(
                        "complete-head-todos",
                        (context, response) ->
                                completeTodosCollectionChallenge(
                                        context, response, challengers, CHALLENGE.GET_HEAD_TODOS));
    }

    private void completeTodosCollectionChallenge(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response,
            final Challengers challengers,
            final CHALLENGE challenge) {

        if (response.statusCode() != OK || context.queryParams().size() != 0) {
            return;
        }

        challengers.pass(challengerFor(context, challengers), challenge);
    }

    private ChallengerAuthData challengerFor(
            final ThingifierApiOperationContext context, final Challengers challengers) {
        final Object principal = context.authenticatedPrincipal();
        if (principal instanceof ChallengerAuthData challenger) {
            return challenger;
        }

        return challengers.getChallenger(context.requestHeaders().get(X_CHALLENGER));
    }
}
