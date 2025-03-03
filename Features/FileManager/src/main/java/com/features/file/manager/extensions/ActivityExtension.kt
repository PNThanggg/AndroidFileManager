package com.features.file.manager.extensions

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.features.file.manager.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.module.core.extensions.baseConfig
import com.modules.core.ui.MyTextView

fun Activity.getAlertDialogBuilder() = if (baseConfig.isUsingSystemTheme) {
    MaterialAlertDialogBuilder(this)
} else {
    AlertDialog.Builder(this)
}


fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog.Builder,
    titleId: Int = 0,
    titleText: String = "",
    cancelOnTouchOutside: Boolean = true,
    callback: ((alertDialog: AlertDialog) -> Unit)? = null
) {
    if (isDestroyed || isFinishing) {
        return
    }

//    val textColor = getProperTextColor()
//    val primaryColor = getProperPrimaryColor()
//
//    if (view is ViewGroup) {
//        updateTextColors(view)
//    } else if (view is MyTextView) {
//        view.setColors(textColor, primaryColor)
//    }

    if (dialog is MaterialAlertDialogBuilder) {
        dialog.create().apply {
            if (titleId != 0) {
                setTitle(titleId)
            } else if (titleText.isNotEmpty()) {
                setTitle(titleText)
            }

            setView(view)
            setCancelable(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
//            getButton(Dialog.BUTTON_POSITIVE)?.setTextColor(primaryColor)
//            getButton(Dialog.BUTTON_NEGATIVE)?.setTextColor(primaryColor)
//            getButton(Dialog.BUTTON_NEUTRAL)?.setTextColor(primaryColor)
            callback?.invoke(this)
        }
    } else {
//        var title: DialogTitleBinding? = null
//        if (titleId != 0 || titleText.isNotEmpty()) {
//            title = DialogTitleBinding.inflate(layoutInflater, null, false)
//            title.dialogTitleTextview.apply {
//                if (titleText.isNotEmpty()) {
//                    text = titleText
//                } else {
//                    setText(titleId)
//                }
//                setTextColor(textColor)
//            }
//        }

//        // if we use the same primary and background color, use the text color for dialog confirmation buttons
//        val dialogButtonColor = if (primaryColor == baseConfig.backgroundColor) {
//            textColor
//        } else {
//            primaryColor
//        }

        dialog.create().apply {
            setView(view)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            setCustomTitle(title?.root)
            setCanceledOnTouchOutside(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
//            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
//            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
//            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialogButtonColor)
//
//            val bgDrawable = when {
//                isBlackAndWhiteTheme() -> resources.getDrawable(
//                    R.drawable.black_dialog_background, theme
//                )
//
//                baseConfig.isUsingSystemTheme -> resources.getDrawable(
//                    R.drawable.dialog_you_background, theme
//                )
//
//                else -> resources.getColoredDrawableWithColor(
//                    R.drawable.dialog_bg, baseConfig.backgroundColor
//                )
//            }
//
//            window?.setBackgroundDrawable(bgDrawable)
            callback?.invoke(this)
        }
    }
}
