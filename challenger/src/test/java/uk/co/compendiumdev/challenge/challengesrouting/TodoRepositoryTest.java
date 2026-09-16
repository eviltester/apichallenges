package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.thingifier.Thingifier;

class TodoRepositoryTest {
    private Thingifier thingifier;
    private TodoRepository repository;

    @BeforeEach
    void createRepository() {
        thingifier = new ChallengeApiModel().get();
        repository = new TodoRepository(thingifier);
    }

    @AfterEach
    void closeThingifier() {
        thingifier.close();
    }

    @Test
    void readsFromTheDefaultStoreWhenExchangeHasNoStore() {
        Assertions.assertTrue(repository.count(null) > 0);
        Assertions.assertNotNull(repository.findByIdentifier(null, "1"));
        Assertions.assertTrue(repository.countByField(null, "doneStatus", "false") > 0);
    }

    @Test
    void knowsTheTodoFieldsFromTheThingifierModel() {
        Assertions.assertTrue(repository.hasFieldNamed("id"));
        Assertions.assertTrue(repository.hasFieldNamed("title"));
        Assertions.assertTrue(repository.hasFieldNamed("doneStatus"));
        Assertions.assertTrue(repository.hasFieldNamed("description"));
        Assertions.assertFalse(repository.hasFieldNamed("notATodoField"));
    }
}
