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

package org.noelware.remi.testing.support.azure;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.UploadRequest;
import org.noelware.remi.support.azure.AzureBlobStorageConfig;
import org.noelware.remi.support.azure.AzureBlobStorageService;
import org.noelware.remi.support.azure.authentication.AzureConnectionStringAuth;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
public class AzureBlobStorageServiceTests {
    @Container
    private static final GenericContainer<?> azuriteContainer = new GenericContainer<>(
                    DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.20.1"))
            .withCommand("azurite-blob", "--blobHost", "0.0.0.0")
            .withExposedPorts(10000);

    private String getTestConnectionString() {
        return format(
                "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://%s:%d/devstoreaccount1",
                azuriteContainer.getHost(), azuriteContainer.getMappedPort(10000));
    }

    private void withAzureStorageService(Consumer<AzureBlobStorageService> serviceConsumer) {
        final AzureBlobStorageConfig config = new AzureBlobStorageConfig(
                "test-container",
                format(
                        "http://%s:%d/devstoreaccount1",
                        azuriteContainer.getHost(), azuriteContainer.getMappedPort(10000)),
                new AzureConnectionStringAuth(getTestConnectionString()));

        final AzureBlobStorageService service = new AzureBlobStorageService(config);
        service.init();

        serviceConsumer.accept(service);
    }

    @Test
    public void test_canWeConnect() {
        withAzureStorageService(service -> {
            final List<Blob> blobs = assertDoesNotThrow(() -> service.blobs());
            assertEquals(0, blobs.size());
        });
    }

    @Test
    public void test_canWeUploadContent() {
        withAzureStorageService(service -> {
            assertDoesNotThrow(() -> service.upload(UploadRequest.builder()
                    .withPath("wuff.json")
                    .withContentType("application/json")
                    .withInputStream(new ByteArrayInputStream("{\"wuffs\":true}".getBytes(StandardCharsets.UTF_8)))
                    .build()));

            final List<Blob> blobs = assertDoesNotThrow(() -> service.blobs());
            assertEquals(1, blobs.size());
        });
    }
}
