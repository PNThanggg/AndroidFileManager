package com.library.icon_loader_lib.icons.cache;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.LocaleList;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.library.icon_loader_lib.icons.BitmapInfo;

public interface CachingLogic<T> {

    ComponentName getComponent(T object);

    UserHandle getUser(T object);

    CharSequence getLabel(T object);

    default CharSequence getDescription(T object, CharSequence fallback) {
        return fallback;
    }

    @NonNull
    BitmapInfo loadIcon(Context context, T object);

    /**
     * Provides a option list of keywords to associate with this object
     */
    @Nullable
    default String getKeywords(T object, LocaleList localeList) {
        return null;
    }

    /**
     * Returns the timestamp the entry was last updated in cache.
     */
    default long getLastUpdatedTime(T object, PackageInfo info) {
        return info.lastUpdateTime;
    }

    /**
     * Returns true the object should be added to mem cache; otherwise returns false.
     */
    default boolean addToMemCache() {
        return true;
    }
}
