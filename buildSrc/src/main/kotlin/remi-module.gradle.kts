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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.noelware.remi.gradle.*
import dev.floofy.utils.gradle.*

plugins {
    kotlin("plugin.serialization")
    id("com.diffplug.spotless")
    id("org.jetbrains.dokka")
    id("io.kotest")
    kotlin("jvm")
}

group = "org.noelware.remi"
version = "$VERSION"

repositories {
    mavenCentral()
    mavenLocal()
    noel()
}

dependencies {
    // kotlinx.serialization support
    api("org.jetbrains.kotlinx:kotlinx-serialization-core")
    api(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.3"))

    // kotlinx.coroutines support
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.2"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")

    // noel utils
    implementation("dev.floofy.commons:slf4j:2.1.0.1")

    // SLF4J for logging
    api("org.slf4j:slf4j-api:1.7.36")

    // testing utilities
    testImplementation(platform("io.kotest:kotest-bom:5.3.0"))
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-assertions-core")
    testImplementation("io.kotest:kotest-property")

    // Tika (for content type checking)
    implementation("org.apache.tika:tika-core:2.4.0")

    if (name.startsWith("support-")) {
        implementation(project(":core"))
    }
}

spotless {
    kotlin {
        trimTrailingWhitespace()
        licenseHeaderFile("${rootProject.projectDir}/assets/HEADING")
        endWithNewline()

        // We can't use the .editorconfig file, so we'll have to specify it here
        // issue: https://github.com/diffplug/spotless/issues/142
        // ktlint 0.35.0 (default for Spotless) doesn't support trailing commas
        ktlint("0.43.0")
            .userData(
                mapOf(
                    "no-consecutive-blank-lines" to "true",
                    "no-unit-return" to "true",
                    "disabled_rules" to "no-wildcard-imports,colon-spacing",
                    "indent_size" to "4"
                )
            )
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JAVA_VERSION.toString()
        kotlinOptions.javaParameters = true
        kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }

    dokkaHtml {
        dokkaSourceSets {
            configureEach {
                platform.set(org.jetbrains.dokka.Platform.jvm)
                jdkVersion.set(17)
                includes.from("DokkaDescription.md")

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(uri("https://github.com/Noelware/remi/tree/master/${project.name}/src/main/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

java {
    sourceCompatibility = JAVA_VERSION
    targetCompatibility = JAVA_VERSION
}
