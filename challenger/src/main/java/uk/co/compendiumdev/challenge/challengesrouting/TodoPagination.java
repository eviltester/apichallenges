package uk.co.compendiumdev.challenge.challengesrouting;

final class TodoPagination {
    private final TodoRepository repository;
    private final TodoQueryFilters filters;

    TodoPagination(final TodoRepository repository, final TodoQueryFilters filters) {
        this.repository = repository;
        this.filters = filters;
    }

    int expectedPageSize(final ChallengerApiExchange exchange) {
        Integer limit = RouteQueryParameters.integerValue(exchange, "_limit");
        Integer offset = RouteQueryParameters.integerValue(exchange, "_offset");
        int count = expectedPaginatedTodoCount(exchange);
        if (limit == null) {
            return -1;
        }
        int pageOffset = offset == null ? 0 : offset;
        return Math.max(0, Math.min(limit, count - pageOffset));
    }

    private int expectedPaginatedTodoCount(final ChallengerApiExchange exchange) {
        if (filters.hasTodoFilter(exchange)) {
            return filters.countMatchingQueryFilter(exchange, "doneStatus");
        }
        return repository.count(exchange);
    }
}
