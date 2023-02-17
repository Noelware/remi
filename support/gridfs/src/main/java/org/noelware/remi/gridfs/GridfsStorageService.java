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

package org.noelware.remi.gridfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.*;

public class GridfsStorageService implements StorageService<Configuration.None> {
    @Override
    public Blob blob(String path) throws IOException {
        return null;
    }

    @Override
    public List<Blob> blobs(@Nullable ListBlobsRequest request) throws IOException {
        return null;
    }

    @Override
    public List<Blob> blobs() throws IOException {
        return null;
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
