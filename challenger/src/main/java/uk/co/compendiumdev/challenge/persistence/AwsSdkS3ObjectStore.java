package uk.co.compendiumdev.challenge.persistence;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

class AwsSdkS3ObjectStore implements S3ObjectStore {

    private final S3StorageConfig config;
    private final S3Client client;

    AwsSdkS3ObjectStore(final S3StorageConfig config) {
        this.config = config;
        this.client = createClient(config);
    }

    @Override
    public void put(final String key, final String content, final String contentType) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(config.bucketName())
                        .key(key)
                        .contentType(contentType)
                        .build();

        client.putObject(
                request,
                RequestBody.fromString(content == null ? "" : content, StandardCharsets.UTF_8));
    }

    @Override
    public Optional<S3ObjectDetails> get(final String key) {
        try {
            ResponseBytes<GetObjectResponse> response =
                    client.getObjectAsBytes(
                            GetObjectRequest.builder()
                                    .bucket(config.bucketName())
                                    .key(key)
                                    .build());
            return Optional.of(
                    new S3ObjectDetails(
                            key, response.asUtf8String(), response.response().lastModified()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public Optional<Instant> lastModified(final String key) {
        try {
            HeadObjectResponse response =
                    client.headObject(
                            HeadObjectRequest.builder()
                                    .bucket(config.bucketName())
                                    .key(key)
                                    .build());
            return Optional.ofNullable(response.lastModified());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<S3ObjectDetails> list(final String prefix) {
        List<S3ObjectDetails> objects = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Response response =
                    client.listObjectsV2(
                            ListObjectsV2Request.builder()
                                    .bucket(config.bucketName())
                                    .prefix(prefix)
                                    .continuationToken(continuationToken)
                                    .build());

            for (S3Object object : response.contents()) {
                objects.add(new S3ObjectDetails(object.key(), "", object.lastModified()));
            }
            continuationToken = response.nextContinuationToken();
        } while (continuationToken != null);

        return objects;
    }

    @Override
    public void delete(final String key) {
        client.deleteObject(
                DeleteObjectRequest.builder().bucket(config.bucketName()).key(key).build());
    }

    @Override
    public void close() {
        client.close();
    }

    private static S3Client createClient(final S3StorageConfig config) {
        S3ClientBuilder builder =
                S3Client.builder()
                        .region(Region.of(config.region()))
                        .endpointOverride(URI.create(config.endpoint()))
                        .httpClientBuilder(UrlConnectionHttpClient.builder())
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(config.pathStyleAccess())
                                        .build());

        if (config.hasStaticCredentials()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    config.accessKeyId(), config.secretAccessKey())));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
