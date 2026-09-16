package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

final class TodoJsonResponse {
    private final List<JsonObject> todos;

    private TodoJsonResponse(final List<JsonObject> todos) {
        this.todos = List.copyOf(todos);
    }

    static TodoJsonResponse from(final ChallengerApiExchange exchange) {
        return fromBody(exchange.responseBody());
    }

    static TodoJsonResponse fromBody(final String body) {
        final List<JsonObject> todos = new ArrayList<>();
        if (body == null || body.trim().isEmpty()) {
            return new TodoJsonResponse(todos);
        }

        try {
            JsonElement parsed = JsonParser.parseString(body);
            if (!parsed.isJsonObject()) {
                return new TodoJsonResponse(todos);
            }
            JsonObject responseObject = parsed.getAsJsonObject();
            if (!responseObject.has("todos") || !responseObject.get("todos").isJsonArray()) {
                return new TodoJsonResponse(todos);
            }
            JsonArray array = responseObject.get("todos").getAsJsonArray();
            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    todos.add(element.getAsJsonObject());
                }
            }
        } catch (Exception e) {
            return new TodoJsonResponse(List.of());
        }

        return new TodoJsonResponse(todos);
    }

    int size() {
        return todos.size();
    }

    boolean isEmpty() {
        return todos.isEmpty();
    }

    boolean idsAreDescending() {
        for (int index = 1; index < todos.size(); index++) {
            if (idOf(todos.get(index - 1)) <= idOf(todos.get(index))) {
                return false;
            }
        }
        return !todos.isEmpty();
    }

    boolean allDoneStatusFalse() {
        return !todos.isEmpty()
                && todos.stream()
                        .allMatch(
                                todo ->
                                        todo.has("doneStatus")
                                                && !todo.get("doneStatus").getAsBoolean());
    }

    boolean allDoneStatusTrue() {
        return !todos.isEmpty()
                && todos.stream()
                        .allMatch(
                                todo ->
                                        todo.has("doneStatus")
                                                && todo.get("doneStatus").getAsBoolean());
    }

    boolean allIdsGreaterThan(final int threshold) {
        return todos.stream().allMatch(todo -> idOf(todo) > threshold);
    }

    boolean allIdsLessThan(final int threshold) {
        return todos.stream().allMatch(todo -> idOf(todo) < threshold);
    }

    boolean singleTodoHasId(final int expectedId) {
        return todos.size() == 1 && idOf(todos.get(0)) == expectedId;
    }

    boolean allDescriptionsMatch(final java.util.regex.Pattern pattern) {
        return !todos.isEmpty()
                && todos.stream()
                        .allMatch(
                                todo -> {
                                    String description = descriptionOf(todo);
                                    return !description.isEmpty()
                                            && pattern.matcher(description).matches();
                                });
    }

    private static int idOf(final JsonObject todo) {
        if (!todo.has("id")) {
            return Integer.MIN_VALUE;
        }
        return todo.get("id").getAsInt();
    }

    private static String descriptionOf(final JsonObject todo) {
        if (!todo.has("description") || todo.get("description").isJsonNull()) {
            return "";
        }
        return todo.get("description").getAsString();
    }
}
