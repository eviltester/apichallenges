package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.ArrayList;
import java.util.Arrays;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RouteCompletionTestFixture implements AutoCloseable {
    final Thingifier thingifier;
    final Challengers challengers;
    final ChallengerAuthData challenger;
    final EntityDefinition todo;
    final ThingStore store;

    RouteCompletionTestFixture() {
        thingifier = new ChallengeApiModel().get();
        challengers = new Challengers(thingifier.getERmodel(), Arrays.asList(CHALLENGE.values()));
        challengers.setMultiPlayerMode();
        challenger = challengers.createNewChallenger();
        todo = thingifier.getDefinitionNamed("todo");
        store = thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    RouteChallengeTestExchange exchange() {
        return new RouteChallengeTestExchange().withConfig(thingifier.apiConfig()).withStore(store);
    }

    boolean completed(final CHALLENGE challenge) {
        return challenger.statusOfChallenge(challenge);
    }

    EntityInstance addTodo(final String title, final String doneStatus) {
        return addTodo(title, doneStatus, "");
    }

    EntityInstance addTodo(final String title, final String doneStatus, final String description) {
        return store.entities()
                .create(
                        EntityInstanceDraft.forEntity(todo)
                                .withField("title", title)
                                .withField("doneStatus", doneStatus)
                                .withField("description", description));
    }

    void deleteAllTodos() {
        for (EntityInstance instance : new ArrayList<>(store.entityQueries().list(todo))) {
            store.entities().delete(instance);
        }
    }

    String todosJson(final int... ids) {
        StringBuilder json = new StringBuilder("{\"todos\":[");
        for (int index = 0; index < ids.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append(todoJson(ids[index], false, ""));
        }
        return json.append("]}").toString();
    }

    String todosJsonWithDoneStatus(final boolean doneStatus, final int... ids) {
        StringBuilder json = new StringBuilder("{\"todos\":[");
        for (int index = 0; index < ids.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append(todoJson(ids[index], doneStatus, ""));
        }
        return json.append("]}").toString();
    }

    String todosJsonWithDescriptions(final String... descriptions) {
        StringBuilder json = new StringBuilder("{\"todos\":[");
        for (int index = 0; index < descriptions.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append(todoJson(index + 1, false, descriptions[index]));
        }
        return json.append("]}").toString();
    }

    @Override
    public void close() {
        thingifier.close();
    }

    private String todoJson(final int id, final boolean doneStatus, final String description) {
        return "{\"id\":"
                + id
                + ",\"title\":\"todo "
                + id
                + "\",\"doneStatus\":"
                + doneStatus
                + ",\"description\":\""
                + description
                + "\"}";
    }
}
