package uk.co.compendiumdev.challenge.challengesrouting;

final class RouteExchange {
    private RouteExchange() {}

    static boolean ok(final ChallengerApiExchange exchange) {
        return exchange.statusCode() >= 200 && exchange.statusCode() < 300;
    }

    static String responseBodyAndErrors(final ChallengerApiExchange exchange) {
        String response = exchange.responseBody();
        for (String error : exchange.errorMessages()) {
            response += "\n" + error;
        }
        return response;
    }
}
