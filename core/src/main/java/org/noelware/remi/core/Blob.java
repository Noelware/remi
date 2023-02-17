/*
 * 🧶 remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
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

import static java.lang.String.format;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents metadata about a file in the specific storage provider.
 */
public class Blob {
    private final Instant lastModifiedAt;
    private final Instant createdAt;
    private final String contentType;
    private final InputStream inputStream;
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
     * @param stream         Inner {@link InputStream} to read from
     * @param etag           The <code>Etag</code> of this {@link Blob}.
     * @param name           object name
     * @param providerName   The {@link StorageService} provider that this {@link Blob} is contained in
     * @param path           The actual path to this {@link Blob}.
     * @param size           The size of this blob.
     */
    public Blob(
            @Nullable Instant lastModifiedAt,
            @Nullable Instant createdAt,
            @Nullable String contentType,
            @Nullable InputStream stream,
            @Nullable String etag,
            @NotNull String name,
            @NotNull String providerName,
            @NotNull String path,
            long size) {
        this.lastModifiedAt = lastModifiedAt;
        this.contentType = contentType;
        this.inputStream = stream;
        this.createdAt = createdAt;
        this.etag = etag;
        this.name = name;
        this.path = String.format("%s://%s", providerName, path);
        this.size = size;
    }

    /**
     * @return {@link Instant} of when this object was last modified. Can return <code>null</code>
     * if storage implementations don't keep track of this.
     */
    @Nullable
    public Instant lastModifiedAt() {
        return lastModifiedAt;
    }

    /**
     * @return {@link Instant} of when this object was first created. Can return <code>null</code>
     * if any storage implementations don't keep track of it.
     */
    @Nullable
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * @return inner <code>Content-Type</code> of the inner {@link #inputStream() input stream}
     */
    @Nullable
    public String contentType() {
        return contentType;
    }

    /**
     * @return inner data represented as a {@link InputStream}. can return <code>null</code> if this blob
     * was ever called for listing, rather than retrieval.
     */
    @Nullable
    public InputStream inputStream() {
        return inputStream;
    }

    /**
     * @return object name
     */
    @NotNull
    public String name() {
        return name;
    }

    /**
     * @return object path represented as <code>(provider)://(path)</code>, i.e, <code>azure:///some/object</code>
     */
    @NotNull
    public String path() {
        return path;
    }

    /**
     * @return etag for this blob
     */
    @Nullable
    public String etag() {
        return etag;
    }

    /**
     * @return object size
     */
    public long size() {
        return size;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final String IDENT = "  ";

        builder.append("org.noelware.remi.core.Blob {\n");
        builder.append(format(
                "%slastModifiedAt => %s\n", IDENT, lastModifiedAt != null ? lastModifiedAt.toString() : "(unknown)"));
        builder.append(format("%scontentType    => %s\n", IDENT, contentType));
        builder.append(format("%screatedAt      => %s\n", IDENT, createdAt));
        builder.append(format("%setag           => %s\n", IDENT, etag));
        builder.append(format("%sname           => %s\n", IDENT, name));
        builder.append(format("%ssize           => %s\n", IDENT, size));
        builder.append("}");

        return builder.toString();
    }
}
