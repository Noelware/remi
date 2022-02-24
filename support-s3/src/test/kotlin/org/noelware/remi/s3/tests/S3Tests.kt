/*
 * 🧶 Remi: Library to handling files for persistent storage with Google Cloud Storage
 * and Amazon S3-compatible server, made in Kotlin!
 *
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

package org.noelware.remi.s3.tests

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.noelware.remi.s3.S3Provider
import org.noelware.remi.s3.S3StorageConfig
import org.noelware.remi.s3.S3StorageTrailer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import java.io.ByteArrayInputStream

class S3Tests: DescribeSpec({
    val storage = S3StorageTrailer(
        S3StorageConfig(
            defaultObjectAcl = ObjectCannedACL.PUBLIC_READ,
            accessKey = System.getenv("S3_ACCESS_KEY"),
            secretKey = System.getenv("S3_SECRET_KEY"),
            endpoint = "https://s3.wasabisys.com",
            provider = S3Provider.Custom,
            bucket = "remi-noelware-test",
            region = Region.US_EAST_1
        )
    )

    beforeSpec {
        storage.init()
    }

    afterSpec {
        // Delete the remaining file (because it will fail other tests)
        storage.delete("owo/da/uwu.txt")
    }

    describe("org.noelware.remi.s3") {
        it("should return `null` if key `owo/da/uwu.txt` was not found") {
            val result = storage.open("owo/da/uwu.txt")

            result shouldBe null
        }

        it("should return `true` if we can write `owo/da/uwu.txt`") {
            val stream = ByteArrayInputStream("owo da uwu".toByteArray())
            val result = storage.upload(
                "owo/da/uwu.txt",
                stream,
                "text/plain"
            )

            result shouldBe true
            result shouldNotBe false

            // check if we can read it
            val res = storage.open("owo/da/uwu.txt")

            res shouldNotBe null

            withContext(Dispatchers.IO) {
                val contents = res!!.readAllBytes()
                val resultString = String(contents, Charsets.UTF_8)

                resultString shouldBe "owo da uwu"
                resultString shouldNotBe "cute furries doing cute things. >~<"
            }
        }
    }
})
