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

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.*;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("resource")
@Testcontainers(disabledWithoutDocker = true)
public class GridfsStorageServiceTests {
    private final AtomicReference<GridfsStorageService> service = new AtomicReference<>(null);

    @Container
    private static final DockerComposeContainer<?> container = new DockerComposeContainer<>(
                    new File("src/test/resources/docker-compose.yml"))
            .withExposedService("mongo", 27017)
            .waitingFor("mongo", new DockerHealthcheckWaitStrategy().withStartupTimeout(Duration.ofMinutes(1)));

    private void withStorageService(Consumer<GridfsStorageService> serviceConsumer) {
        if (service.get() != null) {
            serviceConsumer.accept(service.get());
            return;
        }

        final MongoClient client = new MongoClient(
                "mongodb://%s:%d"
                        .formatted(container.getServiceHost("mongo", 27017), container.getServicePort("mongo", 27017)),
                new MongoClientOptions.Builder()
                        .applicationName("Noelware/remi")
                        .build());

        final GridfsStorageService gridfsStorageService = new GridfsStorageService(client.getDB("woof"), "woof");
        gridfsStorageService.init();
        service.set(gridfsStorageService);

        serviceConsumer.accept(gridfsStorageService);
    }

    @Test
    public void test_canWeConnect() {
        withStorageService((service) ->
                assertDoesNotThrow(() -> assertEquals(0, service.blobs().size())));
    }
}
