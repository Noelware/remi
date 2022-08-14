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

package org.noelware.remi.filesystem

import kotlinx.serialization.SerialName

/**
 * Represents the statistics of the current disk that the storage
 * trailer is using.
 */
@kotlinx.serialization.Serializable
data class Stats(
    /** Returns the unallocated space, in bytes. */
    @SerialName("unallocated_space")
    val unallocatedSpace: Long,

    /** Returns all the usuable space, in bytes */
    @SerialName("usable_space")
    val usableSpace: Long,

    /** The total space the disk has, in bytes. */
    @SerialName("total_space")
    val totalSpace: Long,

    /** The drive that the storage trailer is using. */
    val drive: String,

    /** Represents the type the disk is, i.e, NTFS, ext4, etc. */
    val type: String
)
