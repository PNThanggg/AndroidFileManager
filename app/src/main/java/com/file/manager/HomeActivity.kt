package com.file.manager

import android.content.Intent
import android.view.LayoutInflater
import com.activities.videos.VideosActivity
import com.file.manager.databinding.ActivityHomeBinding
import com.module.core.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(inflater)
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
        binding.buttonDocs.setOnClickListener {
            val intent = Intent(this@HomeActivity, DocsActivity::class.java)
            startActivity(intent)
        }

        binding.buttonImages.setOnClickListener {
            val intent = Intent(this@HomeActivity, ImagesActivity::class.java)
            startActivity(intent)
        }

        binding.buttonVideos.setOnClickListener {
            val intent = Intent(this@HomeActivity, VideosActivity::class.java)
            startActivity(intent)
        }

        binding.buttonMusic.setOnClickListener {
            val intent = Intent(this@HomeActivity, MusicActivity::class.java)
            startActivity(intent)
        }
    }
}