/*
 * 🧶 remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022-2023 Noelware, LLC. <team@noelware.org>
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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.noelware.infra.gradle.*
import org.noelware.remi.gradle.*
import dev.floofy.utils.gradle.*

plugins {
    id("org.noelware.gradle.java-library")
}

group = "org.noelware.remi"
version = "$VERSION"
description = "\uD83E\uDDF6 Remi subproject '$jarFileName' located in [$path]"

// Check if we have the `NOELWARE_PUBLISHING_ACCESS_KEY` and `NOELWARE_PUBLISHING_SECRET_KEY` environment
// variables, and if we do, set it in the publishing.properties loader.
val snapshotRelease: Boolean = run {
    val env = System.getenv("NOELWARE_PUBLISHING_IS_SNAPSHOT") ?: "false"
    env == "true"
}

noelware {
    mavenPublicationName by "remi"
    minimumJavaVersion by JAVA_VERSION
    projectDescription by "Robust, and simple Java-based library to handle storage-related communications with different storage provider."
    projectEmoji by "\uD83E\uDDF6"
    projectName by "remi"
    currentYear by "2022-2023"
    s3BucketUrl by if (snapshotRelease) "s3://august/noelware/maven/snapshots" else "s3://august/noelware/maven"
    unitTests by true
    license by Licenses.MIT
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.slf4j:slf4j-api:2.0.7")

    // test deps
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.testcontainers:testcontainers:1.18.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.1")
    testImplementation("org.slf4j:slf4j-simple:2.0.7")

    if (path.startsWith(":support")) {
        implementation(project(":core"))
    }
}

tasks {
    withType<Jar> {
        manifest {
            attributes(mapOf(
                "Implementation-Version" to "$VERSION",
                "Implementation-Vendor" to "Noelware, LLC. [team@noelware.org]",
                "Implementation-Title" to project.jarFileName
            ))
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        outputs.upToDateWhen { false }

        maxParallelForks = Runtime.getRuntime().availableProcessors()
        failFast = true

        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_ERROR, TestLogEvent.STANDARD_OUT, TestLogEvent.STARTED)

            showCauses = true
            showExceptions = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

publishing {
    publications {
        named<MavenPublication>("remi") {
            createPublicationMetadata(project)
        }
    }
}
