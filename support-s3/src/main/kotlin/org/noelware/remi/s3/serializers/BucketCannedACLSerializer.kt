package org.noelware.remi.s3.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import software.amazon.awssdk.services.s3.model.BucketCannedACL

/**
 * kotlinx.serialization support for [BucketCannedACL].
 */
object BucketCannedACLSerializer: KSerializer<BucketCannedACL> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("s3.BucketCannedACL", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BucketCannedACL) {
        // Since `BucketCannedACL#value` is null, we need to do this
        // ourselves. Thanks AWS.
        val actualValue = when (value) {
            BucketCannedACL.PUBLIC_READ -> "public-read"
            BucketCannedACL.AUTHENTICATED_READ -> "authenticated-read"
            BucketCannedACL.PRIVATE -> "private"
            BucketCannedACL.PUBLIC_READ_WRITE -> "public-read-write"
            BucketCannedACL.UNKNOWN_TO_SDK_VERSION -> null
            else -> null
        } ?: error("Unknown ACL: $value")

        encoder.encodeString(actualValue)
    }

    override fun deserialize(decoder: Decoder): BucketCannedACL = when (val key = decoder.decodeString()) {
        "private" -> BucketCannedACL.PRIVATE
        "public-read" -> BucketCannedACL.PUBLIC_READ
        "public-read-write" -> BucketCannedACL.PUBLIC_READ_WRITE
        "authenticated-read" -> BucketCannedACL.AUTHENTICATED_READ
        else -> error("Unknown ACL: $key")
    }
}
