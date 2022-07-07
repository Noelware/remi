/*
 * 🧶 Remi: Library to handling files for persistent storage with Google Cloud Storage and Amazon S3-compatible server, made in Kotlin!
 * Copyright 2022 Noelware <team@noelware.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        // compose.start()
    }

    fun destroy() {
        if (!::compose.isInitialized) return

        log.info("Destroying MinIO server...")
        compose.close()
    }

    fun addressUri(): String? = if (::compose.isInitialized) {
        "http://${compose.getServiceHost("minio_1", 9000)}:${compose.getServicePort("minio_1", 9000)}"
    } else {
        null
    }
}
