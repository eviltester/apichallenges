package uk.co.compendiumdev.robodepot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RoboDepotSupport {

    private RoboDepotSupport() {}

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
        List<EntityInstance> related = related(store, instance, relationshipName);
        return related.isEmpty() ? null : related.get(0);
    }

    static String stringValue(final EntityInstance instance, final String fieldName) {
        return instance.getFieldValue(fieldName).asString();
    }

    static int intValue(final EntityInstance instance, final String fieldName) {
        return instance.getFieldValue(fieldName).asInteger();
    }

    static boolean booleanValue(final EntityInstance instance, final String fieldName) {
        return Boolean.parseBoolean(stringValue(instance, fieldName));
    }

    static EntityInstance patch(
            final ThingStore store, final EntityInstance instance, final String... fieldPairs) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(instance.getEntity());
        for (int index = 0; index < fieldPairs.length; index += 2) {
            draft.withField(fieldPairs[index], fieldPairs[index + 1]);
        }
        return store.entities().patch(instance, draft);
    }

    static List<EntityInstance> sortedById(final List<EntityInstance> instances) {
        List<EntityInstance> sorted = new ArrayList<>(instances);
        sorted.sort(Comparator.comparingInt(instance -> asInt(instance.getPrimaryKeyValue(), 0)));
        return sorted;
    }

    static int asInt(final String value, final int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
