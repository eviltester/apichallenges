package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;

class SecretRouteChallengeCompletionTest {

    @Test
    void getSecretTokenRequiresAuthorizationHeaderAndCompletesStatusChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            SecretRouteChallengeCompletion completion =
                    new SecretRouteChallengeCompletion(fixture.challengers);

            completion.getSecretToken(fixture.exchange().withStatusCode(401), fixture.challenger);
            completion.getSecretToken(
                    fixture.exchange()
                            .withStatusCode(401)
                            .withRequestHeader("Authorization", "short"),
                    fixture.challenger);
            Assertions.assertFalse(fixture.completed(CHALLENGE.GET_SECRET_TOKEN_401));

            completion.getSecretToken(
                    fixture.exchange()
                            .withStatusCode(401)
                            .withRequestHeader("Authorization", "basic invalid"),
                    fixture.challenger);
            completion.getSecretToken(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Authorization", "basic valid"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_SECRET_TOKEN_401));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_SECRET_TOKEN_200));
        }
    }

    @Test
    void getSecretNoteCompletesApiKeyAndBearerStatusChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            SecretRouteChallengeCompletion completion =
                    new SecretRouteChallengeCompletion(fixture.challengers);

            completion.getSecretNote(
                    fixture.exchange()
                            .withStatusCode(403)
                            .withRequestHeader("X-AUTH-TOKEN", "wrong"),
                    fixture.challenger);
            completion.getSecretNote(fixture.exchange().withStatusCode(401), fixture.challenger);
            completion.getSecretNote(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("X-AUTH-TOKEN", "valid"),
                    fixture.challenger);
            completion.getSecretNote(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Authorization", "Bearer token"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_SECRET_NOTE_403));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_SECRET_NOTE_401));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_SECRET_NOTE_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.GET_SECRET_NOTE_BEARER_200));
        }
    }

    @Test
    void postSecretNoteRequiresNoteFieldAndCompletesApiKeyAndBearerStatusChallenges() {
        try (RouteCompletionTestFixture fixture = new RouteCompletionTestFixture()) {
            SecretRouteChallengeCompletion completion =
                    new SecretRouteChallengeCompletion(fixture.challengers);

            completion.postSecretNote(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("X-AUTH-TOKEN", "valid")
                            .withRequestBody("{\"other\":\"field\"}"),
                    fixture.challenger);
            Assertions.assertFalse(fixture.completed(CHALLENGE.POST_SECRET_NOTE_200));

            completion.postSecretNote(
                    fixture.exchange()
                            .withStatusCode(403)
                            .withRequestHeader("X-AUTH-TOKEN", "wrong")
                            .withRequestBody("{\"note\":\"updated\"}"),
                    fixture.challenger);
            completion.postSecretNote(
                    fixture.exchange()
                            .withStatusCode(401)
                            .withRequestBody("{\"note\":\"updated\"}"),
                    fixture.challenger);
            completion.postSecretNote(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("X-AUTH-TOKEN", "valid")
                            .withRequestBody("{\"note\":\"updated\"}"),
                    fixture.challenger);
            completion.postSecretNote(
                    fixture.exchange()
                            .withStatusCode(200)
                            .withRequestHeader("Authorization", "Bearer token")
                            .withRequestBody("{\"note\":\"updated\"}"),
                    fixture.challenger);

            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_SECRET_NOTE_403));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_SECRET_NOTE_401));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_SECRET_NOTE_200));
            Assertions.assertTrue(fixture.completed(CHALLENGE.POST_SECRET_NOTE_BEARER_200));
        }
    }
}
