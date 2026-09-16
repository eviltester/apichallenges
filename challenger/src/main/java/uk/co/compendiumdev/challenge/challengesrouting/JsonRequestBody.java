package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

final class JsonRequestBody {
    private JsonRequestBody() {}

    static boolean hasField(final ChallengerApiExchange exchange, final String fieldName) {
        String body = exchange.requestBody();
        if (body == null || body.trim().isEmpty()) {
            return false;
        }

        try {
            JsonElement parsed = JsonParser.parseString(body);
            return parsed.isJsonObject() && parsed.getAsJsonObject().has(fieldName);
        } catch (Exception e) {
            return false;
        }
    }
}
