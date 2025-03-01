package com.activities.videos.models

data class ShortcutItem(
    val textResId: Int, val iconResId: Int, val onClick: () -> Unit,
)