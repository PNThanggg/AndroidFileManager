package com.library.icon_loader;

import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class PackageItemInfoCompat {
    private PackageItemInfoCompat() {
    }

    public static Drawable loadUnbadgedIcon(@NonNull PackageItemInfo packageItemInfo, @NonNull PackageManager packageManager) {
        return packageItemInfo.loadUnbadgedIcon(packageManager);
    }

    @Nullable
    private static ApplicationInfo getApplicationInfo(@NonNull PackageItemInfo packageItemInfo) {
        if (packageItemInfo instanceof ApplicationInfo) {
            return (ApplicationInfo) packageItemInfo;
        } else if (packageItemInfo instanceof ComponentInfo) {
            return ((ComponentInfo) packageItemInfo).applicationInfo;
        } else {
            return null;
        }
    }

    @NonNull
    private static Drawable loadDefaultIcon(@NonNull PackageItemInfo packageItemInfo, @NonNull PackageManager packageManager) {
        return packageManager.getDefaultActivityIcon();
    }
}
