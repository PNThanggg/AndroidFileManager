package com.library.root_tools.internal;

import com.library.root_tools.containers.Mount;
import com.library.root_tools.containers.Permissions;
import com.library.root_tools.containers.Symlink;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class InternalVariables {
    protected static boolean nativeToolsReady = false;
    protected static boolean found = false;
    protected static boolean processRunning = false;

    protected static String[] space;
    protected static String getSpaceFor;
    protected static String busyboxVersion;
    protected static String pid_list = "";
    protected static ArrayList<Mount> mounts;
    protected static ArrayList<Symlink> symlinks;
    protected static String inode = "";
    protected static Permissions permissions;

    // regex to get pid out of ps line, example:
    // root 2611 0.0 0.0 19408 2104 pts/2 S 13:41 0:00 bash
    protected static final String PS_REGEX = "^\\S+\\s+([0-9]+).*$";
    protected static Pattern psPattern;

    static {
        psPattern = Pattern.compile(PS_REGEX);
    }
}
