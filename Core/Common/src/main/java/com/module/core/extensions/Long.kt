package com.module.core.extensions

import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Formats the given duration in milliseconds to a string in the format of `mm:ss` or `hh:mm:ss`.
 */
val Long.formatDurationMillis: String
    get() {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(hours)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(
                hours
            )
        return if (hours > 0) {
            String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }

/**
 * Formats the given duration in milliseconds to a string in the format of
 * `+mm:ss` or `+hh:mm:ss` or `-mm:ss` or `-hh:mm:ss`.
 */
val Long.formatDurationMillisSign: String
    get() {
        return if (this >= 0) {
            "+${this.formatDurationMillis}"
        } else {
            "-${abs(this).formatDurationMillis}"
        }
    }

val Long.formatFileSize: String
    get() {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            this < kb -> "$this B"
            this < mb -> "%.2f KB".format(this / kb.toDouble())
            this < gb -> "%.2f MB".format(this / mb.toDouble())
            else -> "%.2f GB".format(this / gb.toDouble())
        }
    }

val Long.formatBitrate: String?
    get() {
        if (this <= 0) {
            return null
        }

        val kiloBitrate = this.toDouble() / 1000.0
        val megaBitrate = kiloBitrate / 1000.0
        val gigaBitrate = megaBitrate / 1000.0

        return when {
            gigaBitrate >= 1.0 -> String.format(Locale.ROOT, "%.1f Gbps", gigaBitrate)
            megaBitrate >= 1.0 -> String.format(Locale.ROOT, "%.1f Mbps", megaBitrate)
            kiloBitrate >= 1.0 -> String.format(Locale.ROOT, "%.1f kbps", kiloBitrate)
            else -> String.format(Locale.ROOT, "%d bps", this)
        }
    }

fun Long.formatDate(
    dateFormat: String? = null, timeFormat: String? = null
): String {
    val useDateFormat = dateFormat ?: "dd-MM-yyyy"
    val useTimeFormat = timeFormat ?: "HH:mm"
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format("$useDateFormat, $useTimeFormat", cal).toString()
}
