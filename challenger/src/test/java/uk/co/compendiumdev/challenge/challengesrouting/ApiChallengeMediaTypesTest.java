package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiChallengeMediaTypesTest {

    @Test
    void requestContentTypeMatchingIgnoresParametersAndCase() {
        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange()
                        .withRequestHeader("Content-Type", "Application/JSON; charset=utf-8");

        Assertions.assertTrue(
                ApiChallengeMediaTypes.requestContentTypeIs(
                        exchange, ApiChallengeMediaTypes.APPLICATION_JSON));
    }

    @Test
    void acceptHeaderEqualsComparesOnlyTheMediaType() {
        RouteChallengeTestExchange exchange =
                new RouteChallengeTestExchange().withRequestHeader("Accept", "text/xml; q=0.8");

        Assertions.assertTrue(
                ApiChallengeMediaTypes.acceptHeaderEquals(
                        exchange, ApiChallengeMediaTypes.TEXT_XML));
    }

    @Test
    void todoXmlResponseTypeIncludesSupportedXmlRepresentations() {
        Assertions.assertTrue(
                ApiChallengeMediaTypes.isTodoXmlResponseType(
                        ApiChallengeMediaTypes.APPLICATION_XML));
        Assertions.assertTrue(
                ApiChallengeMediaTypes.isTodoXmlResponseType(ApiChallengeMediaTypes.TEXT_XML));
        Assertions.assertTrue(
                ApiChallengeMediaTypes.isTodoXmlResponseType(
                        ApiChallengeMediaTypes.TODO_VENDOR_XML));
        Assertions.assertFalse(
                ApiChallengeMediaTypes.isTodoXmlResponseType(
                        ApiChallengeMediaTypes.APPLICATION_JSON));
    }
}
