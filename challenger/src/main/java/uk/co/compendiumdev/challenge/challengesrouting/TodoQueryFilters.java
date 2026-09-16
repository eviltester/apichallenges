package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;

final class TodoQueryFilters {
    private final TodoRepository repository;

    TodoQueryFilters(final TodoRepository repository) {
        this.repository = repository;
    }

    boolean hasTodoFilter(final ChallengerApiExchange exchange) {
        for (String fieldName : exchange.rawQueryParams().keySet()) {
            if (repository.hasFieldNamed(fieldName)) {
                return true;
            }
        }
        if (exchange.queryParams() == null) {
            return false;
        }
        for (FilterBy filterBy : exchange.queryParams().fieldFilters().toList()) {
            if (repository.hasFieldNamed(filterBy.fieldName)) {
                return true;
            }
        }
        return false;
    }

    boolean idFilterResponseIsProperSubset(
            final ChallengerApiExchange exchange,
            final TodoJsonResponse response,
            final FilterOperation operation) {
        FilterBy filter = filterBy(exchange, "id", operation);
        Integer threshold = integerValue(filter);
        if (threshold == null
                || response.isEmpty()
                || !isNonEmptyProperSubset(exchange, response)) {
            return false;
        }
        if (operation == FilterOperation.GREATER_THAN) {
            return response.allIdsGreaterThan(threshold);
        }
        if (operation == FilterOperation.LESS_THAN) {
            return response.allIdsLessThan(threshold);
        }
        return false;
    }

    boolean idSingleResultFilterResponseMatches(
            final ChallengerApiExchange exchange, final TodoJsonResponse response) {
        FilterBy filter = filterBy(exchange, "id", FilterOperation.EQUALS);
        Integer expectedId = integerValue(filter);
        return expectedId != null
                && repository.count(exchange) > 1
                && response.singleTodoHasId(expectedId);
    }

    boolean descriptionRegexFilterResponseMatches(
            final ChallengerApiExchange exchange, final TodoJsonResponse response) {
        FilterBy filter = filterBy(exchange, "description", FilterOperation.REGEX_MATCH);
        if (filter == null || filter.fieldValue == null || filter.fieldValue.isEmpty()) {
            return false;
        }

        final Pattern pattern;
        try {
            pattern = Pattern.compile(filter.fieldValue);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return response.allDescriptionsMatch(pattern);
    }

    boolean descriptionWildcardFilterResponseMatches(
            final ChallengerApiExchange exchange, final TodoJsonResponse response) {
        FilterBy filter = filterBy(exchange, "description", FilterOperation.WILDCARD_MATCH);
        if (filter == null || filter.fieldValue == null || filter.fieldValue.isEmpty()) {
            return false;
        }

        return response.allDescriptionsMatch(Pattern.compile(wildcardAsRegex(filter.fieldValue)));
    }

    int countMatchingQueryFilter(final ChallengerApiExchange exchange, final String fieldName) {
        String value = exchange.rawQueryParams().get(fieldName);
        if (value == null) {
            FilterBy filter = filterBy(exchange, fieldName, FilterOperation.EQUALS);
            value = filter == null ? null : filter.fieldValue;
        }
        return value == null ? 0 : repository.countByField(exchange, fieldName, value);
    }

    boolean hasDoneAndNotDoneTodos(final ChallengerApiExchange exchange) {
        return repository.countByField(exchange, "doneStatus", "true") > 0
                && repository.countByField(exchange, "doneStatus", "false") > 0;
    }

    private boolean isNonEmptyProperSubset(
            final ChallengerApiExchange exchange, final TodoJsonResponse response) {
        return !response.isEmpty() && response.size() < repository.count(exchange);
    }

    private FilterBy filterBy(
            final ChallengerApiExchange exchange,
            final String fieldName,
            final FilterOperation operation) {
        if (exchange.queryParams() == null) {
            return null;
        }
        for (FilterBy filterBy : exchange.queryParams().toList()) {
            if (filterBy.fieldName.equals(fieldName) && filterBy.filterOperation == operation) {
                return filterBy;
            }
        }
        return null;
    }

    private Integer integerValue(final FilterBy filter) {
        if (filter == null) {
            return null;
        }
        try {
            return Integer.parseInt(filter.fieldValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String wildcardAsRegex(final String wildcard) {
        StringBuilder regex = new StringBuilder();
        for (int index = 0; index < wildcard.length(); index++) {
            char character = wildcard.charAt(index);
            if (character == '*') {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }
        return regex.toString();
    }
}
