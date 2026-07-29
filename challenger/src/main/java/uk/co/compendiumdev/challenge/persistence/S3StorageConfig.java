package uk.co.compendiumdev.challenge.persistence;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

public class S3StorageConfig {

    static final String DEFAULT_ENDPOINT = "https://storage.railway.app";
    static final String DEFAULT_REGION = "auto";
    static final String DEFAULT_PREFIX = "apichallenges/sessions/";
    static final int DEFAULT_SAVE_AFTER_COMPLETED_CHALLENGES = 10;

    private final String bucketName;
    private final String endpoint;
    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final boolean pathStyleAccess;
    private final String managedPrefix;
    private final boolean cleanupEnabled;
    private final Duration retention;
    private final Duration cleanupInterval;
    private final Duration activityMarkerInterval;
    private final boolean cleanupSinglePlayer;
    private final int saveAfterCompletedChallenges;

    private S3StorageConfig(
            final String bucketName,
            final String endpoint,
            final String region,
            final String accessKeyId,
            final String secretAccessKey,
            final boolean pathStyleAccess,
            final String managedPrefix,
            final boolean cleanupEnabled,
            final Duration retention,
            final Duration cleanupInterval,
            final Duration activityMarkerInterval,
            final boolean cleanupSinglePlayer,
            final int saveAfterCompletedChallenges) {
        this.bucketName = blankToEmpty(bucketName);
        this.endpoint = blankToDefault(endpoint, DEFAULT_ENDPOINT);
        this.region = blankToDefault(region, DEFAULT_REGION);
        this.accessKeyId = blankToEmpty(accessKeyId);
        this.secretAccessKey = blankToEmpty(secretAccessKey);
        this.pathStyleAccess = pathStyleAccess;
        this.managedPrefix = normalizePrefix(managedPrefix);
        this.cleanupEnabled = cleanupEnabled;
        this.retention = retention;
        this.cleanupInterval = cleanupInterval;
        this.activityMarkerInterval = activityMarkerInterval;
        this.cleanupSinglePlayer = cleanupSinglePlayer;
        this.saveAfterCompletedChallenges = saveAfterCompletedChallenges;
    }

    public static S3StorageConfig fromEnvironment() {
        return from(System.getenv());
    }

    static S3StorageConfig from(final Map<String, String> environment) {
        return new S3StorageConfig(
                firstValue(environment, "AWS_S3_BUCKET_NAME", "BUCKET", "AWSBUCKET"),
                firstValue(environment, "AWS_ENDPOINT_URL", "ENDPOINT"),
                firstValue(environment, "AWS_DEFAULT_REGION", "AWS_REGION", "REGION"),
                firstValue(environment, "AWS_ACCESS_KEY_ID", "ACCESS_KEY_ID"),
                firstValue(environment, "AWS_SECRET_ACCESS_KEY", "SECRET_ACCESS_KEY"),
                isPathStyle(firstValue(environment, "AWS_S3_URL_STYLE")),
                firstValue(environment, "API_CHALLENGES_S3_PREFIX"),
                booleanValue(environment, "API_CHALLENGES_S3_CLEANUP_ENABLED", true),
                days(environment, "API_CHALLENGES_S3_RETENTION_DAYS", 7),
                hours(environment, "API_CHALLENGES_S3_CLEANUP_INTERVAL_HOURS", 24),
                hours(environment, "API_CHALLENGES_S3_ACTIVITY_MARKER_INTERVAL_HOURS", 24),
                booleanValue(environment, "API_CHALLENGES_S3_CLEANUP_SINGLE_PLAYER_ENABLED", false),
                positiveIntValue(
                        environment,
                        "API_CHALLENGES_S3_SAVE_AFTER_COMPLETED_CHALLENGES",
                        DEFAULT_SAVE_AFTER_COMPLETED_CHALLENGES));
    }

    public String bucketName() {
        return bucketName;
    }

    public String endpoint() {
        return endpoint;
    }

    public String region() {
        return region;
    }

    public String accessKeyId() {
        return accessKeyId;
    }

    public String secretAccessKey() {
        return secretAccessKey;
    }

    public boolean hasStaticCredentials() {
        return !accessKeyId.isEmpty() && !secretAccessKey.isEmpty();
    }

    public boolean pathStyleAccess() {
        return pathStyleAccess;
    }

    public String managedPrefix() {
        return managedPrefix;
    }

    public boolean cleanupEnabled() {
        return cleanupEnabled;
    }

    public Duration retention() {
        return retention;
    }

    public Duration cleanupInterval() {
        return cleanupInterval;
    }

    public Duration activityMarkerInterval() {
        return activityMarkerInterval;
    }

    public boolean cleanupSinglePlayer() {
        return cleanupSinglePlayer;
    }

    public int saveAfterCompletedChallenges() {
        return saveAfterCompletedChallenges;
    }

    public boolean hasBucketName() {
        return !bucketName.isEmpty();
    }

    private static String firstValue(final Map<String, String> environment, final String... names) {
        if (environment == null) {
            return "";
        }

        for (String name : names) {
            String value = environment.get(name);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    static boolean booleanValue(
            final Map<String, String> environment, final String name, final boolean defaultValue) {
        String value = firstValue(environment, name);
        if (value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
    }

    private static Duration days(
            final Map<String, String> environment, final String name, final long defaultValue) {
        return Duration.ofDays(positiveLongValue(environment, name, defaultValue));
    }

    private static Duration hours(
            final Map<String, String> environment, final String name, final long defaultValue) {
        return Duration.ofHours(positiveLongValue(environment, name, defaultValue));
    }

    private static long positiveLongValue(
            final Map<String, String> environment, final String name, final long defaultValue) {
        String value = firstValue(environment, name);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int positiveIntValue(
            final Map<String, String> environment, final String name, final int defaultValue) {
        String value = firstValue(environment, name);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean isPathStyle(final String urlStyle) {
        return "path".equals(blankToEmpty(urlStyle).toLowerCase(Locale.ROOT));
    }

    private static String normalizePrefix(final String prefix) {
        String normalized = blankToDefault(prefix, DEFAULT_PREFIX).trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private static String blankToDefault(final String value, final String defaultValue) {
        String normalized = blankToEmpty(value);
        return normalized.isEmpty() ? defaultValue : normalized;
    }

    private static String blankToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }
}
