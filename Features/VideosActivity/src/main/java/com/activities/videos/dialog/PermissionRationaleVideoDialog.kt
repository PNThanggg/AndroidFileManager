package com.activities.videos.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.activities.videos.R
import com.activities.videos.databinding.DialogPermissionRationaleVideoBinding
import com.activities.videos.utils.Utils.storagePermission
import com.modules.core.ui.R as coreR

class PermissionRationaleVideoDialog(
    private val context: Context,
) {
    private val binding by lazy {
        DialogPermissionRationaleVideoBinding.inflate(LayoutInflater.from(context))
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context, coreR.style.MyDialogTheme).setView(binding.root).create()
    }

    init {
        val width: Int = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.apply {
            setBackgroundDrawableResource(coreR.drawable.dialog_you_background)
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