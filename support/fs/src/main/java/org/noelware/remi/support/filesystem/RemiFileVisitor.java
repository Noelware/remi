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

package org.noelware.remi.support.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.ListBlobsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link java.nio.file.SimpleFileVisitor<java.nio.file.Path> file visitor} implementation
 * for the specific use cases for this storage service.
 */
public class RemiFileVisitor extends SimpleFileVisitor<Path> {
    private final Logger LOG = LoggerFactory.getLogger(RemiFileVisitor.class);
    private final ArrayList<Blob> blobs = new ArrayList<>();
    private final FilesystemStorageService service;
    private final ListBlobsRequest request;

    private int visited = 0;
    private int failed = 0;

    protected RemiFileVisitor(FilesystemStorageService service, ListBlobsRequest request) {
        this.service = service;
        this.request = request;
    }

    /**
     * Returns how many times this {@link RemiFileVisitor file visitor} has failed to
     * look up a path.
     */
    public int failed() {
        return failed;
    }

    /**
     * Returns how many times this {@link RemiFileVisitor file visitor} has visited in the
     * tree.
     */
    public int visited() {
        return visited;
    }

    /**
     * Returns all the collected blobs in this {@link RemiFileVisitor file visitor}.
     */
    public List<Blob> blobs() {
        return Collections.unmodifiableList(blobs);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOG.warn("Failed to visit file [{}] due to an unknown exception", file, exc);
        failed++;

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOG.debug(
                "Visiting file [{}] in current tree [isSymbolicLink={}, isDirectory={}, isFile={}]",
                file,
                attrs.isSymbolicLink(),
                attrs.isDirectory(),
                attrs.isRegularFile());

        if (attrs.isDirectory()) {
            LOG.debug("Skipping directory [{}]", file);
            return FileVisitResult.CONTINUE;
        }

        visited++;
        final String dir = request.getPrefix() == null ? service.config().directory() : request.getPrefix();
        final String result = service.normalizePath(dir + file);

        if (request.getExclude().contains(result)) {
            LOG.debug("Excluding file [{}] due to it being set", result);
            return FileVisitResult.CONTINUE;
        }

        final String name = file.getName(file.getNameCount() - 1).toString();
        final String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";

        if (!request.getExtensions().isEmpty() && !request.getExtensions().contains(ext)) {
            LOG.debug(
                    "Excluding file [{}] due to not having any of the following extensions: {}",
                    file,
                    request.getExtensions());

            return FileVisitResult.CONTINUE;
        }

        // Create the ByteBuffer of the file content
        byte[] data;
        try (final FileInputStream fileChannel = new FileInputStream(file.toFile())) {
            data = fileChannel.readAllBytes();
        }

        // Get the content type of the buffer
        final String contentType = service.getContentTypeOf(data);

        // Create the Etag for this file
        final String etag =
                "\"%s-%s\"".formatted(Long.toString(16), service.sha1(data).substring(0, 27));

        blobs.add(new Blob(
                attrs.lastModifiedTime().toInstant(),
                attrs.creationTime().toInstant(),
                contentType,
                new ByteArrayInputStream(data),
                etag,
                name,
                "fs",
                String.format("fs://%s", file),
                attrs.size()));

        return FileVisitResult.CONTINUE;
    }
}
