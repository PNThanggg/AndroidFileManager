package com.modules.core.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationPreferences(
    val firstLaunch: Boolean = true,
    val accessToken: String = "",
)
