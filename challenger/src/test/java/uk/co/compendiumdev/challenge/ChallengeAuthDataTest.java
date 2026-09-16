package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengeAuthDataTest {

    @Test
    void canCreateAuthData() {

        final long timeNow = System.currentTimeMillis();

        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        Assertions.assertTrue(authData.getLastAccessed() >= timeNow);
        Assertions.assertTrue(authData.expiresAt() >= timeNow + 30000);
        Assertions.assertNotNull(authData.getXChallenger());
        Assertions.assertNotNull(authData.getXAuthToken());
        Assertions.assertNotNull(authData.getNote());
        Assertions.assertEquals("", authData.getNote());
        Assertions.assertFalse(authData.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
    }

    @Test
    void canOverrideGuid() {
        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        authData.setXChallengerGUID("bob");
        Assertions.assertEquals("bob", authData.getXChallenger());
    }

    @Test
    void canCreatePassAChallenge() {

        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        authData.pass(CHALLENGE.GET_CHALLENGES);

        Assertions.assertTrue(authData.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
    }

    @Test
    void countsUniquelyCompletedChallenges() {

        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        Assertions.assertEquals(0, authData.completedChallengeCount());

        authData.pass(CHALLENGE.GET_CHALLENGES);
        Assertions.assertEquals(1, authData.completedChallengeCount());

        authData.pass(CHALLENGE.GET_CHALLENGES);
        Assertions.assertEquals(1, authData.completedChallengeCount());

        authData.pass(CHALLENGE.GET_HEARTBEAT_204);
        Assertions.assertEquals(2, authData.completedChallengeCount());
    }

    @Test
    void canPassAllChallenges() {

        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        for (CHALLENGE challenge : CHALLENGE.values()) {
            authData.pass(challenge);
        }

        Assertions.assertTrue(authData.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
        Assertions.assertTrue(authData.statusOfChallenge(CHALLENGE.GET_HEARTBEAT_204));

        for (CHALLENGE challenge : CHALLENGE.values()) {
            Assertions.assertTrue(authData.statusOfChallenge(challenge));
        }
    }

    @Test
    void canMapLegacySecretTokenChallengeStatuses() {

        String legacyJson =
                """
                {
                  "xAuthToken": "4567023d-5fe0-4d46-a0fa-2fe717ae29fe",
                  "xChallenger": "2ce954c6-caa1-4299-85af-205a9d9f7867",
                  "secretNote": "",
                  "challengeStatus": {
                    "CREATE_SECRET_TOKEN_401": true,
                    "CREATE_SECRET_TOKEN_201": true
                  }
                }
                """;

        ChallengerAuthData legacyData = new Gson().fromJson(legacyJson, ChallengerAuthData.class);
        ChallengerAuthData restored =
                new ChallengerAuthData(
                                List.of(
                                        CHALLENGE.GET_SECRET_TOKEN_401,
                                        CHALLENGE.GET_SECRET_TOKEN_200))
                        .fromData(
                                legacyData,
                                List.of(
                                        CHALLENGE.GET_SECRET_TOKEN_401,
                                        CHALLENGE.GET_SECRET_TOKEN_200));

        Assertions.assertTrue(restored.statusOfChallenge(CHALLENGE.GET_SECRET_TOKEN_401));
        Assertions.assertTrue(restored.statusOfChallenge(CHALLENGE.GET_SECRET_TOKEN_200));
    }

    @Test
    void canNotSetNullNote() {
        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        Assertions.assertThrows(RuntimeException.class, () -> authData.setNote(null));
    }

    @Test
    void canSetEmptyNote() {
        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        authData.setNote("");
        Assertions.assertEquals("", authData.getNote());
    }

    @Test
    void noteTruncatedTo100Chars() {
        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        String morethanonehundred = stringOfLength(101);
        Assertions.assertEquals(101, morethanonehundred.length());

        authData.setNote(morethanonehundred);
        Assertions.assertEquals(100, authData.getNote().length());

        authData.setNote(stringOfLength(200));
        Assertions.assertEquals(100, authData.getNote().length());
    }

    private String stringOfLength(final int desiredLength) {
        String ofLength = "";
        while (ofLength.length() < desiredLength) {
            ofLength = ofLength + "a";
        }

        return ofLength;
    }

    @Test
    void noteAcceptedAt100CharsOrLess() {
        ChallengerAuthData authData = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        authData.setNote(stringOfLength(100));
        Assertions.assertEquals(100, authData.getNote().length());

        authData.setNote(stringOfLength(99));
        Assertions.assertEquals(99, authData.getNote().length());

        authData.setNote(stringOfLength(1));
        Assertions.assertEquals(1, authData.getNote().length());
    }
}
