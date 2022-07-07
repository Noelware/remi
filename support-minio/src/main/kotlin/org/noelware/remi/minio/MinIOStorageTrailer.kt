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

package org.noelware.remi.minio

import io.minio.*
import io.minio.errors.MinioException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import org.noelware.remi.core.*
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.URL
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents the configuration for the [MinIOStorageTrailer]. This implements usage with **kotlinx.serialization**
 * so you don't have to create boilerplate code for this.
 *
 * @param endpoint The endpoint to connect to MinIO. You can use:
 *
 *      - The `minio.endpoint` JVM argument (`-Dminio.endpoint=...`)
 *      - The `MINIO_ENDPOINT` environment variable (`MINIO_ENDPOINT=... java -jar ./something.jar`)
 *      - Set this property to a URI.
 *
 * @param accessKey The access key to a service account. You can use:
 *
 *      - The `minio.accessKey` JVM argument (`-Dminio.accessKey=...`)
 *      - The `MINIO_ACCESS_KEY` environment variable (`MINIO_ACCESS_KEY=... java -jar ./something.jar`)
 *      - Set this property to a service account access key.
 *
 * @param secretKey The secret key from a service account. You can use:
 *
 *      - The `minio.secretKey` JVM argument (`-Dminio.secretKey=...`)
 *      - The `MINIO_SECRET_KEY` environment variable (`MINIO_SECRET_KEY=... java -jar ./something.jar`)
 *      - Set this property to a service account access key.
 *
 * @param bucket The bucket to use for this storage trailer.
 */
@kotlinx.serialization.Serializable
data class MinIOStorageConfig(
    val endpoint: String = "",

    @SerialName("access_key")
    val accessKey: String = "",

    @SerialName("secret_key")
    val secretKey: String = "",
    val bucket: String = ""
): Configuration

@OptIn(ExperimentalContracts::class)
fun MinIOStorageTrailer(builder: MinIOStorageConfig.() -> Unit): MinIOStorageTrailer {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }

    val config = MinIOStorageConfig().apply(builder)
    return MinIOStorageTrailer(config)
}

class MinIOStorageTrailer(override val config: MinIOStorageConfig): StorageTrailer<MinIOStorageConfig> {
    override val name: String = "remi:minio"
    lateinit var client: MinioAsyncClient
    private val log = LoggerFactory.getLogger(MinIOStorageTrailer::class.java)

    init {
        require(config.bucket != "") { "Missing `bucket` property in configuration." }
    }

    private fun getEndpointFromConfig(): String {
        // Check if we can get it from the JVM args, environment variables, or from the config.
        // The priority is: System Property > Environment Variables > Configuration
        val systemEndpoint = System.getProperty("minio.endpoint", "")
        val envEndpoint = System.getenv("MINIO_ENDPOINT") ?: ""

        if (systemEndpoint.isNotEmpty())
            return systemEndpoint

        if (envEndpoint.isNotEmpty())
            return envEndpoint

        return config.endpoint.ifEmpty {
            throw IllegalStateException("Cannot configure endpoint due to none being present!")
        }
    }

    private fun getAccessKeyFromConfig(): String {
        // Check if we can get it from the JVM args, environment variables, or from the config.
        // The priority is: System Property > Environment Variables > Configuration
        val systemEndpoint = System.getProperty("minio.accessKey", "")
        val envEndpoint = System.getenv("MINIO_ACCESS_KEY") ?: ""

        if (systemEndpoint.isNotEmpty())
            return systemEndpoint

        if (envEndpoint.isNotEmpty())
            return envEndpoint

        return config.endpoint.ifEmpty {
            throw IllegalStateException("Cannot configure access key from service account due to none being present!")
        }
    }

    private fun getSecretKeyFromConfig(): String {
        // Check if we can get it from the JVM args, environment variables, or from the config.
        // The priority is: System Property > Environment Variables > Configuration
        val systemEndpoint = System.getProperty("minio.secretKey", "")
        val envEndpoint = System.getenv("MINIO_SECRET_KEY") ?: ""

        if (systemEndpoint.isNotEmpty())
            return systemEndpoint

        if (envEndpoint.isNotEmpty())
            return envEndpoint

        return config.endpoint.ifEmpty {
            throw IllegalStateException("Cannot configure secret key from service account due to none being present!")
        }
    }

    override suspend fun init() {
        log.debug("Initializing the MinIO storage trailer...")

        val endpoint = getEndpointFromConfig()
        val accessKey = getAccessKeyFromConfig()
        val secretKey = getSecretKeyFromConfig()

        client = MinioAsyncClient.builder()
            .endpoint(URL(endpoint))
            .credentials(accessKey, secretKey)
            .build()

        val found = client.bucketExists(BucketExistsArgs.builder().bucket(config.bucket).build()).await()
        if (!found) {
            log.warn("Bucket '${config.bucket}' doesn't exist, now creating...")
            client
                .makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(config.bucket)
                        .build()
                )

            log.debug("Bucket '${config.bucket}' should be created!")
        } else {
            log.debug("Bucket '${config.bucket}' already exists!")
        }
    }

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    override suspend fun open(path: String): InputStream? {
        if (!::client.isInitialized) throw IllegalStateException("MinIO client was not constructed, please call `init`.")
        return client.getObject(
            GetObjectArgs.builder()
                .bucket(config.bucket)
                .`object`(path)
                .build()
        ).await()
    }

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    override suspend fun delete(path: String): Boolean =
        try {
            // TODO: support `versionId` in this
            client.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(config.bucket)
                    .`object`(path)
                    .build()
            ).await()

            true
        } catch (e: MinioException) {
            false
        } catch (e: Exception) {
            throw e
        }

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    override suspend fun exists(path: String): Boolean {
        val stat = client.statObject(
            StatObjectArgs.builder()
                .bucket(config.bucket)
                .`object`(path)
                .build()
        ).await()

        return stat.deleteMarker()
    }

    /**
     * Uploads file to this storage trailer and returns a [Boolean] result
     * if the operation was a success or not.
     *
     * @param path The path to upload the file to
     * @param stream The [InputStream] that represents the raw data.
     * @param contentType The content type of the file (useful for S3 and GCS support)!
     */
    override suspend fun upload(path: String, stream: InputStream, contentType: String): Boolean {
        // Check if the stream is over >50MB, if so, let's do a multipart upload
        // rather than a single upload.
        val size = withContext(Dispatchers.IO) {
            stream.available()
        }

        return try {
            client.putObject(
                PutObjectArgs.builder()
                    .bucket(config.bucket)
                    .`object`(path)
                    .stream(stream, size.toLong(), -1)
                    .build()
            ).await()

            true
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Lists all the contents as a list of [objects][Object].
     */
    override suspend fun listAll(includeInputStream: Boolean): List<Object> {
        val objects = client.listObjects(ListObjectsArgs.builder().bucket(config.bucket).recursive(true).build())
        val list = mutableListOf<Object>()

        for (obj in objects) {
            val result = obj.get()

            // If it is a directory, continue!
            if (result.isDir) continue

            // If it has a deleted marker, also continue!
            if (result.isDeleteMarker) continue

            val name = result.objectName()
            val size = result.size()
            val lastModified = result.lastModified().toLocalDateTime().toKotlinLocalDateTime()
            val inputStream = if (includeInputStream) {
                try {
                    open(name)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            list.add(
                Object(
                    if (inputStream == null) CHECK_WITH else figureContentType(inputStream),
                    inputStream,
                    null,
                    lastModified,
                    null,
                    size,
                    name,
                    result.etag(),
                    "minio://$name"
                )
            )
        }

        return list.toList()
    }

    override suspend fun list(prefix: String, includeInputStream: Boolean): List<Object> {
        val objects = client.listObjects(ListObjectsArgs.builder().bucket(config.bucket).recursive(true).prefix(prefix).build())
        val list = mutableListOf<Object>()

        for (obj in objects) {
            val result = obj.get()

            // If it is a directory, continue!
            if (result.isDir) continue

            // If it has a deleted marker, also continue!
            if (result.isDeleteMarker) continue

            val name = result.objectName()
            val size = result.size()
            val lastModified = result.lastModified().toLocalDateTime().toKotlinLocalDateTime()
            val inputStream = if (includeInputStream) {
                try {
                    open(name)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            list.add(
                Object(
                    if (inputStream == null) CHECK_WITH else figureContentType(inputStream),
                    inputStream,
                    null,
                    lastModified,
                    null,
                    size,
                    name,
                    result.etag(),
                    "minio://$name"
                )
            )
        }

        return list.toList()
    }

    override suspend fun fetch(key: String): Object? = fetch(key) {}
    suspend fun fetch(key: String, builder: GetObjectArgs.Builder.() -> Unit = {}): Object? {
        val obj = client.getObject(
            GetObjectArgs.builder()
                .bucket(config.bucket)
                .`object`(key)
                .apply(builder)
                .build()
        ).await() ?: return null

        val stat = client.statObject(StatObjectArgs.builder().bucket(config.bucket).`object`(key).build()).await()
        return Object(
            figureContentType(obj),
            obj,
            null,
            stat.lastModified().toInstant().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
            null,
            stat.size(),
            key,
            stat.etag(),
            "minio://$key"
        )
    }
}
