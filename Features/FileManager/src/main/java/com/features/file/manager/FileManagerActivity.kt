package com.features.file.manager

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.widget.Toast
import com.features.file.manager.databinding.ActivityFileManagerBinding
import com.features.file.manager.dialogs.ConfirmationAdvancedDialog
import com.features.file.manager.extensions.config
import com.features.file.manager.extensions.getAndroidSAFFileItems
import com.features.file.manager.extensions.getOTGItems
import com.features.file.manager.extensions.hasProperStoredAndroidTreeUri
import com.features.file.manager.extensions.isPathOnRoot
import com.features.file.manager.models.FileDirItem
import com.features.file.manager.models.ListItem
import com.features.file.manager.utils.RootHelpers
import com.features.file.manager.view.Breadcrumbs
import com.library.root_tools.RootTools
import com.module.core.base.BaseActivity
import com.module.core.ensureBackgroundThread
import com.module.core.extensions.createAndroidDataOrObbUri
import com.module.core.extensions.getAndroidTreeUri
import com.module.core.extensions.getDirectChildrenCount
import com.module.core.extensions.getFolderLastModified
import com.module.core.extensions.getMimeType
import com.module.core.extensions.getProperSize
import com.module.core.extensions.hideKeyboard
import com.module.core.extensions.isPathOnOTG
import com.module.core.extensions.isRestrictedSAFOnlyRoot
import com.module.core.extensions.toast
import com.module.core.utils.SORT_BY_SIZE
import com.module.core.utils.VIEW_TYPE_GRID
import com.module.core.utils.VIEW_TYPE_LIST
import com.modules.core.ui.MyRecyclerView
import java.io.File

class FileManagerActivity : BaseActivity<ActivityFileManagerBinding>(),
    Breadcrumbs.BreadcrumbsListener {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityFileManagerBinding {
        return ActivityFileManagerBinding.inflate(inflater)
    }

    companion object {
        private const val BACK_PRESS_TIMEOUT = 5000
        private const val MANAGE_STORAGE_RC = 201
        private const val PICKED_PATH = "picked_path"

        const val OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB = 1000
        const val EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"

        var funAfterSAFPermission: ((success: Boolean) -> Unit)? = null
    }

    private var showHidden = false
    private var lastSearchedText = ""
    private var zoomListener: MyRecyclerView.MyZoomListener? = null

    private var storedItems = ArrayList<ListItem>()
    private var itemsIgnoringSearch = ArrayList<ListItem>()

    var wantedMimeTypes = listOf("")

    var currentPath = ""
    var checkedDocumentPath = ""

    override fun initView() {
        binding.breadcrumbs.listener = this

//        binding.itemsSwipeRefresh.setOnRefreshListener { refreshFragment() }
    }

    override fun initData() {}

    override fun initListener() {}

    fun openPath(path: String, forceRefresh: Boolean = false) {
        var realPath = path.trimEnd('/')
        if (realPath.isEmpty()) {
            realPath = "/"
        }

        currentPath = realPath
        showHidden = config.shouldShowHidden()
        showProgressBar()
        getItems(currentPath) { originalPath, listItems ->
            if (currentPath != originalPath) {
                return@getItems
            }

            FileDirItem.sorting = config.getFolderSorting(currentPath)
            listItems.sort()

            if (config.getFolderViewType(currentPath) == VIEW_TYPE_GRID && listItems.none { it.isSectionTitle }) {
                if (listItems.any { it.mIsDirectory } && listItems.any { !it.mIsDirectory }) {
                    val firstFileIndex = listItems.indexOfFirst { !it.mIsDirectory }
                    if (firstFileIndex != -1) {
                        val sectionTitle = ListItem(
                            "", "", false, 0, 0, 0, isSectionTitle = false, isGridTypeDivider = true
                        )
                        listItems.add(firstFileIndex, sectionTitle)
                    }
                }
            }

            itemsIgnoringSearch = listItems
            runOnUiThread {
//                (activity as? MainActivity)?.refreshMenuItems()
//                addItems(listItems, forceRefresh)
//                if (currentViewType != config.getFolderViewType(currentPath)) {
//                    setupLayoutManager()
//                }
                hideProgressBar()
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.show()
    }

    private fun hideProgressBar() {
        binding.progressBar.hide()
    }

    private fun handleAndroidSAFDialog(
        path: String, callback: (success: Boolean) -> Unit
    ): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingAndroidSAFDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    private fun isShowingAndroidSAFDialog(path: String): Boolean {
        return if (isRestrictedSAFOnlyRoot(path) && (getAndroidTreeUri(path).isEmpty() || !hasProperStoredAndroidTreeUri(
                path
            ))
        ) {
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    ConfirmationAdvancedDialog(
                        this,
                        "",
                        R.string.confirm_storage_access_android_text,
                        R.string.ok,
                        R.string.cancel
                    ) { success ->
                        if (success) {
                            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                putExtra(EXTRA_SHOW_ADVANCED, true)
                                putExtra(
                                    DocumentsContract.EXTRA_INITIAL_URI,
                                    createAndroidDataOrObbUri(path)
                                )
                                try {
                                    startActivityForResult(
                                        this, OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
                                    )
                                    checkedDocumentPath = path
                                    return@apply
                                } catch (e: Exception) {
                                    type = "*/*"
                                }

                                try {
                                    startActivityForResult(
                                        this, OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
                                    )
                                    checkedDocumentPath = path
                                } catch (e: ActivityNotFoundException) {
                                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                                } catch (e: Exception) {
                                    toast(R.string.unknown_error_occurred)
                                }
                            }
                        }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    private fun getItems(
        path: String, callback: (originalPath: String, items: ArrayList<ListItem>) -> Unit
    ) {
        ensureBackgroundThread {
            if (!isDestroyed && !isFinishing) {
                if (isRestrictedSAFOnlyRoot(path)) {
                    handleAndroidSAFDialog(path) {
                        if (!it) {
                            toast(R.string.no_storage_permissions)
                            return@handleAndroidSAFDialog
                        }
                        val getProperChildCount =
                            config.getFolderViewType(currentPath) == VIEW_TYPE_LIST
                        getAndroidSAFFileItems(
                            path, config.shouldShowHidden(), getProperChildCount
                        ) { fileItems ->
                            callback(path, getListItemsFromFileDirItems(fileItems))
                        }
                    }
                } else if (isPathOnOTG(path) && config.OTGTreeUri.isNotEmpty()) {
                    val getProperFileSize =
                        config.getFolderSorting(currentPath) and SORT_BY_SIZE != 0
                    getOTGItems(path, config.shouldShowHidden(), getProperFileSize) {
                        callback(path, getListItemsFromFileDirItems(it))
                    }
                } else if (!config.enableRootAccess || !isPathOnRoot(path)) {
                    getRegularItemsOf(path, callback)
                } else {
                    RootHelpers(this).getFiles(path, callback)
                }
            }
        }
    }

    private fun getRegularItemsOf(
        path: String, callback: (originalPath: String, items: ArrayList<ListItem>) -> Unit
    ) {
        val items = ArrayList<ListItem>()
        val files = File(path).listFiles()?.filterNotNull()
        if (files == null) {
            callback(path, items)
            return
        }

        val isSortingBySize = config.getFolderSorting(currentPath) and SORT_BY_SIZE != 0
        val getProperChildCount = config.getFolderViewType(currentPath) == VIEW_TYPE_LIST
        val lastModifieds = getFolderLastModified(path)

        for (file in files) {
            val listItem = getListItemFromFile(file, isSortingBySize, lastModifieds, false)
            if (listItem != null) {
                if (wantedMimeTypes.any {
                        isProperMimeType(
                            it, file.absolutePath, file.isDirectory
                        )
                    }) {
                    items.add(listItem)
                }
            }
        }

        // send out the initial item list asap, get proper child count asynchronously as it can be slow
        callback(path, items)

        if (getProperChildCount) {
            items.filter { it.mIsDirectory }.forEach {
                val childrenCount = it.getDirectChildrenCount(this, showHidden)
                if (childrenCount != 0) {
                    runOnUiThread {
//                        getRecyclerAdapter()?.updateChildCount(it.mPath, childrenCount)
                    }
                }
            }
        }
    }

    private fun getListItemFromFile(
        file: File,
        isSortingBySize: Boolean,
        lastModifieds: HashMap<String, Long>,
        getProperChildCount: Boolean
    ): ListItem? {
        val curPath = file.absolutePath
        val curName = file.name
        if (!showHidden && curName.startsWith(".")) {
            return null
        }

        var lastModified = lastModifieds.remove(curPath)
        val isDirectory = if (lastModified != null) false else file.isDirectory
        val children = if (isDirectory && getProperChildCount) file.getDirectChildrenCount(
            this, showHidden
        ) else 0
        val size = if (isDirectory) {
            if (isSortingBySize) {
                file.getProperSize(showHidden)
            } else {
                0L
            }
        } else {
            file.length()
        }

        if (lastModified == null) {
            lastModified = file.lastModified()
        }

        return ListItem(
            curPath,
            curName,
            isDirectory,
            children,
            size,
            lastModified,
            isSectionTitle = false,
            isGridTypeDivider = false
        )
    }

    private fun isProperMimeType(
        wantedMimeType: String, path: String, isDirectory: Boolean
    ): Boolean {
        return if (wantedMimeType.isEmpty() || wantedMimeType == "*/*" || isDirectory) {
            true
        } else {
            val fileMimeType = path.getMimeType()
            if (wantedMimeType.endsWith("/*")) {
                fileMimeType.substringBefore("/").equals(wantedMimeType.substringBefore("/"), true)
            } else {
                fileMimeType.equals(wantedMimeType, true)
            }
        }
    }

    private fun getListItemsFromFileDirItems(fileDirItems: ArrayList<FileDirItem>): ArrayList<ListItem> {
        val listItems = ArrayList<ListItem>()
        fileDirItems.forEach {
            val listItem = ListItem(
                it.path,
                it.name,
                it.isDirectory,
                it.children,
                it.size,
                it.modified,
                isSectionTitle = false,
                isGridTypeDivider = false
            )

            val isProperMimeType = wantedMimeTypes.any { mimeType ->
                isProperMimeType(
                    mimeType, it.path, it.isDirectory
                )
            }

            if (isProperMimeType) {
                listItems.add(listItem)
            }
        }
        return listItems
    }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
//            StoragePickerDialog(
//                activity as SimpleActivity, currentPath, context!!.config.enableRootAccess, true
//            ) {
//                getRecyclerAdapter()?.finishActMode()
//                openPath(it)
//            }
        } else {
            val item = binding.breadcrumbs.getItem(id)
            openPath(item.path)
        }
    }
}