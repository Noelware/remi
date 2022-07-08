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
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.utility.DockerImageName
import java.time.Duration

object DockerUtils {
    private lateinit var container: GenericContainer<*>
    private val log by logging<DockerUtils>()

    const val IMAGE = "minio/minio:RELEASE.2022-07-08T00-05-23Z"

    fun startContainer() {
        log.info("Starting up MinIO container...")

        container = GenericContainer(DockerImageName.parse(IMAGE)).apply {
            withNetworkAliases("minio-default")
            addExposedPort(9000)
            withEnv("MINIO_ROOT_USER", "blehfluff")
            withEnv("MINIO_ROOT_PASSWORD", "blehfluff")
            withCommand("server", "/data")
            setWaitStrategy(HttpWaitStrategy().forPort(9000).forPath("/minio/health/ready").withStartupTimeout(Duration.ofMinutes(2L)))
        }

        container.start()
    }

    fun destroyContainer() {
        if (!::container.isInitialized) return

        log.warn("Destroying container...")
        container.stop()
    }

    fun url(): String = "${container.host}:${container.getMappedPort(9000)}"
}
