/*
 * 🧶 Remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022-2023 Noelware <team@noelware.org>
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

rootProject.name = "remi"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.12.2"
}

include(
    ":bom",
    ":core",
    ":support:azure",
    ":support:fs",
    ":support:gcs",
    ":support:oracle-cloud",
    ":support:s3"
)

gradle.settingsEvaluated {
    logger.info("Checking if we can overwrite cache...")
    val overrideBuildCacheProp: String? = System.getProperty("org.noelware.gradle.overwriteCache")
    val buildCacheDir = when (val prop = System.getProperty("org.noelware.gradle.cachedir")) {
        null -> "${System.getProperty("user.dir")}/.caches/gradle"
        else -> when {
            prop.startsWith("~/") -> "${System.getProperty("user.home")}${prop.substring(1)}"
            prop.startsWith("./") -> "${System.getProperty("user.dir")}${prop.substring(1)}"
            else -> prop
        }
    }

    if (overrideBuildCacheProp == null) {
        logger.info("""
        |If you wish to override the build cache for this Gradle process, you can use the
        |-Dorg.noelware.gradle.overwriteCache=<bool> Java property in `~/.gradle/gradle.properties`
        |to overwrite it in $buildCacheDir!
        """.trimMargin("|"))
    } else {
        logger.info("Setting up build cache in directory [$buildCacheDir]")
        val file = File(buildCacheDir)
        if (!file.exists()) file.mkdirs()

        buildCache {
            local {
                directory = "$file"
                removeUnusedEntriesAfterDays = 7
            }
        }
    }

    val disableJavaSanityCheck = when {
        System.getProperty("org.noelware.gradle.ignoreJavaCheck", "false").matches("^(yes|true|1|si|si*)$".toRegex()) -> true
        (System.getenv("DISABLE_JAVA_SANITY_CHECK") ?: "false").matches("^(yes|true|1|si|si*)$".toRegex()) -> true
        else -> false
    }

    if (disableJavaSanityCheck)
        return@settingsEvaluated

    val version = JavaVersion.current()
    if (version.majorVersion.toInt() < 17)
        throw GradleException("Developing charted-server requires JDK 17 or higher, it is currently set in [${System.getProperty("java.home")}, ${System.getProperty("java.version")}] - You can ignore this check by providing the `-Dorg.noelware.charted.ignoreJavaCheck=true` system property.")
}

val buildScanServer = System.getProperty("org.noelware.gradle.build-scan-server", "") ?: ""
gradleEnterprise {
    buildScan {
        if (buildScanServer.isNotEmpty()) {
            server = buildScanServer
            isCaptureTaskInputFiles = true
            publishAlways()
        } else {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"

            // Always publish if we're on CI.
            if (System.getenv("CI") != null) {
                publishAlways()
            }
        }

        obfuscation {
            ipAddresses { listOf("0.0.0.0") }
            hostname { "[redacted]" }
            username { "[redacted]" }
        }
    }
}
