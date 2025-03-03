package com.module.core.extensions

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback


/**
 * Adds a custom back press handler to the [ComponentActivity].
 *
 * This extension function registers a callback to the [onBackPressedDispatcher] of the activity,
 * allowing you to define a custom action to be executed when the back button is pressed.
 *
 * @param action A lambda function that defines the custom behavior to be executed when the back button is pressed.
 */
fun ComponentActivity.handleBackPressed(action: () -> Unit) {
    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                action()
            }
        },
    )
}