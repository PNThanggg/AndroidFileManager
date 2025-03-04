//package com.modules.core.ui
//
//import android.app.Activity
//import android.view.View
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewbinding.ViewBinding
//
//abstract class MyRecyclerViewAdapter<T, VB : ViewBinding>(
//    val activity: Activity, val recyclerView: MyRecyclerView, val itemClick: (T) -> Unit
//) : RecyclerView.Adapter<MyRecyclerViewAdapter<T, VB>.ViewHolder>() {
//    private var lastLongPressedItem = -1
//
//    protected var actModeCallback: MyActionModeCallback
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        fun bindView(
//            any: T,
//            allowSingleClick: Boolean,
//            allowLongClick: Boolean,
//            callback: (itemView: View, adapterPosition: Int) -> Unit
//        ): View {
//            return itemView.apply {
//                callback(this, adapterPosition)
//
//                if (allowSingleClick) {
//                    setOnClickListener { viewClicked(any) }
//                    setOnLongClickListener {
//                        if (allowLongClick) viewLongClicked() else viewClicked(
//                            any
//                        ); true
//                    }
//                } else {
//                    setOnClickListener(null)
//                    setOnLongClickListener(null)
//                }
//            }
//        }
//
//        fun viewClicked(any: T) {
//            if (actModeCallback.isSelectable) {
//                val currentPosition = adapterPosition - positionOffset
//                val isSelected = selectedKeys.contains(getItemSelectionKey(currentPosition))
//                toggleItemSelection(!isSelected, currentPosition, true)
//            } else {
//                itemClick.invoke(any)
//            }
//            lastLongPressedItem = -1
//        }
//
//        fun viewLongClicked() {
//            val currentPosition = adapterPosition - positionOffset
//            if (!actModeCallback.isSelectable) {
//                activity.startActionMode(actModeCallback)
//            }
//
//            toggleItemSelection(true, currentPosition, true)
//            itemLongClicked(currentPosition)
//        }
//    }
//}