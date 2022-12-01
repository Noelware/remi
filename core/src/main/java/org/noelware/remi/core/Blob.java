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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents metadata about a file in the specific storage provider.
 */
public class Blob {
    private final AtomicBoolean _read = new AtomicBoolean(false);

    private final Instant lastModifiedAt;
    private final Instant createdAt;
    private final String contentType;
    private final ByteBuffer buffer;
    private final String etag;
    private final String name;
    private final String path;
    private final long size;

    /**
     * Constructs a {@link Blob}.
     *
     * @param lastModifiedAt {@link LocalDateTime} of when this object was last modified at
     * @param createdAt      {@link LocalDateTime} of when this object was created at
     * @param contentType    The <code>Content-Type</code> of this {@link Blob}.
     * @param buffer         The underlying {@link ByteBuffer} that contains the file contents itself.
     * @param etag           The <code>Etag</code> of this {@link Blob}.
     * @param name           object name
     * @param providerName   The {@link StorageService} provider that this {@link Blob} is contained in
     * @param path           The actual path to this {@link Blob}.
     * @param size           The size of this blob.
     */
    public Blob(
            Instant lastModifiedAt,
            Instant createdAt,
            String contentType,
            ByteBuffer buffer,
            String etag,
            String name,
            String providerName,
            String path,
            long size) {
        this.lastModifiedAt = lastModifiedAt;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.buffer = buffer;
        this.etag = etag;
        this.name = name;
        this.path = String.format("%s://%s", providerName, path);
        this.size = size;
    }

    /**
     * Returns the underlying buffer as an {@link OutputStream}.
     * @return {@link OutputStream} of the underlying source
     * @throws IOException if writing to the output stream has failed
     * @throws IllegalStateException if earlier invocations to {@link #toOutputStream()} was used.
     */
    public OutputStream toOutputStream() throws IOException {
        if (_read.get())
            throw new IllegalStateException("Blob already has been read from an earlier #toOutputStream invocation.");
        _read.set(true);

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Channels.newChannel(stream).write(buffer);

        return stream;
    }

    /**
     * Returns the {@link LocalDateTime} of when this object was last modified. Can return <code>null</code>
     * if storage implementations don't keep track of this.
     */
    @Nullable
    public Instant lastModifiedAt() {
        return lastModifiedAt;
    }

    @Nullable
    public Instant createdAt() {
        return createdAt;
    }

    @Nullable
    public String contentType() {
        return contentType;
    }

    @NotNull
    public ByteBuffer data() {
        return buffer;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public String path() {
        return path;
    }

    public long size() {
        return size;
    }
}
