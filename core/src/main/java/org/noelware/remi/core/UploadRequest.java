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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the underlying {@link InputStream} that will be used to upload
     * to the specific storage implementation.
     */
    @NotNull
    public InputStream inputStream() {
        return inputStream;
    }

    /**
     * Returns the <code>Content-Type</code> header for this {@link UploadRequest}.
     */
    @NotNull
    public String contentType() {
        return contentType == null ? "application/octet-stream" : contentType;
    }

    /**
     * Returns the path to upload this request to.
     */
    public String path() {
        return key;
    }

    public static class Builder {
        private InputStream inputStream = null;
        private String ct = null;
        private String path = null;

        public Builder withContentType(String contentType) {
            if (this.ct != null) throw new IllegalStateException("Content-Type header was already set in this builder");

            this.ct = contentType;
            return this;
        }

        public Builder withInputStream(InputStream stream) {
            if (inputStream != null) throw new IllegalStateException("Input stream was already set in this builder");

            inputStream = stream;
            return this;
        }

        public Builder withInputStream(ByteBuffer buffer) throws IOException {
            final PipedInputStream is = new PipedInputStream();
            try (final PipedOutputStream out = new PipedOutputStream(is)) {
                Channels.newChannel(out).write(buffer);
            }

            return withInputStream(is);
        }

        public Builder withPath(String path) {
            if (this.path != null)
                throw new IllegalStateException("Path to upload was already specified in this builder");

            this.path = path;
            return this;
        }

        public UploadRequest build() {
            if (inputStream == null || path == null)
                throw new IllegalStateException("`inputStream` or `path` was not specified");

            return new UploadRequest(inputStream, ct, path);
        }
    }
}
