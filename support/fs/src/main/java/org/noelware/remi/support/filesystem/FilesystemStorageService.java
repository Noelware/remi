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

package org.noelware.remi.support.filesystem;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.ListBlobsRequest;
import org.noelware.remi.core.StorageService;
import org.noelware.remi.core.UploadRequest;
import org.noelware.remi.core.contenttype.ContentTypeResolver;
import org.noelware.remi.core.contenttype.TikaContentTypeResolver;
import org.noelware.remi.support.filesystem.stats.FilesystemStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an implementation of the {@link StorageService} for the local filesystem
 */
public class FilesystemStorageService implements StorageService<FilesystemStorageConfig> {
    protected final ContentTypeResolver contentTypeResolver = new TikaContentTypeResolver();
    private final FilesystemStorageConfig config;
    private final Logger LOG = LoggerFactory.getLogger(FilesystemStorageService.class);

    /**
     * Initializes a new {@link FilesystemStorageService}.
     * @param directory The directory to look up objects in
     */
    public FilesystemStorageService(String directory) {
        this(new FilesystemStorageConfig(directory));
    }

    /**
     * Initializes a new {@link FilesystemStorageService}.
     * @param config The configuration object
     */
    public FilesystemStorageService(FilesystemStorageConfig config) {
        this.config = config;
    }

    /**
     * Returns a normalized specified <code>path</code> with the following rules:
     * <ul>
     *     <li>If the given <code>path</code> had the prefix of <code>./</code>, then the path will be replaced with the configuration's directory with the given path specified.</li>
     *     <li>If the given <code>path</code> had the prefix of <code>~/</code>, then the path will be replaced with the user (who is running this JVM application)'s home directory with the given path specified.</li>
     *     <li>Otherwise, it will just return the <code>path</code> if both clauses above was not met.</li>
     * </ul>
     *
     * @param path The given <code>path</code> to normalize
     * @throws NullPointerException if <code>path</code> was null
     * @return normalized path
     */
    public String normalizePath(String path) {
        if (path == null) throw new NullPointerException("path");
        if (path.equals(config.directory())) {
            try {
                return Paths.get(config.directory()).toRealPath().toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (path.startsWith("./")) {
            return normalizePath(config.directory()) + path.substring(1);
        }

        if (path.startsWith("~/")) {
            return System.getProperty("user.home", "/") + path.substring(1);
        }

        return path;
    }

    /**
     * @return the absolute path of the {@link FilesystemStorageConfig#directory() directory} configured.
     */
    public String directory() {
        return normalizePath(config.directory());
    }

    /**
     * @return Same as {@link #directory()} but as an {@link File}.
     */
    public File directoryAsFile() {
        return new File(directory());
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
        final File file = Paths.get(config.directory(), path).toFile();
        if (file.isDirectory()) throw new IllegalArgumentException("Path %s was a directory".formatted(path));
        if (!file.exists()) return null;

        final BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class)
                .readAttributes();

        byte[] data;
        try (final FileInputStream fileChannel = new FileInputStream(file)) {
            data = fileChannel.readAllBytes();
        }

        // Get the content type of the buffer
        final String contentType = contentTypeResolver.resolve(data);
        return new Blob(
                attributes.lastModifiedTime().toInstant(),
                attributes.creationTime().toInstant(),
                contentType,
                new ByteArrayInputStream(data),
                null,
                file.getName(),
                "fs",
                String.format("fs://%s", file),
                attributes.size());
    }

    /**
     * Lists all the {@link Blob blobs} given with a {@link ListBlobsRequest request} object.
     *
     * @param request The request options object, if <code>null</code> was provided, then all the blobs in this
     *                storage service will be retrieved, which might take a while if not using pagination.
     * @return A {@link List<Blob> list of blobs} received from the {@link ListBlobsRequest request}.
     */
    @Override
    public List<Blob> blobs(@Nullable ListBlobsRequest request) throws IOException {
        if (request == null) {
            return Files.walk(Paths.get(config.directory()))
                    .filter(Files::isRegularFile)
                    .map(file -> {
                        final BasicFileAttributes attributes;
                        try {
                            attributes = Files.getFileAttributeView(file, BasicFileAttributeView.class)
                                    .readAttributes();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // Create the ByteBuffer of the file content
                        byte[] data;
                        try (final FileInputStream fileChannel = new FileInputStream(file.toFile())) {
                            data = fileChannel.readAllBytes();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // Get the content type of the buffer
                        final String contentType;
                        try {
                            contentType = contentTypeResolver.resolve(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return new Blob(
                                attributes.lastModifiedTime().toInstant(),
                                attributes.creationTime().toInstant(),
                                contentType,
                                new ByteArrayInputStream(data),
                                null,
                                file.toFile().getName(),
                                "fs",
                                String.format("fs://%s", file),
                                attributes.size());
                    })
                    .toList();
        }

        final RemiFileVisitor visitor = new RemiFileVisitor(this, request);
        final Path directory =
                request.getPrefix() == null ? Path.of(config.directory()) : Path.of(normalizePath(request.getPrefix()));

        Files.walkFileTree(directory, visitor);
        LOG.info(
                "Walked through directory [{}] and visited {} files and failed to visit {} files ({}% succeeded)",
                directory, visitor.visited(), visitor.failed(), (visitor.failed() / visitor.visited()) * 100);

        return visitor.blobs();
    }

    /**
     * Refer to {@link #blobs(ListBlobsRequest)} on how this method works.
     * @return A {@link List<Blob> list of blobs}
     */
    @Override
    public List<Blob> blobs() throws IOException {
        return blobs(null);
    }

    /**
     * Uploads a file to the given storage provider with the given {@link UploadRequest upload request}. If the
     * contents exceed over >=50MB, then the storage provider will attempt to do a multipart request on some implementations.
     *
     * @param request The request options object
     * @throws IOException If any I/O exceptions had occurred while uploading the file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void upload(UploadRequest request) throws IOException {
        final String path = request.path();
        final File file = Paths.get(config.directory(), path).toFile();
        Files.createDirectories(Paths.get(file.getParent()));
        file.createNewFile();

        try (final InputStream stream = request.inputStream();
                final FileOutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[stream.available()];
            int len;

            while ((len = stream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
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
        final File file = Paths.get(config.directory(), path).toFile();
        return file.exists();
    }

    /**
     * Deletes the given <code>path</code> from the storage service.
     *
     * @param path The given relative (or absolute) path.
     * @return {@link Boolean boolean} if it was deleted or not.
     */
    @Override
    public boolean delete(String path) throws IOException {
        final File file = Paths.get(config.directory(), path).toFile();
        LOG.debug("Deleting path [{}]", file);

        if (file.isDirectory()) {
            return deleteDir(file);
        } else {
            return file.delete();
        }
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
        final File file = Paths.get(config.directory(), path).toFile();
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ignored) {
            return null;
        }
    }

    /**
     * This method initializes this {@link StorageService}, if necessary.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void init() {
        final File directory = new File(config.directory());
        if (!directory.exists()) {
            LOG.debug("Directory [{}] does not exist on local disk, creating!", config.directory());
            directory.mkdirs();
        }

        try {
            final FileStore store = Files.getFileStore(directory.toPath());
            if (store.isReadOnly())
                throw new IOException(
                        "Directory [%s] cannot be read-only with this storage service!".formatted(directory));

            LOG.info(
                    "Initialized filesystem storage service on directory [{}] with drive [{} ({})]",
                    directory,
                    store.name(),
                    store.type());
        } catch (IOException e) {
            throw new RuntimeException("Unable to access the file store", e);
        }
    }

    /**
     * @return the name of this {@link StorageService}.
     */
    @Override
    public @NotNull String name() {
        return "remi:filesystem";
    }

    /**
     * @return the configuration object of this {@link StorageService}.
     */
    @Override
    public @NotNull FilesystemStorageConfig config() {
        return config;
    }

    /**
     * @return {@link FilesystemStats statistics} about the storage service
     */
    public @NotNull FilesystemStats stats() {
        return new FilesystemStats(this);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean deleteDir(File file) throws IOException {
        LOG.debug("Deleting directory [{}] recursively", file);

        final File[] contents = file.listFiles();
        if (contents == null) {
            LOG.debug("File content [{}] couldn't list files", file);
            return Files.deleteIfExists(file.toPath().toRealPath());
        }

        if (contents.length == 0) return deleteDir(file);
        for (File f : contents) {
            f.delete();
        }

        final File absolute = file.toPath().toRealPath().toFile();
        return absolute.delete();
    }

    protected String sha1(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }
}
