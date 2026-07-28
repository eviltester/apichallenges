package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartSupport {

    static final Gson GSON = new Gson();

    private ShoppingCartSupport() {}

    static ThingStore store(final Thingifier thingifier) {
        return thingifier.getERmodel().getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    static EntityDefinition definition(final Thingifier thingifier, final String entityName) {
        return thingifier.getDefinitionNamed(entityName);
    }

    static EntityInstance findById(
            final Thingifier thingifier,
            final ThingStore store,
            final String entityName,
            final String id) {
        return store.entityQueries().findByPrimaryKey(definition(thingifier, entityName), id);
    }

    static List<EntityInstance> list(
            final Thingifier thingifier, final ThingStore store, final String entityName) {
        return store.entityQueries().list(definition(thingifier, entityName));
    }

    static List<EntityInstance> related(
            final ThingStore store, final EntityInstance instance, final String relationshipName) {
        if (instance == null) {
            return List.of();
        }
        return store.relationships().listRelated(instance, relationshipName);
    }

    static EntityInstance firstRelated(
            final ThingStore store, final EntityInstance instance, final String relationshipName) {
        final List<EntityInstance> related = related(store, instance, relationshipName);
        return related.isEmpty() ? null : related.get(0);
    }

    static String stringValue(final EntityInstance instance, final String fieldName) {
        return instance.getFieldValue(fieldName).asString();
    }

    static int intValue(final EntityInstance instance, final String fieldName) {
        return instance.getFieldValue(fieldName).asInteger();
    }

    static float floatValue(final EntityInstance instance, final String fieldName) {
        return instance.getFieldValue(fieldName).asFloat();
    }

    static EntityInstance patch(
            final ThingStore store, final EntityInstance instance, final String... fieldPairs) {
        final EntityInstanceDraft draft = EntityInstanceDraft.forEntity(instance.getEntity());
        for (int index = 0; index < fieldPairs.length; index += 2) {
            draft.withField(fieldPairs[index], fieldPairs[index + 1]);
        }
        return store.entities().patch(instance, draft);
    }

    static List<EntityInstance> sortedById(final List<EntityInstance> instances) {
        final List<EntityInstance> sorted = new ArrayList<>(instances);
        sorted.sort(Comparator.comparingInt(instance -> asInt(instance.getPrimaryKeyValue(), 0)));
        return sorted;
    }

    static List<String> segments(final String path) {
        final String cleanPath = path == null ? "" : path.replaceFirst("^/+", "");
        if (cleanPath.isBlank()) {
            return List.of();
        }
        return List.of(cleanPath.split("/"));
    }

    static int asInt(final String value, final int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    static HttpApiResponse jsonResponse(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final int status,
            final Object body) {
        final ApiResponse apiResponse = new ApiResponse(status);
        apiResponse.setBody(GSON.toJson(body));
        return new HttpApiResponse(
                request.getHeaders(), apiResponse, new JsonThing(config.jsonOutput()), config);
    }

    static HttpApiResponse error(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final int status,
            final String message) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorMessages", List.of(message));
        return jsonResponse(request, config, status, body);
    }

    static Map<String, Object> body(final Object... pairs) {
        final Map<String, Object> body = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            body.put(String.valueOf(pairs[index]), pairs[index + 1]);
        }
        return body;
    }

    static String bearerToken(final String authorizationHeader) {
        if (authorizationHeader == null) {
            return "";
        }
        final String value = authorizationHeader.trim();
        if (!value.toLowerCase().startsWith("bearer ")) {
            return "";
        }
        return value.substring("bearer ".length()).trim();
    }
}
