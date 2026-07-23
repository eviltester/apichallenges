package uk.co.compendiumdev.challenger.restassured._15_status_code_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.ErrorMessages;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C061GetChallengesTooLongXChallenger431Test extends RestAssuredBaseTest {

    @Test
    public void canGetChallengesWithTooLongXChallengerHeaderAndReceive431() {

        Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger + "x".repeat(65))
                        .accept("application/json")
                        .get(apiPath("/challenges"))
                        .then()
                        .statusCode(431)
                        .contentType(ContentType.JSON)
                        .and()
                        .extract()
                        .response();

        ErrorMessages messages = response.as(ErrorMessages.class);
        Assertions.assertTrue(
                messages.errorMessages.contains(
                        "X-CHALLENGER header is too large, maximum allowed is 100 characters"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("GET /challenges (431) X-CHALLENGER too long").status,
                "challenge not passed");
    }
}
