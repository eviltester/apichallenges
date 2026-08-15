package uk.co.compendiumdev.challenger.restassured.api;

import static uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest.xChallenger;

import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.challenger.payloads.Challenge;
import uk.co.compendiumdev.challenger.payloads.Challenges;
import uk.co.compendiumdev.serverstart.Environment;

public class ChallengesStatus {

    private Challenges challengeStatuses;

    public ChallengesStatus() {
        challengeStatuses = new Challenges();
        challengeStatuses.challenges = new ArrayList<>();
    }

    public List<Challenge> get() {

        getFor(xChallenger);

        return challengeStatuses.challenges;
    }

    public List<Challenge> getFor(String aChallenger) {

        challengeStatuses =
                RestAssured.given()
                        .header("X-CHALLENGER", aChallenger)
                        .accept("application/json")
                        .get(Environment.getEnv("/challenges"))
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .response()
                        .as(Challenges.class);

        return challengeStatuses.challenges;
    }

    public Challenge getChallengeNamed(String name) {
        return getChallengeNamed(challengeStatuses.challenges, name);
    }

    public static Challenge getChallengeNamed(final List<Challenge> challenges, final String name) {
        Challenge challengeByName = findChallengeNamed(challenges, name);
        if (challengeByName != null) {
            return challengeByName;
        }

        return findChallengeNamed(challenges, canonicalApiChallengeName(name));
    }

    private static Challenge findChallengeNamed(
            final List<Challenge> challenges, final String name) {
        if (challenges == null) {
            return null;
        }

        for (Challenge challenge : challenges) {
            if (challenge.name.equals(name)) {
                return challenge;
            }
        }
        return null;
    }

    private static String canonicalApiChallengeName(final String name) {
        if (name == null || name.contains(" /api/")) {
            return name;
        }

        return name.replaceFirst(
                " /(todos|todo|challenger|challenges|heartbeat|secret)(?=$|[ /{(])", " /api/$1");
    }
}
