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

@file:Suppress("UNUSED")

package org.noelware.remi.tests.s3

import dev.floofy.utils.slf4j.logging
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.noelware.remi.s3.S3Provider
import org.noelware.remi.s3.S3StorageTrailer
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class S3Tests: DescribeSpec({
    val log by logging<S3Tests>()

    beforeSpec {
        log.debug("Starting up containers...")
        DockerUtils.startContainer()
    }

    afterSpec {
        DockerUtils.destroyContainer()
    }

    describe("org.noelware.remi.s3.tests") {
        it("should be able to create bucket") {
            shouldNotThrow<Exception> {
                val trailer = S3StorageTrailer {
                    accessKey = "blehfluff"
                    secretKey = "blehfluff"
                    provider = S3Provider.Custom
                    endpoint = "http://${DockerUtils.url()}"
                    bucket = "remi"
                }

                trailer.init()
            }
        }

        it("should be able to upload") {
            val trailer = S3StorageTrailer {
                enforcePathAccessStyle = true
                accessKey = "blehfluff"
                secretKey = "blehfluff"
                provider = S3Provider.Custom
                endpoint = "http://${DockerUtils.url()}"
                bucket = "remi"
            }

            // This works but the `trailer.exists` call doesn't?
            trailer.init()

            // Make sure it doesn't exist
            trailer.exists("owo/da/uwu.txt") shouldBe false

            // Upload!
            val res = trailer.upload("owo/da/uwu.txt", ByteArrayInputStream("owo da uwuing!".toByteArray()), "text/plain")
            res shouldBe true

            // Fetch the file
            val file = trailer.fetch("owo/da/uwu.txt")
            file shouldNotBe null
            file!!.inputStream shouldNotBe null

            val reader = BufferedReader(InputStreamReader(file.inputStream!!))
            val contents = reader.readText()
            contents shouldBe "owo da uwuing!"
        }
    }
})
