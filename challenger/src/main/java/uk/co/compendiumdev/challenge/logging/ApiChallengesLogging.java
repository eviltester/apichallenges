package uk.co.compendiumdev.challenge.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ApiChallengesLogging {

    private ApiChallengesLogging() {}

    public static void configureConsoleLogging() {
        configureLogger(LogManager.getLogManager().getLogger(""), System.out, System.err);
    }

    static void configureLogger(
            final Logger logger, final PrintStream standardOut, final PrintStream standardError) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
            handler.close();
        }
        logger.addHandler(new ApiChallengesConsoleHandler(standardOut, standardError));
        logger.setLevel(Level.INFO);
    }

    static final class ApiChallengesConsoleHandler extends Handler {
        private final PrintStream standardOut;
        private final PrintStream standardError;

        ApiChallengesConsoleHandler(
                final PrintStream standardOut, final PrintStream standardError) {
            this.standardOut = standardOut;
            this.standardError = standardError;
            setLevel(Level.ALL);
            setFormatter(new ApiChallengesLogFormatter());
        }

        @Override
        public void publish(final LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }

            final PrintStream stream =
                    record.getLevel().intValue() >= Level.SEVERE.intValue()
                            ? standardError
                            : standardOut;
            synchronized (stream) {
                stream.print(getFormatter().format(record));
                stream.flush();
            }
        }

        @Override
        public void flush() {
            standardOut.flush();
            standardError.flush();
        }

        @Override
        public void close() {
            flush();
        }
    }

    static final class ApiChallengesLogFormatter extends Formatter {

        @Override
        public String format(final LogRecord record) {
            final StringBuilder output = new StringBuilder();
            output.append(Instant.ofEpochMilli(record.getMillis()));
            output.append(" ");
            output.append(record.getLevel().getName());
            output.append(" ");
            output.append(record.getLoggerName());
            output.append(" - ");
            output.append(formatMessage(record));
            output.append(System.lineSeparator());
            if (record.getThrown() != null) {
                final StringWriter stackTrace = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(stackTrace));
                output.append(stackTrace);
            }
            return output.toString();
        }
    }
}
