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

@file:JvmName("RemiTypesKt")
@file:Suppress("UNUSED")
package org.noelware.remi.core

import kotlinx.datetime.LocalDateTime
import java.io.File
import java.io.InputStream

/**
 * Represents the base configuration for constructing a [StorageTrailer].
 */
interface Configuration

/**
 * Represents a "empty configuration" container
 */
object EmptyConfiguration: Configuration

/**
 * Represents an object from the [StorageTrailer] when using the listAll operation. This is a
 * [Serializable] object, so you can use it with kotlinx.serialization :)
 */
data class Object(
    /**
     * Represents the content type of this [Object], if needed to be sent
     * through HTTP. By default, it will use the `application/octet-stream` content
     * type if the content type couldn't be identified.
     */
    val contentType: String,

    /**
     * The raw input stream from the original file or object that was found.
     */
    val inputStream: InputStream,

    /**
     * Represents when this [Object] was created at.
     */
    val createdAt: LocalDateTime,

    /**
     * Represents the original file that was found. Returns `null` if it was not found, this is
     * usually the case with S3 and Google Cloud Storage. To get the raw bytes, you can use [inputStream]. :)
     */
    val original: File?,

    /**
     * Size in bytes how big this [Object] is.
     */
    val size: Long,

    /**
     * The name of this [Object].
     */
    val name: String
) {
    /**
     * Returns this [Object]'s input stream from the original file or from the [inputStream] variable.
     */
    fun toInputStream(): InputStream = original?.inputStream() ?: inputStream
}
