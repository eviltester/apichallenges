package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.ArrayList;
import java.util.List;

final class TodoSortCriteria {
    private final List<TodoSortCriterion> criteria;

    private TodoSortCriteria(final List<TodoSortCriterion> criteria) {
        this.criteria = List.copyOf(criteria);
    }

    static TodoSortCriteria from(final ChallengerApiExchange exchange) {
        return from(exchange.rawQueryParams().get("_sortBy"));
    }

    static TodoSortCriteria from(final String sortBy) {
        final List<TodoSortCriterion> criteria = new ArrayList<>();
        for (String value : sortByValuesFrom(sortBy)) {
            TodoSortCriterion criterion = criterionFrom(value);
            if (criterion != null) {
                criteria.add(criterion);
            }
        }
        return new TodoSortCriteria(criteria);
    }

    boolean isEmpty() {
        return criteria.isEmpty();
    }

    boolean hasOnlyTodoFields(final TodoRepository repository) {
        return !criteria.isEmpty()
                && criteria.stream().allMatch(sort -> repository.hasFieldNamed(sort.fieldName()));
    }

    boolean isSingleFieldAscending() {
        return criteria.size() == 1 && criteria.get(0).ascending();
    }

    boolean isSingleFieldDescending() {
        return criteria.size() == 1 && !criteria.get(0).ascending();
    }

    boolean isMultipleFields() {
        return criteria.size() > 1;
    }

    boolean isDescendingId() {
        return criteria.size() == 1
                && criteria.get(0).fieldName().equals("id")
                && !criteria.get(0).ascending();
    }

    private static List<String> sortByValuesFrom(final String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return List.of();
        }
        final List<String> values = new ArrayList<>();
        for (String rawValue : sortBy.split(",")) {
            String value = rawValue.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private static TodoSortCriterion criterionFrom(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        boolean ascending = !trimmed.startsWith("-");
        if (trimmed.startsWith("-") || trimmed.startsWith("+")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.isEmpty()) {
            return null;
        }
        return new TodoSortCriterion(trimmed, ascending);
    }

    private record TodoSortCriterion(String fieldName, boolean ascending) {}
}
