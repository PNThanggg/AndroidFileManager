package com.features.file.manager.models

data class ListItem(
    val mPath: String,
    val mName: String = "",
    var mIsDirectory: Boolean = false,
    var mChildren: Int = 0,
    var mSize: Long = 0L,
    var mModified: Long = 0L,
    var isSectionTitle: Boolean,
    val isGridTypeDivider: Boolean
) : FileDirItem(mPath, mName, mIsDirectory, mChildren, mSize, mModified)
