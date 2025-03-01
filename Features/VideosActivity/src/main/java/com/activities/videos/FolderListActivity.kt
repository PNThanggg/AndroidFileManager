package com.activities.videos

import android.view.LayoutInflater
import com.activities.videos.databinding.ActivityFolderListBinding
import com.module.core.base.BaseActivity
import com.modules.core.model.Folder

class FolderListActivity : BaseActivity<ActivityFolderListBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityFolderListBinding {
        return ActivityFolderListBinding.inflate(inflater)
    }

    override fun initView() {
        val folder = intent.getSerializableExtra("folder") as? Folder

        if (folder != null) {
            binding.headerTitle.text = folder.name
        }
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}