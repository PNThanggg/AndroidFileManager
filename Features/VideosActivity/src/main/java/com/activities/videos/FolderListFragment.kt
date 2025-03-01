package com.activities.videos

import android.view.LayoutInflater
import android.view.ViewGroup
import com.activities.videos.databinding.FragmentFolderListBinding
import com.module.core.base.BaseFragment

class FolderListFragment : BaseFragment<FragmentFolderListBinding>() {
    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFolderListBinding {
        return FragmentFolderListBinding.inflate(inflater, container, false)
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initListener() {

    }
}