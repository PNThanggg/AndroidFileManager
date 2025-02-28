package com.modules.core.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoStreamInfo(
    val index: Int,
    val title: String? = null,
    val codecName: String,
    val language: String? = null,
    val disposition: Int,
    val bitRate: Long,
    val frameRate: Double,
    val frameWidth: Int,
    val frameHeight: Int
)