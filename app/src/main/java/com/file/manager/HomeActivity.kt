package com.file.manager

import android.view.LayoutInflater
import com.file.manager.databinding.ActivityHomeBinding
import com.module.core.base.BaseActivity

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(inflater)
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }
}