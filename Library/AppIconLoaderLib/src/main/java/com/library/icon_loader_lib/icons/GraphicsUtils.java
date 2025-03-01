package com.library.icon_loader_lib.icons;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.ColorInt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GraphicsUtils {

    private static final String TAG = "GraphicsUtils";

    public static Runnable sOnNewBitmapRunnable = () -> {
    };

    /**
     * Set the alpha component of {@code color} to be {@code alpha}. Unlike the support lib version,
     * it bounds the alpha in valid range instead of throwing an exception to allow for safer
     * interpolation of color animations
     */
    @ColorInt
    public static int setColorAlphaBound(int color, int alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 255) {
            alpha = 255;
        }
        return (color & 0x00ffffff) | (alpha << 24);
    }

    /**
     * Compresses the bitmap to a byte array for serialization.
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(getExpectedBitmapSize(bitmap));
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w(TAG, "Could not write bitmap");
            return null;
        }
    }

    /**
     * Try go guesstimate how much space the icon will take when serialized to avoid unnecessary
     * allocations/copies during the write (4 bytes per pixel).
     */
    static int getExpectedBitmapSize(Bitmap bitmap) {
        return bitmap.getWidth() * bitmap.getHeight() * 4;
    }

    public static int getArea(Region r) {
        RegionIterator itr = new RegionIterator(r);
        int area = 0;
        Rect tempRect = new Rect();
        while (itr.next(tempRect)) {
            area += tempRect.width() * tempRect.height();
        }
        return area;
    }

    /**
     * Utility method to track new bitmap creation
     */
    public static void noteNewBitmapCreated() {
        sOnNewBitmapRunnable.run();
    }


    /**
     * Returns the default path to be used by an icon
     */
    public static Path getShapePath(int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AdaptiveIconDrawable drawable = new AdaptiveIconDrawable(new ColorDrawable(Color.BLACK), new ColorDrawable(Color.BLACK));
            drawable.setBounds(0, 0, size, size);
            return new Path(drawable.getIconMask());
        } else {
            return new Path();
        }
    }

    /**
     * Returns the color associated with the attribute
     */
    public static int getAttrColor(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    /**
     * Returns the alpha corresponding to the theme attribute {@param attr}
     */
    public static float getFloat(Context context, int attr, float defValue) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float value = ta.getFloat(0, defValue);
        ta.recycle();
        return value;
    }
}
