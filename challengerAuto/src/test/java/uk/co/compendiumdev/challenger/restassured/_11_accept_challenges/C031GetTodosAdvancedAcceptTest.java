package uk.co.compendiumdev.challenger.restassured._11_accept_challenges;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Challenge;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C031GetTodosAdvancedAcceptTest extends RestAssuredBaseTest {

    @Test
    void canPreferXmlWithAcceptQualityValues() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/json;q=0.5, application/xml;q=1")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "application/xml");
        assertChallengeCompleted("GET /todos (200) q XML preferred");
    }

    @Test
    void canPreferJsonWithAcceptQualityValues() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/xml;q=0.5, application/json;q=1")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "application/json");
        assertChallengeCompleted("GET /todos (200) q JSON preferred");
    }

    @Test
    void canRejectAllSupportedAcceptTypesWithZeroQualityValues() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/json;q=0, application/xml;q=0")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(406)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "application/json");
        assertChallengeCompleted("GET /todos (406) q rejects all");
    }

    @Test
    void canRejectUnsupportedStructuredJsonAcceptType() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/*+json")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(406)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "application/json");
        assertChallengeCompleted("GET /todos (406) unsupported +json");
    }

    @Test
    void canRequestTodosAsTextXml() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "text/xml")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "text/xml");
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        assertChallengeCompleted("GET /todos (200) text/xml");
    }

    @Test
    void canRequestTodosAsVendorXml() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/vnd.apichallenges.todo+xml")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "application/vnd.apichallenges.todo+xml");
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        assertChallengeCompleted("GET /todos (200) vendor XML");
    }

    @Test
    void canRequestTodosWithStructuredXmlWildcard() {
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .header("Accept", "application/*+xml")
                        .get(apiPath("/todos"))
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        assertContentTypeStartsWith(response, "application/todo+xml");
        Assertions.assertTrue(response.body().asString().contains("<todos>"));
        assertChallengeCompleted("GET /todos (200) structured XML wildcard");
    }

    private void assertChallengeCompleted(final String name) {
        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Challenge challenge = statuses.getChallengeNamed(name);
        Assertions.assertNotNull(challenge, "Expected challenge " + name);
        Assertions.assertTrue(challenge.status, name + " should be complete");
    }

    private void assertContentTypeStartsWith(final Response response, final String expectedType) {
        Assertions.assertTrue(
                response.getHeader("Content-Type").startsWith(expectedType),
                "Expected Content-Type to start with " + expectedType);
    }
}
