package uk.co.compendiumdev.robodepot;

public enum RoboDepotBugMode {
    NONE("none"),
    CLASSIC("classic"),
    ACTIVE_JOB_UNASSIGN_BLOCKED("active-job-unassign-blocked"),
    HELD_STOCK_UNLINK_BLOCKED("held-stock-unlink-blocked"),
    CLOSED_ZONE_ACCEPTS_ROBOTS("closed-zone-accepts-robots"),
    CLOSED_ZONE_ACCEPTS_STOCK("closed-zone-accepts-stock"),
    FRAGILE_FROZEN_STOCK_ALLOWED("fragile-frozen-stock-allowed"),
    PAYLOAD_MISMATCH_ALLOWED("payload-mismatch-allowed"),
    ZONE_CAPACITY_OFF_BY_ONE("zone-capacity-off-by-one"),
    VALID_ROBOT_COLOR_YELLOW_REJECTED("valid-robot-color-yellow-rejected"),
    VALID_JOB_STATE_CANCELLED_REJECTED("valid-job-state-cancelled-rejected"),
    STOCK_PUT_INCREMENTS("stock-put-increments"),
    PRIORITY_INVERTED("priority-inverted"),
    LOW_BATTERY_ROBOT_WORKS("low-battery-robot-works"),
    CHARGING_ROBOT_ASSIGNED_JOB("charging-robot-assigned-job"),
    OFFLINE_ROBOT_MOVES("offline-robot-moves"),
    DAMAGED_STOCK_PICKED("damaged-stock-picked"),
    WRONG_SKU_ADJUSTED("wrong-sku-adjusted"),
    STOCK_SHORTAGE_COMPLETES("stock-shortage-completes"),
    ROBOT_STATUS_STALE("robot-status-stale");

    private final String argumentValue;

    RoboDepotBugMode(final String argumentValue) {
        this.argumentValue = argumentValue;
    }

    public static RoboDepotBugMode fromArgs(final String[] args) {
        if (args == null) {
            return CLASSIC;
        }

        for (String arg : args) {
            if (arg != null && arg.toLowerCase().startsWith("-robodepotbugs=")) {
                String value = arg.substring(arg.indexOf("=") + 1);
                if (NONE.argumentValue.equalsIgnoreCase(value.trim())) {
                    return NONE;
                }
            }
        }
        return CLASSIC;
    }

    public boolean enables(final RoboDepotBugMode bugMode) {
        return this == CLASSIC || this == bugMode;
    }

    public String argumentValue() {
        return argumentValue;
    }

    public static RoboDepotBugMode fromValue(final String value) {
        if (value == null) {
            return CLASSIC;
        }

        String normalized = value.trim().toLowerCase();
        for (RoboDepotBugMode mode : values()) {
            if (mode.argumentValue.equals(normalized)) {
                return mode;
            }
        }
        return CLASSIC;
    }
}
