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
