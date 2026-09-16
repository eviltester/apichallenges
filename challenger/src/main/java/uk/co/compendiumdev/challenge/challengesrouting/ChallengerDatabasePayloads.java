package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

public final class ChallengerDatabasePayloads {
    private static final Gson GSON = new Gson();
    private static final String TODOS_COLLECTION = "todos";

    private ChallengerDatabasePayloads() {}

    public static String publicDatabaseExportAsJson(
            final EntityRelModel model, final String databaseName) {
        return GSON.toJson(publicDatabaseExport(model, databaseName));
    }

    public static JsonObject publicDatabaseExport(
            final EntityRelModel model, final String databaseName) {
        final JsonObject publicExport = new JsonObject();
        if (model == null
                || databaseName == null
                || !model.getDatabaseNames().contains(databaseName)) {
            return publicExport;
        }

        final JsonObject fullExport =
                parseJsonObjectOrEmpty(model.exportInstanceDataAsJson(databaseName));
        final JsonElement todos = fullExport.get(TODOS_COLLECTION);
        if (todos != null && todos.isJsonArray()) {
            publicExport.add(TODOS_COLLECTION, todos.deepCopy());
        } else {
            publicExport.add(TODOS_COLLECTION, new JsonArray());
        }
        return publicExport;
    }

    private static JsonObject parseJsonObjectOrEmpty(final String json) {
        try {
            final JsonElement element = JsonParser.parseString(json);
            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        } catch (Exception ignored) {
            // Return empty JSON below.
        }
        return new JsonObject();
    }
}
