package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

final class TodoRepository {
    private final Thingifier thingifier;

    TodoRepository(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    EntityInstance findByIdentifier(final ChallengerApiExchange exchange, final String identifier) {
        EntityDefinition todo = definition();
        EntityInstanceQuery query = queryFor(exchange);
        if (todo == null || query == null) {
            return null;
        }
        return query.findByQueryIdentifier(todo, identifier);
    }

    int count(final ChallengerApiExchange exchange) {
        EntityDefinition todo = definition();
        EntityInstanceQuery query = queryFor(exchange);
        if (todo == null || query == null) {
            return 0;
        }
        return query.count(todo);
    }

    int countByField(
            final ChallengerApiExchange exchange, final String fieldName, final String value) {
        EntityDefinition todo = definition();
        EntityInstanceQuery query = queryFor(exchange);
        if (todo == null || query == null) {
            return 0;
        }
        return query.list(todo).stream()
                .filter(
                        instance ->
                                instance.getFieldValue(fieldName) != null
                                        && instance.getFieldValue(fieldName)
                                                .asString()
                                                .equalsIgnoreCase(value))
                .toList()
                .size();
    }

    boolean hasFieldNamed(final String fieldName) {
        EntityDefinition todo = definition();
        return todo != null && todo.hasFieldNameDefined(fieldName);
    }

    private EntityInstanceQuery queryFor(final ChallengerApiExchange exchange) {
        if (exchange == null || exchange.store() == null) {
            return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME).entityQueries();
        }
        return exchange.store().entityQueries();
    }

    private EntityDefinition definition() {
        return thingifier.getDefinitionNamed("todo");
    }
}
