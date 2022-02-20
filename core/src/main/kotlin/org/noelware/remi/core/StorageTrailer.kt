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

package org.noelware.remi.core

import java.io.InputStream

/**
 * Represents a trailer on how to handle storage operations.
 */
open interface StorageTrailer<C: Configuration> {
    val config: C
    val name: String

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    fun open(path: String): InputStream

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    fun delete(path: String): Boolean

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    fun exists(path: String): Boolean
}
