package com.au.lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAssetAvailableBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.priceServiceResume
import com.au.lyber.models.Data
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.viewmodels.PortfolioViewModel
import kotlin.reflect.KFunction1

class AvailableAssetAdapter(
    private val clickListener: (priceServiceResume) -> Unit = { _ -> }) :
    BaseAdapter<priceServiceResume>() {

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
                    val currency : AssetBaseData? = BaseActivity.currencies.firstNotNullOfOrNull{ item -> item.takeIf {item.id == id}}
                    if (currency != null) {
                        ivAsset.loadCircleCrop(currency.image)
                    }

                    tvAssetNameCode.text = it.id?.uppercase()
                    if (it.change.toFloat() > 0) {
                        tvAssetVariation.setTextColor(
                            getColor(
                                tvAssetVariation.context,
                                R.color.green_500)
                        )
                        tvAssetVariation.text = "+${it.change.commaFormatted}%"
                    } else {
                        tvAssetVariation.setTextColor(
                            getColor(
                                tvAssetVariation.context,
                                R.color.red_500)
                        )
                        tvAssetVariation.text = "${it.change.commaFormatted}%"
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