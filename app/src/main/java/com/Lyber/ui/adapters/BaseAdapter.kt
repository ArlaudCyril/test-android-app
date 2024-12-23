
package com.Lyber.ui.adapters

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.RIBData

abstract class BaseAdapter<items> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var copyList = mutableListOf<items?>()
    protected val itemList: MutableList<items?> = mutableListOf()

    fun clearList() {
        val count = itemList.count()
        itemList.clear()
        notifyItemRangeRemoved(0, count)
    }

    fun addList(list: List<items>) {
        val startCount = itemList.count()
        itemList.addAll(list)
//        notifyDataSetChanged()
        notifyItemRangeInserted(startCount, list.count())
    }

    fun addItem(item: items) {
        val startCount = itemList.count()
        itemList.add(item)
        notifyItemInserted(startCount)
    }
    fun removeItem(item: items) {
        val startCount = itemList.count()
        itemList.remove(item)
        notifyDataSetChanged()
    }
    fun getItem(position: Int): items? {
        return itemList[position]
    }

    fun changeItemAt(position: Int, item: items) {
        itemList[position] = item
        notifyItemChanged(position)
    }


    fun changeItems(list: List<items>) {
        val count = itemList.count()
        itemList.clear()
        notifyItemRangeRemoved(0, count)

        itemList.addAll(list)
        notifyItemRangeChanged(0, list.count(), list)

    }

    fun setList(list: List<items>) {

        val count = itemList.count()
        itemList.clear()
        notifyItemRangeRemoved(0, count)

        itemList.addAll(list)
        notifyItemRangeInserted(0, list.count())

    }

    fun addProgress() {
        val position = itemList.count()
        itemList.add(null)
        notifyItemInserted(position)
    }

    fun removeProgress() {
        if (itemList.isNotEmpty())
            if (itemList.last() == null) {
                itemList.removeLast()
                notifyItemRemoved(itemList.count())
            }
    }

//    fun setSearchResults(items: List<items>) {
//        copyList.clear()
//        copyList.addAll(itemList)
//        setList(items)
//    }

    fun setResults() {
        val start = itemList.count()
        itemList.clear()
        notifyItemRangeRemoved(0, start)
        itemList.addAll(copyList)
        notifyItemRangeInserted(0, copyList.count())
    }

    override fun getItemCount() = itemList.count()

    inner class LoaderViewHolder(val bind: LoaderViewBinding) :
        RecyclerView.ViewHolder(bind.root) {
        init {
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (itemList[holder.adapterPosition] == null) {
            (holder as BaseAdapter<*>.LoaderViewHolder).bind.ivLoader.animation =
                AnimationUtils.loadAnimation(
                    holder.bind.ivLoader.context,
                    R.anim.rotate_drawable
                )
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
    }

    protected val LOADER_VIEW = 0
    protected val ORDINARY_VIEW = 1

}
