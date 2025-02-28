package com.modules.feature.player.extensions

import com.modules.core.datastore.models.PlayerPreferences
import com.modules.core.model.FastSeek

fun PlayerPreferences.shouldFastSeek(duration: Long): Boolean {
    return when (fastSeek) {
        FastSeek.ENABLE -> true
        FastSeek.DISABLE -> false
        FastSeek.AUTO -> duration >= minDurationForFastSeek
    }
}
