package com.modules.feature.player.model

import android.net.Uri

data class Subtitle(
    val name: String?,
    val uri: Uri,
    val isSelected: Boolean,
)
