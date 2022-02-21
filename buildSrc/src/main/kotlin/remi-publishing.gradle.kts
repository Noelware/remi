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

import org.noelware.remi.gradle.*
import java.util.Properties

plugins {
    `maven-publish`
}

// Get the `publishing.properties` file from the `gradle/` directory
// in the root project.
val publishingPropsFile = file("${rootProject.projectDir}/gradle/publishing.properties")
val publishingProps = Properties()

// If the file exists, let's get the input stream
// and load it.
if (publishingPropsFile.exists()) {
    publishingProps.load(publishingPropsFile.inputStream())
}

// Check if we have the `NOELWARE_PUBLISHING_ACCESS_KEY` and `NOELWARE_PUBLISHING_SECRET_KEY` environment
// variables, and if we do, set it in the publishing.properties loader.
val accessKey: String? = System.getenv("NOELWARE_PUBLISHING_ACCESS_KEY")
val secretKey: String? = System.getenv("NOELWARE_PUBLISHING_SECRET_KEY")
val snapshotRelease: Boolean = run {
    val env = System.getenv("NOELWARE_PUBLISHING_IS_SNAPSHOT") ?: "false"
    env == "true"
}

if (accessKey != null && publishingProps.getProperty("s3.accessKey") == "") {
    publishingProps.setProperty("s3.accessKey", accessKey)
}

if (secretKey != null && publishingProps.getProperty("s3.secretKey") == "") {
    publishingProps.setProperty("s3.secretKey", secretKey)
}

publishing {
    publications {
        create<MavenPublication>("remi") {
            artifactId = "remi-${project.name}"
            groupId = "org.noelware.remi"
            version = "$VERSION"

            pom {
                description by "Library to handling files for persistent storage with Google Cloud Storage, Amazon S3, and the file system."
                name by "remi-$name"
                url by "https://docs.noelware.org/libs/remi"

                organization {
                    name by "Noelware"
                    url by "https://noelware.org"
                }

                developers {
                    developer {
                        name by "Noel"
                        email by "cutie@floofy.dev"
                        url by "https://floofy.dev"
                    }

                    developer {
                        name by "Noelware Team"
                        email by "team@noelware.org"
                        url by "https://noelware.org"
                    }
                }

                issueManagement {
                    system by "GitHub"
                    url by "https://github.com/Noelware/remi/issues"
                }

                licenses {
                    license {
                        name by "Apache-2.0"
                        url by "http://www.apache.org/licenses/LICENSE-2.0"
                    }
                }

                scm {
                    connection by "scm:git:ssh://github.com/Noelware/remi.git"
                    developerConnection by "scm:git:ssh://git@github.com:Noelware/remi.git"
                    url by "https://github.com/Noelware/remi"
                }
            }
        }
    }

    repositories {
        val url = if (snapshotRelease) "s3://maven.noelware.org/snapshots" else "s3://maven.noelware.org"
        maven(url) {
            credentials(AwsCredentials::class.java) {
                accessKey = publishingProps.getProperty("s3.accessKey") ?: ""
                secretKey = publishingProps.getProperty("s3.secretKey") ?: ""
            }
        }
    }
}
