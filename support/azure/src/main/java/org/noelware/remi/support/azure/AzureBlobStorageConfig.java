/*
 * 🧶 Remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022 Noelware <team@noelware.org>
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

package org.noelware.remi.support.azure;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
import org.noelware.remi.core.Configuration;

/**
 * Represents the configuration for the {@link AzureBlobStorageService}.
 */
public class AzureBlobStorageConfig implements Serializable, Configuration {
    private final String containerName;
    private final String endpoint;
    private final String sasToken;

    /**
     * Creates a new {@link AzureBlobStorageConfig} object.
     *
     * @param containerName The name of the container to use for the storage service.
     * @param endpoint The endpoint URL to use. If this doesn't end with <code>.blob.core.windows.net</code>,
     *                 the {@link #endpoint()} method will do it for  you.
     *
     * @param sasToken The token to use for authenticating to Azure Blob Storage. Refer to the <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md#authenticate-the-client">official Java SDK for Azure Blob Storage</a>
     */
    public AzureBlobStorageConfig(@NotNull String containerName, @NotNull String endpoint, @NotNull String sasToken) {
        this.containerName = containerName;
        this.endpoint = endpoint;
        this.sasToken = sasToken;
    }

    @NotNull
    public String endpoint() {
        if (!endpoint.endsWith(".blob.core.windows.net")) {
            return String.format("%s.blob.core.windows.net", endpoint);
        }

        return endpoint;
    }

    @NotNull
    public String token() {
        return sasToken;
    }

    @NotNull
    public String containerName() {
        return containerName;
    }
}
