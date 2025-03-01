package com.library.icon_loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.library.icon_loader_lib.icons.BaseIconFactory;
import com.library.icon_loader_lib.icons.BitmapInfo;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AppIconLoader {
    @Px
    private final int mIconSize;
    private final boolean mShrinkNonAdaptiveIcons;
    @NonNull
    private final Context mContext;

    @NonNull
    private final ConcurrentLinkedQueue<IconFactory> mIconFactoryPool = new ConcurrentLinkedQueue<>();

    public AppIconLoader(@Px int iconSize, boolean shrinkNonAdaptiveIcons, @NonNull Context context) {
        mIconSize = iconSize;
        mShrinkNonAdaptiveIcons = shrinkNonAdaptiveIcons;
        mContext = context;
    }

    @NonNull
    public static String getIconKey(@NonNull ApplicationInfo applicationInfo, long versionCode, @NonNull Context context) {
        UserHandle user = UserHandleCompat.getUserHandleForUid(applicationInfo.uid);
        return applicationInfo.packageName + ":" + versionCode + ":" + UserSerialNumberCache.getSerialNumber(user, context);
    }

    @NonNull
    public static String getIconKey(@NonNull PackageInfo packageInfo, @NonNull Context context) {
        return getIconKey(packageInfo.applicationInfo, PackageInfoCompat.getLongVersionCode(packageInfo), context);
    }

    @NonNull
    public Bitmap loadIcon(@NonNull ApplicationInfo applicationInfo, boolean isInstantApp) {
        Drawable unbadgedIcon = PackageItemInfoCompat.loadUnbadgedIcon(applicationInfo, mContext.getPackageManager());
        UserHandle user = UserHandleCompat.getUserHandleForUid(applicationInfo.uid);
        IconFactory iconFactory = mIconFactoryPool.poll();
        if (iconFactory == null) {
            iconFactory = new IconFactory(mIconSize, mContext);
        }
        try {
            return iconFactory.createBadgedIconBitmap(unbadgedIcon, user, mShrinkNonAdaptiveIcons, isInstantApp).icon;
        } finally {
            mIconFactoryPool.offer(iconFactory);
        }
    }

    @NonNull
    public Bitmap loadIcon(@NonNull ApplicationInfo applicationInfo) {
        return loadIcon(applicationInfo, false);
    }

    private static class IconFactory extends BaseIconFactory {
        private final float[] mTempScale = new float[1];

        public IconFactory(@Px int iconBitmapSize, @NonNull Context context) {
            super(context, context.getResources().getConfiguration().densityDpi, iconBitmapSize, true);

            disableColorExtraction();
        }

        @NonNull
        public BitmapInfo createBadgedIconBitmap(@NonNull Drawable icon, @Nullable UserHandle user, boolean shrinkNonAdaptiveIcons, boolean isInstantApp) {
            return super.createBadgedIconBitmap(icon, user, shrinkNonAdaptiveIcons, isInstantApp, mTempScale);
        }
    }
}
