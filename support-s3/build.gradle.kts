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
    `remi-publishing`
    `remi-module`
}

dependencies {
    api("software.amazon.awssdk:s3:2.17.260")

    testApi("org.slf4j:slf4j-api:2.0.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.0")
    testImplementation("dev.floofy.commons:slf4j:2.3.0")
    testImplementation("org.testcontainers:testcontainers:1.17.3")
}
