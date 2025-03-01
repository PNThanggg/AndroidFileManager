package com.activities.videos

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.activities.videos.adapter.FolderAdapter
import com.activities.videos.adapter.ShortcutAdapter
import com.activities.videos.databinding.ActivityVideosBinding
import com.activities.videos.dialog.PermissionRationaleVideoDialog
import com.activities.videos.models.ShortcutItem
import com.activities.videos.utils.Utils.storagePermission
import com.module.core.base.BaseActivity
import com.modules.core.model.Folder
import com.modules.feature.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@UnstableApi
@AndroidEntryPoint
class VideosActivity : BaseActivity<ActivityVideosBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityVideosBinding {
        return ActivityVideosBinding.inflate(inflater)
    }

    private val viewModel: VideosViewModel by viewModels()
    private val applicationPreferences get() = viewModel.preferences.value

    companion object {
        private const val TAG = "VideosActivity"
    }

    private val permissionRationaleVideoDialog by lazy {
        PermissionRationaleVideoDialog(this@VideosActivity)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.permissionNotGrantedLayout.visibility = View.GONE
            } else {
                binding.permissionNotGrantedLayout.visibility = View.VISIBLE
            }
        }

    private val selectVideoFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val intent = Intent(Intent.ACTION_VIEW, uri, this, PlayerActivity::class.java)
                startActivity(intent)
            }
        }

    private val listShortcut = listOf(
        ShortcutItem(textResId = R.string.open_local_video, iconResId = R.drawable.ic_file_open) {
            selectVideoFileLauncher.launch("video/*")
        },
        ShortcutItem(
            textResId = R.string.open_network_stream, iconResId = R.drawable.ic_file_open
        ) {},
    )

    override fun onResume() {
        super.onResume()

        requestStoragePermission(storagePermission)
    }

    override fun initView() {
        binding.shortcutRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.shortcutRecyclerView.adapter = ShortcutAdapter(listShortcut)

        binding.folderRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.folderRecyclerView.adapter = FolderAdapter(
            context = this@VideosActivity,
            preferences = applicationPreferences,
            folders = listOf(
                Folder.rootFolder,
                Folder.sample,
                Folder.sampleHaveData,
            ),
            onFolderClick = { folder ->
                val intent = Intent(this, FolderListActivity::class.java).apply {
                    putExtra("folder", folder)
                }
                startActivity(intent)
            },
        )

        binding.permissionNotGrantedSub.text =
            getString(R.string.permission_info, storagePermission)
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.buttonOpenSetting.setOnClickListener {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                startActivity(this)
            }
        }
    }

    private fun requestStoragePermission(permission: String) {
        when {
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> {
                binding.permissionNotGrantedLayout.visibility = View.GONE
            }

            shouldShowRequestPermissionRationale(permission) -> {
                permissionRationaleVideoDialog.show {
                    requestPermissionLauncher.launch(permission)
                }
            }

            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}