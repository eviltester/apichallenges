package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonRequestBodyTest {

    @Test
    void detectsFieldInJsonObjectBody() {
        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange().withRequestBody("{\"id\":1,\"title\":\"demo\"}");

        Assertions.assertTrue(JsonRequestBody.hasField(exchange, "id"));
        Assertions.assertFalse(JsonRequestBody.hasField(exchange, "doneStatus"));
    }

    @Test
    void treatsInvalidOrNonObjectJsonAsMissingTheField() {
        Assertions.assertFalse(
                JsonRequestBody.hasField(
                        new RouteChallengeTestExchange().withRequestBody("not json"), "id"));
        Assertions.assertFalse(
                JsonRequestBody.hasField(
                        new RouteChallengeTestExchange().withRequestBody("[{\"id\":1}]"), "id"));
        Assertions.assertFalse(JsonRequestBody.hasField(new RouteChallengeTestExchange(), "id"));
    }
}
