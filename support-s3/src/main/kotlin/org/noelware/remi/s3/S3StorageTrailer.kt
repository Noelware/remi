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
package org.noelware.remi.s3

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.serialization.*
import org.noelware.remi.core.CHECK_WITH
import org.noelware.remi.core.Configuration
import org.noelware.remi.core.Object
import org.noelware.remi.core.StorageTrailer
import org.noelware.remi.s3.serializers.AwsRegionSerializer
import org.noelware.remi.s3.serializers.BucketCannedACLSerializer
import org.noelware.remi.s3.serializers.ObjectCannedACLSerializer
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketCannedACL
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.InputStream
import java.net.URI

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
    val enableSignerV4Requests: Boolean = false,

    /**
     * If the S3 client should be configured to use the new path style for S3 connections, read more
     * [here](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3Builder.html#setPathStyleAccessEnabled-java.lang.Boolean-). This needs to be `true` if you're using a MinIO connection.
     */
    @SerialName("enforce_path_access_style")
    val enforcePathAccessStyle: Boolean = false,

    @SerialName("default_object_acl")
    @Serializable(with = ObjectCannedACLSerializer::class)
    val defaultObjectAcl: ObjectCannedACL = ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL,

    @SerialName("default_bucket_acl")
    @Serializable(with = BucketCannedACLSerializer::class)
    val defaultAcl: BucketCannedACL = BucketCannedACL.PUBLIC_READ,
    val secretKey: String? = null,
    val accessKey: String? = null,
    val endpoint: String? = null,
    val provider: S3Provider = S3Provider.Amazon,

    @Serializable(with = AwsRegionSerializer::class)
    val region: Region = Region.US_EAST_1,
    val bucket: String = "remi"
): Configuration

class S3StorageTrailer(override val config: S3StorageConfig): StorageTrailer<S3StorageConfig> {
    lateinit var client: S3Client
    override val name: String = "remi:s3"

    override suspend fun init() {
        val builder = S3Client.builder()
            .region(config.region)

        if (config.enforcePathAccessStyle) {
            builder.serviceConfiguration {
                it.pathStyleAccessEnabled()
            }
        }

        if (config.secretKey != null || config.accessKey != null) {
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

            if (uri.isEmpty())
                error("the endpoint cannot be empty")

            builder.endpointOverride(URI.create(uri))
        }

        client = builder.build()
        val buckets = client.listBuckets().buckets()
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
            if (e.message?.contains("key does not exist") == true)
                return null

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

        val obj = client.headObject {
            it.bucket(config.bucket)
            it.key(path)
        }

        // TODO: does this mean the file is deleted...?
        if (obj.deleteMarker()) return false

        // TODO: does this mean it exists? LOL
        return obj.hasMetadata()
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
    override suspend fun listAll(): List<Object> {
        var request = ListObjectsV2Request.builder()
            .bucket(config.bucket)
            .build()

        val list = mutableListOf<Object>()
        while (true) {
            val objects = client.listObjectsV2(request)
            for (content in objects.contents()) {
                val isDir = content.key().split("").last() == "/"
                if (isDir) continue

                list.add(org.noelware.remi.core.Object(
                    CHECK_WITH,
                    null,
                    content.lastModified().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
                    null,
                    content.size(),
                    content.key()
                ))
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
}
