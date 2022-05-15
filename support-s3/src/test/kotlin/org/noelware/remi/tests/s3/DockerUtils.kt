package org.noelware.remi.tests.s3

import dev.floofy.utils.slf4j.logging
import org.testcontainers.containers.DockerComposeContainer
import java.io.File

object DockerUtils {
    private lateinit var compose: DockerComposeContainer<*>
    private val log by logging<DockerUtils>()

    fun startMinioServer() {
        log.info("Starting up MinIO server...")

        compose = DockerComposeContainer(File("src/test/resources/docker-compose.yml"))
            .withExposedService("minio_1", 9000)
            .withBuild(true)
            .withLogConsumer("minio_1") {
                log.debug("MINIO SERVER :: ${it.utf8String}")
            }
    }

    fun destroy() {
        if (!::compose.isInitialized) return

        log.info("Destroying MinIO server...")
        compose.close()
    }

    fun addressUri(): String? = if (::compose.isInitialized)
        "http://${compose.getServiceHost("minio_1", 9000)}:${compose.getServicePort("minio_1", 9000)}"
    else
        null
}
