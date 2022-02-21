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

@file:Suppress("UNUSED")
package org.noelware.remi.filesystem

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.noelware.remi.core.Configuration
import org.noelware.remi.core.StorageTrailer
import java.io.File
import java.io.InputStream

/**
 * Represents the configuration of configuring the [FilesystemStorageTrailer].
 * @param directory A valid, absolute path on where it should be stored.
 */
@Serializable
data class FilesystemStorageConfig(val directory: String): Configuration

/**
 * Constructs a new [FilesystemStorageTrailer] instance with a supplied [directory].
 */
fun FilesystemStorageTrailer(directory: String): FilesystemStorageTrailer =
    FilesystemStorageTrailer(FilesystemStorageConfig(directory))

/**
 * Represents a [storage trailer][StorageTrailer] for supporting the local disk that the
 * application is running to use to do simple operations on.
 */
class FilesystemStorageTrailer(override val config: FilesystemStorageConfig): StorageTrailer<FilesystemStorageConfig> {
    override val name: String = "remi:filesystem"

    override suspend fun init() {
        val directory = File(config.directory)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    override suspend fun open(path: String): InputStream? {
        val actualPath = when {
            path.startsWith("./") -> (config.directory + path.replaceFirstChar { "" }).trim()
            path.startsWith("~/") -> System.getProperty("user.home", "/") + path
            else -> path
        }

        val file = File(actualPath)
        return if (file.exists()) file.inputStream() else null
    }

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    override suspend fun delete(path: String): Boolean {
        val actualPath = when {
            path.startsWith("./") -> (config.directory + path.replaceFirstChar { "" }).trim()
            path.startsWith("~/") -> System.getProperty("user.home", "/") + path
            else -> path
        }

        val file = File(actualPath)
        if (!file.exists()) return false

        file.deleteRecursively()
        return true
    }

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    override suspend fun exists(path: String): Boolean {
        val actualPath = when {
            path.startsWith("./") -> (config.directory + path.replaceFirstChar { "" }).trim()
            path.startsWith("~/") -> System.getProperty("user.home", "/") + path
            else -> path
        }

        return File(actualPath).exists()
    }

    /**
     * Uploads file to this storage trailer and returns a [Boolean] result
     * if the operation was a success or not.
     *
     * @param path The path to upload the file to
     * @param stream The [InputStream] that represents the raw data.
     * @param contentType This property is ignored in this storage trailer
     */
    override suspend fun upload(path: String, stream: InputStream, contentType: String): Boolean {
        val actualPath = when {
            path.startsWith("./") -> (config.directory + path.replaceFirstChar { "" }).trim()
            path.startsWith("~/") -> System.getProperty("user.home", "/") + path
            else -> path
        }

        val file = File(actualPath)
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }

        // convert the stream into an array of bytes
        val contents = withContext(Dispatchers.IO) {
            stream.readAllBytes()
        }

        file.writeBytes(contents)
        return true
    }
}
