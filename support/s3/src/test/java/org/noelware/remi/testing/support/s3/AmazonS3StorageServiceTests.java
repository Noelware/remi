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

package org.noelware.remi.testing.support.s3;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.noelware.remi.core.Blob;
import org.noelware.remi.support.s3.AmazonS3StorageConfig;
import org.noelware.remi.support.s3.AmazonS3StorageService;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
public class AmazonS3StorageServiceTests {
    private static final GenericContainer<?> minioContainer = new GenericContainer<>(
                    DockerImageName.parse("quay.io/minio/minio:RELEASE.2022-12-07T00-56-37Z"))
            .withExposedPorts(9000, 9090)
            .withCommand("server", "/data")
            .withEnv(Map.of(
                    "MINIO_ROOT_USER", "remitest",
                    "MINIO_ROOT_PASSWORD", "remitest",
                    "MINIO_ACCESS_KEY", "remitest",
                    "MINIO_SECRET_KEY", "remitest"));

    private void withAmazonS3StorageService(Consumer<AmazonS3StorageService> serviceConsumer) {
        final AmazonS3StorageConfig config = AmazonS3StorageConfig.builder()
                .withAccessKeyId("remitest")
                .withSecretAccessKey("remitest")
                .withBucket("remi-test")
                .withEnforcedPathAccessStyle()
                .withEndpoint(format("http://%s:%d", minioContainer.getHost(), minioContainer.getMappedPort(9000)))
                .build();

        final AmazonS3StorageService service = new AmazonS3StorageService(config);
        service.init();
        serviceConsumer.accept(service);
    }

    @BeforeAll
    public static void beforeRun() {
        minioContainer.setWaitStrategy(new HttpWaitStrategy()
                .forPath("/minio/health/ready")
                .forPort(9000)
                .withStartupTimeout(Duration.ofMinutes(2)));
        minioContainer.start();
    }

    @Test
    public void test_canWeConnectToS3() {
        withAmazonS3StorageService(service -> {
            assertFalse(service.exists("/a/blob/owo"));
            final List<Blob> blobs = assertDoesNotThrow(() -> service.blobs());

            assertEquals(0, blobs.size());
        });
    }
}
