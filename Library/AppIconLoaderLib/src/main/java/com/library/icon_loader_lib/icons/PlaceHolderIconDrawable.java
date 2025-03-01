package com.library.icon_loader_lib.icons;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.graphics.ColorUtils;

import com.library.icon_loader_lib.R;

/**
 * Subclass which draws a placeholder icon when the actual icon is not yet loaded
 */
public class PlaceHolderIconDrawable extends FastBitmapDrawable {

    // Path in [0, 100] bounds.
    private final Path mProgressPath;

    public PlaceHolderIconDrawable(BitmapInfo info, Context context) {
        super(info);

        mProgressPath = GraphicsUtils.getShapePath(100);
        mPaint.setColor(ColorUtils.compositeColors(GraphicsUtils.getAttrColor(context, R.attr.loadingIconColor), info.color));
    }

    @Override
    protected void drawInternal(Canvas canvas, Rect bounds) {
        int saveCount = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        canvas.scale(bounds.width() / 100f, bounds.height() / 100f);
        canvas.drawPath(mProgressPath, mPaint);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Updates this placeholder to {@code newIcon} with animation.
     */
    public void animateIconUpdate(Drawable newIcon) {
        int placeholderColor = mPaint.getColor();
        int originalAlpha = Color.alpha(placeholderColor);

        ValueAnimator iconUpdateAnimation = ValueAnimator.ofInt(originalAlpha, 0);
        iconUpdateAnimation.setDuration(375);
        iconUpdateAnimation.addUpdateListener(valueAnimator -> {
            int newAlpha = (int) valueAnimator.getAnimatedValue();
            int newColor = ColorUtils.setAlphaComponent(placeholderColor, newAlpha);

            newIcon.setColorFilter(new PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_ATOP));
        });
        iconUpdateAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                newIcon.setColorFilter(null);
            }
        });
        iconUpdateAnimation.start();
    }

}
