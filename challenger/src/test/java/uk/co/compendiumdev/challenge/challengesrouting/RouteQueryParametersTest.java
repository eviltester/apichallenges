package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RouteQueryParametersTest {

    @Test
    void recognisesRequestsWithoutQueryParameters() {
        Assertions.assertTrue(RouteQueryParameters.hasNoQuery(new RouteChallengeTestExchange()));
        Assertions.assertFalse(
                RouteQueryParameters.hasNoQuery(
                        new RouteChallengeTestExchange().withQuery("_limit=5")));
    }

    @Test
    void readsIntegerQueryValues() {
        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange().withQuery("_limit=5&_offset=not-a-number");

        Assertions.assertEquals(5, RouteQueryParameters.integerValue(exchange, "_limit"));
        Assertions.assertNull(RouteQueryParameters.integerValue(exchange, "_offset"));
        Assertions.assertNull(RouteQueryParameters.integerValue(exchange, "missing"));
    }

    @Test
    void comparesIntegerQueryValues() {
        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange().withQuery("_limit=21");

        Assertions.assertTrue(RouteQueryParameters.integerGreaterThan(exchange, "_limit", 20));
        Assertions.assertFalse(RouteQueryParameters.integerGreaterThan(exchange, "_limit", 21));
    }
}
