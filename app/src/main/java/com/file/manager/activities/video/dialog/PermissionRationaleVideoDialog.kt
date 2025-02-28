package com.file.manager.activities.video.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.file.manager.R
import com.file.manager.databinding.DialogPermissionRationaleVideoBinding
import com.file.manager.utils.Utils.storagePermission
import com.module.core.common.R as coreR

class PermissionRationaleVideoDialog(
    private val context: Context,
) {
    private val binding by lazy {
        DialogPermissionRationaleVideoBinding.inflate(LayoutInflater.from(context))
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, coreR.style.ActivityDialog).setView(binding.root).create()
    }

    init {
        val width: Int = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.dialog_background)
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }

        dialog.apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    fun show(
        onGrantPermission: () -> Unit,
    ) {
        binding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.buttonGrantPermission.setOnClickListener {
            onGrantPermission.invoke()
            dialog.dismiss()
        }

        binding.permissionInfoText.text =
            context.getString(R.string.permission_info, storagePermission)

        if (!dialog.isShowing) {
            dialog.show()
        }
    }
}