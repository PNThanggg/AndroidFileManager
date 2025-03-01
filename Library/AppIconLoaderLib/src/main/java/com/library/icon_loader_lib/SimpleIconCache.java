package com.library.icon_loader_lib;

import static android.content.Intent.ACTION_MANAGED_PROFILE_ADDED;
import static android.content.Intent.ACTION_MANAGED_PROFILE_REMOVED;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.SparseLongArray;

import com.library.icon_loader_lib.icons.BaseIconFactory;
import com.library.icon_loader_lib.icons.cache.BaseIconCache;

/**
 * Wrapper class to provide access to {@link BaseIconFactory} and also to provide pool of this class
 * that are threadsafe.
 */
@TargetApi(Build.VERSION_CODES.P)
public class SimpleIconCache extends BaseIconCache {

    private static SimpleIconCache sIconCache = null;
    private static final Object CACHE_LOCK = new Object();

    private final SparseLongArray mUserSerialMap = new SparseLongArray(2);
    private final UserManager mUserManager;

    public SimpleIconCache(Context context, String dbFileName, Looper bgLooper, int iconDpi, int iconPixelSize, boolean inMemoryCache) {
        super(context, dbFileName, bgLooper, iconDpi, iconPixelSize, inMemoryCache);
        mUserManager = context.getSystemService(UserManager.class);

        // Listen for user cache changes.
        IntentFilter filter = new IntentFilter(ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(ACTION_MANAGED_PROFILE_REMOVED);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                resetUserCache();
            }
        }, filter, null, new Handler(bgLooper), 0);
    }

    @Override
    protected long getSerialNumberForUser(UserHandle user) {
        synchronized (mUserSerialMap) {
            int index = mUserSerialMap.indexOfKey(user.getIdentifier());
            if (index >= 0) {
                return mUserSerialMap.valueAt(index);
            }
            long serial = mUserManager.getSerialNumberForUser(user);
            mUserSerialMap.put(user.getIdentifier(), serial);
            return serial;
        }
    }

    private void resetUserCache() {
        synchronized (mUserSerialMap) {
            mUserSerialMap.clear();
        }
    }

    @Override
    protected boolean isInstantApp(ApplicationInfo info) {
        return info.isInstantApp();
    }

    @Override
    public BaseIconFactory getIconFactory() {
        return IconFactory.obtain(mContext);
    }

    public static SimpleIconCache getIconCache(Context context) {
        synchronized (CACHE_LOCK) {
            if (sIconCache != null) {
                return sIconCache;
            }
            boolean inMemoryCache = context.getResources().getBoolean(R.bool.simple_cache_enable_im_memory);
            String dbFileName = context.getString(R.string.cache_db_name);

            HandlerThread bgThread = new HandlerThread("simple-icon-cache");
            bgThread.start();

            sIconCache = new SimpleIconCache(context.getApplicationContext(), dbFileName, bgThread.getLooper(), context.getResources().getConfiguration().densityDpi, context.getResources().getDimensionPixelSize(R.dimen.default_icon_bitmap_size), inMemoryCache);
            return sIconCache;
        }
    }
}
