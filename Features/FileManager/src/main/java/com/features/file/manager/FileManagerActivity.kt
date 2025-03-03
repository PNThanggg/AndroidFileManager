package com.features.file.manager

import android.view.LayoutInflater
import com.features.file.manager.databinding.ActivityFileManagerBinding
import com.features.file.manager.extensions.config
import com.features.file.manager.utils.RootHelpers
import com.library.root_tools.RootTools
import com.module.core.base.BaseActivity
import com.module.core.ensureBackgroundThread

class FileManagerActivity : BaseActivity<ActivityFileManagerBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityFileManagerBinding {
        return ActivityFileManagerBinding.inflate(inflater)
    }

    companion object {
        private const val BACK_PRESS_TIMEOUT = 5000
        private const val MANAGE_STORAGE_RC = 201
        private const val PICKED_PATH = "picked_path"
    }


    override fun initView() {}

    override fun initData() {}

    override fun initListener() {}

    private fun checkIfRootAvailable() {
        ensureBackgroundThread {
            config.isRootAvailable = RootTools.isRootAvailable()
            if (config.isRootAvailable && config.enableRootAccess) {
                RootHelpers(this).askRootIfNeeded {
                    config.enableRootAccess = it
                }
            }
        }
    }
}