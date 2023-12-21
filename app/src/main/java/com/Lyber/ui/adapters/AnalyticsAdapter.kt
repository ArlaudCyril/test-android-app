/*
package com.au.lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAnalyticsBinding
import com.au.lyber.models.AnalyticsData

class AnalyticsAdapter(private val clickListener: (AnalyticsData) -> Unit) :
    BaseAdapter<AnalyticsData>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AnalyticsViewHolder(
            ItemAnalyticsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemList[position] != null)
            (holder as AnalyticsViewHolder).binding.apply {
                itemList[position]?.let {
                    tvTotalEarning.text = it.title
                    tvValueTotalEarning.text = it.figure

                    lineChart.setLineData(
                        it.list,
                        R.color.purple_600,
                        R.drawable.drawable_purple_fill_line_chart
                    )
                }
            }
    }

    inner class AnalyticsViewHolder(val binding: ItemAnalyticsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
            }

            binding.lineChart.setOnClickListener {
                itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
            }
        }
    }
}*/
