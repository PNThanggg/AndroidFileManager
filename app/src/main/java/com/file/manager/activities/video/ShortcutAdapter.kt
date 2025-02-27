package com.file.manager.activities.video

import android.view.LayoutInflater
import android.view.ViewGroup
import com.file.manager.activities.video.models.ShortcutItem
import com.file.manager.databinding.ItemShortcutBinding
import com.module.core.base.BaseAdapterRecyclerView

class ShortcutAdapter(
    shortcuts: List<ShortcutItem>
) : BaseAdapterRecyclerView<ShortcutItem, ItemShortcutBinding>() {
    init {
        setDataList(shortcuts)
    }

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemShortcutBinding {
        return ItemShortcutBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemShortcutBinding, item: ShortcutItem, position: Int) {
        binding.shortcutIcon.setImageResource(item.iconResId)
        binding.shortcutText.setText(item.textResId)
        binding.root.setOnClickListener { item.onClick() }
    }
}