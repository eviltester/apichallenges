package uk.co.compendiumdev.challenge.challengehooks;

import java.util.List;

final class ApiChallengeHookPath {

    private static final List<String> VALID_ENDPOINT_PREFIXES =
            List.of("challenger", "todo", "todos", "challenges", "heartbeat", "secret");

    private ApiChallengeHookPath() {}

    static String normalize(final String rawPath) {
        if (rawPath == null) {
            return "";
        }

        String path = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
        if (path.equals("api")) {
            return "";
        }
        if (path.startsWith("api/")) {
            return path.substring("api/".length());
        }
        return path;
    }

    static boolean isApiChallengesEndpoint(final String path) {
        final String normalizedPath = normalize(path);
        if (normalizedPath.isEmpty()) {
            return false;
        }

        final String[] pathSegments = normalizedPath.split("/");
        return VALID_ENDPOINT_PREFIXES.contains(pathSegments[0]);
    }
}
