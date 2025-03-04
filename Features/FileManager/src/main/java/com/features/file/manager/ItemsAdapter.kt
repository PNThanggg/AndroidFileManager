package com.features.file.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import com.features.file.manager.databinding.ItemFileDirListBinding
import com.features.file.manager.models.ListItem
import com.module.core.base.BaseAdapterRecyclerView

class ItemsAdapter : BaseAdapterRecyclerView<ListItem, ItemFileDirListBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater, parent: ViewGroup
    ): ItemFileDirListBinding {
        return ItemFileDirListBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemFileDirListBinding, item: ListItem, position: Int) {
        binding.apply {
            itemName.text = item.name
        }
    }
}