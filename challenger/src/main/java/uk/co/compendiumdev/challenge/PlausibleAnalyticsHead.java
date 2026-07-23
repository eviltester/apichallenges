package uk.co.compendiumdev.challenge;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlausibleAnalyticsHead {

    public static final String ENABLED_ENV = "APICHALLENGES_PLAUSIBLE_ENABLED";
    public static final String SCRIPT_URL_ENV = "APICHALLENGES_PLAUSIBLE_SCRIPT_URL";
    public static final String DEFAULT_SCRIPT_URL =
            "https://plausible.io/js/pa-vy94qfz2VidfC85rOuttW.js";

    private static final Logger LOGGER = LoggerFactory.getLogger(PlausibleAnalyticsHead.class);

    private final Map<String, String> environment;

    public PlausibleAnalyticsHead(final Map<String, String> environment) {
        this.environment = environment;
    }

    public static PlausibleAnalyticsHead fromEnvironment() {
        return new PlausibleAnalyticsHead(System.getenv());
    }

    public String asHtml() {
        if (!"true".equalsIgnoreCase(envValue(ENABLED_ENV))) {
            return "";
        }

        String scriptUrl = envValue(SCRIPT_URL_ENV);
        if (scriptUrl.isEmpty()) {
            scriptUrl = DEFAULT_SCRIPT_URL;
        }

        if (!isSafeScriptUrl(scriptUrl)) {
            LOGGER.warn(
                    "Ignoring unsafe Plausible script URL from {} and using default",
                    SCRIPT_URL_ENV);
            scriptUrl = DEFAULT_SCRIPT_URL;
        }

        return """
        <!-- Privacy-friendly analytics by Plausible -->
        <script async src="%s"></script>
        <script>
          window.plausible=window.plausible||function(){(plausible.q=plausible.q||[]).push(arguments)},plausible.init=plausible.init||function(i){plausible.o=i||{}};
          plausible.init()
        </script>
        """
                .formatted(scriptUrl);
    }

    public static boolean isSafeScriptUrl(final String scriptUrl) {
        if (scriptUrl == null) {
            return false;
        }

        final String trimmedScriptUrl = scriptUrl.trim();
        if (trimmedScriptUrl.isEmpty()) {
            return false;
        }

        if (trimmedScriptUrl.contains("\"")
                || trimmedScriptUrl.contains("'")
                || trimmedScriptUrl.contains("<")
                || trimmedScriptUrl.contains(">")
                || trimmedScriptUrl.matches(".*\\s+.*")) {
            return false;
        }

        try {
            final URI uri = new URI(trimmedScriptUrl);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private String envValue(final String name) {
        final String value = environment.get(name);
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
