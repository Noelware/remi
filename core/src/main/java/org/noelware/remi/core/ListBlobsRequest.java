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

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a request object for {@link StorageService#blobs(ListBlobsRequest) listing blobs}.
 */
public class ListBlobsRequest {
    private final List<String> extensions;
    private final List<String> exclude;
    private final String prefix;

    protected ListBlobsRequest(List<String> extensions, List<String> exclude, String prefix) {
        this.extensions = extensions;
        this.exclude = exclude;
        this.prefix = prefix;
    }

    /**
     * Returns a list of extensions to allow when listing blobs.
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * Returns a list of objects (file paths, glob patterns) to exclude from the final result
     * when listing blobs.
     */
    public List<String> getExclude() {
        return exclude;
    }

    /**
     * Returns a prefix to use when listing out blobs.
     */
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    public static class Builder {
        private final ArrayList<String> extensions = new ArrayList<>();
        private final ArrayList<String> excludes = new ArrayList<>();
        private String prefix;

        /**
         * Excludes (filters out) any objects that can be excluded. This can be represented
         * as file paths, glob patterns, and much more.
         *
         * @param excluded A list of objects that could be excluded.
         */
        public Builder exclude(String... excluded) {
            this.excludes.addAll(List.of(excluded));
            return this;
        }

        public Builder withExtensions(String... extensions_) {
            this.extensions.addAll(List.of(extensions_));
            return this;
        }

        public Builder withPrefix(String prefix_) {
            this.prefix = prefix_;
            return this;
        }

        public ListBlobsRequest build() {
            return new ListBlobsRequest(extensions, excludes, prefix);
        }
    }
}
