package com.activities.videos.utils

import android.Manifest
import com.module.core.utils.isSdkR
import com.module.core.utils.isSdkTIRAMISU

object Utils {
    val storagePermission = when {
        isSdkTIRAMISU() -> Manifest.permission.READ_MEDIA_VIDEO
        isSdkR() -> Manifest.permission.READ_EXTERNAL_STORAGE
        else -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}