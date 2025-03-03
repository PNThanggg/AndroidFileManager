package com.module.core.extensions

import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.module.core.common.R
import com.module.core.utils.isSdk34
import com.module.core.utils.isSdkR

/**
 * Retrieves the screen width of the device in pixels, accounting for system bars when applicable.
 *
 * This extension function calculates the available screen width for the [Activity]. For devices running
 * Android SDK 30 (R) or higher, it uses [WindowManager.currentWindowMetrics] and adjusts for system bar
 * insets (e.g., status bar, navigation bar). For older SDK versions, it falls back to the deprecated
 * [Display.getMetrics] method to retrieve the width in pixels.
 *
 * @return The width of the screen in pixels, adjusted for system bars if present.
 */
fun Activity.getScreenWidth(): Int {
    return if (isSdkR()) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun Activity.getScreenHeight(): Int {
    return if (isSdkR()) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.top - insets.bottom
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

/**
 * call when finish activity to apply animation
 *
 * */
fun Activity.finishWithSlide() {
    finish()
    if (isSdk34()) {
        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            Color.TRANSPARENT
        )
    } else {
        @Suppress("DEPRECATION") overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
        )
    }
}

/**
 * Displays the soft keyboard for the specified [EditText] in the [Activity].
 *
 * This extension function requests focus for the provided [EditText] and triggers the soft keyboard
 * to appear by using the [InputMethodManager]. The keyboard is shown implicitly, meaning it does not
 * force the keyboard to appear if it is not appropriate (e.g., if the user has disabled it).
 *
 * @param et The [EditText] view for which the keyboard should be shown.
 */
fun Activity.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

private fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

/**
 * Hides the soft keyboard synchronously for the specified [Activity] and clears focus from the current view.
 *
 * This function uses the [InputMethodManager] to hide the soft keyboard from the window of the currently
 * focused view in the provided [Activity] (or a fallback view if no view is focused). It also adjusts
 * the window's soft input mode to ensure the keyboard remains hidden and clears focus from the current
 * view if one exists.
 *
 * @param activity The [Activity] in which the keyboard should be hidden.
 */
private fun hideKeyboardSync(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(
        (activity.currentFocus ?: View(activity)).windowToken, 0
    )
    activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    activity.currentFocus?.clearFocus()
}

/**
 * Hides the soft keyboard in the [Activity], ensuring the operation runs on the main thread.
 *
 * This extension function checks if the current thread is the main thread. If it is, it directly
 * calls [hideKeyboardSync] with the current [Activity] to hide the keyboard. If not, it posts the
 * [hideKeyboardSync] operation to the main thread using a [Handler] tied to the main looper.
 */
fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync(this)
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync(this)
        }
    }
}
