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

@file:Suppress("UNUSED")
package org.noelware.remi.tests.s3

import dev.floofy.utils.slf4j.logging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class S3Tests: DescribeSpec({
    val log by logging<S3Tests>()

    beforeSpec {
        log.debug("Starting up containers...")
        DockerUtils.startMinioServer()
    }

    afterSpec {
        DockerUtils.destroy()
    }

    describe("org.noelware.remi.s3.tests") {
        it("should be `true`") {
            true shouldBe true
        }
    }
})