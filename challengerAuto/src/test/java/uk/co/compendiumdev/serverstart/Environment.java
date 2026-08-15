package uk.co.compendiumdev.serverstart;

public class Environment {

    public static String getEnv(String urlPath) {
        return getBaseUri() + routePath(urlPath);
    }

    public static String routePath(final String urlPath) {
        return ChallengerAutoRuntime.currentConfig().routePath(urlPath);
    }

    public static String getBaseUri() {
        return ChallengerAutoRuntime.current().getBaseUrl();
    }

    public static boolean shouldRunFullSuite() {
        return ChallengerAutoRuntime.currentConfig().shouldRunFullSuite();
    }

    public static String fullSuiteSkipReason() {
        return ChallengerAutoRuntime.currentConfig().fullSuiteSkipReason();
    }

    public static void useConfiguration(final ChallengerAutoConfig config) {
        ChallengerAutoRuntime.useConfiguration(config);
    }

    public static void reset() {
        ChallengerAutoRuntime.reset();
    }
}
