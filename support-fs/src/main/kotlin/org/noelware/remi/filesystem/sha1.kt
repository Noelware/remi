package org.noelware.remi.filesystem

import java.security.MessageDigest
import java.util.*

fun sha1(bytes: ByteArray): String {
    val sha1 = MessageDigest.getInstance("SHA1")
    return String(Base64.getEncoder().encode(sha1.digest(bytes)))
}
