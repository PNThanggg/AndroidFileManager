package com.module.core.extensions

import android.app.Activity
import android.app.UiModeManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.module.core.common.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.universalchardet.UniversalDetector
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.coroutines.suspendCoroutine

val VIDEO_COLLECTION_URI: Uri
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        Video.Media.EXTERNAL_CONTENT_URI
    }


fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.error), msg), length)
}

fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}

/**
 * get path from uri
 * @param uri uri of the file
 * @return path of the file
 */
fun Context.getPath(uri: Uri): String? {
    if (DocumentsContract.isDocumentUri(this, uri)) {
        when {
            uri.isExternalStorageDocument -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().path + "/" + split[1]
                }

                // TODO: handle non-primary volumes
            }

            uri.isDownloadsDocument -> {
                val docId = DocumentsContract.getDocumentId(uri)
                if (docId.isDigitsOnly()) {
                    return try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            docId.toLong(),
                        )
                        getDataColumn(contentUri, null, null)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            uri.isMediaDocument -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1],
                )
                return contentUri?.let { getDataColumn(it, selection, selectionArgs) }
            }
        }
    } else if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)) {
        if (uri.isLocalPhotoPickerUri) return null
        if (uri.isCloudPhotoPickerUri) return null

        return if (uri.isGooglePhotosUri) {
            uri.lastPathSegment
        } else {
            getDataColumn(uri, null, null)
        }
    } else if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return null
}

/**
 * get data column from uri
 * @param uri uri of the file
 * @param selection selection
 * @param selectionArgs selection arguments
 * @return data column
 */
private fun Context.getDataColumn(
    uri: Uri,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
): String? {
    val column = Images.Media.DATA
    val projection = arrayOf(column)
    try {
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

/**
 * get filename from uri
 * @param uri uri of the file
 * @return filename of the file
 */
fun Context.getFilenameFromUri(uri: Uri): String {
    return if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
        File(uri.toString()).name
    } else {
        getFilenameFromContentUri(uri) ?: uri.lastPathSegment ?: ""
    }
}

/**
 * get filename from content uri
 * @param uri uri of the file
 * @return filename of the file
 */
fun Context.getFilenameFromContentUri(uri: Uri): String? {
    val projection = arrayOf(
        OpenableColumns.DISPLAY_NAME,
    )

    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

/**
 * Retrieves the content URI for a media file based on the provided URI in the [Context].
 *
 * This extension function attempts to resolve a [Uri] to a media content URI from the [MediaStore].
 * It first extracts the file path from the input [Uri] using [getPath]. Then, it queries the
 * [MediaStore.Video.Media] collection to find a matching entry based on the file path. If found,
 * it constructs and returns a content URI using the media item's ID. If the path cannot be resolved,
 * no matching media is found, or an error occurs, it returns null.
 *
 * @param uri The input [Uri] representing the media file to query.
 * @return A [Uri] representing the content URI of the media file in [MediaStore], or null if not found or an error occurs.
 */
fun Context.getMediaContentUri(uri: Uri): Uri? {
    val path = getPath(uri) ?: return null

    val column = Video.Media._ID
    val projection = arrayOf(column)
    try {
        contentResolver.query(
            VIDEO_COLLECTION_URI,
            projection,
            "${Images.Media.DATA} = ?",
            arrayOf(path),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                val id = cursor.getLong(index)
                return ContentUris.withAppendedId(VIDEO_COLLECTION_URI, id)
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

suspend fun Context.scanPaths(paths: List<String>): Boolean = suspendCoroutine { continuation ->
    try {
        MediaScannerConnection.scanFile(
            this@scanPaths,
            paths.toTypedArray(),
            arrayOf("video/*"),
        ) { path, uri ->
            Log.d("ScanPath", "com.module.core.extensions.scanPaths: path=$path, uri=$uri")
            continuation.resumeWith(Result.success(true))
        }
    } catch (e: Exception) {
        continuation.resumeWith(Result.failure(e))
    }
}

/**
 * Recursively scans a file or directory path to update the media store in the [Context].
 *
 * This suspendable extension function checks if the provided [File] is a directory or a single file.
 * If it is a directory, it recursively scans all files within it by calling itself on each sub-file,
 * returning true only if all scans succeed. If it is a single file, it delegates to [scanPaths]
 * to perform the media scan for the file's path. The operation is designed to ensure media files
 * are indexed in the media store.
 *
 * @param file The [File] object representing the file or directory to scan.
 * @return A [Boolean] indicating whether the scan was successful (true) for all files, or false if any scan fails.
 */
suspend fun Context.scanPath(file: File): Boolean {
    return if (file.isDirectory) {
        file.listFiles()?.all { scanPath(it) } ?: true
    } else {
        scanPaths(listOf(file.path))
    }
}

/**
 * Scans the storage at the specified path to update the media store in the [Context].
 *
 * This suspendable extension function runs on the [Dispatchers.IO] coroutine context to perform
 * a media scan of the provided storage path. By default, it uses the external storage directory
 * path if no [storagePath] is specified. On Android Q (API 29) and above, it delegates to [scanPaths]
 * for a single path scan. On older versions, it uses [scanPath] to recursively scan the storage
 * directory. Returns false if the storage path is null or scanning fails.
 *
 * @param storagePath The path to scan, defaults to the external storage directory path if null.
 * @return A [Boolean] indicating whether the scan was successful (true) or not (false).
 */
suspend fun Context.scanStorage(
    storagePath: String? = Environment.getExternalStorageDirectory()?.path,
): Boolean = withContext(Dispatchers.IO) {
    if (storagePath != null) {
        return@withContext if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scanPaths(listOf(storagePath))
        } else {
            scanPath(File(storagePath))
        }
    } else {
        false
    }
}

suspend fun Context.convertToUTF8(uri: Uri, charset: Charset? = null): Uri =
    withContext(Dispatchers.IO) {
        try {
            when {
                uri.scheme?.let { it in listOf("http", "https", "ftp") } == true -> {
                    val url = URL(uri.toString())
                    val detectedCharset = charset ?: detectCharset(url)
                    if (detectedCharset == StandardCharsets.UTF_8) {
                        uri
                    } else {
                        convertNetworkUriToUTF8(url = url, sourceCharset = detectedCharset)
                    }
                }

                else -> {
                    val detectedCharset =
                        charset ?: detectCharset(uri = uri, context = this@convertToUTF8)
                    if (detectedCharset == StandardCharsets.UTF_8) {
                        uri
                    } else {
                        convertLocalUriToUTF8(uri = uri, sourceCharset = detectedCharset)
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            uri
        }
    }

private fun detectCharset(uri: Uri, context: Context): Charset {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        detectCharsetFromStream(inputStream)
    } ?: StandardCharsets.UTF_8
}

private fun detectCharset(url: URL): Charset {
    return url.openStream().use { inputStream ->
        detectCharsetFromStream(inputStream)
    }
}

private fun detectCharsetFromStream(inputStream: InputStream): Charset {
    return BufferedInputStream(inputStream).use { bufferedStream ->
        val maxBytes = 1024 * 100 // 100 KB
        val data = ByteArray(maxBytes)
        val bytesRead = bufferedStream.read(data, 0, maxBytes)

        if (bytesRead <= 0) {
            return@use Charset.forName(StandardCharsets.UTF_8.name())
        }

        UniversalDetector(null).run {
            handleData(data, 0, data.size)
            dataEnd()
            Charset.forName(detectedCharset ?: StandardCharsets.UTF_8.name())
        }
    }
}

private fun Context.convertLocalUriToUTF8(uri: Uri, sourceCharset: Charset): Uri {
    val fileName = getFilenameFromUri(uri)
    val file = File(subtitleCacheDir, fileName)

    contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.reader(sourceCharset).buffered().use { reader ->
            file.outputStream().writer(StandardCharsets.UTF_8).buffered().use { writer ->
                reader.copyTo(writer)
            }
        }
    }

    return Uri.fromFile(file)
}

private fun Context.convertNetworkUriToUTF8(url: URL, sourceCharset: Charset): Uri {
    val fileName = url.path.substringAfterLast('/')
    val file = File(subtitleCacheDir, fileName)

    url.openStream().use { inputStream ->
        inputStream.reader(sourceCharset).buffered().use { reader ->
            file.outputStream().writer(StandardCharsets.UTF_8).buffered().use { writer ->
                reader.copyTo(writer)
            }
        }
    }

    return Uri.fromFile(file)
}

fun Context.isDeviceTvBox(): Boolean {
    val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
        return true
    }

    // Fire tv
    if (packageManager.hasSystemFeature("amazon.hardware.fire_tv")) {
        return true
    }

    // Missing Files app (DocumentsUI) means box (some boxes still have non functional app or stub)
    if (!hasStorageAccessFrameworkChooser()) {
        return true
    }

    if (Build.VERSION.SDK_INT < 30) {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) {
            return true
        }

        if (packageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
            return true
        }

        if (Build.MANUFACTURER.equals("zidoo", ignoreCase = true)) {
            return true
        }
    }
    return false
}

fun Context.hasStorageAccessFrameworkChooser(): Boolean {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "video/*"
    return intent.resolveActivity(packageManager) != null
}

fun Context.pxToDp(px: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, resources.displayMetrics)

fun Context.dpToPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

val Context.subtitleCacheDir: File
    get() {
        val dir = File(cacheDir, "subtitles")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

val Context.thumbnailCacheDir: File
    get() {
        val dir = File(cacheDir, "thumbnails")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

suspend fun ContentResolver.updateMedia(
    uri: Uri,
    contentValues: ContentValues,
): Boolean = withContext(Dispatchers.IO) {
    return@withContext try {
        update(
            uri,
            contentValues,
            null,
            null,
        ) > 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun ContentResolver.deleteMedia(
    uri: Uri,
): Boolean = withContext(Dispatchers.IO) {
    return@withContext try {
        delete(uri, null, null) > 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Context.getStorageVolumes() = try {
    getExternalFilesDirs(null)?.mapNotNull {
        File(it.path.substringBefore("/Android")).takeIf { file -> file.exists() }
    } ?: listOf(Environment.getExternalStorageDirectory())
} catch (e: Exception) {
    listOf(Environment.getExternalStorageDirectory())
}

fun Context.getSizeFromContentUri(uri: Uri): Long {
    val projection = arrayOf(OpenableColumns.SIZE)
    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val sizeColumn = it.getColumnIndexOrThrow(OpenableColumns.SIZE)

            if (cursor.moveToFirst()) {
                return cursor.getLong(sizeColumn)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0L
}

fun Context.getFileUri(path: String): Uri = when {
    path.isImageSlow() -> Images.Media.EXTERNAL_CONTENT_URI
    path.isVideoSlow() -> Video.Media.EXTERNAL_CONTENT_URI
    path.isAudioSlow() -> Audio.Media.EXTERNAL_CONTENT_URI
    else -> Files.getContentUri("external")
}

/**
 * Retrieves the album name of an audio file based on its path in the [Context].
 *
 * This extension function attempts to fetch the album name of an audio file specified by [path].
 * It first queries the [MediaStore] using the provided path, either as a content URI or file path,
 * to extract the album name from [Audio.Media.ALBUM]. If successful, it returns the album name as a
 * string. If the query fails (e.g., due to an invalid URI or exception), it falls back to using
 * [MediaMetadataRetriever] to extract the album name directly from the file's metadata. Returns null
 * if both attempts fail.
 *
 * @param path The file path or content URI of the audio file.
 * @return The album name as a [String], or null if the album name cannot be retrieved.
 */
fun Context.getAlbum(path: String): String? {
    val projection = arrayOf(
        Audio.Media.ALBUM
    )

    val uri = getFileUri(path)
    val selection =
        if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs =
        if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val albumColumn = it.getColumnIndexOrThrow(Audio.Media.ALBUM)

            if (cursor.moveToFirst()) {
                return cursor.getString(albumColumn)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Retrieves the duration of a media file in seconds based on its path in the [Context].
 *
 * This extension function attempts to fetch the duration of a media file specified by [path].
 * It first tries to query the [MediaStore] using the provided path, either as a content URI or file path,
 * to extract the duration from [MediaColumns.DURATION]. If successful, it converts the duration from
 * milliseconds to seconds. If the query fails (e.g., due to an invalid URI or exception), it falls back
 * to using [MediaMetadataRetriever] to extract the duration from the file directly. Returns null if
 * both attempts fail.
 *
 * @param path The file path or content URI of the media file (e.g., a video or audio file).
 * @return The duration of the media file in seconds as an [Int], or null if the duration cannot be retrieved.
 */
fun Context.getDuration(path: String): Int? {
    val projection = arrayOf(
        MediaColumns.DURATION
    )

    val uri = getFileUri(path)
    val selection =
        if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs =
        if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val durationColumn = it.getColumnIndexOrThrow(MediaColumns.DURATION)

            if (cursor.moveToFirst()) {
                return Math.round(cursor.getInt(durationColumn) / 1000.toDouble()).toInt()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        Math.round(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toInt() / 1000f
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Retrieves the artist name of an audio file based on its path in the [Context].
 *
 * This extension function attempts to fetch the artist name of an audio file specified by [path].
 * It first queries the [MediaStore] using the provided path, either as a content URI or file path,
 * to extract the artist name from [Audio.Media.ARTIST]. If successful, it returns the artist name as a
 * string. If the query fails (e.g., due to an invalid URI or exception), it falls back to using
 * [MediaMetadataRetriever] to extract the artist name directly from the file's metadata. Returns null
 * if both attempts fail.
 *
 * @param path The file path or content URI of the audio file.
 * @return The artist name as a [String], or null if the artist name cannot be retrieved.
 */
fun Context.getArtist(path: String): String? {
    val projection = arrayOf(
        Audio.Media.ARTIST
    )

    val uri = getFileUri(path)
    val selection =
        if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs =
        if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val artistColumn = it.getColumnIndexOrThrow(Audio.Media.ARTIST)
            if (cursor.moveToFirst()) {
                return cursor.getString(artistColumn)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Retrieves the title of a media file based on its path in the [Context].
 *
 * This extension function attempts to fetch the title of a media file specified by [path].
 * It first queries the [MediaStore] using the provided path, either as a content URI or file path,
 * to extract the title from [MediaColumns.TITLE]. If successful, it returns the title as a string.
 * If the query fails (e.g., due to an invalid URI or exception), it falls back to using
 * [MediaMetadataRetriever] to extract the title directly from the file's metadata. Returns null
 * if both attempts fail.
 *
 * @param path The file path or content URI of the media file (e.g., audio or video).
 * @return The title as a [String], or null if the title cannot be retrieved.
 */
fun Context.getTitle(path: String): String? {
    val projection = arrayOf(
        MediaColumns.TITLE
    )

    val uri = getFileUri(path)
    val selection =
        if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs =
        if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val titleColumn = it.getColumnIndexOrThrow(Audio.Media.TITLE)

            if (cursor.moveToFirst()) {
                return cursor.getString(titleColumn)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.isRTLLayout: Boolean get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL