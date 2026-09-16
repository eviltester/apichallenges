package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.thingifier.Thingifier;

class TodoPaginationTest {
    private Thingifier thingifier;
    private TodoPagination pagination;

    @BeforeEach
    void createPagination() {
        thingifier = new ChallengeApiModel().get();
        TodoRepository repository = new TodoRepository(thingifier);
        pagination = new TodoPagination(repository, new TodoQueryFilters(repository));
    }

    @AfterEach
    void closeThingifier() {
        thingifier.close();
    }

    @Test
    void returnsMinusOneWhenNoLimitIsRequested() {
        Assertions.assertEquals(-1, pagination.expectedPageSize(new RouteChallengeTestExchange()));
    }

    @Test
    void calculatesExpectedLimitAndOffsetPageSizes() {
        Assertions.assertEquals(
                3,
                pagination.expectedPageSize(
                        new RouteChallengeTestExchange().withQuery("_limit=3")));
        Assertions.assertEquals(
                2,
                pagination.expectedPageSize(
                        new RouteChallengeTestExchange().withQuery("_limit=3&_offset=8")));
        Assertions.assertEquals(
                0,
                pagination.expectedPageSize(
                        new RouteChallengeTestExchange().withQuery("_limit=3&_offset=30")));
    }

    @Test
    void calculatesExpectedFilteredPageSizes() {
        Assertions.assertEquals(
                2,
                pagination.expectedPageSize(
                        new RouteChallengeTestExchange()
                                .withQuery("doneStatus=false&_limit=2&_offset=1")));
    }
}
