package com.library.icon_loader_lib;


public class LayoutType {

    //     ------
    //    | icon |
    //     ------
    //      text
    public static final String ICON_SINGLE_VERTICAL_TEXT = "icon";

    // Below three layouts (to be deprecated) and two layouts render
    // {@link SearchTarget}s in following layout.
    //     ------                            ------   ------
    //    |      | title                    |(opt)|  |(opt)|
    //    | icon | subtitle (optional)      | icon|  | icon|
    //     ------                            ------  ------
    @Deprecated
    public static final String ICON_SINGLE_HORIZONTAL_TEXT = "icon_text_row";
    @Deprecated
    public static final String ICON_DOUBLE_HORIZONTAL_TEXT = "icon_texts_row";
    @Deprecated
    public static final String ICON_DOUBLE_HORIZONTAL_TEXT_BUTTON = "icon_texts_button";

    // will replace ICON_DOUBLE_* ICON_SINGLE_* layouts
    public static final String ICON_HORIZONTAL_TEXT = "icon_row";
    public static final String HORIZONTAL_MEDIUM_TEXT = "icon_row_medium";
    public static final String SMALL_ICON_HORIZONTAL_TEXT = "short_icon_row";
    public static final String SMALL_ICON_HORIZONTAL_TEXT_THUMBNAIL = "short_icon_row_thumbnail";

    // This layout creates square thumbnail image (currently 3 column)
    public static final String THUMBNAIL = "thumbnail";

    // This layout contains an icon and slice
    public static final String ICON_SLICE = "slice";

    // Widget bitmap preview
    public static final String WIDGET_PREVIEW = "widget_preview";

    // Live widget search result
    public static final String WIDGET_LIVE = "widget_live";

    // Layout type used to display people tiles using shortcut info
    public static final String PEOPLE_TILE = "people_tile";

    // text based header to group various layouts in low confidence section of the results.
    public static final String TEXT_HEADER = "header";

    // horizontal bar to be inserted between fallback search results and low confidence section
    public static final String DIVIDER = "divider";

    // horizontal bar to be inserted between fallback search results and low confidence section
    public static final String EMPTY_DIVIDER = "empty_divider";

    // layout representing quick calculations
    public static final String CALCULATOR = "calculator";
}
