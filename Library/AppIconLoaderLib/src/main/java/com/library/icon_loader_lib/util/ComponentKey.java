package com.library.icon_loader_lib.util;

import android.content.ComponentName;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class ComponentKey {

    public final ComponentName componentName;
    public final UserHandle user;

    private final int mHashCode;

    public ComponentKey(ComponentName componentName, UserHandle user) {
        if (componentName == null || user == null) {
            throw new NullPointerException();
        }
        this.componentName = componentName;
        this.user = user;
        mHashCode = Arrays.hashCode(new Object[]{componentName, user});

    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object o) {
        ComponentKey other = (ComponentKey) o;
        return other.componentName.equals(componentName) && other.user.equals(user);
    }

    /**
     * Encodes a component key as a string of the form [flattenedComponentString#userId].
     */
    @NonNull
    @Override
    public String toString() {
        return componentName.flattenToString() + "#" + user.hashCode();
    }

    /**
     * Parses and returns ComponentKey objected from string representation
     * Returns null if string is not properly formatted
     */
    @Nullable
    public static ComponentKey fromString(@NonNull String str) {
        int sep = str.indexOf('#');
        if (sep < 0 || (sep + 1) >= str.length()) {
            return null;
        }
        ComponentName componentName = ComponentName.unflattenFromString(str.substring(0, sep));
        if (componentName == null) {
            return null;
        }
        try {
            return new ComponentKey(componentName, UserHandle.getUserHandleForUid(Integer.parseInt(str.substring(sep + 1))));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}