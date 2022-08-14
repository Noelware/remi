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

package org.noelware.remi.s3

import dev.floofy.utils.slf4j.logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.serialization.*
import org.apache.commons.io.output.ByteArrayOutputStream
import org.noelware.remi.core.*
import org.noelware.remi.s3.serializers.AwsRegionSerializer
import org.noelware.remi.s3.serializers.BucketCannedACLSerializer
import org.noelware.remi.s3.serializers.ObjectCannedACLSerializer
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketCannedACL
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Represents the configuration for the [S3StorageTrailer].
 *
 * @param defaultObjectAcl The default ACL policy for all objects uploaed using the `upload/3` API.
 * @param defaultAcl The default ACL policy for this bucket if it needs to be created.
 * @param secretKey The secret key to use to authenticate with S3. If it doesn't exist, it will check for:
 *
 *                    - `aws.secret_key` JVM system property
 *                    - AWS_SECRET_KEY environment variable
 *                    - Anything under `~/.aws/config` or using the `AWS_CONFIG_FILE` environment variable
 *                      to determine the location of the configuration endpoint.
 *                    - Finding a profile under the `AWS_PROFILE` environment variable or using the `aws.profile`
 *                      system property.
 *                    - If all the above fails, the trailer will fail with an [Exception] thrown.
 *
 * @param accessKey The access key to use to authenticate with S3. If it doesn't exist, it will check for:
 *
 *                    - `aws.access_key` JVM system property
 *                    - AWS_ACCESS_KEY environment variable
 *                    - Anything under `~/.aws/config` or using the `AWS_CONFIG_FILE` environment variable
 *                      to determine the location of the configuration endpoint.
 *                    - Finding a profile under the `AWS_PROFILE` environment variable or using the `aws.profile`
 *                      system property.
 *                    - If all the above fails, the trailer will fail with an [Exception] thrown.
 *
 * @param endpoint An endpoint URI to use when using anything other than the [S3Provider.Amazon] or [S3Provider.Wasabi]
 *                 providers.
 *
 * @param provider The [S3Provider] to use when configuring the S3 client.
 * @param region   The region to use when connecting to the S3 service.
 * @param bucket   The bucket name to use, it will default to `remi` if this is empty.
 */
@Serializable
data class S3StorageConfig(
    /**
     * If we should enable signer v4 requests when requesting to Amazon S3. This must be needs to be true
     * for MinIO connections. Read more [here](https://docs.min.io/docs/how-to-use-aws-sdk-for-java-with-minio-server.html).
     */
    @SerialName("enable_signer_requests")
    var enableSignerV4Requests: Boolean = false,

    /**
     * If the S3 client should be configured to use the new path style for S3 connections, read more
     * [here](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3Builder.html#setPathStyleAccessEnabled-java.lang.Boolean-). This needs to be `true` if you're using a MinIO connection.
     */
    @SerialName("enforce_path_access_style")
    var enforcePathAccessStyle: Boolean = false,

    /**
     * The default ACL for creating new objects.
     */
    @SerialName("default_object_acl")
    @Serializable(with = ObjectCannedACLSerializer::class)
    var defaultObjectAcl: ObjectCannedACL = ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL,

    /**
     * THe default ACL for creating the bucket if it doesn't exist.
     */
    @SerialName("default_bucket_acl")
    @Serializable(with = BucketCannedACLSerializer::class)
    var defaultAcl: BucketCannedACL = BucketCannedACL.PUBLIC_READ,
    var secretKey: String? = null,
    var accessKey: String? = null,
    var endpoint: String? = null,
    var provider: S3Provider = S3Provider.Amazon,

    @Serializable(with = AwsRegionSerializer::class)
    var region: Region = Region.US_EAST_1,
    var bucket: String = "remi"
): Configuration

fun S3StorageTrailer(config: S3StorageConfig.() -> Unit = {}): S3StorageTrailer =
    S3StorageTrailer(S3StorageConfig().apply(config))

class S3StorageTrailer(override val config: S3StorageConfig): StorageTrailer<S3StorageConfig> {
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    lateinit var client: S3Client
    override val name: String = "remi:s3"
    private val log by logging<S3StorageTrailer>()

    override suspend fun init() {
        log.debug("Initializing S3 storage trailer...")
        log.debug("* enableSignerV4Requests = ${config.enableSignerV4Requests}")
        log.debug("* enforcePathAccessStyle = ${config.enforcePathAccessStyle}")
        log.debug("* defaultObjectAcl       = ${config.defaultObjectAcl}")
        log.debug("* defaultAcl             = ${config.defaultAcl}")
        log.debug("* secretKey              = ${if (config.secretKey == null) "****" else "*".repeat(config.secretKey!!.length)}")
        log.debug("* accessKey              = ${if (config.accessKey == null) "****" else "*".repeat(config.accessKey!!.length)}")
        log.debug("* endpoint               = ${config.endpoint}")
        log.debug("* provider               = ${config.provider.key}")
        log.debug("* region                 = ${config.region.id()}")
        log.debug("* bucket                 = ${config.bucket}")

        val builder = S3Client.builder()
            .region(config.region)

        if (config.enforcePathAccessStyle) {
            builder.serviceConfiguration {
                it.pathStyleAccessEnabled()
            }
        }

        if (config.secretKey != null || config.accessKey != null) {
            log.debug("Using static credentials!")
            builder.credentialsProvider(
                StaticCredentialsProvider.create(object: AwsCredentials {
                    override fun accessKeyId(): String = config.accessKey!!
                    override fun secretAccessKey(): String = config.secretKey!!
                })
            )
        }

        if (config.endpoint != null) {
            val uri = when (config.provider) {
                S3Provider.Custom -> config.endpoint
                S3Provider.Amazon -> "s3.amazonaws.com"
                S3Provider.Wasabi -> config.provider.endpoint
            } ?: error("Unable to locate endpoint in the provider or under the 'config.endpoint' option")

            if (uri.isEmpty()) {
                error("the endpoint cannot be empty")
            }

            builder.endpointOverride(URI.create(uri))
        }

        client = builder.build()
        log.debug("Created S3 client! Checking if bucket [${config.bucket}] exists!")

        val buckets = client.listBuckets().buckets()
        log.debug("Buckets: [${buckets.joinToString(", ") { it.name() }}]")

        val found = buckets.find { it.name() == config.bucket }
        if (found == null) {
            try {
                client.createBucket {
                    it.acl(config.defaultAcl)
                    it.bucket(config.bucket)
                }
            } catch (e: Exception) {
                throw e
            }
        } else {
            // do nothing if it was found
        }
    }

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    override suspend fun open(path: String): InputStream? {
        if (!::client.isInitialized) error("S3StorageTrailer#init/0 was not called.")

        // Check if we can find the object
        return try {
            client.getObject({
                it.bucket(config.bucket)
                it.key(path)
            }, ResponseTransformer.toInputStream()) ?: return null
        } catch (e: Exception) {
            if (e.message?.contains("key does not exist") == true) {
                return null
            }

            throw e
        }
    }

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    override suspend fun delete(path: String): Boolean {
        if (!::client.isInitialized) error("S3StorageTrailer#init/0 was not called.")

        val result = client.deleteObject {
            it.bucket(config.bucket)
            it.key(path)
        }

        // i don't know
        return result?.deleteMarker() ?: true
    }

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    override suspend fun exists(path: String): Boolean {
        if (!::client.isInitialized) error("S3StorageTrailer#init/0 was not called.")

        return try {
            val obj = client.headObject {
                it.bucket(config.bucket)
                it.key(path)
            }

            if (obj.deleteMarker()) return false
            true
        } catch (e: NoSuchKeyException) {
            false
        } catch (e: S3Exception) {
            throw e
        }
    }

    /**
     * Uploads file to this storage trailer and returns a [Boolean] result
     * if the operation was a success or not.
     *
     * @param path The path to upload the file to
     * @param stream The [InputStream] that represents the raw data.
     */
    override suspend fun upload(
        path: String,
        stream: InputStream,
        contentType: String
    ): Boolean {
        if (!::client.isInitialized) error("S3StorageTrailer#init/0 was not called.")

        // Get the content from the InputStream
        val contents = withContext(Dispatchers.IO) {
            stream.readAllBytes()
        }

        return try {
            client.putObject({
                it.bucket(config.bucket)
                it.key(path)
                it.contentLength(contents.size.toLong())
                it.contentType(contentType)
                it.acl(config.defaultObjectAcl)
            }, RequestBody.fromBytes(contents))

            true
        } catch (e: S3Exception) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lists all the contents as a list of [objects][Object].
     */
    override suspend fun listAll(includeInputStream: Boolean): List<Object> {
        var request = ListObjectsV2Request.builder()
            .bucket(config.bucket)
            .build()

        val list = mutableListOf<Object>()
        while (true) {
            val objects = client.listObjectsV2(request)
            for (content in objects.contents()) {
                val isDir = content.key().split("").last() == "/"
                if (isDir) continue

                val inputStream = if (includeInputStream) {
                    try {
                        client.getObject({
                            it.bucket(config.bucket)
                            it.key(content.key())
                        }, ResponseTransformer.toInputStream()) ?: null
                    } catch (e: Exception) {
                        if (e.message?.contains("key does not exist") == false) {
                            throw e
                        }

                        null
                    }
                } else {
                    null
                }

                val newInputStream = if (inputStream != null) {
                    val baos = ByteArrayOutputStream()
                    withContext(Dispatchers.IO) {
                        inputStream.transferTo(baos)
                    }

                    ByteArrayInputStream(baos.toByteArray())
                } else {
                    null
                }

                list.add(
                    Object(
                        if (inputStream == null) CHECK_WITH else figureContentType(inputStream),
                        newInputStream,
                        null,
                        content.lastModified().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
                        null,
                        content.size(),
                        content.key(),
                        content.eTag(),
                        "s3://${content.key()}"
                    )
                )
            }

            if (objects.nextContinuationToken() == null) {
                break
            }

            request = request.toBuilder()
                .continuationToken(objects.nextContinuationToken())
                .build()
        }

        return list.toList()
    }

    /**
     * Returns all the objects found via a [prefix].
     */
    override suspend fun list(prefix: String, includeInputStream: Boolean): List<Object> {
        var request = ListObjectsV2Request.builder()
            .bucket(config.bucket)
            .prefix(prefix)
            .build()

        val list = mutableListOf<Object>()
        while (true) {
            val objects = client.listObjectsV2(request)
            for (content in objects.contents()) {
                val isDir = content.key().split("").last() == "/"
                if (isDir) continue

                val inputStream = if (includeInputStream) {
                    try {
                        client.getObject({
                            it.bucket(config.bucket)
                            it.key(content.key())
                        }, ResponseTransformer.toInputStream()) ?: null
                    } catch (e: Exception) {
                        if (e.message?.contains("key does not exist") == false) {
                            throw e
                        }

                        null
                    }
                } else {
                    null
                }

                val newInputStream = if (inputStream != null) {
                    val baos = ByteArrayOutputStream()
                    withContext(Dispatchers.IO) {
                        inputStream.transferTo(baos)
                    }

                    ByteArrayInputStream(baos.toByteArray())
                } else {
                    null
                }

                list.add(
                    Object(
                        if (inputStream == null) CHECK_WITH else figureContentType(inputStream),
                        newInputStream,
                        null,
                        content.lastModified().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
                        null,
                        content.size(),
                        content.key(),
                        content.eTag(),
                        "s3://${content.key()}"
                    )
                )
            }

            if (objects.nextContinuationToken() == null) {
                break
            }

            request = request.toBuilder()
                .continuationToken(objects.nextContinuationToken())
                .build()
        }

        return list.toList()
    }

    override suspend fun fetch(key: String): Object? = fetch(key) {}
    suspend fun fetch(key: String, builder: GetObjectRequest.Builder.() -> Unit = {}): Object? {
        val request = GetObjectRequest.builder()
            .bucket(config.bucket)
            .key(key)
            .apply(builder)
            .build()

        val res = client.getObject(request) ?: return null
        val obj = res.response()

        val baos = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            res.transferTo(baos)
        }

        val inputStream = ByteArrayInputStream(baos.toByteArray())
        return Object(
            figureContentType(res),
            inputStream,
            null,
            obj.lastModified().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
            null,
            obj.contentLength(),
            key,
            obj.eTag(),
            "s3://$key"
        )
    }

    // Spawns a new executed thread via the executor service to push the load
    // into a new thread rather than blocking the new thread.
    private fun <R> withThreaded(
        timeout: Long,
        unit: TimeUnit,
        block: suspend () -> R
    ): R = try {
        val fut = executorService.submit {
            runBlocking {
                block()
            }
        }

        fut.get(timeout, unit) as R
    } catch (e: Exception) {
        log.error("Unable to run chunk of code in seperate thread pool:", e)
        throw e
    }
}
