package uk.co.compendiumdev.challenge.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface S3ObjectStore extends AutoCloseable {

    void put(String key, String content, String contentType);

    Optional<S3ObjectDetails> get(String key);

    Optional<Instant> lastModified(String key);

    List<S3ObjectDetails> list(String prefix);

    void delete(String key);

    @Override
    void close();
}
