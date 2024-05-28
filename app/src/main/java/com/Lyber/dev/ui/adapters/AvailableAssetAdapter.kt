package com.Lyber.dev.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.ItemAssetAvailableBinding
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.loadCircleCrop

class AvailableAssetAdapter(
    private val clickListener: (PriceServiceResume) -> Unit = { _ -> }) :
    BaseAdapter<PriceServiceResume>() {

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
                itemList[position]?.let { it ->
                    val id = it.id
                    val currency : AssetBaseData? = com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull{ item -> item.takeIf {item.id == id}}
                    if (currency != null) {
                        ivAsset.loadCircleCrop(currency.imageUrl)
                    }

                    tvAssetNameCode.text = it.id?.uppercase()
                    if (it.priceServiceResumeData.change.toFloat() > 0) {
                        tvAssetVariation.setTextColor(
                            getColor(
                                tvAssetVariation.context,
                                R.color.green_500)
                        )
                        tvAssetVariation.text = "+${it.priceServiceResumeData.change.commaFormatted}%"
                    } else {
                        tvAssetVariation.setTextColor(
                            getColor(
                                tvAssetVariation.context,
                                R.color.red_500)
                        )
                        tvAssetVariation.text = "${it.priceServiceResumeData.change.commaFormatted}%"
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