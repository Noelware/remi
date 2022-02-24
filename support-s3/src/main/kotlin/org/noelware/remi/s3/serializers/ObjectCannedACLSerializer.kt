package org.noelware.remi.s3.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import software.amazon.awssdk.services.s3.model.ObjectCannedACL

object ObjectCannedACLSerializer: KSerializer<ObjectCannedACL> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("s3.ObjectCannedACL", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ObjectCannedACL) {
        val aclType = when (value) {
            ObjectCannedACL.PRIVATE -> "private"
            ObjectCannedACL.PUBLIC_READ -> "public-read"
            ObjectCannedACL.PUBLIC_READ_WRITE -> "public-read-write"
            ObjectCannedACL.AUTHENTICATED_READ -> "authenticated-read"
            ObjectCannedACL.AWS_EXEC_READ -> "aws-exec-read"
            ObjectCannedACL.BUCKET_OWNER_READ -> "bucket-owner-read"
            ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL -> "bucket-owner-full-control"
            ObjectCannedACL.UNKNOWN_TO_SDK_VERSION -> error("cannot use `UNKNOWN_TO_SDK_VERSION`.")
            else -> error("Unknown object ACL: $value")
        }

        encoder.encodeString(aclType)
    }

    override fun deserialize(decoder: Decoder): ObjectCannedACL = when (val key = decoder.decodeString()) {
        "bucket-owner-full-control" -> ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL
        "bucket-owner-read" -> ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL
        "authenticated-read" -> ObjectCannedACL.AUTHENTICATED_READ
        "aws-exec-read" -> ObjectCannedACL.AWS_EXEC_READ
        "public-read-write" -> ObjectCannedACL.PUBLIC_READ_WRITE
        "public-read" -> ObjectCannedACL.PUBLIC_READ
        "private" -> ObjectCannedACL.PRIVATE
        else -> error("Unknown ACL type: $key")
    }
}
