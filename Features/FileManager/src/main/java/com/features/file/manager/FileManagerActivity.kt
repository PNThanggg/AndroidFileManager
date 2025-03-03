package com.features.file.manager

import android.view.LayoutInflater
import com.features.file.manager.databinding.ActivityFileManagerBinding
import com.module.core.base.BaseActivity

class FileManagerActivity : BaseActivity<ActivityFileManagerBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityFileManagerBinding {
        return ActivityFileManagerBinding.inflate(inflater)
    }

    override fun initView() {}

    override fun initData() {}

    override fun initListener() {}
}