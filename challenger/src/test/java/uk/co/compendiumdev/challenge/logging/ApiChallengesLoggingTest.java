package uk.co.compendiumdev.challenge.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApiChallengesLoggingTest {

    private Logger logger;

    @AfterEach
    void removeHandlers() {
        if (logger != null) {
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
                handler.close();
            }
        }
    }

    @Test
    void infoRecordsAreWrittenToStandardOutOnly() {
        final CapturedLogging logging = configureCapturedLogger();

        logger.info("normal message");

        Assertions.assertTrue(logging.standardOut().contains("INFO"));
        Assertions.assertTrue(logging.standardOut().contains("normal message"));
        Assertions.assertEquals("", logging.standardError());
    }

    @Test
    void warningRecordsAreWrittenToStandardOutOnly() {
        final CapturedLogging logging = configureCapturedLogger();

        logger.warning("warning message");

        Assertions.assertTrue(logging.standardOut().contains("WARNING"));
        Assertions.assertTrue(logging.standardOut().contains("warning message"));
        Assertions.assertEquals("", logging.standardError());
    }

    @Test
    void severeRecordsAreWrittenToStandardErrorOnly() {
        final CapturedLogging logging = configureCapturedLogger();

        logger.severe("severe message");

        Assertions.assertEquals("", logging.standardOut());
        Assertions.assertTrue(logging.standardError().contains("SEVERE"));
        Assertions.assertTrue(logging.standardError().contains("severe message"));
    }

    @Test
    void repeatedConfigurationDoesNotDuplicateHandlers() {
        final CapturedLogging logging = configureCapturedLogger();

        ApiChallengesLogging.configureLogger(
                logger, logging.standardOutStream(), logging.standardErrorStream());
        logger.info("once only");

        Assertions.assertEquals(1, logger.getHandlers().length);
        Assertions.assertEquals(1, countOccurrences(logging.standardOut(), "once only"));
        Assertions.assertEquals("", logging.standardError());
    }

    private CapturedLogging configureCapturedLogger() {
        logger = Logger.getLogger("test.api.challenges.logging." + UUID.randomUUID());
        logger.setUseParentHandlers(false);
        final CapturedLogging logging = new CapturedLogging();
        ApiChallengesLogging.configureLogger(
                logger, logging.standardOutStream(), logging.standardErrorStream());
        return logging;
    }

    private int countOccurrences(final String text, final String value) {
        int count = 0;
        int index = text.indexOf(value);
        while (index != -1) {
            count++;
            index = text.indexOf(value, index + value.length());
        }
        return count;
    }

    private static final class CapturedLogging {
        private final ByteArrayOutputStream standardOut = new ByteArrayOutputStream();
        private final ByteArrayOutputStream standardError = new ByteArrayOutputStream();
        private final PrintStream standardOutStream =
                new PrintStream(standardOut, true, StandardCharsets.UTF_8);
        private final PrintStream standardErrorStream =
                new PrintStream(standardError, true, StandardCharsets.UTF_8);

        PrintStream standardOutStream() {
            return standardOutStream;
        }

        PrintStream standardErrorStream() {
            return standardErrorStream;
        }

        String standardOut() {
            standardOutStream.flush();
            return standardOut.toString(StandardCharsets.UTF_8);
        }

        String standardError() {
            standardErrorStream.flush();
            return standardError.toString(StandardCharsets.UTF_8);
        }
    }
}
