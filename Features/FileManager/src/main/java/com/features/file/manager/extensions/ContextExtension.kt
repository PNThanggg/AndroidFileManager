package com.features.file.manager.extensions

import android.content.Context
import android.net.Uri
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.features.file.manager.models.FileDirItem
import com.features.file.manager.utils.Config
import com.module.core.extensions.baseConfig
import com.module.core.extensions.createAndroidSAFDocumentId
import com.module.core.extensions.getAndroidTreeUri
import com.module.core.extensions.getBasePath
import com.module.core.extensions.getDirectChildrenCount
import com.module.core.extensions.getFileSize
import com.module.core.extensions.getStorageRootIdForAndroidDir
import com.module.core.extensions.isPathOnOTG
import com.module.core.extensions.isPathOnSD
import com.module.core.extensions.otgPath
import com.module.core.extensions.showErrorToast
import com.module.core.extensions.storeAndroidTreeUri
import com.module.core.utils.ExternalStorageProviderHack
import com.module.core.utils.isSdkO
import java.net.URLDecoder
import java.util.Locale

const val PRIMARY_VOLUME_NAME = "external_primary"

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.getAndroidSAFFileItems(
    path: String,
    shouldShowHidden: Boolean,
    getProperFileSize: Boolean = true,
    callback: (ArrayList<FileDirItem>) -> Unit
) {
    if (!isSdkO()) {
        callback(ArrayList())
        return
    }

    val items = ArrayList<FileDirItem>()
    val rootDocId = getStorageRootIdForAndroidDir(path)
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    val childrenUri = try {
        DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
    } catch (e: Exception) {
        showErrorToast(e)
        storeAndroidTreeUri(path, "")
        null
    }

    if (childrenUri == null) {
        callback(items)
        return
    }

    val projection = arrayOf(
        Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_MIME_TYPE,
        Document.COLUMN_LAST_MODIFIED
    )
    try {
        val rawCursor = contentResolver.query(childrenUri, projection, null, null)!!
        val cursor =
            ExternalStorageProviderHack.transformQueryResult(rootDocId, childrenUri, rawCursor)
        cursor.use {
            val idColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_DOCUMENT_ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_MIME_TYPE)
            val lastModifiedColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_LAST_MODIFIED)

            if (cursor.moveToFirst()) {
                do {
                    val docId = cursor.getString(idColumn)
                    val name = cursor.getString(displayNameColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val lastModified = cursor.getLong(lastModifiedColumn)
                    val isDirectory = mimeType == Document.MIME_TYPE_DIR
                    val filePath = docId.substring("${getStorageRootIdForAndroidDir(path)}:".length)
                    if (!shouldShowHidden && name.startsWith(".")) {
                        continue
                    }

                    val decodedPath =
                        path.getBasePath(this) + "/" + URLDecoder.decode(filePath, "UTF-8")
                    val fileSize = when {
                        getProperFileSize -> getFileSize(treeUri, docId)
                        isDirectory -> 0L
                        else -> getFileSize(treeUri, docId)
                    }

                    val childrenCount = if (isDirectory) {
                        getDirectChildrenCount(rootDocId, treeUri, docId, shouldShowHidden)
                    } else {
                        0
                    }

                    val fileDirItem = FileDirItem(
                        decodedPath, name, isDirectory, childrenCount, fileSize, lastModified
                    )
                    items.add(fileDirItem)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        showErrorToast(e)
    }
    callback(items)
}


fun Context.getOTGItems(
    path: String,
    shouldShowHidden: Boolean,
    getProperFileSize: Boolean,
    callback: (java.util.ArrayList<FileDirItem>) -> Unit
) {
    val items = java.util.ArrayList<FileDirItem>()
    val OTGTreeUri = baseConfig.OTGTreeUri
    var rootUri = try {
        DocumentFile.fromTreeUri(applicationContext, Uri.parse(OTGTreeUri))
    } catch (e: Exception) {
        showErrorToast(e)
        baseConfig.OTGPath = ""
        baseConfig.OTGTreeUri = ""
        baseConfig.OTGPartition = ""
        null
    }

    if (rootUri == null) {
        callback(items)
        return
    }

    val parts = path.split("/").dropLastWhile { it.isEmpty() }
    for (part in parts) {
        if (path == otgPath) {
            break
        }

        if (part == "otg:" || part == "") {
            continue
        }

        val file = rootUri!!.findFile(part)
        if (file != null) {
            rootUri = file
        }
    }

    val files = rootUri!!.listFiles().filter { it.exists() }

    val basePath = "${baseConfig.OTGTreeUri}/document/${baseConfig.OTGPartition}%3A"
    for (file in files) {
        val name = file.name ?: continue
        if (!shouldShowHidden && name.startsWith(".")) {
            continue
        }

        val isDirectory = file.isDirectory
        val filePath = file.uri.toString().substring(basePath.length)
        val decodedPath = otgPath + "/" + URLDecoder.decode(filePath, "UTF-8")
        val fileSize = when {
            getProperFileSize -> file.getItemSize(shouldShowHidden)
            isDirectory -> 0L
            else -> file.length()
        }

        val childrenCount = if (isDirectory) {
            file.listFiles().size
        } else {
            0
        }

        val lastModified = file.lastModified()
        val fileDirItem =
            FileDirItem(decodedPath, name, isDirectory, childrenCount, fileSize, lastModified)
        items.add(fileDirItem)
    }

    callback(items)
}


fun Context.isPathOnRoot(path: String) =
    !(path.startsWith(config.internalStoragePath) || isPathOnOTG(path) || (isPathOnSD(path)))

fun Context.getAllVolumeNames(): List<String> {
    val volumeNames = mutableListOf(PRIMARY_VOLUME_NAME)
    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    getExternalFilesDirs(null).mapNotNull { storageManager.getStorageVolume(it) }
        .filterNot { it.isPrimary }.mapNotNull { it.uuid?.lowercase(Locale.US) }.forEach {
            volumeNames.add(it)
        }
    return volumeNames
}

fun Context.hasProperStoredAndroidTreeUri(path: String): Boolean {
    val uri = getAndroidTreeUri(path)
    val hasProperUri = contentResolver.persistedUriPermissions.any { it.uri.toString() == uri }
    if (!hasProperUri) {
        storeAndroidTreeUri(path, "")
    }
    return hasProperUri
}
