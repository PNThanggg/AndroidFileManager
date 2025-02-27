package com.file.manager.activities.video

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.file.manager.R
import com.file.manager.activities.video.models.ShortcutItem
import com.file.manager.databinding.ActivityVideosBinding
import com.file.manager.utils.Utils.storagePermission
import com.module.core.base.BaseActivity

class VideosActivity : BaseActivity<ActivityVideosBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityVideosBinding {
        return ActivityVideosBinding.inflate(inflater)
    }

    // Khởi tạo launcher để yêu cầu quyền
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Quyền được cấp
                Toast.makeText(this, "Quyền được cấp thành công!", Toast.LENGTH_SHORT).show()
            } else {
                // Quyền bị từ chối
                Toast.makeText(this, "Quyền bị từ chối!", Toast.LENGTH_SHORT).show()
            }
        }

    private val listShortcut = listOf(
        ShortcutItem(textResId = R.string.open_local_video, iconResId = R.drawable.ic_file_open) {},
        ShortcutItem(
            textResId = R.string.open_network_stream, iconResId = R.drawable.ic_file_open
        ) {},
    )

    override fun initView() {
        requestStoragePermission(storagePermission)

        binding.shortcutRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.shortcutRecyclerView.adapter = ShortcutAdapter(listShortcut)
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
            // Kiểm tra xem quyền đã được cấp chưa
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "Quyền đã được cấp!", Toast.LENGTH_SHORT).show()
            }
            // Nếu cần giải thích lý do yêu cầu quyền (người dùng đã từ chối trước đó)
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(
                    this, "Ứng dụng cần quyền này để truy cập bộ nhớ!", Toast.LENGTH_LONG
                ).show()
                // Sau khi giải thích, yêu cầu lại quyền
                requestPermissionLauncher.launch(permission)
            }
            // Yêu cầu quyền lần đầu
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}