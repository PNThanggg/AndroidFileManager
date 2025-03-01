package com.activities.videos.extensions

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun Bitmap.saveTo(
    storageDir: File,
    quality: Int = 100,
    fileName: String,
): String? = withContext(Dispatchers.IO) {
    val thumbFile = File(storageDir, fileName)
    try {
        FileOutputStream(thumbFile).use { fos ->
            compress(Bitmap.CompressFormat.JPEG, quality, fos)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext if (thumbFile.exists()) thumbFile.path else null
}
