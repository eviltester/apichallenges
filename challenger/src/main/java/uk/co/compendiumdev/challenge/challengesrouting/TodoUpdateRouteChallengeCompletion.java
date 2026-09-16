package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;

final class TodoUpdateRouteChallengeCompletion {
    private final Challengers challengers;

    TodoUpdateRouteChallengeCompletion(final Challengers challengers) {
        this.challengers = challengers;
    }

    void putTodos(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 200 && JsonRequestBody.hasField(exchange, "id")) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_BODY_ID_200);
        }
        if (exchange.statusCode() == 422 && !JsonRequestBody.hasField(exchange, "id")) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_NO_ID_422);
        }
    }

    void putTodo(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 200) {
            String body = exchange.requestBody().toLowerCase();
            if (body.contains("donestatus") && body.contains("description")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_FULL_200);
            }
            if (!body.contains("donestatus") && !body.contains("description")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_PARTIAL_200);
            }
            if (!JsonRequestBody.hasField(exchange, "id")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_ID_NO_BODY_ID_200);
            }
            return;
        }

        String response = RouteExchange.responseBodyAndErrors(exchange);

        if (exchange.statusCode() == 422
                && response.contains("Cannot create todo with PUT due to Auto fields id")) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_422);
        }
        if (exchange.statusCode() == 422 && response.contains("title : field is mandatory")) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_MISSING_TITLE_422);
        }
        if (exchange.statusCode() == 422 && response.contains("Can not amend id from")) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_422_NO_AMEND_ID);
        }
    }

    void patchTodo(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() != 200) {
            return;
        }
        if (ApiChallengeMediaTypes.requestContentTypeIs(
                exchange, ApiChallengeMediaTypes.APPLICATION_JSON)) {
            challengers.pass(challenger, CHALLENGE.PATCH_TODOS_PARTIAL_200);
        }
        if (ApiChallengeMediaTypes.requestContentTypeIs(exchange, "application/merge-patch+json")) {
            challengers.pass(challenger, CHALLENGE.PATCH_TODOS_MERGE_PATCH_200);
        }
        if (ApiChallengeMediaTypes.requestContentTypeIs(exchange, "application/json-patch+json")) {
            challengers.pass(challenger, CHALLENGE.PATCH_TODOS_JSON_PATCH_200);
        }
    }
}
