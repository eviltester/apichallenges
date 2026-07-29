package uk.co.compendiumdev.challenge.persistence;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

public class PersistenceLayerS3Test {

    @Test
    void cloudPersistenceUsesAwsAllowFlagsAndStoresFullSessionState() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        PersistenceLayer layer =
                new PersistenceLayer(PersistenceLayer.StorageType.CLOUD, cloudEnvironment(), store);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        String guid = challenger.getXChallenger();
        String databaseContents = "{\"todos\":[{\"title\":\"saved through layer\"}]}";
        completeChallenges(challenger, 10);

        PersistenceResponse saved = layer.saveChallengerStatus(challenger, databaseContents);
        PersistenceResponse loaded = layer.loadChallengerStatus(guid);

        Assertions.assertTrue(layer.willAutoSaveChallengerStatusToPersistenceLayer());
        Assertions.assertTrue(layer.willAutoLoadChallengerStatusFromPersistenceLayer());
        Assertions.assertTrue(saved.isSuccess());
        Assertions.assertTrue(loaded.isSuccess());
        Assertions.assertEquals(guid, loaded.getAuthData().getXChallenger());
        Assertions.assertEquals(databaseContents, loaded.getDatabaseContents());
        layer.close();
        Assertions.assertTrue(store.isClosed());
    }

    @Test
    void cloudPersistenceSkipsS3ObjectsBeforeCompletedChallengeThreshold() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        PersistenceLayer layer =
                new PersistenceLayer(PersistenceLayer.StorageType.CLOUD, cloudEnvironment(), store);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        String guid = challenger.getXChallenger();
        completeChallenges(challenger, 9);

        PersistenceResponse saved = layer.saveChallengerStatus(challenger, "{\"todos\":[]}");

        Assertions.assertTrue(saved.isSuccess());
        Assertions.assertTrue(saved.getErrorMessage().contains("S3 save skipped"));
        Assertions.assertFalse(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".activity.txt")));
        layer.close();
    }

    @Test
    void cloudSavedStatusIsBlankBeforeProgressIsSaved() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        PersistenceLayer layer =
                new PersistenceLayer(PersistenceLayer.StorageType.CLOUD, cloudEnvironment(), store);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));

        Assertions.assertEquals("", layer.savedStatusTextFor(challenger.getXChallenger()));
        layer.close();
    }

    @Test
    void cloudPersistenceWritesS3ObjectsAtCompletedChallengeThreshold() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        PersistenceLayer layer =
                new PersistenceLayer(PersistenceLayer.StorageType.CLOUD, cloudEnvironment(), store);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        String guid = challenger.getXChallenger();
        completeChallenges(challenger, 10);

        PersistenceResponse saved = layer.saveChallengerStatus(challenger, "{\"todos\":[]}");

        Assertions.assertTrue(saved.isSuccess());
        Assertions.assertTrue(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".activity.txt")));
        Assertions.assertEquals("S3 Saved", layer.savedStatusTextFor(guid));
        layer.close();
    }

    @Test
    void cloudPersistenceContinuesSavingAfterCompletedChallengeThreshold() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        PersistenceLayer layer =
                new PersistenceLayer(PersistenceLayer.StorageType.CLOUD, cloudEnvironment(), store);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        String guid = challenger.getXChallenger();
        completeChallenges(challenger, 10);
        layer.saveChallengerStatus(challenger, "{\"todos\":[]}");

        challenger.pass(CHALLENGE.QUERY_TODOS_FILTERED);
        layer.saveChallengerStatus(challenger, "{\"todos\":[{\"title\":\"updated\"}]}");
        PersistenceResponse loaded = layer.loadChallengerStatus(guid);

        Assertions.assertTrue(loaded.isSuccess());
        Assertions.assertEquals(11, loaded.getAuthData().completedChallengeCount());
        Assertions.assertEquals(
                "{\"todos\":[{\"title\":\"updated\"}]}", loaded.getDatabaseContents());
        layer.close();
    }

    @Test
    void awsAllowSaveFalseStillPreventsSavingAboveCompletedChallengeThreshold() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        Map<String, String> environment = cloudEnvironment();
        environment.put("AWS_ALLOW_SAVE", "false");
        PersistenceLayer layer =
                new PersistenceLayer(PersistenceLayer.StorageType.CLOUD, environment, store);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        String guid = challenger.getXChallenger();
        completeChallenges(challenger, 10);

        PersistenceResponse saved = layer.saveChallengerStatus(challenger, "{\"todos\":[]}");

        Assertions.assertFalse(saved.isSuccess());
        Assertions.assertFalse(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".activity.txt")));
        layer.close();
    }

    @Test
    void localPersistenceStillSavesBeforeCompletedChallengeThreshold() {
        PersistenceLayer layer = new PersistenceLayer(PersistenceLayer.StorageType.LOCAL);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        challenger.pass(CHALLENGE.GET_CHALLENGES);
        String guid = challenger.getXChallenger();
        String databaseContents = "{\"todos\":[{\"title\":\"local\"}]}";

        try {
            PersistenceResponse saved = layer.saveChallengerStatus(challenger, databaseContents);
            PersistenceResponse loaded = layer.loadChallengerStatus(guid);

            Assertions.assertTrue(saved.isSuccess());
            Assertions.assertTrue(loaded.isSuccess());
            Assertions.assertEquals(guid, loaded.getAuthData().getXChallenger());
            Assertions.assertEquals(databaseContents, loaded.getDatabaseContents());
        } finally {
            deleteLocalSessionFiles(guid);
            layer.close();
        }
    }

    @Test
    void localSavedStatusReflectsLocalProgressFile() {
        PersistenceLayer layer = new PersistenceLayer(PersistenceLayer.StorageType.LOCAL);
        ChallengerAuthData challenger = new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
        String guid = challenger.getXChallenger();

        try {
            Assertions.assertEquals("", layer.savedStatusTextFor(guid));

            PersistenceResponse saved = layer.saveChallengerStatus(challenger, "{\"todos\":[]}");

            Assertions.assertTrue(saved.isSuccess());
            Assertions.assertEquals("Local Saved", layer.savedStatusTextFor(guid));
        } finally {
            deleteLocalSessionFiles(guid);
            layer.close();
        }
    }

    private static Map<String, String> cloudEnvironment() {
        Map<String, String> environment = new HashMap<>();
        environment.put("AWS_ALLOW_SAVE", "true");
        environment.put("AWS_ALLOW_LOAD", "true");
        environment.put("AWS_S3_BUCKET_NAME", "bucket");
        environment.put("API_CHALLENGES_S3_CLEANUP_ENABLED", "false");
        return environment;
    }

    private static void completeChallenges(
            final ChallengerAuthData challenger, final int completedChallenges) {
        CHALLENGE[] challenges = CHALLENGE.values();
        for (int i = 0; i < completedChallenges; i++) {
            challenger.pass(challenges[i]);
        }
    }

    private static String managedKey(final String guid, final String suffix) {
        return S3StorageConfig.DEFAULT_PREFIX + guid + suffix;
    }

    private static void deleteLocalSessionFiles(final String guid) {
        File folder = new File(System.getProperty("user.dir"), "challengersessions");
        new File(folder, guid + ".data.txt").delete();
        new File(folder, guid + ".content.txt").delete();
    }
}
