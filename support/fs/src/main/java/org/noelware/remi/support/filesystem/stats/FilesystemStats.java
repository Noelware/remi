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

package org.noelware.remi.support.filesystem.stats;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import org.noelware.remi.support.filesystem.FilesystemStorageService;

/**
 * Represents all the statistics from the {@link FilesystemStorageService}.
 */
public class FilesystemStats {
    private final FileStore fileStore;

    public FilesystemStats(FilesystemStorageService service) {
        try {
            fileStore = Files.getFileStore(service.directoryAsFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long usableSpace() {
        try {
            return fileStore.getUsableSpace();
        } catch (IOException ignored) {
            return -1;
        }
    }

    public long unallocatedSpace() {
        try {
            return fileStore.getUnallocatedSpace();
        } catch (IOException ignored) {
            return -1;
        }
    }

    public long totalSpace() {
        try {
            return fileStore.getTotalSpace();
        } catch (IOException ignored) {
            return -1;
        }
    }

    public String type() {
        return fileStore.type();
    }

    public String drive() {
        return fileStore.name();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("org.noelware.remi.support.filesystem.stats.FilesystemStats{\n");
        {
            builder.append(format("\tunallocatedSpace => %d\n", unallocatedSpace()));
            builder.append(format("\tusableSpace      => %d\n", usableSpace()));
            builder.append(format("\ttotalSpace       => %d\n", totalSpace()));
            builder.append(format("\tdrive            => %s [%s]\n", drive(), type()));
        }

        builder.append("}");

        return builder.toString();
    }
}
