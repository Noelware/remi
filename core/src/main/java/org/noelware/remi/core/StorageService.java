/*
 * 🧶 Remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022 Noelware <team@noelware.org>
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StorageService<C extends Configuration> {
    /**
     * Returns a {@link Blob} from the given <code>path</code> specified. This method can return
     * <code>null</code> if the blob was not found.
     *
     * @param path The relative (or absolute) path to get the blob from
     * @return {@link Blob} object if any, or <code>null</code>
     */
    Blob blob(String path) throws IOException;

    /**
     * Lists all the {@link Blob blobs} given with a {@link ListBlobsRequest request} object.
     * @param request The request options object, if <code>null</code> was provided, then all the blobs in this
     *                storage service will be retrieved, which might take a while if not using pagination.
     *
     * @return A {@link List<Blob> list of blobs} received from the {@link ListBlobsRequest request}.
     */
    List<Blob> blobs(@Nullable ListBlobsRequest request) throws IOException;

    /**
     * Refer to {@link #blobs(ListBlobsRequest)} on how this method works.
     * @return A {@link List<Blob> list of blobs}
     */
    List<Blob> blobs() throws IOException;

    /**
     * Uploads a file to the given storage provider with the given {@link UploadRequest upload request}. If the
     * contents exceed over >=50MB, then the storage provider will attempt to do a multipart request on some implementations.
     *
     * @param request The request options object
     * @throws IOException If any I/O exceptions had occurred while uploading the file.
     */
    void upload(UploadRequest request) throws IOException;

    /**
     * Checks if the given <code>path</code> exists on the storage server.
     * @param path The given relative (or absolute) path.
     * @return {@link Boolean boolean} if it exists or not.
     */
    boolean exists(String path);

    /**
     * Deletes the given <code>path</code> from the storage service.
     * @param path The given relative (or absolute) path.
     * @return {@link Boolean boolean} if it was deleted or not.
     */
    boolean delete(String path) throws IOException;

    /**
     * Opens a file from the given <code>path</code> and returns a {@link InputStream stream} of the object's contents, if any. This
     * method can also return <code>null</code> if the given <code>path</code> was not found.
     * @param path The relative (or absolute) path.
     * @return {@link InputStream} if the <code>path</code> exists on the storage service, otherwise <code>null</code>.
     */
    @Nullable
    InputStream open(String path);

    /**
     * Returns the content type of this {@link InputStream}.
     * @param stream The stream to check the content type of
     */
    @Nullable
    String getContentTypeOf(InputStream stream) throws IOException;

    /**
     * Returns the content type of the given byte contents.
     * @param bytes Byte array to use
     */
    @Nullable
    String getContentTypeOf(byte[] bytes) throws IOException;

    /**
     * Returns the content type of this {@link ByteBuffer}.
     * @param buffer {@link ByteBuffer} to use.
     */
    @Nullable
    String getContentTypeOf(ByteBuffer buffer) throws IOException;

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
