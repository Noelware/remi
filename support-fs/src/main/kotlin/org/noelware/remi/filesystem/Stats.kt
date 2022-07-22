package org.noelware.remi.filesystem

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Stats(
    @SerialName("unallocated_space")
    val unallocatedSpace: Long,

    @SerialName("usable_space")
    val usableSpace: Long,

    @SerialName("total_space")
    val totalSpace: Long,
    val drive: String,
    val type: String
)
