package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;

final class SecretRouteChallengeCompletion {
    private final Challengers challengers;

    SecretRouteChallengeCompletion(final Challengers challengers) {
        this.challengers = challengers;
    }

    void getSecretToken(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (!exchange.hasRequestHeader("Authorization")) {
            return;
        }
        if (exchange.requestHeader("Authorization").length() <= 10) {
            return;
        }
        if (exchange.statusCode() == 401) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_TOKEN_401);
        }
        if (exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_TOKEN_200);
        }
    }

    void getSecretNote(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (exchange.hasRequestHeader("X-AUTH-TOKEN")
                && exchange.requestHeader("X-AUTH-TOKEN").length() > 1
                && exchange.statusCode() == 403) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_403);
        }
        if (!exchange.hasRequestHeader("X-AUTH-TOKEN") && exchange.statusCode() == 401) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_401);
        }
        if (exchange.hasRequestHeader("X-AUTH-TOKEN") && exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_200);
        }
        if (exchange.hasRequestHeader("Authorization")
                && new BearerAuthHeaderParser(exchange.requestHeader("Authorization")).isValid()
                && exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_BEARER_200);
        }
    }

    void postSecretNote(final ChallengerApiExchange exchange, final ChallengerAuthData challenger) {
        if (!exchange.requestBody().contains("\"note\"")) {
            return;
        }
        if (exchange.hasRequestHeader("X-AUTH-TOKEN")
                && exchange.requestHeader("X-AUTH-TOKEN").length() > 1
                && exchange.statusCode() == 403) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_403);
        }
        if (!exchange.hasRequestHeader("X-AUTH-TOKEN") && exchange.statusCode() == 401) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_401);
        }
        if (exchange.hasRequestHeader("X-AUTH-TOKEN") && exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_200);
        }
        if (exchange.hasRequestHeader("Authorization")
                && new BearerAuthHeaderParser(exchange.requestHeader("Authorization")).isValid()
                && exchange.statusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_BEARER_200);
        }
    }
}
