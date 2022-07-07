/*
 * 🧶 Remi: Library to handling files for persistent storage with Google Cloud Storage and Amazon S3-compatible server, made in Kotlin!
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

@file:JvmName("StorageTrailerDefinitionsKt")
@file:Suppress("UNUSED")

package org.noelware.remi.core

import org.apache.tika.Tika
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Represents a trailer on how to handle storage operations.
 */
interface StorageTrailer<C: Configuration> {
    /**
     * Returns the configuration that the storage trailer was initialized with.
     */
    val config: C

    /**
     * The name of the storage trailer.
     */
    val name: String

    /**
     * Initializes a storage trailer, if needed.
     * @throws IllegalStateException If the storage trailer doesn't support this call.
     */
    suspend fun init() {
        TODO("Storage trailer $name doesn't support StorageTrailer#init/0")
    }

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    suspend fun open(path: String): InputStream?

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    suspend fun delete(path: String): Boolean

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    suspend fun exists(path: String): Boolean

    /**
     * Uploads file to this storage trailer and returns a [Boolean] result
     * if the operation was a success or not.
     *
     * @param path The path to upload the file to
     * @param stream The [InputStream] that represents the raw data.
     * @param contentType The content type of the file (useful for S3 and GCS support)!
     */
    suspend fun upload(
        path: String,
        stream: InputStream,
        contentType: String = "application/octet-stream"
    ): Boolean

    /**
     * Lists all the contents as a list of [objects][Object].
     * @param includeInputStream If the input stream should be fetched. This is only applicable
     *                           in the S3 or MinIO storage trailers. The filesystem one just ignores this.
     *                           This is a property since re-fetching the input stream from the data source
     *                           can be time-consuming if iterating over a lot of objects.
     */
    suspend fun listAll(includeInputStream: Boolean = true): List<Object>

    /**
     * Lists all the contents via a [prefix] to search from.
     * @param prefix The prefix
     * @param includeInputStream If the input stream should be fetched. This is only applicable
     *                           in the S3 or MinIO storage trailers. The filesystem one just ignores this.
     *                           This is a property since re-fetching the input stream from the data source
     *                           can be time-consuming if iterating over a lot of objects.
     */
    suspend fun list(prefix: String, includeInputStream: Boolean = true): List<Object>

    /**
     * Fetches an object from this storage trailer and transforms the metadata to a [Remi Object][Object].
     * @param key The key to select to find the object.
     * @return The metadata object or `null` if the object with the specified [key] wasn't found.
     */
    suspend fun fetch(key: String): Object?
}

/**
 * Returns the content type of [InputStream] given using Apache Tika, or it'll default
 * to `application/octet-stream` if the content type couldn't be found.
 *
 * @param stream The input stream to detect the content type
 * @return The content type itself, defaults to `application/octet-stream` if Apache Tika
 *         couldn't detect it.
 */
fun <C: Configuration> StorageTrailer<C>.figureContentType(stream: InputStream): String =
    Tika().detect(stream) ?: "application/octet-stream"

/**
 * Returns the content type of the bytes given using Apache Tika, or it'll default to
 * `application/octet-stream` if the content type couldn't be found.
 *
 * @param bytes The bytes to transform it into a [ByteArrayInputStream].
 * @return The content type itself, defaults to `application/octet-stream` if Apache Tika
 *         couldn't detect it.
 */
fun <C: Configuration> StorageTrailer<C>.figureContentType(bytes: ByteArray): String =
    figureContentType(ByteArrayInputStream(bytes))
