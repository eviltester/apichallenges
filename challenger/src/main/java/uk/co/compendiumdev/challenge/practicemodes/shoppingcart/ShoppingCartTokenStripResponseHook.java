package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

final class ShoppingCartTokenStripResponseHook implements InternalHttpResponseHook {

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {
        final String path = request.getPath();
        if (!path.startsWith("shop/carts")) {
            return;
        }
        if (response.getBody() == null || response.getBody().isBlank()) {
            return;
        }

        try {
            final JsonElement parsed = JsonParser.parseString(response.getBody());
            removeTokenFields(parsed);
            response.setBody(ShoppingCartSupport.GSON.toJson(parsed));
        } catch (RuntimeException ignored) {
        }
    }

    private void removeTokenFields(final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            object.remove("token");
            for (String key : object.keySet().toArray(String[]::new)) {
                removeTokenFields(object.get(key));
            }
        }
        if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                removeTokenFields(item);
            }
        }
    }
}
