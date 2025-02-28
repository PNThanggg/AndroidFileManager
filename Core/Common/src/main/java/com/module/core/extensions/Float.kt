package com.module.core.extensions

import android.content.res.Resources
import kotlin.math.pow
import kotlin.math.roundToInt

fun Float.round(decimalPlaces: Int): Float {
    val numerator = this * 10.0.pow(decimalPlaces.toDouble()).roundToInt()
    val denominator = 10.0.pow(decimalPlaces.toDouble()).toFloat()
    return numerator / denominator
}

/**
 * Converts px to dp.
 */
val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density