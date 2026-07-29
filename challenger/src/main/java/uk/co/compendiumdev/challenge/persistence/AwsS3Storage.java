package uk.co.compendiumdev.challenge.persistence;

import com.google.gson.Gson;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;

public class AwsS3Storage
        implements ChallengerPersistenceMechanism, DatabaseContentPersistenceMechanism {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String TEXT_CONTENT_TYPE = "text/plain";
    private static final String CHALLENGER_SUFFIX = ".data.txt";
    private static final String DATABASE_SUFFIX = ".content.txt";
    private static final String ACTIVITY_SUFFIX = ".activity.txt";

    private final Logger logger = LoggerFactory.getLogger(AwsS3Storage.class);
    private final boolean allowSave;
    private final boolean allowLoad;
    private final S3StorageConfig config;
    private final Clock clock;
    private S3ObjectStore store;

    public AwsS3Storage(final boolean allowSave, final boolean allowLoad, final String awsBucket) {
        this(
                allowSave,
                allowLoad,
                withLegacyBucket(S3StorageConfig.fromEnvironment(), awsBucket),
                Clock.systemUTC());
    }

    AwsS3Storage(
            final boolean allowSave,
            final boolean allowLoad,
            final S3StorageConfig config,
            final Clock clock) {
        this(allowSave, allowLoad, config, null, clock);
    }

    AwsS3Storage(
            final boolean allowSave,
            final boolean allowLoad,
            final S3StorageConfig config,
            final S3ObjectStore store,
            final Clock clock) {
        this.allowSave = allowSave;
        this.allowLoad = allowLoad;
        this.config = config;
        this.store = store;
        this.clock = clock;
        logger.debug(
                "AWS S3 storage configured: allowSave={}, allowLoad={}, bucket={}, prefix={}",
                allowSave,
                allowLoad,
                config.bucketName(),
                config.managedPrefix());
    }

    @Override
    public PersistenceResponse saveChallengerStatus(final ChallengerAuthData data) {

        if (!allowSave) {
            return failure("AWS Configuration does not allow saving challenger status");
        }

        if (data == null) {
            return failure("no data provided");
        }

        if (!config.hasBucketName()) {
            return failure("AWS S3 bucket name is not configured");
        }

        try {
            final String dataString = new Gson().toJson(data);
            objectStore().put(challengerKey(data.getXChallenger()), dataString, JSON_CONTENT_TYPE);
            updateActivityMarker(data.getXChallenger());
            return new PersistenceResponse().withSuccess(true);
        } catch (Exception e) {
            logger.error("Error storing data to bucket for guid: {}", data.getXChallenger(), e);
            return failure("Error storing data to S3");
        }
    }

    @Override
    public PersistenceResponse loadChallengerStatus(final String guid) {

        if (!allowLoad) {
            return failure("AWS Configuration does not allow loading challenger status");
        }

        if (!config.hasBucketName()) {
            return failure("AWS S3 bucket name is not configured");
        }

        try {
            Optional<S3ObjectDetails> data = firstExistingObject(challengerLoadKeys(guid));
            if (data.isEmpty()) {
                return failure("Could not find challenger status in S3");
            }

            PersistenceResponse response =
                    new PersistenceResponse()
                            .withSuccess(true)
                            .withChallengerAuthData(
                                    new Gson()
                                            .fromJson(
                                                    data.get().content(),
                                                    ChallengerAuthData.class));

            Optional<S3ObjectDetails> database = firstExistingObject(databaseLoadKeys(guid));
            database.ifPresent(
                    s3ObjectDetails -> response.withDatabaseContents(s3ObjectDetails.content()));
            return response;
        } catch (Exception e) {
            logger.error("Error Reading Challenge Status From S3: {}", guid, e);
            return failure("Error Reading Challenges Status from S3");
        }
    }

    public boolean hasChallengerStatus(final String guid) {
        if (!allowSave && !allowLoad) {
            return false;
        }

        if (!config.hasBucketName()) {
            return false;
        }

        try {
            return firstExistingLastModified(challengerLoadKeys(guid)).isPresent();
        } catch (Exception e) {
            logger.warn("Could not check S3 challenger status for guid: {}", guid, e);
            return false;
        }
    }

    @Override
    public PersistenceResponse saveDatabaseContent(
            final String guid, final String databaseContents) {
        if (!allowSave) {
            return failure("AWS Configuration does not allow saving challenger database content");
        }

        if (!config.hasBucketName()) {
            return failure("AWS S3 bucket name is not configured");
        }

        try {
            objectStore()
                    .put(
                            databaseKey(guid),
                            databaseContents == null ? "" : databaseContents,
                            JSON_CONTENT_TYPE);
            updateActivityMarker(guid);
            return new PersistenceResponse().withSuccess(true);
        } catch (Exception e) {
            logger.error("Error storing database content to bucket for guid: {}", guid, e);
            return failure("Error storing database content to S3");
        }
    }

    @Override
    public PersistenceResponse loadDatabaseContent(final String guid) {
        if (!allowLoad) {
            return failure("AWS Configuration does not allow loading challenger database content");
        }

        if (!config.hasBucketName()) {
            return failure("AWS S3 bucket name is not configured");
        }

        try {
            Optional<S3ObjectDetails> data = firstExistingObject(databaseLoadKeys(guid));
            if (data.isEmpty()) {
                return failure("Could not find challenger database content in S3");
            }
            return new PersistenceResponse()
                    .withSuccess(true)
                    .withDatabaseContents(data.get().content());
        } catch (Exception e) {
            logger.error("Error reading database content from bucket for guid: {}", guid, e);
            return failure("Error reading database content from S3");
        }
    }

    public void cleanupInactiveSessions(final Set<String> activeSessionIds) {
        if (!config.cleanupEnabled()) {
            return;
        }

        if (!config.hasBucketName()) {
            logger.warn("Skipping S3 cleanup because no bucket name is configured");
            return;
        }

        Instant cutoff = clock.instant().minus(config.retention());
        Set<String> activeIds =
                activeSessionIds == null ? Set.of() : new HashSet<>(activeSessionIds);

        for (SessionObjects session : sessionsFromManagedPrefix()) {
            if (activeIds.contains(session.guid())) {
                continue;
            }
            if (isProtectedSinglePlayer(session.guid())) {
                continue;
            }
            Optional<Instant> lastActivity = session.lastActivity();
            if (lastActivity.isPresent() && lastActivity.get().isBefore(cutoff)) {
                deleteSession(session.guid());
            }
        }
    }

    public boolean cleanupEnabled() {
        return config.cleanupEnabled() && config.hasBucketName();
    }

    public Duration cleanupInterval() {
        return config.cleanupInterval();
    }

    public void close() {
        if (store != null) {
            store.close();
        }
    }

    private void deleteSession(final String guid) {
        logger.info("Deleting inactive S3 challenger session {}", guid);
        objectStore().delete(challengerKey(guid));
        objectStore().delete(databaseKey(guid));
        objectStore().delete(activityKey(guid));
    }

    private List<SessionObjects> sessionsFromManagedPrefix() {
        Map<String, SessionObjects> sessions = new HashMap<>();
        for (S3ObjectDetails object : objectStore().list(config.managedPrefix())) {
            SessionKey sessionKey = SessionKey.from(object.key(), config.managedPrefix());
            if (sessionKey == null) {
                continue;
            }

            SessionObjects session =
                    sessions.computeIfAbsent(sessionKey.guid(), SessionObjects::new);
            session.register(sessionKey.kind(), object.lastModified());
        }

        return List.copyOf(sessions.values());
    }

    private boolean isProtectedSinglePlayer(final String guid) {
        return Challengers.SINGLE_PLAYER_GUID.equals(guid) && !config.cleanupSinglePlayer();
    }

    private Optional<S3ObjectDetails> firstExistingObject(final List<String> keys) {
        for (String key : keys) {
            Optional<S3ObjectDetails> data = objectStore().get(key);
            if (data.isPresent()) {
                return data;
            }
        }
        return Optional.empty();
    }

    private Optional<Instant> firstExistingLastModified(final List<String> keys) {
        for (String key : keys) {
            Optional<Instant> lastModified = objectStore().lastModified(key);
            if (lastModified.isPresent()) {
                return lastModified;
            }
        }
        return Optional.empty();
    }

    private void updateActivityMarker(final String guid) {
        String key = activityKey(guid);
        Optional<Instant> markerLastModified = objectStore().lastModified(key);
        if (markerLastModified.isPresent()
                && markerLastModified
                        .get()
                        .plus(config.activityMarkerInterval())
                        .isAfter(clock.instant())) {
            return;
        }
        objectStore().put(key, clock.instant().toString(), TEXT_CONTENT_TYPE);
    }

    private S3ObjectStore objectStore() {
        if (store == null) {
            store = new AwsSdkS3ObjectStore(config);
        }
        return store;
    }

    private String challengerKey(final String guid) {
        return config.managedPrefix() + guid + CHALLENGER_SUFFIX;
    }

    private String databaseKey(final String guid) {
        return config.managedPrefix() + guid + DATABASE_SUFFIX;
    }

    private String activityKey(final String guid) {
        return config.managedPrefix() + guid + ACTIVITY_SUFFIX;
    }

    private List<String> challengerLoadKeys(final String guid) {
        return List.of(
                challengerKey(guid), config.managedPrefix() + guid, guid + CHALLENGER_SUFFIX, guid);
    }

    private List<String> databaseLoadKeys(final String guid) {
        return List.of(databaseKey(guid), guid + DATABASE_SUFFIX);
    }

    private PersistenceResponse failure(final String message) {
        return new PersistenceResponse().withSuccess(false).withErrorMessage(message);
    }

    private static S3StorageConfig withLegacyBucket(
            final S3StorageConfig config, final String legacyBucketName) {
        if (config.hasBucketName()
                || legacyBucketName == null
                || legacyBucketName.trim().isEmpty()) {
            return config;
        }

        Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("AWSBUCKET", legacyBucketName);
        return S3StorageConfig.from(environment);
    }

    private record SessionKey(String guid, SessionObjectKind kind) {

        static SessionKey from(final String key, final String prefix) {
            if (!key.startsWith(prefix)) {
                return null;
            }
            String name = key.substring(prefix.length());
            if (name.endsWith(CHALLENGER_SUFFIX)) {
                return new SessionKey(
                        name.substring(0, name.length() - CHALLENGER_SUFFIX.length()),
                        SessionObjectKind.CHALLENGER);
            }
            if (name.endsWith(DATABASE_SUFFIX)) {
                return new SessionKey(
                        name.substring(0, name.length() - DATABASE_SUFFIX.length()),
                        SessionObjectKind.DATABASE);
            }
            if (name.endsWith(ACTIVITY_SUFFIX)) {
                return new SessionKey(
                        name.substring(0, name.length() - ACTIVITY_SUFFIX.length()),
                        SessionObjectKind.ACTIVITY);
            }
            return null;
        }
    }

    private enum SessionObjectKind {
        CHALLENGER,
        DATABASE,
        ACTIVITY
    }

    private static class SessionObjects {
        private final String guid;
        private Instant challengerLastModified;
        private Instant databaseLastModified;
        private Instant activityLastModified;

        SessionObjects(final String guid) {
            this.guid = guid;
        }

        String guid() {
            return guid;
        }

        void register(final SessionObjectKind kind, final Instant lastModified) {
            if (kind == SessionObjectKind.CHALLENGER) {
                challengerLastModified = lastModified;
            }
            if (kind == SessionObjectKind.DATABASE) {
                databaseLastModified = lastModified;
            }
            if (kind == SessionObjectKind.ACTIVITY) {
                activityLastModified = lastModified;
            }
        }

        Optional<Instant> lastActivity() {
            if (activityLastModified != null) {
                return Optional.of(activityLastModified);
            }
            if (challengerLastModified != null) {
                return Optional.of(challengerLastModified);
            }
            return Optional.ofNullable(databaseLastModified);
        }
    }
}
