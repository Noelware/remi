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
import kotlinx.serialization.SerialName

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
@kotlinx.serialization.Serializable
data class Object(
    /**
     * Represents the content type of this [Object], if needed to be sent
     * through HTTP. By default, it will use the `application/octet-stream` content
     * type if the content type couldn't be identified.
     */
    @SerialName("content_type")
    val contentType: String,

    /**
     * Represents when this [Object] was created at.
     */
    @SerialName("created_at")
    val createdAt: LocalDateTime,

    /**
     * Size in bytes how big this [Object] is.
     */
    val size: Long,

    /**
     * The name of this [Object].
     */
    val name: String
)
