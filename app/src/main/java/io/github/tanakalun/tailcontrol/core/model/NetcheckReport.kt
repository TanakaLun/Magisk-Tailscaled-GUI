package io.github.tanakalun.tailcontrol.core.model

import kotlinx.serialization.Serializable

@Serializable
data class NetcheckReport(
    val timestampMillis: Long,
    val raw: String,
)
