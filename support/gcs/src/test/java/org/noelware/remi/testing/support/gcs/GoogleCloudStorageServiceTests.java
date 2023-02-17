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

package org.noelware.remi.testing.support.gcs;

import static java.lang.String.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.noelware.remi.core.Blob;
import org.noelware.remi.support.gcs.GoogleCloudStorageConfig;
import org.noelware.remi.support.gcs.GoogleCloudStorageService;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("resource")
@Testcontainers(disabledWithoutDocker = true)
public class GoogleCloudStorageServiceTests {
    private final AtomicReference<GoogleCloudStorageService> service = new AtomicReference<>(null);

    @Container
    private static final GenericContainer<?> container = new GenericContainer<>("fsouza/fake-gcs-server:1.44.0")
            .withExposedPorts(4443)
            .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/bin/fake-gcs-server", "-scheme", "http"));

    // https://github.com/fsouza/fake-gcs-server/tree/main/examples/java#resumable-upload-operations-and-containerised-fake-gcs-server
    @BeforeAll
    public static void setup() throws IOException, InterruptedException {
        final String externalUrl = format("http://%s:%d", container.getHost(), container.getMappedPort(4443));
        final HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(externalUrl + "/_internal/config"))
                .header("Content-Type", "application/json; charset=utf-8")
                .PUT(HttpRequest.BodyPublishers.ofString(format("{\"externalUrl\":\"%s\"}", externalUrl)))
                .build();

        final HttpResponse<Void> res =
                HttpClient.newBuilder().build().send(req, HttpResponse.BodyHandlers.discarding());
        if (res.statusCode() != 200) throw new RuntimeException("Unable to update fake-gcs-server with external url");
    }

    private void withStorageService(Consumer<GoogleCloudStorageService> serviceConsumer) {
        if (service.get() != null) {
            serviceConsumer.accept(service.get());
            return;
        }

        final GoogleCloudStorageConfig config = new GoogleCloudStorageConfig(
                null,
                "test-project",
                "remi-test",
                format("http://%s:%d", container.getHost(), container.getMappedPort(4443)));

        final GoogleCloudStorageService gcsService = new GoogleCloudStorageService(config);
        service.set(gcsService);
        gcsService.init();

        serviceConsumer.accept(gcsService);
    }

    @Test
    public void test_canWeConnectToGcs() {
        withStorageService(service -> {
            final List<Blob> blobs = assertDoesNotThrow(() -> service.blobs());
            assertEquals(0, blobs.size());
        });
    }
}
