/*
 * 🧶 remi: Simple Java library to handle communication between applications and storage providers.
 * Copyright (c) 2022-2023 Noelware, LLC. <team@noelware.org>
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

package org.noelware.remi.support.gcs;

import static java.lang.String.format;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.ListBlobsRequest;
import org.noelware.remi.core.StorageService;
import org.noelware.remi.core.UploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCloudStorageService implements StorageService<GoogleCloudStorageConfig> {
    private final Logger LOG = LoggerFactory.getLogger(GoogleCloudStorageService.class);
    private final GoogleCloudStorageConfig config;
    private Storage storage;
    private Bucket bucket;

    public GoogleCloudStorageService(GoogleCloudStorageConfig config) {
        this.config = config;
    }

    @Override
    public Blob blob(String path) {
        final com.google.cloud.storage.Blob blob = bucket.get(path);
        if (!blob.exists()) return null;

        final byte[] bytes = blob.getContent();
        final BlobInfo info = blob.asBlobInfo();

        return new Blob(
                info.getUpdateTimeOffsetDateTime().toInstant(),
                info.getCreateTimeOffsetDateTime().toInstant(),
                info.getContentType(),
                new ByteArrayInputStream(bytes),
                info.getEtag(),
                info.getName(),
                "gcs",
                format("gcs://%s", info.getName()),
                info.getSize());
    }

    @Override
    public List<Blob> blobs(@Nullable ListBlobsRequest request) throws IOException {
        if (request == null) {
            final ArrayList<Blob> allBlobs = new ArrayList<>();
            final Page<com.google.cloud.storage.Blob> blobs = bucket.list();

            for (com.google.cloud.storage.Blob blob : blobs.iterateAll()) {
                final byte[] bytes = blob.getContent();
                final BlobInfo info = blob.asBlobInfo();

                allBlobs.add(new Blob(
                        info.getUpdateTimeOffsetDateTime().toInstant(),
                        info.getCreateTimeOffsetDateTime().toInstant(),
                        info.getContentType(),
                        new ByteArrayInputStream(bytes),
                        info.getEtag(),
                        info.getName(),
                        "gcs",
                        format("gcs://%s", info.getName()),
                        info.getSize()));
            }

            return allBlobs;
        }

        throw new IOException(new Exception("#blobs(ListBlobsRequest) is not supported at this time"));
    }

    @Override
    public List<Blob> blobs() throws IOException {
        return blobs(null);
    }

    @Override
    public void upload(UploadRequest request) throws IOException {
        final com.google.cloud.storage.Blob blob = bucket.get(request.path());
        if (blob.exists()) return;

        try (final InputStream stream = request.inputStream()) {
            if (stream.available() == 0)
                throw new IllegalStateException(
                        "InputStream was previously consumed, please create a new one that has proper data");

            final byte[] bytes = stream.readAllBytes();
            storage.create(
                    BlobInfo.newBuilder(config.bucketName(), request.path())
                            .setContentType(request.contentType())
                            .build(),
                    new ByteArrayInputStream(bytes));
        }
    }

    @Override
    public boolean exists(String path) {
        return bucket.get(path).exists();
    }

    @Override
    public boolean delete(String path) {
        if (!exists(path)) return false;

        return bucket.get(path).delete();
    }

    @Override
    public @Nullable InputStream open(String path) {
        final com.google.cloud.storage.Blob blob = bucket.get(path);
        if (!blob.exists()) return null;

        return new ByteArrayInputStream(blob.getContent());
    }

    @Override
    public void init() {
        if (storage != null && bucket != null) {
            LOG.warn("Google Cloud Storage service was already initialized!");
            return;
        }

        LOG.info(format(
                "Initializing Google Cloud Storage service with project ID [%s] in bucket [%s]",
                config.projectId(), config.bucketName()));

        final StorageOptions.Builder optionsBuilder =
                StorageOptions.newBuilder().setCredentials(config.credentials()).setProjectId(config.projectId());

        if (config.hostName() != null) optionsBuilder.setHost(config.hostName());

        final StorageOptions options = optionsBuilder.build();
        LOG.debug(format(
                "Connected to Google Cloud Storage with host [%s] on project ID [%s]/bucket [%s] (app name: %s/user agent: %s)",
                options.getHost(),
                config.projectId(),
                config.bucketName(),
                options.getApplicationName(),
                options.getUserAgent()));

        storage = options.getService();
        LOG.info(format("Creating bucket [%s] if it doesn't exist...", config.bucketName()));

        final Page<Bucket> buckets = storage.list();
        boolean exists = false;

        for (Bucket b : buckets.iterateAll()) {
            if (b.getName().equals(config.bucketName())) {
                bucket = b;
                exists = true;
                break;
            }
        }

        LOG.info(format(
                exists
                        ? "Bucket [%s] exists already, not re-creating..."
                        : "Bucket [%s] doesn't exist, will be creating!",
                config.bucketName()));
        if (!exists) {
            bucket = storage.create(BucketInfo.of(config.bucketName()));
        }
    }

    @Override
    public @NotNull String name() {
        return "remi:gcs";
    }

    @Override
    public @NotNull GoogleCloudStorageConfig config() {
        return config;
    }
}
