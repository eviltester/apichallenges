package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.thingifier.Thingifier;

class TodoSortCriteriaTest {

    @Test
    void blankOrSymbolOnlyValuesAreEmpty() {
        Assertions.assertTrue(TodoSortCriteria.from((String) null).isEmpty());
        Assertions.assertTrue(TodoSortCriteria.from("").isEmpty());
        Assertions.assertTrue(TodoSortCriteria.from("+").isEmpty());
        Assertions.assertTrue(TodoSortCriteria.from(" , ").isEmpty());
    }

    @Test
    void parsesAscendingDescendingAndMultipleSorts() {
        Assertions.assertTrue(TodoSortCriteria.from("title").isSingleFieldAscending());
        Assertions.assertTrue(TodoSortCriteria.from("+title").isSingleFieldAscending());
        Assertions.assertTrue(TodoSortCriteria.from("-title").isSingleFieldDescending());
        Assertions.assertTrue(TodoSortCriteria.from("doneStatus,-id").isMultipleFields());
        Assertions.assertTrue(TodoSortCriteria.from("-id").isDescendingId());
        Assertions.assertFalse(TodoSortCriteria.from("id").isDescendingId());
    }

    @Test
    void canBeReadFromAnExchange() {
        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange().withQuery("_sortBy=-id");

        Assertions.assertTrue(TodoSortCriteria.from(exchange).isDescendingId());
    }

    @Test
    void validatesSortFieldsAgainstTheTodoModel() {
        Thingifier thingifier = new ChallengeApiModel().get();
        try {
            TodoRepository repository = new TodoRepository(thingifier);

            Assertions.assertTrue(TodoSortCriteria.from("title,-id").hasOnlyTodoFields(repository));
            Assertions.assertFalse(
                    TodoSortCriteria.from("title,unknown").hasOnlyTodoFields(repository));
            Assertions.assertFalse(TodoSortCriteria.from("").hasOnlyTodoFields(repository));
        } finally {
            thingifier.close();
        }
    }
}
