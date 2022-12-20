package com.au.lyber.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAssetAvailableBinding
import com.au.lyber.models.Data
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop

class AvailableAssetAdapter(private val clickListener: (Data) -> Unit = { _ -> }) :
    BaseAdapter<Data>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return AvailableAssetViewHolder(
            ItemAssetAvailableBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemList[position] != null)
            (holder as AvailableAssetViewHolder).binding.apply {
                itemList[position]?.let {

                    ivAsset.loadCircleCrop(it.image)

                    tvAssetNameCode.text = it.symbol?.uppercase()
                    if (it.price_change_percentage_24h.toFloat() > 0) {
                        tvAssetVariation.setTextColor(
                            getColor(
                                tvAssetVariation.context,
                                R.color.green_500)
                        )
                        tvAssetVariation.text = "+${it.price_change_percentage_24h.commaFormatted}%"
                    } else {
                        tvAssetVariation.setTextColor(
                            getColor(
                                tvAssetVariation.context,
                                R.color.red_500)
                        )
                        tvAssetVariation.text = "${it.price_change_percentage_24h.commaFormatted}%"
                    }
                }
            }
    }

    inner class AvailableAssetViewHolder(val binding: ItemAssetAvailableBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
            }
        }
    }
}