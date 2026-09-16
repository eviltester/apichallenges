package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiResponseCallback;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class ChallengerRouteResponseCallbacks {
    public void configure(final Thingifier thingifier, final Challengers challengers) {
        final ChallengerThingifierRouteChallengeCompletion completion =
                new ChallengerThingifierRouteChallengeCompletion(thingifier, challengers);

        afterResponse(
                thingifier, RoutingVerb.GET, "/todos", "complete-get-todos", completion::getTodos);
        afterResponse(
                thingifier,
                RoutingVerb.HEAD,
                "/todos",
                "complete-head-todos",
                completion::headTodos);
        afterResponse(
                thingifier,
                RoutingVerb.GET,
                "/todos/{id}",
                "complete-get-todo",
                completion::getTodo);
        afterResponse(
                thingifier,
                RoutingVerb.POST,
                "/todos",
                "complete-post-todos",
                completion::postTodos);
        afterResponse(
                thingifier,
                RoutingVerb.POST,
                "/todos/{id}",
                "complete-post-todo-update",
                completion::postTodo);
        afterResponse(
                thingifier, RoutingVerb.PUT, "/todos", "complete-put-todos", completion::putTodos);
        afterResponse(
                thingifier,
                RoutingVerb.PUT,
                "/todos/{id}",
                "complete-put-todo",
                completion::putTodo);
        afterResponse(
                thingifier,
                RoutingVerb.PATCH,
                "/todos/{id}",
                "complete-patch-todo",
                completion::patchTodo);
        afterResponse(
                thingifier,
                RoutingVerb.DELETE,
                "/todos/{id}",
                "complete-delete-todo",
                completion::deleteTodo);
        afterResponse(
                thingifier,
                RoutingVerb.QUERY,
                "/todos",
                "complete-query-todos",
                completion::queryTodos);
    }

    private void afterResponse(
            final Thingifier thingifier,
            final RoutingVerb verb,
            final String path,
            final String callbackName,
            final ThingifierApiResponseCallback completion) {
        thingifier.apiContract().route(verb, path).afterResponse(callbackName, completion);
    }
}
