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
