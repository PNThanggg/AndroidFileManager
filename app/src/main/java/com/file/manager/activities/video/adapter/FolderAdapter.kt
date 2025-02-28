package com.file.manager.activities.video.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.file.manager.R
import com.file.manager.databinding.ItemFolderBinding
import com.module.core.base.BaseAdapterRecyclerView
import com.module.core.extensions.formatFileSize
import com.modules.core.datastore.models.ApplicationPreferences
import com.modules.core.model.Folder

class FolderAdapter(
    private val context: Context,
    folders: List<Folder>,
    private val preferences: ApplicationPreferences,
) : BaseAdapterRecyclerView<Folder, ItemFolderBinding>() {
    init {
        setDataList(folders)
    }

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemFolderBinding {
        return ItemFolderBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemFolderBinding, item: Folder, position: Int) {
        binding.folderIcon.setImageResource(R.drawable.folder_thumb)
        binding.folderIcon.setColorFilter(
            context.resources.getColor(R.color.splash, context.theme)
        )

        binding.folderName.text = item.name

        binding.folderPath.text = item.path

        binding.infoChipsContainer.removeAllViews()
        if (item.mediaList.isNotEmpty()) {
            val mediaCountText = "${item.mediaList.size} " + context.getString(
                if (item.mediaList.size == 1) R.string.video else R.string.videos
            )
            binding.infoChipsContainer.addView(createChip(context, mediaCountText))
        }
        if (item.folderList.isNotEmpty()) {
            val folderCountText = "${item.folderList.size} " + context.getString(
                if (item.folderList.size == 1) R.string.folder else R.string.folders
            )
            binding.infoChipsContainer.addView(createChip(context, folderCountText))
        }
        if (preferences.showSizeField) {
            binding.infoChipsContainer.addView(createChip(context, item.mediaSize.formatFileSize))
        }
    }

    private fun createChip(context: Context, text: String): TextView {
        val chip = LayoutInflater.from(context).inflate(R.layout.chip_info, null) as TextView
        chip.text = text
        return chip
    }
}