package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;

class TodoQueryFiltersTest {
    private Thingifier thingifier;
    private TodoRepository repository;
    private TodoQueryFilters filters;

    @BeforeEach
    void createFilters() {
        thingifier = new ChallengeApiModel().get();
        repository = new TodoRepository(thingifier);
        filters = new TodoQueryFilters(repository);
    }

    @AfterEach
    void closeThingifier() {
        thingifier.close();
    }

    @Test
    void detectsTodoFiltersInRawAndParsedQueryParameters() {
        Assertions.assertTrue(
                filters.hasTodoFilter(
                        new RouteChallengeTestExchange().withQuery("doneStatus=false")));
        Assertions.assertTrue(
                filters.hasTodoFilter(new RouteChallengeTestExchange().withQuery("id%3E1")));
        Assertions.assertFalse(
                filters.hasTodoFilter(new RouteChallengeTestExchange().withQuery("_limit=5")));
    }

    @Test
    void recognisesIdGreaterThanAndLessThanFilteredResponses() {
        TodoJsonResponse greaterThanResponse =
                TodoJsonResponse.fromBody("{\"todos\":[{\"id\":2},{\"id\":3}]}");
        TodoJsonResponse lessThanResponse =
                TodoJsonResponse.fromBody("{\"todos\":[{\"id\":1},{\"id\":2}]}");

        Assertions.assertTrue(
                filters.idFilterResponseIsProperSubset(
                        new RouteChallengeTestExchange().withQuery("id%3E1"),
                        greaterThanResponse,
                        FilterOperation.GREATER_THAN));
        Assertions.assertTrue(
                filters.idFilterResponseIsProperSubset(
                        new RouteChallengeTestExchange().withQuery("id%3C3"),
                        lessThanResponse,
                        FilterOperation.LESS_THAN));
        Assertions.assertFalse(
                filters.idFilterResponseIsProperSubset(
                        new RouteChallengeTestExchange().withQuery("id%3E1"),
                        lessThanResponse,
                        FilterOperation.GREATER_THAN));
    }

    @Test
    void recognisesSingleIdRegexAndWildcardFilteredResponses() {
        TodoJsonResponse singleId = TodoJsonResponse.fromBody("{\"todos\":[{\"id\":1}]}");
        TodoJsonResponse descriptions =
                TodoJsonResponse.fromBody(
                        "{\"todos\":["
                                + "{\"description\":\"alpha fixture\"},"
                                + "{\"description\":\"beta fixture\"}"
                                + "]}");

        Assertions.assertTrue(
                filters.idSingleResultFilterResponseMatches(
                        new RouteChallengeTestExchange().withQuery("id=1"), singleId));
        Assertions.assertTrue(
                filters.descriptionRegexFilterResponseMatches(
                        new RouteChallengeTestExchange().withQuery("description%7E=.%2Afixture"),
                        descriptions));
        Assertions.assertTrue(
                filters.descriptionWildcardFilterResponseMatches(
                        new RouteChallengeTestExchange().withQuery("description%2A=%2Afixture"),
                        descriptions));
    }

    @Test
    void countsMatchingFiltersAndDetectsMixedDoneStatuses() {
        addTodo("done filter fixture", "true");

        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange().withQuery("doneStatus=false");

        Assertions.assertTrue(filters.countMatchingQueryFilter(exchange, "doneStatus") > 0);
        Assertions.assertTrue(filters.hasDoneAndNotDoneTodos(exchange));
    }

    private void addTodo(final String title, final String doneStatus) {
        final EntityDefinition todo = thingifier.getDefinitionNamed("todo");
        thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(todo)
                                .withField("title", title)
                                .withField("doneStatus", doneStatus)
                                .withField("description", ""));
    }
}
