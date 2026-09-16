package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.Locale;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

final class TodoCreateRouteChallengeCompletion {
    private final Challengers challengers;
    private final TodoRepository todos;

    TodoCreateRouteChallengeCompletion(final Thingifier thingifier, final Challengers challengers) {
        this.challengers = challengers;
        this.todos = new TodoRepository(thingifier);
    }

    void postTodos(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS);
            completeCreateContentTypeChallenges(exchange, challenger);
            completeMaxTitleDescriptionChallenge(exchange, challenger);
            return;
        }

        if (exchange.statusCode() == 415) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_415);
        }
        if (exchange.statusCode() == 409
                && exchange.responseBody().contains("maximum limit of 20")) {
            challengers.pass(challenger, CHALLENGE.POST_ALL_TODOS);
        }
        if (exchange.statusCode() == 422
                || exchange.statusCode() == 400
                || exchange.statusCode() == 413) {
            postTodoValidationFailures(exchange, challenger);
        }
    }

    void postTodo(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_UPDATE_TODO);
        }
        if (exchange.statusCode() == 404) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_404);
        }
    }

    private void completeCreateContentTypeChallenges(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (ApiChallengeMediaTypes.requestContentTypeIs(
                exchange, ApiChallengeMediaTypes.APPLICATION_JSON)) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_JSON);
            if (exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_XML)) {
                challengers.pass(challenger, CHALLENGE.POST_CREATE_JSON_ACCEPT_XML);
            }
        }
        if (ApiChallengeMediaTypes.requestContentTypeIs(
                exchange, ApiChallengeMediaTypes.APPLICATION_XML)) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_XML);
            if (exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_JSON)) {
                challengers.pass(challenger, CHALLENGE.POST_CREATE_XML_ACCEPT_JSON);
            }
        }
        if (ApiChallengeMediaTypes.requestContentTypeIs(
                exchange, ApiChallengeMediaTypes.TODO_VENDOR_XML)) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_VENDOR_XML);
        }
    }

    private void completeMaxTitleDescriptionChallenge(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        String id = todoIdFromLocation(exchange);
        EntityInstance todo = todos.findByIdentifier(exchange, id);
        if (todo == null) {
            return;
        }

        String title = todo.getFieldValue("title").asString();
        String description = todo.getFieldValue("description").asString();
        if (title.length() >= 50 && description.length() >= 200) {
            challengers.pass(challenger, CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH);
        }
    }

    private void postTodoValidationFailures(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        String response =
                RouteExchange.responseBodyAndErrors(exchange) + "\n" + exchange.requestBody();
        String normalizedResponse = response.toLowerCase(Locale.ROOT);

        if (normalizedResponse.contains("donestatus")
                && (normalizedResponse.contains("boolean")
                        || normalizedResponse.contains("true or false"))) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_BAD_DONE_STATUS);
        }
        if (normalizedResponse.contains("title")
                && ((normalizedResponse.contains("maximum")
                                && normalizedResponse.contains("length"))
                        || normalizedResponse.contains("max length")
                        || normalizedResponse.contains("allowable length exceeded"))) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH);
        }
        if (normalizedResponse.contains("description")
                && ((normalizedResponse.contains("maximum")
                                && normalizedResponse.contains("length"))
                        || normalizedResponse.contains("max length")
                        || normalizedResponse.contains("allowable length exceeded"))) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH);
        }
        if (normalizedResponse.contains("payload")
                || normalizedResponse.contains("body too large")
                || normalizedResponse.contains("request body too large")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE);
        }
        if (normalizedResponse.contains("extra")
                || normalizedResponse.contains("could not find field")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD);
        }
    }

    private String todoIdFromLocation(final ChallengerApiExchange exchange) {
        String location = exchange.responseHeader("Location");
        if (location == null || location.trim().isEmpty()) {
            return "";
        }
        int slash = location.lastIndexOf("/");
        return slash == -1 ? location : location.substring(slash + 1);
    }
}
