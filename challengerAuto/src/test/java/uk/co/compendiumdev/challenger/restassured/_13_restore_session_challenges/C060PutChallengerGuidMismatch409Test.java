package uk.co.compendiumdev.challenger.restassured._13_restore_session_challenges;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Challenger;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C060PutChallengerGuidMismatch409Test extends RestAssuredBaseTest {

    @Test
    public void canPutChallengerSessionWithMismatchedGuidAndReceive409() {

        Response cResponse =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .accept("application/json")
                        .get(apiPath("/challenger/" + xChallenger))
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .and()
                        .extract()
                        .response();

        Challenger challengerResponse =
                new Gson().fromJson(cResponse.body().asString(), Challenger.class);

        Response mismatchResponse =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .accept("application/json")
                        .contentType(ContentType.JSON)
                        .body(challengerResponse)
                        .put(apiPath("/challenger/" + UUID.randomUUID()))
                        .then()
                        .statusCode(409)
                        .and()
                        .extract()
                        .response();

        Assertions.assertTrue(
                mismatchResponse
                        .body()
                        .asString()
                        .contains("URL GUID does not match payload X-CHALLENGER"));

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(
                statuses.getChallengeNamed("PUT /challenger/guid (409) mismatch").status,
                "challenge not passed");
    }
}
