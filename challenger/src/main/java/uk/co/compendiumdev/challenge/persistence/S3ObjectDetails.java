package uk.co.compendiumdev.challenge.persistence;

import java.time.Instant;

record S3ObjectDetails(String key, String content, Instant lastModified) {}
