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
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://maven.floofy.dev/repo/releases")
    }
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.3.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    implementation("gay.floof.utils:gradle-utils:1.2.0")
    implementation(kotlin("gradle-plugin", version = "1.6.10"))
    implementation(kotlin("serialization", version = "1.6.10"))
    implementation(gradleApi())
}
