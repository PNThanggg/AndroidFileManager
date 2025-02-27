package com.file.manager

import android.view.LayoutInflater
import com.file.manager.databinding.ActivityIntroBinding
import com.module.core.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(inflater)
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }
}