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

package org.noelware.remi.core.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.AsyncStorageService;
import org.noelware.remi.core.Configuration;
import org.noelware.remi.core.contenttype.ContentTypeResolver;
import org.noelware.remi.core.contenttype.TikaContentTypeResolver;

/**
 * Common abstraction for a {@link AsyncStorageService} to implement a default {@link ContentTypeResolver}.
 */
public abstract class AbstractAsyncStorageService<C extends Configuration> implements AsyncStorageService<C> {
    private ContentTypeResolver contentTypeResolver;

    /**
     * Constructs a new {@link AbstractAsyncStorageService}, but uses the default {@link TikaContentTypeResolver}
     * content type resolver.
     */
    public AbstractAsyncStorageService() {
        this(null);
    }

    /**
     * Constructs a new {@link AbstractAsyncStorageService}, but sets a {@link ContentTypeResolver}.
     * @param resolver The resolver, if null, then it will use the default {@link TikaContentTypeResolver}.
     */
    public AbstractAsyncStorageService(@Nullable ContentTypeResolver resolver) {
        this.contentTypeResolver = resolver != null ? resolver : new TikaContentTypeResolver();
    }

    @Override
    public @NotNull ContentTypeResolver contentTypeResolver() {
        return contentTypeResolver;
    }

    @Override
    public void setContentTypeResolver(@NotNull ContentTypeResolver resolver) {
        this.contentTypeResolver = resolver;
    }
}
