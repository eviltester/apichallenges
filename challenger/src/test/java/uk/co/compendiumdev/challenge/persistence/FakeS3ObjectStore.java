package uk.co.compendiumdev.challenge.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FakeS3ObjectStore implements S3ObjectStore {

    private final Map<String, S3ObjectDetails> objects = new HashMap<>();
    private Instant now = Instant.parse("2026-07-28T12:00:00Z");
    private boolean closed;

    void setNow(final Instant now) {
        this.now = now;
    }

    void seed(final String key, final String content, final Instant lastModified) {
        objects.put(key, new S3ObjectDetails(key, content, lastModified));
    }

    boolean contains(final String key) {
        return objects.containsKey(key);
    }

    boolean isClosed() {
        return closed;
    }

    @Override
    public void put(final String key, final String content, final String contentType) {
        objects.put(key, new S3ObjectDetails(key, content, now));
    }

    @Override
    public Optional<S3ObjectDetails> get(final String key) {
        return Optional.ofNullable(objects.get(key));
    }

    @Override
    public Optional<Instant> lastModified(final String key) {
        if (!objects.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(objects.get(key).lastModified());
    }

    @Override
    public List<S3ObjectDetails> list(final String prefix) {
        List<S3ObjectDetails> found = new ArrayList<>();
        for (S3ObjectDetails object : objects.values()) {
            if (object.key().startsWith(prefix)) {
                found.add(object);
            }
        }
        return found;
    }

    @Override
    public void delete(final String key) {
        objects.remove(key);
    }

    @Override
    public void close() {
        closed = true;
    }
}
