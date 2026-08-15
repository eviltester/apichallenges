package uk.co.compendiumdev.challenger.restassured._02_first_real_challenge;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Challenge;
import uk.co.compendiumdev.challenger.payloads.Challenges;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C002GetChallengesTest extends RestAssuredBaseTest {

    @Test
    void canGetChallenges() {

        // challenges should be set as soon as we get it - no need for multiple calls
        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .accept("application/json")
                        .get(apiPath("/challenges"))
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .response();

        // challenge should be met
        final Challenges challenges = response.body().as(Challenges.class);
        final Challenge challenge =
                ChallengesStatus.getChallengeNamed(challenges.challenges, "GET /challenges (200)");

        Assertions.assertNotNull(challenge);
        Assertions.assertTrue(challenge.status);
    }
}
