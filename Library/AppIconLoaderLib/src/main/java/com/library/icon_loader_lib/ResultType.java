package com.library.icon_loader_lib;

/**
 * Note, a result type could be a of two types.
 * For example, unpublished settings result type could be in slices:
 * <code> resultType = SETTING | SLICE </code>
 */
public class ResultType {

    // published corpus by 3rd party app, supported by SystemService
    public static final int APPLICATION = 1;
    public static final int SHORTCUT = 1 << 1;
    public static final int SLICE = 1 << 6;
    public static final int WIDGETS = 1 << 7;

    // Not extracted from any of the SystemService
    public static final int PEOPLE = 1 << 2;
    public static final int ACTION = 1 << 3;
    public static final int SETTING = 1 << 4;
    public static final int SCREENSHOT = 1 << 5;
    public static final int PLAY = 1 << 8;
    public static final int SUGGEST = 1 << 9;
    public static final int ASSISTANT = 1 << 10;
    public static final int CHROMETAB = 1 << 11;
    public static final int NAVVYSITE = 1 << 12;
    public static final int TIPS = 1 << 13;
    public static final int PEOPLE_TILE = 1 << 14;
    public static final int LEGACY_SHORTCUT = 1 << 15;
    public static final int MEMORY = 1 << 16;
}
