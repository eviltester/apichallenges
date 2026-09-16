package uk.co.compendiumdev.challenger.http.completechallenges;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;

public class CompleteAllChallengesSingleUserTest extends ChallengeCompleteTest {

    @Override
    public boolean getIsSinglePlayerMode() {
        return true;
    }

    @Override
    public int getNumberOfChallengesToFail() {
        // POST to retrieve session only works in multi-user - but is excluded in challenges
        // GET to retrieve session only works in multi-user - but is excluded in challenges
        // PUT new restored challenger progress only works in multi-user - but is excluded in
        // challenges
        return 0;
    }

    @Test
    void canSimulateCreateChallenger() throws InterruptedException {

        final HttpResponseDetails response = http.post("/challenger", "");

        Assertions.assertEquals(Challengers.SINGLE_PLAYER_GUID, response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals(201, response.statusCode);
    }

    @Test
    void getChallengesHasDifferentLocationRouteThanMultiPlayer() throws InterruptedException {

        final HttpResponseDetails response = http.get("/challenges");

        Assertions.assertEquals(
                "/challenger/" + Challengers.SINGLE_PLAYER_GUID, response.getHeader("Location"));
    }

    @Test
    void apiDatabaseRestoreForUnknownUuidReturnsJson404() {
        final HttpResponseDetails response =
                http.send(
                        "/api/challenger/database/11111111-2222-4333-8444-555555555555",
                        "put",
                        Map.of("Accept", "*/*"),
                        "");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(response.getHeader("Content-Type").contains("application/json"));
        Assertions.assertTrue(response.body.contains("\"errorMessages\""));
    }
}
