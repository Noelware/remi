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

rootProject.name = "remi"

include(
    // Bill of Materials module for Remi
    ":bom",

    // Core implementation blueprints for Remi
    ":core",

    // Filesystem support for Remi
    ":support-fs",

    // any Amazon S3 complaint server support for Remi
    ":support-s3",

    // Google Cloud Storage support for Remi
    //":support-gcs"
)
