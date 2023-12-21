package com.Lyber.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.ItemBottomSheetRecyclerViewBinding
import com.Lyber.models.DataBottomSheet
import com.Lyber.ui.fragments.bottomsheetfragments.BottomSheetDialog

class BottomSheetAdapter(
    private val listener: ItemListener,
    private val sheetType: BottomSheetDialog.SheetType
,private val context:Context) :
    RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

    interface ItemListener {
        fun itemClicked(item: DataBottomSheet)
    }

    private var list: MutableList<DataBottomSheet> = mutableListOf()

    inner class ViewHolder(val binding: ItemBottomSheetRecyclerViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.text.setOnClickListener {
                listener.itemClicked(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBottomSheetRecyclerViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position].let {
            holder.binding.apply {
                when (sheetType) {
                    BottomSheetDialog.SheetType.PERSONAL_ASSETS -> {
                        try {
                            val value = it.title.split("-")
                            text.text = context.getString(R.string.assets, value[0], value[1])
                        } catch (e: Exception) {
                            text.text = it.title + context.getString(R.string.assets)
                        }
                    }
                    BottomSheetDialog.SheetType.ANNUAL_INCOME -> {
                        val value = it.title.split("-")
                        if (value.size > 1) {
                            text.text = context.getString(R.string.k_month, value[0], value[1])
                        }else{
                            text.text = it.title+"€/month"
                        }
                    }
                    else -> text.text = it.title
                }

            }
        }
    }

    override fun getItemCount() = list.count()

    fun setList(list: List<DataBottomSheet>) {
        val start = this.list.count()
        this.list.addAll(list)
        notifyItemRangeChanged(start, list.count())
    }
}