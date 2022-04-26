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

@file:JvmName("RemiFilesystemExtensionsKt")
package org.noelware.remi.filesystem

import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Extension for [File] if the [File] exists, then call the [body] function,
 * or return `null` if it doesn't exist. It is present in utilities like [FilesystemStorageTrailer.open] and such.
 *
 * @param body The body to run if this [File] exists.
 * @return The result of the [body] block, or null.
 */
@OptIn(ExperimentalContracts::class)
fun <T> File.ifExists(body: File.() -> T): T? {
    contract { callsInPlace(body, InvocationKind.EXACTLY_ONCE) }

    return if (exists()) body() else null
}
