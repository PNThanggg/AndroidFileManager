package com.modules.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.res.ResourcesCompat
import com.module.core.extensions.adjustAlpha

class MyAppCompatCheckbox : AppCompatCheckBox {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(context)
    }

    private fun init(context: Context) {
        typeface = ResourcesCompat.getFont(context, R.font.space_grotesk_medium)
    }

    fun setColors(textColor: Int, accentColor: Int) {
        setTextColor(textColor)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(
                textColor.adjustAlpha(0.6f), accentColor
            ),
        )
        supportButtonTintList = colorStateList
    }
}
