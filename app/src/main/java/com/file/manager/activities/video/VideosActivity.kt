package com.file.manager.activities.video

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.file.manager.R
import com.file.manager.activities.video.dialog.PermissionRationaleVideoDialog
import com.file.manager.activities.video.models.ShortcutItem
import com.file.manager.databinding.ActivityVideosBinding
import com.file.manager.utils.Utils.storagePermission
import com.module.core.base.BaseActivity
import timber.log.Timber


class VideosActivity : BaseActivity<ActivityVideosBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityVideosBinding {
        return ActivityVideosBinding.inflate(inflater)
    }

    companion object {
        private const val TAG = "VideosActivity"
    }

    private val permissionRationaleVideoDialog by lazy {
        PermissionRationaleVideoDialog(this@VideosActivity)
    }

    // Khởi tạo launcher để yêu cầu quyền
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Quyền được cấp
//                Toast.makeText(this, "Quyền được cấp thành công!", Toast.LENGTH_SHORT).show()
            } else {
                // Quyền bị từ chối
//                Toast.makeText(this, "Quyền bị từ chối!", Toast.LENGTH_SHORT).show()
                binding.permissionNotGrantedLayout.visibility = View.VISIBLE
            }
        }

    private val selectVideoFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Xử lý URI của tệp video được chọn
                Timber.tag(TAG).d("Uri path: ${it.path}")
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