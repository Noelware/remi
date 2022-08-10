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

plugins {
    `kotlin-dsl`
    groovy
}

repositories {
    maven("https://maven.floofy.dev/repo/releases")
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.9.1")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.10")
    implementation(kotlin("gradle-plugin", version = "1.7.0"))
    implementation(kotlin("serialization", version = "1.7.0"))
    implementation("io.kotest:kotest-gradle-plugin:0.3.9")
    implementation("dev.floofy.commons:gradle:2.2.1.1")
    implementation(gradleApi())
}
