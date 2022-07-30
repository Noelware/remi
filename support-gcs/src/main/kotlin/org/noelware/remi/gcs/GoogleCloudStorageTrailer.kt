package org.noelware.remi.gcs

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.*
import dev.floofy.utils.slf4j.logging
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.SerialName
import org.noelware.remi.core.Configuration
import org.noelware.remi.core.Object
import org.noelware.remi.core.StorageTrailer
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Represents the configuration for the [GoogleCloudStorageTrailer].
 */
@kotlinx.serialization.Serializable
data class GoogleCloudStorageConfiguration(
    /**
     * The credentials file to use. This must be a valid file.
     */
    @SerialName("credentials_file")
    var credentialsFile: String? = null,

    /**
     * Project ID for quotas.
     */
    var quotaProjectId: String? = null,

    /**
     * The access key to connect to your bucket. Not recommended in production,
     * use [credentialsFile] instead.
     */
    var accessKey: String? = null,

    /**
     * The secret key to connect to your bucket. Not recommended in production,
     * use [credentialsFile] instead.
     */
    var secret: String? = null,

    /**
     * The project ID.
     */
    @SerialName("project_id")
    var projectId: String,

    /**
     * The bucket name to store your data in.
     */
    var bucket: String = "remi"
): Configuration {
    init {
        if (credentialsFile != null) {
            val file = File(credentialsFile)
            if (!file.exists())
                throw IllegalStateException("Credentials file [$credentialsFile] must be a valid file path.")

            if (file.isDirectory)
                throw IllegalStateException("Credentials file [$credentialsFile] pointed towards a directory.")
        }
    }
}

fun GoogleCloudStorageTrailer(projectId: String, config: GoogleCloudStorageConfiguration.() -> Unit = {}): GoogleCloudStorageTrailer =
    GoogleCloudStorageTrailer(GoogleCloudStorageConfiguration(projectId = projectId).apply(config))

/**
 * Represents the storage provider for using Google's Cloud Storage service.
 */
class GoogleCloudStorageTrailer(override val config: GoogleCloudStorageConfiguration): StorageTrailer<GoogleCloudStorageConfiguration> {
    private lateinit var storage: Storage
    override val name: String = "remi:gcs"
    private val log by logging<GoogleCloudStorageTrailer>()

    override suspend fun init() {
        if (::storage.isInitialized) {
            log.warn("Storage bucket was already initialized, skipping.")
            return
        }

        log.info("Initializing Google Cloud Storage provider...")
        val builder = StorageOptions.newBuilder().apply {
            if (config.credentialsFile != null) {
                var credentialsFile = File(config.credentialsFile!!)
                if (Files.isSymbolicLink(credentialsFile.toPath())) {
                    val resolved = Files.readSymbolicLink(credentialsFile.toPath())
                    log.info("Resolved symbolic link for credentials to [$resolved]")

                    credentialsFile = resolved.toFile()
                }

                setCredentials(GoogleCredentials.fromStream(credentialsFile.inputStream()))
            } else if (config.accessKey != null) {

            }

            setProjectId(config.projectId)
            if (config.quotaProjectId != null) setQuotaProjectId(config.projectId)
        }.build()

        storage = builder.service

        log.info("Created storage trailer! Creating bucket if it doesn't exist...")
        val bucket = storage.list().iterateAll().toList().singleOrNull { it.name == config.bucket }
        if (bucket == null) {
            log.warn("Bucket ${config.bucket} doesn't exist! Creating...")
            storage.create(BucketInfo.of(config.bucket))
        }
    }

    /**
     * Opens a file under the [path] and returns the [InputStream] of the file.
     */
    override suspend fun open(path: String): InputStream? =
        ByteArrayInputStream(storage.get(BlobId.of(config.bucket, path)).getContent())

    /**
     * Deletes the file under the [path] and returns a [Boolean] if the
     * operation was a success or not.
     */
    override suspend fun delete(path: String): Boolean {
        storage.delete(BlobId.of(config.bucket, path))
        return true
    }

    /**
     * Checks if the file exists under this storage trailer.
     * @param path The path to find the file.
     */
    override suspend fun exists(path: String): Boolean = false

    /**
     * Uploads file to this storage trailer and returns a [Boolean] result
     * if the operation was a success or not.
     *
     * @param path The path to upload the file to
     * @param stream The [InputStream] that represents the raw data.
     * @param contentType The content type of the file (useful for S3 and GCS support)!
     */
    override suspend fun upload(path: String, stream: InputStream, contentType: String): Boolean {
        storage.create(BlobInfo.newBuilder(BlobId.of(config.bucket, path)).apply {
            setContentType(contentType)
        }.build(), stream)

        return true
    }

    /**
     * Lists all the contents as a list of [objects][Object].
     * @param includeInputStream If the input stream should be fetched. This is only applicable
     *                           in the S3 or MinIO storage trailers. The filesystem one just ignores this.
     *                           This is a property since re-fetching the input stream from the data source
     *                           can be time-consuming if iterating over a lot of objects.
     */
    override suspend fun listAll(includeInputStream: Boolean): List<Object> = storage
        .list(config.bucket)
        .iterateAll()
        .toList()
        .map {
            Object(
                it.contentType,
                ByteArrayInputStream(it.getContent()),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.createTime), ZoneId.systemDefault()).toKotlinLocalDateTime(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.updateTime), ZoneId.systemDefault()).toKotlinLocalDateTime(),
                null,
                it.size,
                it.name,
                it.etag,
                "gcs://${it.name}"
            )
        }

    /**
     * Lists all the contents via a [prefix] to search from.
     * @param prefix The prefix
     * @param includeInputStream If the input stream should be fetched. This is only applicable
     *                           in the S3 or MinIO storage trailers. The filesystem one just ignores this.
     *                           This is a property since re-fetching the input stream from the data source
     *                           can be time-consuming if iterating over a lot of objects.
     */
    override suspend fun list(prefix: String, includeInputStream: Boolean): List<Object> = storage
        .list(config.bucket, Storage.BlobListOption.prefix(prefix))
        .iterateAll()
        .toList()
        .map {
            Object(
                it.contentType,
                ByteArrayInputStream(it.getContent()),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.createTime), ZoneId.systemDefault()).toKotlinLocalDateTime(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.updateTime), ZoneId.systemDefault()).toKotlinLocalDateTime(),
                null,
                it.size,
                it.name,
                it.etag,
                "gcs://${it.name}"
            )
        }

    /**
     * Fetches an object from this storage trailer and transforms the metadata to a [Remi Object][Object].
     * @param key The key to select to find the object.
     * @return The metadata object or `null` if the object with the specified [key] wasn't found.
     */
    override suspend fun fetch(key: String): Object? = storage.get(BlobId.of(config.bucket, key)).let {
        Object(
            it.contentType,
            ByteArrayInputStream(it.getContent()),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it.createTime), ZoneId.systemDefault()).toKotlinLocalDateTime(),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it.updateTime), ZoneId.systemDefault()).toKotlinLocalDateTime(),
            null,
            it.size,
            it.name,
            it.etag,
            "gcs://${it.name}"
        )
    }
}
