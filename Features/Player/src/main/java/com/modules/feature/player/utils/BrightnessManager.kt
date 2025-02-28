package com.modules.feature.player.utils

import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.modules.feature.player.PlayerActivity
import com.modules.feature.player.extensions.currentBrightness
import com.modules.feature.player.extensions.swipeToShowStatusBars

@OptIn(UnstableApi::class)
class BrightnessManager(
    private val activity: PlayerActivity
) {

    var currentBrightness = activity.currentBrightness
    val maxBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL

    val brightnessPercentage get() = (currentBrightness / maxBrightness).times(100).toInt()

    fun setBrightness(brightness: Float) {
        currentBrightness = brightness.coerceIn(0f, maxBrightness)
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = currentBrightness
        activity.window.attributes = layoutParams

        // fixes a bug which makes the action bar reappear after changing the brightness
        activity.swipeToShowStatusBars()
    }
}
