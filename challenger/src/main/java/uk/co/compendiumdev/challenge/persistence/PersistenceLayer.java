package uk.co.compendiumdev.challenge.persistence;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.ChallengerState;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonPopulator;

public class PersistenceLayer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceLayer.class);

    private StorageType storeOn;

    // TODO: have a database persistence layer e.g. 'save to disk' option for the todos
    // TODO: single player mode should have this switched on by default
    // TODO: allow configuring what is on and what is off for any storage type using constructor
    // rather than environment variables
    // todo: add all active storage mechanisms in a list and store on all - switch it off by
    // removing from list

    ChallengerPersistenceMechanism file = new ChallengerFileStorage();
    DatabaseContentPersistenceMechanism dbfile = (DatabaseContentPersistenceMechanism) file;

    private ChallengerPersistenceMechanism aws;
    private DatabaseContentPersistenceMechanism awsdb;
    private AwsS3Storage awsS3Storage;
    private S3StorageConfig s3Config;
    boolean allowSaveToS3 = false;
    boolean allowLoadFromS3 = false;
    private ScheduledExecutorService cleanupExecutor;
    private ScheduledFuture<?> cleanupFuture;

    public PersistenceResponse tryToLoadChallenger(
            final Challengers challengers, final String xChallengerGuid) {

        final PersistenceResponse response = loadChallengerStatus(xChallengerGuid);

        if (response.isSuccess()) {
            ChallengerAuthData challenger =
                    new ChallengerAuthData(challengers.getDefinedChallenges())
                            .fromData(response.getAuthData(), challengers.getDefinedChallenges());
            if (xChallengerGuid.equals(Challengers.SINGLE_PLAYER_GUID)) {
                challenger.setXChallengerGUID(xChallengerGuid);
            }
            challenger.touch();
            challenger.setState(
                    ChallengerState.LOADED_FROM_PERSISTENCE); // refresh last accessed date
            challengers.put(challenger);

            String databaseName = challenger.getXChallenger();
            challengers.getErModel().createInstanceDatabaseIfNotExisting(databaseName);

            // did we also load the data? if so, populate the database from it
            if (!response.getDatabaseContents().isEmpty()) {
                new JsonPopulator(response.getDatabaseContents())
                        .populate(
                                challengers.getErModel().getSchema(),
                                challengers.getErModel().getStore(databaseName));
            } else {
                // set the database to default values
                challengers.getErModel().populateDatabase(databaseName);
            }
        }

        return response;
    }

    public enum StorageType {
        LOCAL,
        CLOUD,
        NONE
    };

    public PersistenceLayer(StorageType storeWhere) {
        this(storeWhere, System.getenv(), null);
    }

    PersistenceLayer(
            final StorageType storeWhere,
            final Map<String, String> environment,
            final S3ObjectStore s3ObjectStore) {
        this.storeOn = storeWhere;

        if (this.storeOn == StorageType.CLOUD) {

            allowSaveToS3 = S3StorageConfig.booleanValue(environment, "AWS_ALLOW_SAVE", false);
            allowLoadFromS3 = S3StorageConfig.booleanValue(environment, "AWS_ALLOW_LOAD", false);

            s3Config = S3StorageConfig.from(environment);
            awsS3Storage =
                    new AwsS3Storage(
                            allowSaveToS3,
                            allowLoadFromS3,
                            s3Config,
                            s3ObjectStore,
                            java.time.Clock.systemUTC());
            aws = awsS3Storage;
            awsdb = awsS3Storage;
        }
    }

    public PersistenceResponse saveChallengerStatus(
            ChallengerAuthData data, String databaseContents) {

        if (storeOn == StorageType.LOCAL) {
            PersistenceResponse fileStoreChallenger = file.saveChallengerStatus(data);
            PersistenceResponse fileStoreDatabase =
                    dbfile.saveDatabaseContent(data.getXChallenger(), databaseContents);
            return new PersistenceResponse()
                    .withSuccess(fileStoreChallenger.isSuccess() && fileStoreDatabase.isSuccess())
                    .withErrorMessage(
                            fileStoreChallenger.getErrorMessage()
                                    + fileStoreDatabase.getErrorMessage())
                    .withDatabaseContents(fileStoreDatabase.getDatabaseContents())
                    .withChallengerAuthData(fileStoreChallenger.getAuthData());
        }

        if (storeOn == StorageType.CLOUD && aws != null) {
            if (allowSaveToS3
                    && data.completedChallengeCount() < s3Config.saveAfterCompletedChallenges()) {
                return new PersistenceResponse()
                        .withSuccess(true)
                        .withErrorMessage(
                                String.format(
                                        "S3 save skipped until %d completed challenges",
                                        s3Config.saveAfterCompletedChallenges()))
                        .withDatabaseContents(databaseContents)
                        .withChallengerAuthData(data);
            }

            PersistenceResponse s3StoreChallenger = aws.saveChallengerStatus(data);
            PersistenceResponse s3StoreDatabase =
                    awsdb.saveDatabaseContent(data.getXChallenger(), databaseContents);
            return new PersistenceResponse()
                    .withSuccess(s3StoreChallenger.isSuccess() && s3StoreDatabase.isSuccess())
                    .withErrorMessage(
                            s3StoreChallenger.getErrorMessage() + s3StoreDatabase.getErrorMessage())
                    .withDatabaseContents(s3StoreDatabase.getDatabaseContents())
                    .withChallengerAuthData(s3StoreChallenger.getAuthData());
        }

        // if(storeOn==StorageType.NONE){
        return new PersistenceResponse()
                .withSuccess(false)
                .withErrorMessage("No Persistence Configured - store in memory only.");
        // }
    }

    public PersistenceResponse loadChallengerStatus(String guid) {

        if (storeOn == StorageType.LOCAL) {
            PersistenceResponse fileStoreChallenger = file.loadChallengerStatus(guid);
            PersistenceResponse fileStoreDatabase = dbfile.loadDatabaseContent(guid);
            return new PersistenceResponse()
                    .withSuccess(fileStoreChallenger.isSuccess())
                    . // only track challenger success && fileStoreDatabase.isSuccess()).
                    withErrorMessage(
                            fileStoreChallenger.getErrorMessage()
                                    + fileStoreDatabase.getErrorMessage())
                    .withDatabaseContents(fileStoreDatabase.getDatabaseContents())
                    .withChallengerAuthData(fileStoreChallenger.getAuthData());
        }

        if (storeOn == StorageType.CLOUD && aws != null) {
            return aws.loadChallengerStatus(guid);
        }

        // if(storeOn==StorageType.NONE){
        return new PersistenceResponse()
                .withSuccess(false)
                .withErrorMessage("No Persistence Configured - store in memory only.");
        // }
    }

    public boolean willAutoSaveChallengerStatusToPersistenceLayer() {

        if (storeOn == StorageType.LOCAL) {
            return true;
        }

        if (storeOn == StorageType.CLOUD && allowSaveToS3) {
            return true;
        }

        return false;
    }

    public boolean willAutoLoadChallengerStatusFromPersistenceLayer() {

        if (storeOn == StorageType.LOCAL) {
            return true;
        }

        if (storeOn == StorageType.CLOUD && allowLoadFromS3) {
            return true;
        }

        return false;
    }

    public String savedStatusTextFor(final String guid) {
        if (guid == null || guid.trim().isEmpty()) {
            return "";
        }

        String trimmedGuid = guid.trim();

        if (storeOn == StorageType.LOCAL
                && file instanceof ChallengerFileStorage
                && ((ChallengerFileStorage) file).hasChallengerStatus(trimmedGuid)) {
            return "Local Saved";
        }

        if (storeOn == StorageType.CLOUD
                && awsS3Storage != null
                && awsS3Storage.hasChallengerStatus(trimmedGuid)) {
            return "S3 Saved";
        }

        return "";
    }

    public int autoSaveAfterCompletedChallenges() {
        if (storeOn == StorageType.CLOUD && allowSaveToS3 && s3Config != null) {
            return s3Config.saveAfterCompletedChallenges();
        }

        return 0;
    }

    public void startCloudCleanup(final Supplier<Set<String>> activeSessionIds) {
        if (storeOn != StorageType.CLOUD
                || awsS3Storage == null
                || !awsS3Storage.cleanupEnabled()) {
            return;
        }

        if (cleanupFuture != null) {
            return;
        }

        Runnable cleanup =
                () -> {
                    try {
                        Set<String> suppliedActiveIds =
                                activeSessionIds == null ? Set.of() : activeSessionIds.get();
                        Set<String> activeIds =
                                suppliedActiveIds == null
                                        ? Set.of()
                                        : new HashSet<>(suppliedActiveIds);
                        awsS3Storage.cleanupInactiveSessions(activeIds);
                    } catch (Exception e) {
                        LOGGER.warn("S3 challenger session cleanup failed", e);
                    }
                };

        cleanup.run();
        cleanupExecutor =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable, "api-challenges-s3-cleanup");
                            thread.setDaemon(true);
                            return thread;
                        });
        cleanupFuture =
                cleanupExecutor.scheduleWithFixedDelay(
                        cleanup,
                        awsS3Storage.cleanupInterval().toHours(),
                        awsS3Storage.cleanupInterval().toHours(),
                        TimeUnit.HOURS);
    }

    @Override
    public void close() {
        if (cleanupFuture != null) {
            cleanupFuture.cancel(false);
        }
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
        }
        if (awsS3Storage != null) {
            awsS3Storage.close();
        }
    }
}
