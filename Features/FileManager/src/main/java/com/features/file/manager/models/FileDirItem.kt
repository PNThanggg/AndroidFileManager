package com.features.file.manager.models

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.signature.ObjectKey
import com.features.file.manager.utils.AlphanumericComparator
import com.module.core.extensions.formatDate
import com.module.core.extensions.formatFileSize
import com.module.core.extensions.getAlbum
import com.module.core.extensions.getAndroidSAFDirectChildrenCount
import com.module.core.extensions.getArtist
import com.module.core.extensions.getDirectChildrenCount
import com.module.core.extensions.getDocumentFile
import com.module.core.extensions.getDuration
import com.module.core.extensions.getFormattedDuration
import com.module.core.extensions.getParentPath
import com.module.core.extensions.getTitle
import com.module.core.extensions.isImageFast
import com.module.core.extensions.isPathOnOTG
import com.module.core.extensions.isRestrictedSAFOnlyRoot
import com.module.core.extensions.isVideoFast
import com.module.core.extensions.normalizeString
import com.module.core.utils.SORT_BY_DATE_MODIFIED
import com.module.core.utils.SORT_BY_EXTENSION
import com.module.core.utils.SORT_BY_NAME
import com.module.core.utils.SORT_BY_SIZE
import com.module.core.utils.SORT_DESCENDING
import com.module.core.utils.SORT_USE_NUMERIC_VALUE
import java.io.File

open class FileDirItem(
    val path: String,
    val name: String = "",
    var isDirectory: Boolean = false,
    var children: Int = 0,
    var size: Long = 0L,
    var modified: Long = 0L,
    private var mediaStoreId: Long = 0L
) : Comparable<FileDirItem> {
    companion object {
        var sorting = 0
    }

    override fun toString() =
        "FileDirItem(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified, mediaStoreId=$mediaStoreId)"

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> {
                    result = if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(
                            name.normalizeString.lowercase(), other.name.normalizeString.lowercase()
                        )
                    } else {
                        name.normalizeString.lowercase()
                            .compareTo(other.name.normalizeString.lowercase())
                    }
                }

                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }

                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }

                else -> {
                    result = getExtension().lowercase().compareTo(other.getExtension().lowercase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    private fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")

    fun getBubbleText(dateFormat: String? = null, timeFormat: String? = null) = when {
        sorting and SORT_BY_SIZE != 0 -> size.formatFileSize

        sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(
            dateFormat = dateFormat, timeFormat = timeFormat,
        )

        sorting and SORT_BY_EXTENSION != 0 -> getExtension().lowercase()

        else -> name
    }

//    fun getProperSize(context: Context, countHidden: Boolean): Long {
//        return when {
//            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileSize(path)
//            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getItemSize(countHidden)
//                ?: 0
//
//            path.startsWith("content://") -> {
//                val uri = Uri.parse(path)
//
//                try {
//                    context.contentResolver.openInputStream(uri)?.available()?.toLong() ?: 0L
//                } catch (e: Exception) {
//                    context.getSizeFromContentUri(uri)
//                }
//            }
//
//            else -> File(path).getProperSize(countHidden)
//        }
//    }

//    fun getProperFileCount(context: Context, countHidden: Boolean): Int {
//        return when {
//            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileCount(
//                path, countHidden
//            )
//
//            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getFileCount(countHidden)
//                ?: 0
//
//            else -> File(path).getFileCount(countHidden)
//        }
//    }

    fun getDirectChildrenCount(context: Context, countHiddenItems: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFDirectChildrenCount(
                path, countHiddenItems
            )

            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.listFiles()
                ?.filter { if (countHiddenItems) true else !it.name!!.startsWith(".") }?.size ?: 0

            else -> File(path).getDirectChildrenCount(context, countHiddenItems)
        }
    }

//    fun getLastModified(context: Context): Long {
//        return when {
//            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFLastModified(path)
//            context.isPathOnOTG(path) -> context.getFastDocumentFile(path)?.lastModified() ?: 0L
//            path.startsWith("content://") -> context.getMediaStoreLastModified(
//                path
//            )
//
//            else -> File(path).lastModified()
//        }
//    }

    fun getParentPath() = path.getParentPath()

    fun getDuration(context: Context) = context.getDuration(path)?.getFormattedDuration()

    fun getFileDurationSeconds(context: Context) = context.getDuration(path)

    fun getArtist(context: Context) = context.getArtist(path)

    fun getAlbum(context: Context) = context.getAlbum(path)

    fun getTitle(context: Context) = context.getTitle(path)

//    fun getResolution(context: Context) = context.getResolution(path)
//
//    fun getVideoResolution(context: Context) = context.getVideoResolution(path)
//
//    fun getImageResolution(context: Context) = context.getImageResolution(path)
//
//    fun getPublicUri(context: Context) = context.getDocumentFile(path)?.uri ?: ""

    fun getSignature(): String {
        val lastModified = if (modified > 1) {
            modified
        } else {
            File(path).lastModified()
        }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())

    fun assembleContentUri(): Uri {
        val uri = when {
            path.isImageFast() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        return Uri.withAppendedPath(uri, mediaStoreId.toString())
    }
}

//fun FileDirItem.asReadOnly() = FileDirItemReadOnly(
//    path = path,
//    name = name,
//    isDirectory = isDirectory,
//    children = children,
//    size = size,
//    modified = modified,
//    mediaStoreId = mediaStoreId
//)
//
//fun FileDirItemReadOnly.asFileDirItem() = FileDirItem(
//    path = path,
//    name = name,
//    isDirectory = isDirectory,
//    children = children,
//    size = size,
//    modified = modified,
//    mediaStoreId = mediaStoreId
//)
//
//@Immutable
//class FileDirItemReadOnly(
//    path: String,
//    name: String = "",
//    isDirectory: Boolean = false,
//    children: Int = 0,
//    size: Long = 0L,
//    modified: Long = 0L,
//    mediaStoreId: Long = 0L
//) : FileDirItem(path, name, isDirectory, children, size, modified, mediaStoreId)
