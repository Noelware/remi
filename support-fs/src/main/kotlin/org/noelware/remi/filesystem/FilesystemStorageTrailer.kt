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

@file:Suppress("UNUSED")

package org.noelware.remi.filesystem

import dev.floofy.utils.slf4j.logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.apache.commons.io.output.ByteArrayOutputStream
import org.noelware.remi.core.Configuration
import org.noelware.remi.core.Object
import org.noelware.remi.core.StorageTrailer
import org.noelware.remi.core.figureContentType
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributeView
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.inputStream

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
    private val log by logging<FilesystemStorageTrailer>()

    /**
     * Returns the [path] as a "normalized" version:
     *   - If the [path] starts with `./`, replace `./` with [config.directory][FilesystemStorageConfig.directory]
     *   - If the [path] starts with `~/`, replace `~/` with system property `user.home` (or `/` by default)
     *   - If the clauses both fail, do not do anything with the path.
     *
     * @param path The path to normalize
     * @return The normalized path.
     */
    @Suppress
    fun normalizePath(path: String): String = when {
        path.startsWith("./") -> (config.directory + path.replaceFirstChar { "" }).trim()
        path.startsWith("~/") -> System.getProperty("user.home", "/") + path
        else -> path
    }

    override suspend fun init() {
        val directory = File(config.directory)
        if (!directory.exists()) {
            log.debug("Directory ${config.directory} didn't exist, creating...")
            directory.mkdirs()
        }
    }

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    override suspend fun open(path: String): InputStream? = File(normalizePath(path)).ifExists { inputStream() }

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    override suspend fun delete(path: String): Boolean = File(normalizePath(path)).ifExists {
        deleteRecursively(); true
    } ?: false

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    override suspend fun exists(path: String): Boolean = File(normalizePath(path)).exists()

    /**
     * Uploads file to this storage trailer and returns a [Boolean] result
     * if the operation was a success or not.
     *
     * @param path The path to upload the file to
     * @param stream The [InputStream] that represents the raw data.
     * @param contentType This property is ignored in this storage trailer
     */
    override suspend fun upload(path: String, stream: InputStream, contentType: String): Boolean {
        val file = File(normalizePath(path))
        withContext(Dispatchers.IO) {
            // this is probably a bad idea but i will hope it works
            while (true) {
                val parent = file.parentFile
                if (parent.isDirectory) {
                    if (parent.exists()) {
                        break
                    }

                    parent.mkdir()
                }
            }

            file.createNewFile()
        }

        // convert the stream into an array of bytes
        val contents = withContext(Dispatchers.IO) {
            stream.readAllBytes()
        }

        file.writeBytes(contents)
        return true
    }

    /**
     * Lists all the contents as a list of [objects][Object].
     */
    override suspend fun listAll(includeInputStream: Boolean): List<Object> = walkInDirectory(config.directory)

    /**
     * Lists all the objects via the [prefix] selected, this must be a directory,
     * not a file!
     *
     * @param prefix The prefix to use, this will be transformed via the [normalizePath], so you can use
     *               the `./` and `~/` prefixes.
     *
     * @return The [files][Object] found.
     */
    override suspend fun list(prefix: String, includeInputStream: Boolean): List<Object> {
        val normalizedPath = normalizePath(prefix)
        val file = File(normalizedPath)

        if (!file.isDirectory) {
            throw IllegalStateException("Path $normalizedPath has to be a directory to walk in.")
        }

        return walkInDirectory(normalizedPath)
    }

    /**
     * Fetches an object from this storage trailer and transforms the metadata to a [Remi Object][Object].
     * @param key The key to select to find the object.
     * @return The metadata object or `null` if the object with the specified [key] wasn't found.
     */
    override suspend fun fetch(key: String): Object? {
        val file = File(normalizePath(key))
        if (file.isDirectory) {
            throw FileIsDirectoryException(normalizePath(key))
        }

        if (!file.exists()) {
            return null
        }

        val path = Paths.get(normalizePath(key))
        val attributes = withContext(Dispatchers.IO) {
            Files.getFileAttributeView(path, BasicFileAttributeView::class.java).readAttributes()
        }

        val stream = file.inputStream()
        val os = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            stream.transferTo(os)
        }

        val data = os.toByteArray()
        val contentType = figureContentType(data)
        val etag = "\"${data.size.toString(16)}-${sha1(data).substring(0..27)}\""

        return Object(
            contentType,
            ByteArrayInputStream(data),
            attributes
                .creationTime()
                .toInstant()
                .toKotlinInstant()
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            attributes
                .lastModifiedTime()
                .toInstant()
                .toKotlinInstant()
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            file,
            attributes.size(),
            file.name,
            etag,
            "file://$file"
        )
    }

    private suspend fun walkInDirectory(directory: String): List<Object> =
        withContext(Dispatchers.IO) {
            Files
                .walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .map {
                    val file = it.toFile()
                    val attributes = runBlocking {
                        withContext(Dispatchers.IO) {
                            Files.getFileAttributeView(it, BasicFileAttributeView::class.java).readAttributes()
                        }
                    }

                    val inputStream = it.inputStream()
                    val os = ByteArrayOutputStream()
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            inputStream.transferTo(os)
                        }
                    }

                    val data = os.toByteArray()
                    val contentType = figureContentType(data)
                    val etag = "\"${data.size.toString(16)}-${sha1(data).substring(0..27)}\""

                    Object(
                        contentType,
                        inputStream,
                        attributes
                            .creationTime()
                            .toInstant()
                            .toKotlinInstant()
                            .toLocalDateTime(TimeZone.currentSystemDefault()),
                        attributes
                            .lastModifiedTime()
                            .toInstant()
                            .toKotlinInstant()
                            .toLocalDateTime(TimeZone.currentSystemDefault()),
                        file,
                        attributes.size(),
                        file.name,
                        etag,
                        "file://$file"
                    )
                }.toList()
        }

    private fun sha1(bytes: ByteArray): String {
        val sha1 = MessageDigest.getInstance("SHA1")
        return String(Base64.getEncoder().encode(sha1.digest(bytes)))
    }
}
