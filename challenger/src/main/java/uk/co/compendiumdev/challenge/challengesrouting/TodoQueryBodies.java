package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.co.compendiumdev.thingifier.api.http.UrlQueryParamParser;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

final class TodoQueryBodies {
    private TodoQueryBodies() {}

    static boolean formBodyContainsDoneStatusTrue(final ChallengerApiExchange exchange) {
        QueryFilterParams params = new UrlQueryParamParser().parse(exchange.requestBody());
        for (FilterBy filterBy : params.toList()) {
            if (filterBy.fieldName.equals("doneStatus")
                    && filterBy.filterOperation == FilterOperation.EQUALS
                    && filterBy.fieldValue.equalsIgnoreCase("true")) {
                return true;
            }
        }
        return false;
    }

    static boolean jsonPathBodyTargetsDoneStatusTrue(final ChallengerApiExchange exchange) {
        String body = exchange.requestBody().replaceAll("\\s+", "").toLowerCase();
        return body.contains("donestatus==true") || body.contains("donestatus=true");
    }

    static boolean structuredJsonBodyTargetsDoneStatusTrue(final ChallengerApiExchange exchange) {
        try {
            JsonElement parsed = JsonParser.parseString(exchange.requestBody());
            if (!parsed.isJsonObject()) {
                return false;
            }
            JsonObject object = parsed.getAsJsonObject();
            if (!object.has("filter") || !object.get("filter").isJsonObject()) {
                return false;
            }
            JsonObject filter = object.get("filter").getAsJsonObject();
            return filter.has("doneStatus") && filter.get("doneStatus").getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }
}
