package uk.co.compendiumdev.challenge.persistence;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class S3StorageConfigTest {

    @Test
    void railwayAwsSdkVariablesArePreferred() {
        S3StorageConfig config =
                S3StorageConfig.from(
                        Map.of(
                                "AWS_S3_BUCKET_NAME", "aws-bucket",
                                "BUCKET", "railway-bucket",
                                "AWSBUCKET", "legacy-bucket",
                                "AWS_ENDPOINT_URL", "https://storage.example.com",
                                "AWS_DEFAULT_REGION", "auto",
                                "AWS_ACCESS_KEY_ID", "aws-key",
                                "AWS_SECRET_ACCESS_KEY", "aws-secret",
                                "AWS_S3_URL_STYLE", "path"));

        Assertions.assertEquals("aws-bucket", config.bucketName());
        Assertions.assertEquals("https://storage.example.com", config.endpoint());
        Assertions.assertEquals("auto", config.region());
        Assertions.assertEquals("aws-key", config.accessKeyId());
        Assertions.assertEquals("aws-secret", config.secretAccessKey());
        Assertions.assertTrue(config.hasStaticCredentials());
        Assertions.assertTrue(config.pathStyleAccess());
    }

    @Test
    void railwayRawVariablesCanBeUsedWhenAwsNamesAreMissing() {
        S3StorageConfig config =
                S3StorageConfig.from(
                        Map.of(
                                "BUCKET", "railway-bucket",
                                "ENDPOINT", "https://storage.railway.app",
                                "REGION", "auto",
                                "ACCESS_KEY_ID", "raw-key",
                                "SECRET_ACCESS_KEY", "raw-secret"));

        Assertions.assertEquals("railway-bucket", config.bucketName());
        Assertions.assertEquals("https://storage.railway.app", config.endpoint());
        Assertions.assertEquals("auto", config.region());
        Assertions.assertEquals("raw-key", config.accessKeyId());
        Assertions.assertEquals("raw-secret", config.secretAccessKey());
    }

    @Test
    void legacyBucketNameStillWorksAsFallback() {
        S3StorageConfig config = S3StorageConfig.from(Map.of("AWSBUCKET", "legacy-bucket"));

        Assertions.assertEquals("legacy-bucket", config.bucketName());
        Assertions.assertEquals(S3StorageConfig.DEFAULT_ENDPOINT, config.endpoint());
        Assertions.assertEquals(S3StorageConfig.DEFAULT_REGION, config.region());
        Assertions.assertEquals(S3StorageConfig.DEFAULT_PREFIX, config.managedPrefix());
    }

    @Test
    void cleanupDefaultsMatchRailwayPlan() {
        S3StorageConfig config = S3StorageConfig.from(Map.of());

        Assertions.assertTrue(config.cleanupEnabled());
        Assertions.assertEquals(Duration.ofDays(7), config.retention());
        Assertions.assertEquals(Duration.ofHours(24), config.cleanupInterval());
        Assertions.assertEquals(Duration.ofHours(24), config.activityMarkerInterval());
        Assertions.assertFalse(config.cleanupSinglePlayer());
        Assertions.assertEquals(10, config.saveAfterCompletedChallenges());
    }

    @Test
    void cleanupValuesCanBeConfigured() {
        S3StorageConfig config =
                S3StorageConfig.from(
                        Map.of(
                                "API_CHALLENGES_S3_PREFIX", "/custom/prefix",
                                "API_CHALLENGES_S3_CLEANUP_ENABLED", "false",
                                "API_CHALLENGES_S3_RETENTION_DAYS", "30",
                                "API_CHALLENGES_S3_CLEANUP_INTERVAL_HOURS", "6",
                                "API_CHALLENGES_S3_ACTIVITY_MARKER_INTERVAL_HOURS", "12",
                                "API_CHALLENGES_S3_CLEANUP_SINGLE_PLAYER_ENABLED", "true",
                                "API_CHALLENGES_S3_SAVE_AFTER_COMPLETED_CHALLENGES", "25"));

        Assertions.assertEquals("custom/prefix/", config.managedPrefix());
        Assertions.assertFalse(config.cleanupEnabled());
        Assertions.assertEquals(Duration.ofDays(30), config.retention());
        Assertions.assertEquals(Duration.ofHours(6), config.cleanupInterval());
        Assertions.assertEquals(Duration.ofHours(12), config.activityMarkerInterval());
        Assertions.assertTrue(config.cleanupSinglePlayer());
        Assertions.assertEquals(25, config.saveAfterCompletedChallenges());
    }

    @Test
    void invalidSaveAfterCompletedChallengesFallsBackToDefault() {
        Assertions.assertEquals(
                10,
                S3StorageConfig.from(
                                Map.of(
                                        "API_CHALLENGES_S3_SAVE_AFTER_COMPLETED_CHALLENGES",
                                        "invalid"))
                        .saveAfterCompletedChallenges());
        Assertions.assertEquals(
                10,
                S3StorageConfig.from(
                                Map.of("API_CHALLENGES_S3_SAVE_AFTER_COMPLETED_CHALLENGES", "0"))
                        .saveAfterCompletedChallenges());
        Assertions.assertEquals(
                10,
                S3StorageConfig.from(
                                Map.of("API_CHALLENGES_S3_SAVE_AFTER_COMPLETED_CHALLENGES", "-1"))
                        .saveAfterCompletedChallenges());
    }
}
