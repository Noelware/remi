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

package org.noelware.remi.support.azure;

import static java.lang.String.format;

import com.azure.core.util.Context;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.ListBlobsRequest;
import org.noelware.remi.core.StorageService;
import org.noelware.remi.core.UploadRequest;
import org.noelware.remi.support.azure.authentication.AzureConnectionStringAuth;
import org.noelware.remi.support.azure.authentication.AzureSasTokenAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureBlobStorageService implements StorageService<AzureBlobStorageConfig> {
    private final Logger LOG = LoggerFactory.getLogger(AzureBlobStorageService.class);
    private final AzureBlobStorageConfig config;
    private final Tika TIKA = new Tika();

    private BlobContainerClient blobContainerClient;
    private BlobServiceClient blobServiceClient;

    public AzureBlobStorageService(@NotNull AzureBlobStorageConfig config) {
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
        final BlobClient client = blobContainerClient.getBlobClient(path);
        if (!client.exists()) return null;

        // Get the input stream from the blob client and read into
        // the new allocated ByteBuffer.
        final BlobInputStream stream = client.openInputStream();
        byte[] data;
        try (stream) {
            data = stream.readAllBytes();
        }

        final String contentType = client.getProperties().getContentType();
        final String etag = client.getProperties().getETag();
        return new Blob(
                client.getProperties().getLastAccessedTime().toInstant(),
                client.getProperties().getCreationTime().toInstant(),
                contentType,
                new ByteArrayInputStream(data),
                etag,
                client.getBlobName(),
                "azure",
                String.format("azure://%s", client.getBlobName()),
                client.getProperties().getBlobSize());
    }

    /**
     * Lists all the {@link Blob blobs} given with a {@link ListBlobsRequest request} object.
     *
     * @param request The request options object, if <code>null</code> was provided, then all the blobs in this
     *                storage service will be retrieved, which might take a while if not using pagination.
     * @return A {@link List<Blob> list of blobs} received from the {@link ListBlobsRequest request}.
     */
    @Override
    public List<Blob> blobs(@Nullable ListBlobsRequest request) {
        final Function<BlobItem, Blob> createBlob = (item) -> {
            // Don't create a `Blob` object on a directory.
            if (item.isPrefix()) return null;
            if (request != null) {
                final String name = item.getName();
                if (!request.getExclude().isEmpty() && request.getExclude().contains(name)) {
                    LOG.warn("Blob with name [{}] is excluded", name);
                    return null;
                }

                if (!request.getExtensions().isEmpty()) {
                    LOG.warn(
                            "Extensions API for ListBlobsRequest is not supported in Azure Blob Storage Service at the moment");
                }
            }

            BlobClient client;
            if (item.getSnapshot() != null) {
                client = blobContainerClient.getBlobClient(item.getName(), item.getSnapshot());
            } else {
                client = blobContainerClient.getBlobClient(item.getName());
            }

            // Get the input stream from the blob client and read into
            // the new allocated ByteBuffer.
            final BlobInputStream stream = client.openInputStream();
            byte[] data;
            try (stream) {
                try {
                    data = stream.readAllBytes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            final String contentType = client.getProperties().getContentType();
            final String etag = client.getProperties().getETag();
            final OffsetDateTime lastAccessed = client.getProperties().getLastAccessedTime();

            return new Blob(
                    lastAccessed != null
                            ? client.getProperties().getLastAccessedTime().toInstant()
                            : null,
                    client.getProperties().getCreationTime().toInstant(),
                    contentType,
                    new ByteArrayInputStream(data),
                    etag,
                    item.getName(),
                    "azure",
                    String.format("azure://%s", item),
                    client.getProperties().getBlobSize());
        };

        if (request == null) {
            final ArrayList<Blob> blobs = new ArrayList<>();
            for (BlobItem item : blobContainerClient.listBlobs()) {
                final Blob blob = createBlob.apply(item);
                if (blob != null) blobs.add(blob);
            }

            return blobs;
        }

        final ArrayList<Blob> blobs = new ArrayList<>();
        final ListBlobsOptions options = new ListBlobsOptions();
        if (request.getPrefix() != null) options.setPrefix(request.getPrefix());

        for (BlobItem item : blobContainerClient.listBlobs(options, null)) {
            final Blob blob = createBlob.apply(item);
            if (blob != null) blobs.add(blob);
        }

        return blobs;
    }

    /**
     * Refer to {@link #blobs(ListBlobsRequest)} on how this method works.
     * @return A {@link List<Blob> list of blobs}
     */
    @Override
    public List<Blob> blobs() {
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
        final String contentType = request.contentType();
        final String path = request.path();

        try (final InputStream stream = request.inputStream()) {
            final BlobParallelUploadOptions options = new BlobParallelUploadOptions(stream);
            options.setHeaders(new BlobHttpHeaders().setContentType(contentType));

            final BlobClient client = blobContainerClient.getBlobClient(path);
            client.uploadWithResponse(options, null, Context.NONE).getValue();
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
        return blobContainerClient.getBlobClient(path).exists();
    }

    /**
     * Deletes the given <code>path</code> from the storage service.
     *
     * @param path The given relative (or absolute) path.
     * @return {@link Boolean boolean} if it was deleted or not.
     */
    @Override
    public boolean delete(String path) {
        return blobContainerClient.getBlobClient(path).deleteIfExists();
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
        return blobContainerClient.getBlobClient(path).openInputStream();
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
        // We already had initialized this storage service if these fields
        // are implemented.
        if (blobContainerClient != null && blobServiceClient != null) return;

        LOG.info("Initializing Azure services client with endpoint [{}]", config.endpoint());
        final BlobServiceClientBuilder builder =
                new BlobServiceClientBuilder().endpoint(format("https://%s", config.endpoint()));

        if (config.auth() instanceof AzureConnectionStringAuth) {
            LOG.info(format(
                    "Using connection string [%s] as authentication",
                    "*".repeat(config.auth().supply().length())));
            builder.connectionString(config.auth().supply());
        } else if (config.auth() instanceof AzureSasTokenAuth) {
            LOG.info(format(
                    "Using generated SAS token [%s] as authentication",
                    "*".repeat(config.auth().supply().length())));
            builder.sasToken(config.auth().supply());
        }

        blobServiceClient = builder.buildClient();

        blobContainerClient = blobServiceClient.getBlobContainerClient(config.containerName());
        LOG.info("Attempting to create container [{}] if it doesn't exist...", config.containerName());
        blobContainerClient.createIfNotExists();
    }

    /**
     * Returns the name of this {@link StorageService}.
     */
    @Override
    public @NotNull String name() {
        return "remi:azure";
    }

    /**
     * Returns the configuration object of this {@link StorageService}.
     */
    @Override
    public @NotNull AzureBlobStorageConfig config() {
        return config;
    }
}
