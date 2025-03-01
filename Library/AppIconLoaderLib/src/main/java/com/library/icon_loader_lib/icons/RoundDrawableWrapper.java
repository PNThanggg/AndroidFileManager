package com.library.icon_loader_lib.icons;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

import androidx.annotation.NonNull;

/**
 * A drawable which clips rounded corner around a child drawable
 */
public class RoundDrawableWrapper extends DrawableWrapper {

    private final RectF mTempRect = new RectF();
    private final Path mClipPath = new Path();
    private final float mRoundedCornersRadius;

    public RoundDrawableWrapper(Drawable dr, float radius) {
        super(dr);
        mRoundedCornersRadius = radius;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        mTempRect.set(getBounds());
        mClipPath.reset();
        mClipPath.addRoundRect(mTempRect, mRoundedCornersRadius, mRoundedCornersRadius, Path.Direction.CCW);
        super.onBoundsChange(bounds);
    }

    @Override
    public final void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.clipPath(mClipPath);
        super.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
