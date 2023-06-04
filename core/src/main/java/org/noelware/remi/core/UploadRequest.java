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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a request object for uploading files.
 */
public class UploadRequest {
    private final InputStream inputStream;
    private final String contentType;
    private final String key;

    protected UploadRequest(InputStream stream, String contentType, String key) {
        this.inputStream = stream;
        this.contentType = contentType;
        this.key = key;
    }

    /**
     * Creates a new {@link Builder} to create an {@link UploadRequest}.
     * @return {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Underlying {@link InputStream} that will be used to upload
     * to the specific storage implementation.
     *
     * @return {@link InputStream} that will be used to upload towards
     */
    @NotNull
    public InputStream inputStream() {
        return inputStream;
    }

    /**
     * @return <code>Content-Type</code> header for this {@link UploadRequest}.
     */
    @NotNull
    public String contentType() {
        return contentType == null ? "application/octet-stream" : contentType;
    }

    /**
     * @return Path to upload this request to.
     */
    public String path() {
        return key;
    }

    /**
     * Builder to create a {@link UploadRequest}.
     */
    public static class Builder {
        private InputStream inputStream = null;
        private String ct = null;
        private String path = null;

        /**
         * Sets the content type of this {@link UploadRequest}.
         * @param contentType Valid <code>Content-Type</code> to set
         * @throws IllegalStateException If the <code>Content-Type</code> was already set in this request builder
         * @return this instance to chain methods
         */
        public Builder withContentType(String contentType) {
            if (this.ct != null) throw new IllegalStateException("Content-Type header was already set in this builder");

            this.ct = contentType;
            return this;
        }

        /**
         * Sets the underlying {@link InputStream} to use for uploading to the
         * storage driver.
         *
         * @param stream {@link InputStream} that hasn't been consumed.
         * @return this instance to chain methods
         */
        public Builder withInputStream(InputStream stream) {
            if (inputStream != null) throw new IllegalStateException("Input stream was already set in this builder");

            inputStream = stream;
            return this;
        }

        /**
         * Same as {@link #withInputStream(InputStream)}, but uses a {@link ByteBuffer} as the
         * data which will be transformed into a {@link InputStream}.
         *
         * @param buffer The underlying buffer
         * @return this instance to chain methods
         * @throws IOException if any i/o exception was thrown when transforming the {@link ByteBuffer} -> {@link InputStream}.
         */
        public Builder withInputStream(ByteBuffer buffer) throws IOException {
            final PipedInputStream is = new PipedInputStream();
            try (final PipedOutputStream out = new PipedOutputStream(is)) {
                Channels.newChannel(out).write(buffer);
            }

            return withInputStream(is);
        }

        /**
         * Sets the path to upload this request to. This is used to fetch
         * the {@link Blob} of this uploaded object, if succeeded.
         *
         * @param path Path to insert into
         * @throws IllegalStateException If the path was already set
         * @return this instance to chain methods
         */
        public Builder withPath(String path) {
            if (this.path != null)
                throw new IllegalStateException("Path to upload was already specified in this builder");

            this.path = path;
            return this;
        }

        /**
         * @throws IllegalStateException If the input stream or path were not specified.
         * @return {@link UploadRequest} that was built.
         */
        public UploadRequest build() {
            if (inputStream == null || path == null)
                throw new IllegalStateException("`inputStream` or `path` was not specified");

            return new UploadRequest(inputStream, ct, path);
        }
    }
}
