package com.file.manager

import android.view.LayoutInflater
import com.file.manager.databinding.ActivitySplashBinding
import com.module.core.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }

}