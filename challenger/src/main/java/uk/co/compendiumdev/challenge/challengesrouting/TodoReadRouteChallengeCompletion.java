package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;

final class TodoReadRouteChallengeCompletion {
    private final Challengers challengers;
    private final TodoRepository todos;
    private final TodoQueryFilters filters;
    private final TodoPagination pagination;

    TodoReadRouteChallengeCompletion(final Thingifier thingifier, final Challengers challengers) {
        this.challengers = challengers;
        this.todos = new TodoRepository(thingifier);
        this.filters = new TodoQueryFilters(todos);
        this.pagination = new TodoPagination(todos, filters);
    }

    void getTodos(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS);
        }

        if (exchange.statusCode() == 200 && RouteQueryParameters.hasNoQuery(exchange)) {
            completeAcceptHeaderChallenges(exchange, challenger);
        }

        if (exchange.statusCode() == 406) {
            challengers.pass(challenger, CHALLENGE.GET_UNSUPPORTED_ACCEPT_406);
            completeUnsupportedAcceptChallenges(exchange, challenger);
        }

        if (exchange.statusCode() == 400
                && RouteQueryParameters.integerGreaterThan(
                        exchange, "_limit", exchange.config().forParams().maxPagingLimit())) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH);
        }

        if (!RouteExchange.ok(exchange)) {
            return;
        }

        completeFilterChallenges(exchange, challenger);
        completeSortChallenges(exchange, challenger);
        completePaginationChallenges(exchange, challenger);
    }

    void headTodos(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_HEAD_TODOS);
        }
    }

    void getTodo(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_TODO);
        }
        if (exchange.statusCode() == 404) {
            challengers.pass(challenger, CHALLENGE.GET_TODO_404);
        }
    }

    private void completeAcceptHeaderChallenges(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        final String accept = exchange.requestHeader("Accept");
        final AcceptHeaderParser parser = new AcceptHeaderParser(accept);

        if (parser.missingAcceptHeader()
                && exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_JSON)) {
            challengers.pass(challenger, CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT);
        }
        if (parser.hasAskedForJSON()
                && exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_JSON)) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_JSON);
        }
        if (parser.hasAskedForANY()
                && exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_JSON)) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON);
        }
        if (parser.hasAskedForXmlResponse(ApiChallengeMediaTypes.TODO_XML_ENTITY_NAMES)
                && new AcceptHeaderParser(accept).hasAPreferenceForXml()
                && ApiChallengeMediaTypes.isTodoXmlResponseType(exchange.responseContentType())) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML_PREFERRED);
        }
        if (ApiChallengeMediaTypes.acceptHeaderEquals(
                        exchange, ApiChallengeMediaTypes.APPLICATION_XML)
                && exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_XML)) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML);
        }
        if (ApiChallengeMediaTypes.acceptHeaderEquals(exchange, ApiChallengeMediaTypes.TEXT_XML)
                && exchange.responseTypeIs(ApiChallengeMediaTypes.TEXT_XML)) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_TEXT_XML);
        }
        if (ApiChallengeMediaTypes.acceptHeaderEquals(
                        exchange, ApiChallengeMediaTypes.TODO_VENDOR_XML)
                && exchange.responseTypeIs(ApiChallengeMediaTypes.TODO_VENDOR_XML)) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_VENDOR_XML);
        }
        if (accept != null
                && accept.trim().equalsIgnoreCase("application/*+xml")
                && exchange.responseTypeIs(ApiChallengeMediaTypes.TODO_STRUCTURED_XML)) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_STRUCTURED_XML_WILDCARD);
        }
        if (exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_XML)
                && accept != null
                && accept.toLowerCase().contains("application/xml;q=1")) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML_Q_PREFERRED);
        }
        if (exchange.responseTypeIs(ApiChallengeMediaTypes.APPLICATION_JSON)
                && accept != null
                && accept.toLowerCase().contains("application/json;q=1")) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_JSON_Q_PREFERRED);
        }
    }

    private void completeUnsupportedAcceptChallenges(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        String accept = exchange.requestHeader("Accept");
        if (accept == null) {
            return;
        }
        String normalizedAccept = accept.toLowerCase();
        if (normalizedAccept.contains("q=0")
                || normalizedAccept.contains("application/*+json")
                || normalizedAccept.contains("application/problem+json")) {
            if (normalizedAccept.contains("q=0")) {
                challengers.pass(challenger, CHALLENGE.GET_ACCEPT_Q_REJECTS_ALL_406);
            }
            challengers.pass(challenger, CHALLENGE.GET_UNSUPPORTED_STRUCTURED_JSON_ACCEPT_406);
        }
    }

    private void completeFilterChallenges(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.statusCode() != 200) {
            return;
        }
        TodoJsonResponse response = TodoJsonResponse.from(exchange);
        if (filters.hasTodoFilter(exchange) && filters.hasDoneAndNotDoneTodos(exchange)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED);
        }
        if (filters.idFilterResponseIsProperSubset(
                exchange, response, FilterOperation.GREATER_THAN)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN);
        }
        if (filters.idFilterResponseIsProperSubset(exchange, response, FilterOperation.LESS_THAN)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_ID_LESS_THAN);
        }
        if (filters.idSingleResultFilterResponseMatches(exchange, response)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_ID_SINGLE_RESULT);
        }
        if (filters.descriptionRegexFilterResponseMatches(exchange, response)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX);
        }
        if (filters.descriptionWildcardFilterResponseMatches(exchange, response)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_WILDCARD);
        }
    }

    private void completeSortChallenges(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        final TodoSortCriteria sortCriteria = TodoSortCriteria.from(exchange);
        if (!sortCriteria.hasOnlyTodoFields(todos)) {
            return;
        }

        if (sortCriteria.isSingleFieldAscending()) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_SORTED_ASCENDING);
        }
        if (sortCriteria.isSingleFieldDescending()) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_SORTED_DESCENDING);
        }
        if (sortCriteria.isMultipleFields()) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_SORTED_MULTIPLE_FIELDS);
        }
        if (filters.hasTodoFilter(exchange)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_AND_SORTED);
        }
    }

    private void completePaginationChallenges(
            final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        final TodoJsonResponse response = TodoJsonResponse.from(exchange);
        final int actualPageSize = response.size();
        final int expectedPageSize = pagination.expectedPageSize(exchange);

        if (exchange.statusCode() != 200
                || expectedPageSize < 0
                || actualPageSize != expectedPageSize) {
            return;
        }
        if (RouteQueryParameters.integerValue(exchange, "_limit") != null
                && RouteQueryParameters.integerValue(exchange, "_offset") == null
                && !filters.hasTodoFilter(exchange)) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT);
        }
        if (RouteQueryParameters.integerValue(exchange, "_limit") != null
                && RouteQueryParameters.integerValue(exchange, "_offset") != null
                && !filters.hasTodoFilter(exchange)
                && TodoSortCriteria.from(exchange).isEmpty()) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET);
        }
        if (RouteQueryParameters.integerValue(exchange, "_limit") != null
                && RouteQueryParameters.integerValue(exchange, "_offset") != null
                && TodoSortCriteria.from(exchange).isDescendingId()
                && (response.isEmpty() || response.idsAreDescending())) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_SORTED);
        }
        if (RouteQueryParameters.integerValue(exchange, "_limit") != null
                && RouteQueryParameters.integerValue(exchange, "_offset") != null
                && filters.hasTodoFilter(exchange)
                && response.allDoneStatusFalse()) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_FILTERED);
        }
    }
}
