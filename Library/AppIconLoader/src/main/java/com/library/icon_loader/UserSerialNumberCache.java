package com.library.icon_loader;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

class UserSerialNumberCache {
    private static final long CACHE_MILLIS = 1000;

    @NonNull
    private static final ArrayMap<UserHandle, long[]> sCache = new ArrayMap<>();

    UserSerialNumberCache() {
    }

    public static long getSerialNumber(@NonNull UserHandle user, @NonNull Context context) {
        synchronized (sCache) {
            long[] serialNumberAndTime = sCache.get(user);
            if (serialNumberAndTime == null) {
                serialNumberAndTime = new long[2];
                sCache.put(user, serialNumberAndTime);
            }
            long time = System.currentTimeMillis();
            if (serialNumberAndTime[1] + CACHE_MILLIS <= time) {
                UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
                serialNumberAndTime[0] = userManager.getSerialNumberForUser(user);
                serialNumberAndTime[1] = time;
            }
            return serialNumberAndTime[0];
        }
    }
}
