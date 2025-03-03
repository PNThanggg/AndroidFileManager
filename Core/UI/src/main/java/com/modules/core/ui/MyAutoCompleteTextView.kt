package com.modules.core.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.module.core.extensions.adjustAlpha
import com.module.core.extensions.applyColorFilter

class MyAutoCompleteTextView : AppCompatAutoCompleteTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    )

    fun setColors(textColor: Int, accentColor: Int) {
        background?.mutate()?.applyColorFilter(accentColor)

        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(0.5f))
        setLinkTextColor(accentColor)
    }
}
