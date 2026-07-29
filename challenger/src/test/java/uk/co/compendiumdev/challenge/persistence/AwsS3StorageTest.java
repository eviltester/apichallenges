package uk.co.compendiumdev.challenge.persistence;

import com.google.gson.Gson;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;

public class AwsS3StorageTest {

    private static final Instant NOW = Instant.parse("2026-07-28T12:00:00Z");

    @Test
    void savesAndLoadsChallengerProgressAndDatabaseContent() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        store.setNow(NOW);
        AwsS3Storage storage = storage(store, NOW);
        ChallengerAuthData challenger = challenger();
        String guid = challenger.getXChallenger();
        String databaseContents = "{\"todos\":[{\"title\":\"persisted\"}]}";

        PersistenceResponse saveChallenger = storage.saveChallengerStatus(challenger);
        PersistenceResponse saveDatabase = storage.saveDatabaseContent(guid, databaseContents);
        PersistenceResponse loaded = storage.loadChallengerStatus(guid);

        Assertions.assertTrue(saveChallenger.isSuccess());
        Assertions.assertTrue(saveDatabase.isSuccess());
        Assertions.assertTrue(loaded.isSuccess());
        Assertions.assertEquals(guid, loaded.getAuthData().getXChallenger());
        Assertions.assertEquals(databaseContents, loaded.getDatabaseContents());
        Assertions.assertTrue(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".activity.txt")));
    }

    @Test
    void canLoadLegacyRootLevelChallengerObject() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        AwsS3Storage storage = storage(store, NOW);
        ChallengerAuthData challenger = challenger();
        String guid = challenger.getXChallenger();
        store.seed(guid, new Gson().toJson(challenger), NOW.minusSeconds(60));

        PersistenceResponse loaded = storage.loadChallengerStatus(guid);

        Assertions.assertTrue(loaded.isSuccess());
        Assertions.assertEquals(guid, loaded.getAuthData().getXChallenger());
    }

    @Test
    void cleanupDeletesOldInactiveSessionObjects() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        AwsS3Storage storage = storage(store, NOW);
        String guid = "7c0c3b0e-9632-476d-a920-0ce579c9393c";
        Instant old = NOW.minusSeconds(8 * 24 * 60 * 60);
        store.seed(managedKey(guid, ".data.txt"), "{}", old);
        store.seed(managedKey(guid, ".content.txt"), "{}", old);
        store.seed(managedKey(guid, ".activity.txt"), old.toString(), old);

        storage.cleanupInactiveSessions(Set.of());

        Assertions.assertFalse(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".activity.txt")));
    }

    @Test
    void cleanupFallsBackToProgressObjectAgeWhenActivityMarkerIsMissing() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        AwsS3Storage storage = storage(store, NOW);
        String guid = "3c1d3ff5-dd3a-4df7-a778-5fdcfd2dcb80";
        Instant old = NOW.minusSeconds(8 * 24 * 60 * 60);
        store.seed(managedKey(guid, ".data.txt"), "{}", old);
        store.seed(managedKey(guid, ".content.txt"), "{}", old);

        storage.cleanupInactiveSessions(Set.of());

        Assertions.assertFalse(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertFalse(store.contains(managedKey(guid, ".content.txt")));
    }

    @Test
    void cleanupKeepsActiveSessionObjectsEvenWhenOld() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        AwsS3Storage storage = storage(store, NOW);
        String guid = "c905eaf6-f3da-45e9-b29c-77851b6a2ecf";
        Instant old = NOW.minusSeconds(8 * 24 * 60 * 60);
        store.seed(managedKey(guid, ".data.txt"), "{}", old);
        store.seed(managedKey(guid, ".content.txt"), "{}", old);
        store.seed(managedKey(guid, ".activity.txt"), old.toString(), old);

        storage.cleanupInactiveSessions(Set.of(guid));

        Assertions.assertTrue(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".activity.txt")));
    }

    @Test
    void cleanupKeepsSinglePlayerSessionByDefault() {
        FakeS3ObjectStore store = new FakeS3ObjectStore();
        AwsS3Storage storage = storage(store, NOW);
        String guid = Challengers.SINGLE_PLAYER_GUID;
        Instant old = NOW.minusSeconds(8 * 24 * 60 * 60);
        store.seed(managedKey(guid, ".data.txt"), "{}", old);
        store.seed(managedKey(guid, ".content.txt"), "{}", old);
        store.seed(managedKey(guid, ".activity.txt"), old.toString(), old);

        storage.cleanupInactiveSessions(Set.of());

        Assertions.assertTrue(store.contains(managedKey(guid, ".data.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".content.txt")));
        Assertions.assertTrue(store.contains(managedKey(guid, ".activity.txt")));
    }

    private static AwsS3Storage storage(final FakeS3ObjectStore store, final Instant now) {
        return new AwsS3Storage(
                true,
                true,
                S3StorageConfig.from(Map.of("AWS_S3_BUCKET_NAME", "bucket")),
                store,
                Clock.fixed(now, ZoneOffset.UTC));
    }

    private static ChallengerAuthData challenger() {
        return new ChallengerAuthData(Arrays.asList(CHALLENGE.values()));
    }

    private static String managedKey(final String guid, final String suffix) {
        return S3StorageConfig.DEFAULT_PREFIX + guid + suffix;
    }
}
