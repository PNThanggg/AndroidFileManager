package com.modules.core.ui

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat

class MyTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MyTextView, 0, 0)
            val fontWeight =
                typedArray.getInt(R.styleable.MyTextView_fontWeight, FONT_WEIGHT_MEDIUM)
            val underLine = typedArray.getBoolean(R.styleable.MyTextView_underLine, false)
            typedArray.recycle()

            includeFontPadding = false

            val fontRes = when (fontWeight) {
                FONT_WEIGHT_LIGHT -> R.font.space_grotesk_light
                FONT_WEIGHT_REGULAR -> R.font.space_grotesk_regular
                FONT_WEIGHT_MEDIUM -> R.font.space_grotesk_medium
                FONT_WEIGHT_SEMI_BOLD -> R.font.space_grotesk_semibold
                FONT_WEIGHT_BOLD -> R.font.space_grotesk_bold
                else -> R.font.space_grotesk_medium
            }

            if (underLine) {
                val mSpannableString = SpannableString(text)
                mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
                text = mSpannableString
            }

            typeface = ResourcesCompat.getFont(context, fontRes)
        }
    }

    companion object {
        const val FONT_WEIGHT_LIGHT = 300
        const val FONT_WEIGHT_REGULAR = 400
        const val FONT_WEIGHT_MEDIUM = 500
        const val FONT_WEIGHT_SEMI_BOLD = 600
        const val FONT_WEIGHT_BOLD = 700
    }


    fun setColors(textColor: Int, accentColor: Int) {
        setTextColor(textColor)
        setLinkTextColor(accentColor)
    }
}
