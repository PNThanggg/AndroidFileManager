package com.library.icon_loader;

import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;

class UserHandleCompat {
    private static final int PER_USER_RANGE = 100000;
    private static final int USER_SYSTEM = 0;
    private static final boolean MU_ENABLED = true;

    @Nullable
    private static Constructor<UserHandle> sConstructor;
    @NonNull
    private static final Object sConstructorLock = new Object();

    private UserHandleCompat() {
    }

    @NonNull
    public static UserHandle getUserHandleForUid(int uid) {
        return UserHandle.getUserHandleForUid(uid);
    }

    private static int getUserId(int uid) {
        if (MU_ENABLED) {
            return uid / PER_USER_RANGE;
        } else {
            return USER_SYSTEM;
        }
    }

    @NonNull
    private static Constructor<UserHandle> getConstructor() {
        synchronized (sConstructorLock) {
            if (sConstructor == null) {
                try {
                    sConstructor = UserHandle.class.getDeclaredConstructor(int.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            return sConstructor;
        }
    }
}
