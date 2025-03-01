package com.library.icon_loader_lib.icons;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Build.VERSION_CODES;

/**
 * Interface representing a bitmap draw operation.
 */
public interface BitmapRenderer {

    boolean USE_HARDWARE_BITMAP = Build.VERSION.SDK_INT >= VERSION_CODES.P;

    static Bitmap createSoftwareBitmap(int width, int height, BitmapRenderer renderer) {
        GraphicsUtils.noteNewBitmapCreated();
        Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        renderer.draw(new Canvas(result));
        return result;
    }

    @TargetApi(VERSION_CODES.P)
    static Bitmap createHardwareBitmap(int width, int height, BitmapRenderer renderer) {
        if (!USE_HARDWARE_BITMAP) {
            return createSoftwareBitmap(width, height, renderer);
        }

        GraphicsUtils.noteNewBitmapCreated();
        Picture picture = new Picture();
        renderer.draw(picture.beginRecording(width, height));
        picture.endRecording();
        return Bitmap.createBitmap(picture);
    }

    /**
     * Returns a bitmap from subset of the source bitmap. The new bitmap may be the
     * same object as source, or a copy may have been made.
     */
    static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O && source.getConfig() == Config.HARDWARE) {
            return createHardwareBitmap(width, height, c -> c.drawBitmap(source,
                    new Rect(x, y, x + width, y + height), new RectF(0, 0, width, height), null));
        } else {
            GraphicsUtils.noteNewBitmapCreated();
            return Bitmap.createBitmap(source, x, y, width, height);
        }
    }

    void draw(Canvas out);
}
