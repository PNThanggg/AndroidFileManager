package com.library.icon_loader;

import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.annotation.NonNull;

class PackageInfoCompat {
    private PackageInfoCompat() {
    }

    public static long getLongVersionCode(@NonNull PackageInfo info) {
        if (Build.VERSION.SDK_INT >= 28) {
            return info.getLongVersionCode();
        } else {
            //noinspection deprecation
            return info.versionCode;
        }
    }
}
