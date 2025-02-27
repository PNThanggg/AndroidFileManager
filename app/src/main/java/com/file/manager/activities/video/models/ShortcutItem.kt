package com.file.manager.activities.video.models

data class ShortcutItem(
    val textResId: Int, val iconResId: Int, val onClick: () -> Unit,
)