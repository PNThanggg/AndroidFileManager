package com.file.manager.activities.video

import android.view.LayoutInflater
import android.view.ViewGroup
import com.file.manager.activities.video.models.ShortcutItem
import com.file.manager.databinding.ItemShortcutBinding
import com.module.core.base.BaseAdapterRecyclerView

//class ShortcutAdapter(
//    private val shortcuts: List<ShortcutItem>
//) : RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
//        val view =
//            LayoutInflater.from(parent.context).inflate(R.layout.item_shortcut, parent, false)
//        return ShortcutViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
//        val shortcut = shortcuts[position]
//        holder.bind(shortcut)
//    }
//
//    override fun getItemCount(): Int = shortcuts.size
//
//    class ShortcutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val icon: ImageView = itemView.findViewById(R.id.shortcut_icon)
//        private val text: TextView = itemView.findViewById(R.id.shortcut_text)
//
//        fun bind(shortcut: ShortcutItem) {
//            icon.setImageResource(shortcut.iconResId)
//            text.setText(shortcut.textResId)
//            itemView.setOnClickListener { shortcut.onClick() }
//        }
//    }
//}

class ShortcutAdapter(
    shortcuts: List<ShortcutItem>
) : BaseAdapterRecyclerView<ShortcutItem, ItemShortcutBinding>() {
    init {
        addDataList(shortcuts)
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