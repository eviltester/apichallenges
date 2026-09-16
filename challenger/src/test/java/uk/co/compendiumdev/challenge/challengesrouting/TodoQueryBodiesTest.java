package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TodoQueryBodiesTest {

    @Test
    void formBodyMatchesDoneStatusTrueFilter() {
        Assertions.assertTrue(
                TodoQueryBodies.formBodyContainsDoneStatusTrue(
                        new RouteChallengeTestExchange().withRequestBody("doneStatus=true")));
        Assertions.assertTrue(
                TodoQueryBodies.formBodyContainsDoneStatusTrue(
                        new RouteChallengeTestExchange().withRequestBody("doneStatus=True")));
        Assertions.assertFalse(
                TodoQueryBodies.formBodyContainsDoneStatusTrue(
                        new RouteChallengeTestExchange().withRequestBody("doneStatus=false")));
        Assertions.assertFalse(
                TodoQueryBodies.formBodyContainsDoneStatusTrue(
                        new RouteChallengeTestExchange().withRequestBody("title=true")));
    }

    @Test
    void jsonPathBodyMatchesDoneStatusTrueFilter() {
        Assertions.assertTrue(
                TodoQueryBodies.jsonPathBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange()
                                .withRequestBody("$[?(@.doneStatus == true)]")));
        Assertions.assertTrue(
                TodoQueryBodies.jsonPathBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange()
                                .withRequestBody("$[?(@.doneStatus=true)]")));
        Assertions.assertFalse(
                TodoQueryBodies.jsonPathBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange()
                                .withRequestBody("$[?(@.doneStatus == false)]")));
    }

    @Test
    void structuredJsonBodyMatchesDoneStatusTrueFilter() {
        Assertions.assertTrue(
                TodoQueryBodies.structuredJsonBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange()
                                .withRequestBody("{\"filter\":{\"doneStatus\":true}}")));
        Assertions.assertFalse(
                TodoQueryBodies.structuredJsonBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange()
                                .withRequestBody("{\"filter\":{\"doneStatus\":false}}")));
        Assertions.assertFalse(
                TodoQueryBodies.structuredJsonBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange().withRequestBody("{\"filter\":[]}")));
        Assertions.assertFalse(
                TodoQueryBodies.structuredJsonBodyTargetsDoneStatusTrue(
                        new RouteChallengeTestExchange().withRequestBody("not json")));
    }
}
