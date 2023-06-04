package org.noelware.remi.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.contenttype.ContentTypeResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an interface to perform non-blocking calls from a {@link StorageService}, which will return
 * {@link java.util.concurrent.CompletableFuture completable futures} instead of normal return types.
 */
public interface AsyncStorageService<C extends Configuration> {
    /**
     * @return {@link ContentTypeResolver} that is used to resolve content types
     * from buffers or streams.
     */
    @NotNull
    ContentTypeResolver contentTypeResolver();

    /**
     * Sets the content type resolver for this {@link AsyncStorageService}.
     * @param resolver The resolver to set. Cannot be null.
     */
    void setContentTypeResolver(@NotNull ContentTypeResolver resolver);

    /**
     * Returns a {@link CompletableFuture<Blob> Blob Future} from the given <code>path</code> specified.
     * This method can return <code>null</code> if the blob was not found.
     *
     * @param path The relative (or absolute) path to get the blob from
     * @return {@link CompletableFuture<Blob> Blob future} to resolve a {@link Blob} object.
     */
    CompletableFuture<Blob> blob(String path);

    CompletableFuture<List<Blob>> blobs(@Nullable ListBlobsRequest request);
    CompletableFuture<List<Blob>> blobs();
    CompletableFuture<Void> upload(@NotNull UploadRequest request);
    CompletableFuture<Boolean> exists(@NotNull String path);
    CompletableFuture<Boolean> delete(@NotNull String path);
    CompletableFuture<InputStream> open(@NotNull String path);

    /**
     * This method does the initialization part, if necessary.
     */
    default void init() {}

    /**
     * Returns the name of this {@link StorageService}.
     */
    @NotNull
    String name();

    /**
     * Returns the configuration object of this {@link StorageService}.
     */
    @NotNull
    C config();
}
