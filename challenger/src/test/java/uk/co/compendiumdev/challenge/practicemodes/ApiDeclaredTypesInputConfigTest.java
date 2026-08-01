package uk.co.compendiumdev.challenge.practicemodes;

import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.MANDATORY;
import static uk.co.compendiumdev.thingifier.apiconfig.PutIdentifierPolicy.OPTIONAL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartThingifier;
import uk.co.compendiumdev.challenge.practicemodes.simpleapi.SimpleApiRoutes;
import uk.co.compendiumdev.challenge.practicemodes.simulation.SimulationRoutes;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

class ApiDeclaredTypesInputConfigTest {

    @Test
    void apiChallengesEnforcesDeclaredTypesInInput() {
        Assertions.assertTrue(
                new ChallengeApiModel().get().apiConfig().willApiEnforceDeclaredTypesInInput());
    }

    @Test
    void simpleApiEnforcesDeclaredTypesInInput() {
        Assertions.assertTrue(
                new SimpleApiRoutes(new DefaultGUIHTML())
                        .simplethings
                        .apiConfig()
                        .willApiEnforceDeclaredTypesInInput());
    }

    @Test
    void simulatorEnforcesDeclaredTypesInInput() {
        SimulationRoutes routes = new SimulationRoutes(new DefaultGUIHTML());

        try {
            routes.setUpData();

            Assertions.assertTrue(
                    routes.simulation.apiConfig().willApiEnforceDeclaredTypesInInput());
        } finally {
            routes.close();
        }
    }

    @Test
    void buggyApiDoesNotEnforceDeclaredTypesInInput() {
        Assertions.assertFalse(
                new ShoppingCartThingifier()
                        .get()
                        .apiConfig()
                        .willApiEnforceDeclaredTypesInInput());
    }

    @Test
    void apiChallengesAllowsPutIdentifiersInUriOrPayload() {
        Assertions.assertEquals(
                OPTIONAL,
                new ChallengeApiModel()
                        .get()
                        .apiConfig()
                        .writeMethods()
                        .entities()
                        .putIdentifierInUri());
        Assertions.assertEquals(
                OPTIONAL,
                new ChallengeApiModel()
                        .get()
                        .apiConfig()
                        .writeMethods()
                        .entities()
                        .putIdentifierInPayload());
    }

    @Test
    void simpleApiRequiresPutIdentifiersInUriAndAllowsThemInPayload() {
        Assertions.assertEquals(
                MANDATORY,
                new SimpleApiRoutes(new DefaultGUIHTML())
                        .simplethings
                        .apiConfig()
                        .writeMethods()
                        .entities()
                        .putIdentifierInUri());
        Assertions.assertEquals(
                OPTIONAL,
                new SimpleApiRoutes(new DefaultGUIHTML())
                        .simplethings
                        .apiConfig()
                        .writeMethods()
                        .entities()
                        .putIdentifierInPayload());
    }

    @Test
    void simulatorRequiresPutIdentifiersInUriAndAllowsThemInPayload() {
        SimulationRoutes routes = new SimulationRoutes(new DefaultGUIHTML());

        try {
            routes.setUpData();

            Assertions.assertEquals(
                    MANDATORY,
                    routes.simulation.apiConfig().writeMethods().entities().putIdentifierInUri());
            Assertions.assertEquals(
                    OPTIONAL,
                    routes.simulation
                            .apiConfig()
                            .writeMethods()
                            .entities()
                            .putIdentifierInPayload());
        } finally {
            routes.close();
        }
    }
}
