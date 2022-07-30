package org.noelware.remi.gcs.tests

import io.kotest.core.spec.style.DescribeSpec
import org.noelware.remi.gcs.GoogleCloudStorageTrailer

class GoogleCloudStorageTests: DescribeSpec({
    var trailer: GoogleCloudStorageTrailer? = null

    beforeSpec {
        trailer = GoogleCloudStorageTrailer("magnetic-rite-357203") {
            credentialsFile = "/home/noel/Downloads/owodauwu-314a0e8644b7.json"
            bucket = "remi-test-owo"
        }

        trailer!!.init()
    }
})
