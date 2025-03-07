package com.activities.videos

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.activities.videos.adapter.ShortcutAdapter
import com.activities.videos.adapter.VideoAdapter
import com.activities.videos.databinding.ActivityVideosBinding
import com.activities.videos.dialog.PermissionRationaleVideoDialog
import com.activities.videos.models.ShortcutItem
import com.activities.videos.utils.Utils.storagePermission
import com.module.core.base.BaseActivity
import com.module.core.enums.LoadingState
import com.module.core.extensions.showErrorToast
import com.modules.core.datastore.models.ApplicationPreferences
import com.modules.feature.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@UnstableApi
@AndroidEntryPoint
class VideosActivity : BaseActivity<ActivityVideosBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityVideosBinding {
        return ActivityVideosBinding.inflate(inflater)
    }

    private val viewModel: VideosViewModel by viewModels()
    private val applicationPreferences: ApplicationPreferences get() = viewModel.preferences.value

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
        binding.permissionNotGrantedSub.text =
            getString(R.string.permission_info, storagePermission)

        binding.shortcutRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.shortcutRecyclerView.adapter = ShortcutAdapter(listShortcut)

        binding.videoRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state.loadingState) {
                    LoadingState.Loading -> {
                        binding.videoRecyclerView.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    LoadingState.Success -> {
                        binding.videoRecyclerView.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE

                        binding.videoRecyclerView.adapter = VideoAdapter(
                            context = this@VideosActivity,
                            list = state.videos,
                            applicationPreferences = applicationPreferences,
                            onClickListener = { item ->
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(item.uriString),
                                    this@VideosActivity,
                                    PlayerActivity::class.java
                                )
                                startActivity(intent)
                            },
                        )
                    }

                    is LoadingState.Error -> {
                        binding.videoRecyclerView.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        showErrorToast(state.loadingState.message ?: "")
                    }

                    else -> {
                        binding.videoRecyclerView.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
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
                viewModel.loadListView()
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