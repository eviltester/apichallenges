package uk.co.compendiumdev.challenge.challengesrouting;

final class ApiChallengeRoutePath {

    private ApiChallengeRoutePath() {}

    static String withPrefix(final String pathPrefix, final String path) {
        String normalizedPrefix = pathPrefix == null ? "" : pathPrefix.trim();
        if (normalizedPrefix.endsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }

        if (normalizedPrefix.isEmpty()) {
            return path;
        }

        return normalizedPrefix + path;
    }
}
