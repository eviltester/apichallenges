package uk.co.compendiumdev.robodepot;

public final class RoboDepotTickResult {

    private final int statusCode;
    private final int retryAfterSeconds;
    private final int tick;
    private final int ticksAdvanced;
    private final int jobsCompleted;
    private final int jobsBlocked;
    private final int robotsMoved;
    private final int stockAdjusted;
    private final String errorMessage;

    private RoboDepotTickResult(
            final int statusCode,
            final int retryAfterSeconds,
            final int tick,
            final int ticksAdvanced,
            final int jobsCompleted,
            final int jobsBlocked,
            final int robotsMoved,
            final int stockAdjusted,
            final String errorMessage) {
        this.statusCode = statusCode;
        this.retryAfterSeconds = retryAfterSeconds;
        this.tick = tick;
        this.ticksAdvanced = ticksAdvanced;
        this.jobsCompleted = jobsCompleted;
        this.jobsBlocked = jobsBlocked;
        this.robotsMoved = robotsMoved;
        this.stockAdjusted = stockAdjusted;
        this.errorMessage = errorMessage;
    }

    static RoboDepotTickResult advanced(final int tick, final TickCounters counters) {
        return new RoboDepotTickResult(
                200,
                0,
                tick,
                1,
                counters.jobsCompleted,
                counters.jobsBlocked,
                counters.robotsMoved,
                counters.stockAdjusted,
                "");
    }

    static RoboDepotTickResult rateLimited(final int retryAfterSeconds) {
        return new RoboDepotTickResult(
                429,
                retryAfterSeconds,
                0,
                0,
                0,
                0,
                0,
                0,
                "RoboDepot tick can only happen every 20 seconds");
    }

    static RoboDepotTickResult error(final String message) {
        return new RoboDepotTickResult(500, 0, 0, 0, 0, 0, 0, 0, message);
    }

    public int statusCode() {
        return statusCode;
    }

    public int retryAfterSeconds() {
        return retryAfterSeconds;
    }

    public boolean isRateLimited() {
        return statusCode == 429;
    }

    public String asJson() {
        if (statusCode != 200) {
            return "{\"errorMessages\":[\"" + escape(errorMessage) + "\"]}";
        }

        return "{"
                + "\"tick\":"
                + tick
                + ",\"ticksAdvanced\":"
                + ticksAdvanced
                + ",\"jobsCompleted\":"
                + jobsCompleted
                + ",\"jobsBlocked\":"
                + jobsBlocked
                + ",\"robotsMoved\":"
                + robotsMoved
                + ",\"stockAdjusted\":"
                + stockAdjusted
                + "}";
    }

    private static String escape(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static final class TickCounters {
        int jobsCompleted;
        int jobsBlocked;
        int robotsMoved;
        int stockAdjusted;
    }
}
