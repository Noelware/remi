/*
 * 🧶 Remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022-2023 Noelware <team@noelware.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.noelware.remi.support.s3;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.ListBlobsRequest;
import org.noelware.remi.core.StorageService;
import org.noelware.remi.core.UploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

public class AmazonS3StorageService implements StorageService<AmazonS3StorageConfig> {
    private final Logger LOG = LoggerFactory.getLogger(AmazonS3StorageService.class);
    private final AmazonS3StorageConfig config;
    private final Tika TIKA = new Tika();
    private S3Client client;

    public AmazonS3StorageService(AmazonS3StorageConfig config) {
        this.config = config;
    }

    /**
     * Returns a {@link Blob} from the given <code>path</code> specified. This method can return
     * <code>null</code> if the blob was not found.
     *
     * @param path The relative (or absolute) path to get the blob from
     * @return {@link Blob} object if any, or <code>null</code>
     */
    @Override
    public Blob blob(String path) throws IOException {
        final String prefixed = toPrefixedString(path);
        final ResponseInputStream<GetObjectResponse> resp = client.getObject((builder) -> {
            builder.bucket(config.bucket());
            builder.key(prefixed);
        });

        byte[] data;
        try (final InputStream stream = resp) {
            data = stream.readAllBytes();
        }

        final GetObjectResponse obj = resp.response();
        return new Blob(
                obj.lastModified(),
                null,
                obj.contentType(),
                new ByteArrayInputStream(data),
                obj.eTag(),
                prefixed,
                "s3",
                format("s3://%s", prefixed),
                obj.contentLength());
    }

    /**
     * Lists all the {@link Blob blobs} given with a {@link ListBlobsRequest request} object. To speed up
     * performance, the request doesn't contain the inner data from the object itself, use {@link #blob(String)} to
     * do so.
     *
     * @param request The request options object, if <code>null</code> was provided, then all the blobs in this
     *                storage service will be retrieved, which might take a while if not using pagination.
     * @return A {@link List<Blob> list of blobs} received from the {@link ListBlobsRequest request}.
     */
    @Override
    public List<Blob> blobs(@Nullable ListBlobsRequest request) throws IOException {
        if (request == null) {
            final ArrayList<Blob> blobs = new ArrayList<>();
            ListObjectsV2Request.Builder builder =
                    ListObjectsV2Request.builder().maxKeys(100).bucket(config.bucket());

            if (config.prefix() != null) {
                builder.prefix(config.prefix());
            }

            ListObjectsV2Request listObjectsV2Request = builder.build();
            while (true) {
                final ListObjectsV2Response resp = client.listObjectsV2(listObjectsV2Request);
                try (final Stream<S3Object> stream = resp.contents().parallelStream()) {
                    final List<Blob> createdBlobs = stream.map(obj -> {
                                try {
                                    return fromS3Object(obj);
                                } catch (IOException e) {
                                    LOG.error(format("Unable to get object [%s]:", obj.key()), e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    blobs.addAll(createdBlobs);
                }

                if (resp.nextContinuationToken() != null) {
                    listObjectsV2Request = listObjectsV2Request.toBuilder()
                            .continuationToken(resp.nextContinuationToken())
                            .build();
                } else {
                    break;
                }
            }

            return blobs;
        }

        final ArrayList<Blob> blobs = new ArrayList<>();
        ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder().bucket(config.bucket());

        if (config.prefix() != null) {
            final String prefixed =
                    request.getPrefix() == null ? config.prefix() : toPrefixedString(request.getPrefix());

            builder.prefix(prefixed);
        } else {
            builder.prefix(request.getPrefix());
        }

        ListObjectsV2Request listObjectsV2Request = builder.build();
        while (true) {
            final ListObjectsV2Response resp = client.listObjectsV2(listObjectsV2Request);
            try (final Stream<S3Object> stream = resp.contents().parallelStream()) {
                final List<Blob> newBlobs = stream.map(obj -> {
                            try {
                                return fromS3Object(obj);
                            } catch (IOException e) {
                                // Do we throw or return null? I have no idea to be honest...
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                blobs.addAll(newBlobs);
            }

            if (resp.nextContinuationToken() == null) break;
            listObjectsV2Request = listObjectsV2Request.toBuilder()
                    .continuationToken(resp.nextContinuationToken())
                    .build();
        }

        return blobs;
    }

    /**
     * Refer to {@link #blobs(ListBlobsRequest)} on how this method works.
     *
     * @return A {@link List<Blob> list of blobs}
     */
    @Override
    public List<Blob> blobs() throws IOException {
        return blobs(null);
    }

    /**
     * Uploads a file to the given storage provider with the given {@link UploadRequest upload request}. If the
     * contents exceed over >=50MB, then the storage provider will attempt to do a multipart request on some implementations.
     *
     * @param request The request options object
     * @throws IOException If any I/O exceptions had occurred while uploading the file.
     */
    @Override
    public void upload(UploadRequest request) throws IOException {
        LOG.debug("Uploading object in path [{}] with content type [{}]", request.path(), request.contentType());
        try (final InputStream stream = request.inputStream()) {
            if (stream.available() == 0)
                throw new IllegalStateException(
                        "InputStream was previously consumed, please create a new one that has proper data");

            final byte[] bytes = stream.readAllBytes();
            client.putObject(
                    (builder) -> {
                        builder.contentLength((long) bytes.length);
                        builder.contentType(request.contentType());
                        builder.bucket(config.bucket());
                        builder.key(toPrefixedString(request.path()));
                        builder.acl(config.defaultObjectAcl());
                    },
                    RequestBody.fromBytes(bytes));
        }
    }

    /**
     * Checks if the given <code>path</code> exists on the storage server.
     *
     * @param path The given relative (or absolute) path.
     * @return {@link Boolean boolean} if it exists or not.
     */
    @Override
    public boolean exists(String path) {
        try {
            final HeadObjectResponse resp = client.headObject((builder) -> {
                builder.bucket(config.bucket());
                builder.key(toPrefixedString(path));
            });

            final Boolean deleteMarker = resp.deleteMarker();
            if (deleteMarker != null) return !deleteMarker;

            // assume it is true
            return true;
        } catch (NoSuchKeyException ignored) {
            return false;
        }
    }

    /**
     * Deletes the given <code>path</code> from the storage service.
     *
     * @param path The given relative (or absolute) path.
     * @return {@link Boolean boolean} if it was deleted or not.
     */
    @Override
    public boolean delete(String path) throws IOException {
        return client.deleteObject((builder) -> {
                    builder.bucket(config.bucket());
                    builder.key(toPrefixedString(path));
                })
                .deleteMarker();
    }

    /**
     * Opens a file from the given <code>path</code> and returns a {@link InputStream stream} of the object's contents, if any. This
     * method can also return <code>null</code> if the given <code>path</code> was not found.
     *
     * @param path The relative (or absolute) path.
     * @return {@link InputStream} if the <code>path</code> exists on the storage service, otherwise <code>null</code>.
     */
    @Override
    public @Nullable InputStream open(String path) {
        return client.getObject(
                (builder) -> {
                    builder.bucket(config.bucket());
                    builder.key(toPrefixedString(path));
                },
                ResponseTransformer.toInputStream());
    }

    /**
     * Returns the content type of this {@link InputStream}.
     * @param stream The stream to check the content type of
     */
    @Override
    public @Nullable String getContentTypeOf(InputStream stream) throws IOException {
        return TIKA.detect(stream);
    }

    /**
     * Returns the content type of the given byte contents.
     * @param bytes Byte array to use
     */
    @Override
    public @Nullable String getContentTypeOf(byte[] bytes) throws IOException {
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            return getContentTypeOf(stream);
        }
    }

    /**
     * Returns the content type of this {@link ByteBuffer}.
     * @param buffer {@link ByteBuffer} to use.
     */
    @Override
    public @Nullable String getContentTypeOf(ByteBuffer buffer) throws IOException {
        // We need to convert the ByteBuffer into a InputStream.
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(buffer.array())) {
            return getContentTypeOf(stream);
        }
    }

    /**
     * This method does the initialization part, if necessary.
     */
    @Override
    public void init() {
        if (client != null) {
            LOG.warn("S3 client has already been initialized!");
            return;
        }

        LOG.info("Initializing Amazon S3 storage service...");
        LOG.debug(format("* enableSignerV4Requests = %s", config.enableSignerV4Requests()));
        LOG.debug(format("* enforcePathAccessStyle = %s", config.enforcePathAccessStyle()));
        LOG.debug(format("* defaultObjectAcl       = %s", config.defaultObjectAcl()));
        LOG.debug(format("* defaultBucketAcl       = %s", config.defaultBucketAcl()));
        LOG.debug(format(
                "* secretAccessKey        = %s",
                "*".repeat(config.secretAccessKey().length())));

        LOG.debug(format(
                "* accessKeyId            = %s", "*".repeat(config.accessKeyId().length())));

        LOG.debug(format(
                "* endpoint               = %s",
                config.endpoint() == null ? "https://s3.amazonaws.com" : config.endpoint()));

        LOG.debug(format("* region                 = %s", config.region()));
        LOG.debug(format("* prefix                 = %s", config.prefix() == null ? "(none)" : config.prefix()));
        LOG.debug(format("* bucket                 = %s", config.bucket()));

        final S3ClientBuilder builder = S3Client.builder().region(config.region());
        if (config.enforcePathAccessStyle()) {
            builder.serviceConfiguration((b) -> b.pathStyleAccessEnabled(true));
        }

        builder.credentialsProvider(StaticCredentialsProvider.create(new AwsCredentials() {
            @Override
            public String accessKeyId() {
                return config.accessKeyId();
            }

            @Override
            public String secretAccessKey() {
                return config.secretAccessKey();
            }
        }));

        if (config.endpoint() != null) {
            final String endpoint = config.endpoint();
            final URI uri = URI.create(endpoint);

            builder.endpointOverride(uri);
        }

        client = builder.build();
        LOG.info(format("Created S3 client! Checking if bucket [%s] exists...", config.bucket()));

        final Optional<Bucket> resp = client.listBuckets().buckets().stream()
                .filter(f -> f.name().equals(config.bucket()))
                .findAny();

        if (resp.isEmpty()) {
            LOG.warn(format("Bucket [%s] doesn't exist, creating!", config.bucket()));
            client.createBucket((b) -> {
                b.acl(config.defaultBucketAcl());
                b.bucket(config.bucket());
            });

            LOG.info(format("Bucket [%s] was not created!~", config.bucket()));
        }
    }

    /**
     * Returns the name of this {@link StorageService}.
     */
    @Override
    public @NotNull String name() {
        return "remi:s3";
    }

    /**
     * Returns the configuration object of this {@link StorageService}.
     */
    @Override
    public @NotNull AmazonS3StorageConfig config() {
        return config;
    }

    private String toPrefixedString(String key) {
        if (config.prefix() == null) return key;

        String transformed = key;
        if ((key.charAt(0) == '.' || key.charAt(1) == '~') && key.charAt(1) == '/') {
            transformed = key.substring(1);
        }

        return format("%s/%s", config.prefix(), transformed);
    }

    @Nullable
    private Blob fromS3Object(S3Object obj) throws IOException {
        if (obj.key().endsWith("/")) return null;

        LOG.debug("");
        return blob(obj.key());
    }
}
