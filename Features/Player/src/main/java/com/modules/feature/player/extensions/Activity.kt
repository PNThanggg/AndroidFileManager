package com.modules.feature.player.extensions

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Must call this function after any configuration done to activity to keep system bars behaviour
 */
fun Activity.swipeToShowStatusBars() {
    WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

/**
 * Toggles system bars visibility
 * @param showBars true to show system bars, false to hide
 * @param types [Type.InsetsType] system bars to toggle default is all system bars
 */
fun Activity.toggleSystemBars(showBars: Boolean, @Type.InsetsType types: Int = Type.systemBars()) {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (showBars) show(types) else hide(types)
    }
}

val Activity.currentBrightness: Float
    get() = when (val brightness = window.attributes.screenBrightness) {
        in WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF..WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL -> brightness
        else -> Settings.System.getFloat(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255
    }

val Activity.isPipSupported: Boolean
    get() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

val Activity.isPipEnabled: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps?.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE, Process.myUid(), packageName
                ) == AppOpsManager.MODE_ALLOWED
            } else {
                @Suppress("DEPRECATION")
                appOps?.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE, Process.myUid(), packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
        } else {
            false
        }
    }

val Activity.audioManager: AudioManager
    get() {
        return getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
