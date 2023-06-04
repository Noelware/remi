package org.noelware.remi.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;

public class DirectoryBlob extends Blob {
    /**
     * Constructs a {@link DirectoryBlob}.
     *
     * @param lastModifiedAt {@link Instant} of when this object was last modified at
     * @param createdAt      {@link Instant} of when this object was created at
     * @param name           object name
     * @param providerName   The {@link StorageService} provider that this {@link Blob} is contained in
     * @param path           The actual path to this {@link Blob}.
     */
    public DirectoryBlob(
            @Nullable Instant lastModifiedAt,
            @Nullable Instant createdAt,
            @NotNull String name,
            @NotNull String providerName,
            @NotNull String path
    ) {
        super(lastModifiedAt, createdAt, null, null, null, name, providerName, path, 0);
    }
}
