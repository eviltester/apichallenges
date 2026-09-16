package uk.co.compendiumdev.challenge.challengesrouting;

final class RouteQueryParameters {
    private RouteQueryParameters() {}

    static boolean hasNoQuery(final ChallengerApiExchange exchange) {
        return exchange.queryParams() == null || exchange.queryParams().size() == 0;
    }

    static Integer integerValue(final ChallengerApiExchange exchange, final String paramName) {
        if (!exchange.rawQueryParams().containsKey(paramName)) {
            return null;
        }

        try {
            return Integer.parseInt(exchange.rawQueryParams().get(paramName));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static boolean integerGreaterThan(
            final ChallengerApiExchange exchange, final String paramName, final int minimumValue) {
        Integer actualValue = integerValue(exchange, paramName);
        return actualValue != null && actualValue > minimumValue;
    }
}
