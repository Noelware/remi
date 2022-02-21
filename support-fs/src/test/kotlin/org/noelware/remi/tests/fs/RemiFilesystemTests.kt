package org.noelware.remi.tests.fs

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.noelware.remi.filesystem.FilesystemStorageConfig
import org.noelware.remi.filesystem.FilesystemStorageTrailer
import java.io.ByteArrayInputStream
import java.io.File

class RemiFilesystemTests: DescribeSpec({
    val directory = "./.remi/data"

    beforeSpec {
        File(directory).mkdirs()
    }

    afterSpec {
        File(directory).deleteRecursively()
    }

    describe("org.noelware.remi.support.fs") {
        it("should be `false` if `./owo` exists") {
            val trailer = FilesystemStorageTrailer(directory)

            trailer.exists("./owo") shouldBe false
        }

        it("should be `true` if `./owo.txt` was created") {
            val trailer = FilesystemStorageTrailer(directory)

            // let's make sure!
            trailer.exists("./owo.txt") shouldBe false

            val inputStream = ByteArrayInputStream("owo da uwu".toByteArray())
            val result = trailer.upload("./owo.txt", inputStream, "text/plain; charset=utf-8")

            result shouldBe true
            trailer.exists("./owo.txt") shouldBe true
        }

        it("should be `true` if `./owo.txt` can be deleted") {
            val trailer = FilesystemStorageTrailer(directory)

            // let's make sure!
            trailer.exists("./owo.txt") shouldBe true

            val result = trailer.delete("./owo.txt")
            result shouldBe true
            trailer.exists("./owo.txt") shouldBe false
        }
    }
})
