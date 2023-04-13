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

@file:Suppress("UnstableApiUsage")

rootProject.name = "remi"

pluginManagement {
    repositories {
        maven("https://maven.floofy.dev/repo/releases")
        maven("https://maven.noelware.org")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

buildscript {
    dependencies {
        classpath("org.noelware.gradle:gradle-infra:1.1.1")
    }
}

plugins {
    id("org.noelware.gradle.settings") version "1.1.1"
    id("com.gradle.enterprise") version "3.13"
}

include(
    ":bom",
    ":core",
    ":support:azure",
    ":support:fs",
    ":support:gcs",
    //":support:gridfs",
    ":support:s3"
)

toolchainManagement {
    jvm {
        javaRepositories {
            repository("noelware") {
                resolverClass.set(org.noelware.infra.gradle.toolchains.NoelwareJvmToolchainResolver::class.java)
            }
        }
    }
}
