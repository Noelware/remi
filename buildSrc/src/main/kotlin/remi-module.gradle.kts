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
import org.noelware.remi.gradle.*
import dev.floofy.utils.gradle.*
import java.io.StringReader
import java.util.Properties

plugins {
    id("com.diffplug.spotless")

    `maven-publish`
    `java-library`
    java
}

group = "org.noelware.remi"
description = "\uD83E\uDDF6 Remi subproject '$jarFileName' located in [$path]"
version = "$VERSION"

repositories {
    mavenCentral()
    mavenLocal()
    noel()
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // test deps
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")

    if (path.startsWith(":support")) {
        implementation(project(":core"))
    }
}

spotless {
    java {
        licenseHeaderFile("${rootProject.projectDir}/assets/HEADING")
        trimTrailingWhitespace()
        removeUnusedImports()
        palantirJavaFormat()
        endWithNewline()
    }
}

java {
    toolchain {
        languageVersion by JavaLanguageVersion.of(JAVA_VERSION.majorVersion)
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
        failFast = true // kill gradle if a test fails

        testLogging {
            events.addAll(listOf(
                TestLogEvent.PASSED,
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STARTED
            ))

            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

// Get the `publishing.properties` file from the `gradle/` directory
// in the root project.
val publishingPropsFile = file("${rootProject.projectDir}/gradle/publishing.properties")
val publishingProps = Properties()

// If the file exists, let's get the input stream
// and load it.
if (publishingPropsFile.exists()) {
    publishingProps.load(publishingPropsFile.inputStream())
} else {
    // Check if we do in environment variables
    val accessKey = System.getenv("NOELWARE_PUBLISHING_ACCESS_KEY") ?: ""
    val secretKey = System.getenv("NOELWARE_PUBLISHING_SECRET_KEY") ?: ""

    if (accessKey.isNotEmpty() && secretKey.isNotEmpty()) {
        val data = """
        |s3.accessKey=$accessKey
        |s3.secretKey=$secretKey
        """.trimMargin()

        publishingProps.load(StringReader(data))
    }
}

// Check if we have the `NOELWARE_PUBLISHING_ACCESS_KEY` and `NOELWARE_PUBLISHING_SECRET_KEY` environment
// variables, and if we do, set it in the publishing.properties loader.
val snapshotRelease: Boolean = run {
    val env = System.getenv("NOELWARE_PUBLISHING_IS_SNAPSHOT") ?: "false"
    env == "true"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier by "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    description = "Assemble Java documentation with Javadoc"
    group = JavaBasePlugin.DOCUMENTATION_GROUP

    archiveClassifier by "javadoc"
    from(tasks.javadoc)
    dependsOn(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>("remi") {
            createPublicationMetadata(project, sourcesJar, javadocJar)
        }
    }
}
