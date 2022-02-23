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

package org.noelware.remi.s3

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Represents a provider to use when using the [S3StorageTrailer].
 */
@Serializable(with = S3Provider.Companion::class)
enum class S3Provider(val key: String, val endpoint: String? = null) {
    Wasabi("wasabi", "https://s3.wasabisys.com"),
    Custom("custom"),
    Amazon("amazon");

    companion object: KSerializer<S3Provider> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("remi.S3Provider", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: S3Provider) {
            encoder.encodeString(value.key)
        }

        override fun deserialize(decoder: Decoder): S3Provider = when (decoder.decodeString()) {
            "wasabi" -> Wasabi
            "custom" -> Custom
            "amazon" -> Amazon
            else -> Amazon
        }
    }
}
