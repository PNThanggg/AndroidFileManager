package com.modules.core.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.module.core.extensions.adjustAlpha
import com.module.core.extensions.applyColorFilter

class MyEditText : AppCompatEditText {
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
        background?.mutate()?.applyColorFilter(accentColor)

        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(MEDIUM_ALPHA))
        setLinkTextColor(accentColor)
    }
}
