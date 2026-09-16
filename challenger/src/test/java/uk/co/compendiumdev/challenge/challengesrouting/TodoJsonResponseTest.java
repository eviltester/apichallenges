package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TodoJsonResponseTest {

    @Test
    void emptyResponseIsReturnedForInvalidOrMissingTodoCollections() {
        Assertions.assertEquals(0, TodoJsonResponse.fromBody(null).size());
        Assertions.assertEquals(0, TodoJsonResponse.fromBody("").size());
        Assertions.assertEquals(0, TodoJsonResponse.fromBody("not json").size());
        Assertions.assertEquals(0, TodoJsonResponse.fromBody("{\"todo\":[]}").size());
        Assertions.assertEquals(0, TodoJsonResponse.fromBody("[{\"id\":1}]").size());
    }

    @Test
    void readsOnlyObjectEntriesFromTheTodosCollection() {
        TodoJsonResponse response =
                TodoJsonResponse.fromBody("{\"todos\":[{\"id\":2},\"ignore\",{\"id\":1}]}");

        Assertions.assertEquals(2, response.size());
        Assertions.assertTrue(response.idsAreDescending());
    }

    @Test
    void recognisesDoneStatusAcrossAllReturnedTodos() {
        TodoJsonResponse done =
                TodoJsonResponse.fromBody(
                        "{\"todos\":[{\"doneStatus\":true},{\"doneStatus\":true}]}");
        TodoJsonResponse notDone =
                TodoJsonResponse.fromBody(
                        "{\"todos\":[{\"doneStatus\":false},{\"doneStatus\":false}]}");
        TodoJsonResponse mixed =
                TodoJsonResponse.fromBody(
                        "{\"todos\":[{\"doneStatus\":true},{\"doneStatus\":false}]}");

        Assertions.assertTrue(done.allDoneStatusTrue());
        Assertions.assertFalse(done.allDoneStatusFalse());
        Assertions.assertTrue(notDone.allDoneStatusFalse());
        Assertions.assertFalse(notDone.allDoneStatusTrue());
        Assertions.assertFalse(mixed.allDoneStatusTrue());
        Assertions.assertFalse(mixed.allDoneStatusFalse());
    }

    @Test
    void recognisesIdAndDescriptionMatchers() {
        TodoJsonResponse response =
                TodoJsonResponse.fromBody(
                        "{\"todos\":["
                                + "{\"id\":3,\"description\":\"alpha fixture\"},"
                                + "{\"id\":4,\"description\":\"beta fixture\"}"
                                + "]}");

        Assertions.assertTrue(response.allIdsGreaterThan(2));
        Assertions.assertTrue(response.allIdsLessThan(5));
        Assertions.assertFalse(response.singleTodoHasId(3));
        Assertions.assertTrue(response.allDescriptionsMatch(Pattern.compile(".*fixture")));
        Assertions.assertFalse(response.allDescriptionsMatch(Pattern.compile("alpha.*")));
    }

    @Test
    void recognisesSingleTodoIdMatch() {
        TodoJsonResponse response =
                TodoJsonResponse.fromBody("{\"todos\":[{\"id\":7,\"description\":\"solo\"}]}");

        Assertions.assertTrue(response.singleTodoHasId(7));
        Assertions.assertFalse(response.singleTodoHasId(8));
    }
}
