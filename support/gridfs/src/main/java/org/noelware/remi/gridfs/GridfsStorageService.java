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

package org.noelware.remi.gridfs;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.*;
import org.noelware.remi.core.contenttype.ContentTypeResolver;
import org.noelware.remi.core.contenttype.TikaContentTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridfsStorageService implements StorageService<Configuration.None> {
    private final ContentTypeResolver contentTypeResolver = new TikaContentTypeResolver();
    private final Logger LOG = LoggerFactory.getLogger(GridfsStorageService.class);
    private final GridFS inner;

    public GridfsStorageService(DB db, String bucket) {
        this.inner = new GridFS(db, bucket);
    }

    @Override
    public Blob blob(String path) {
        final GridFSDBFile file = this.inner.findOne(path);
        if (file == null) return null;

        return new Blob(
                null,
                file.getUploadDate().toInstant(),
                file.getContentType(),
                file.getInputStream(),
                null,
                file.getFilename(),
                "gridfs",
                "gridfs://%s".formatted(file.getFilename()),
                file.getLength());
    }

    @Override
    public List<Blob> blobs(@Nullable ListBlobsRequest request) throws IOException {
        return List.of();
    }

    @Override
    public List<Blob> blobs() throws IOException {
        return blobs(null);
    }

    @Override
    public void upload(UploadRequest request) throws IOException {}

    @Override
    public boolean exists(String path) {
        return false;
    }

    @Override
    public boolean delete(String path) throws IOException {
        return false;
    }

    @Override
    public @Nullable InputStream open(String path) {
        return null;
    }

    @Override
    public @NotNull String name() {
        return "remi:gridfs";
    }

    @Override
    public @NotNull Configuration.None config() {
        return new Configuration.None();
    }
}
