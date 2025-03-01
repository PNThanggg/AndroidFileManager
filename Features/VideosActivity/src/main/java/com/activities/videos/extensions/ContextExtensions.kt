package com.activities.videos.extensions

import android.content.Context
import android.provider.MediaStore
import com.module.core.extensions.thumbnailCacheDir
import com.modules.core.model.Video
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import timber.log.Timber

private const val TAG = "ContextExtensions"

suspend fun Context.loadLocalVideo(): MutableList<Video> {
    val videoList = mutableListOf<Video>()

    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.RELATIVE_PATH,
    )

    val cursor = contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        MediaStore.Video.Media.DATE_ADDED + " DESC"
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val heightColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val widthColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val relativePathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn)
            val path = it.getString(pathColumn)
            val size = it.getLong(sizeColumn)
            val duration = it.getLong(durationColumn)
            val height = it.getInt(heightColumn)
            val width = it.getInt(widthColumn)
            val relativePath = it.getString(relativePathColumn)

            val uri = android.content.ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
            )

            val mediaInfo = runCatching {
                MediaInfoBuilder().from(context = this, uri = uri).build()
                    ?: throw NullPointerException()
            }.onFailure { e ->
                e.printStackTrace()
                Timber.tag(TAG).d(e, "sync: MediaInfoBuilder exception")
            }.getOrNull()
            val thumbnail = runCatching { mediaInfo?.getFrame() }.getOrNull()
            val thumbnailPath = thumbnail?.saveTo(
                storageDir = thumbnailCacheDir,
                quality = 40,
                fileName = name,
            )

            videoList.add(
                Video(
                    id = id,
                    nameWithExtension = name,
                    path = path,
                    duration = duration,
                    parentPath = relativePath,
                    uriString = uri.toString(),
                    thumbnailPath = thumbnailPath,
                    width = width,
                    height = height,
                    size = size,
                )
            )
        }
    }

    return videoList
}