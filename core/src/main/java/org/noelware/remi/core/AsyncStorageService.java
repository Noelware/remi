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

package org.noelware.remi.core;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.contenttype.ContentTypeResolver;

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
