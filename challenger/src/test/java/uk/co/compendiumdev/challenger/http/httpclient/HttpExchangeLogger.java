package uk.co.compendiumdev.challenger.http.httpclient;

import java.net.URL;
import org.slf4j.Logger;

final class HttpExchangeLogger {

    private static final String TRACE_PROPERTY = "apichallenges.test.http.trace";
    private static final String TRACE_ENV = "APICHALLENGES_TEST_HTTP_TRACE";

    private HttpExchangeLogger() {}

    static void summary(
            final Logger logger,
            final String method,
            final URL url,
            final int statusCode,
            final String responseBody) {
        logger.info(
                "{} {} -> {} ({} chars)",
                method,
                url,
                statusCode,
                responseBody == null ? 0 : responseBody.length());
    }

    static void detail(final Logger logger, final String message) {
        if (traceEnabled()) {
            logger.info(message);
        } else {
            logger.debug(message);
        }
    }

    static boolean traceEnabled() {
        final String envValue = System.getenv(TRACE_ENV);
        return Boolean.getBoolean(TRACE_PROPERTY) || Boolean.parseBoolean(envValue);
    }
}
