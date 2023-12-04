package com.au.lyber.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAddedAssetBinding
import com.au.lyber.models.AddedAsset
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.formLineData
import com.au.lyber.utils.CommonMethods.Companion.getLineData
import com.au.lyber.utils.CommonMethods.Companion.getRedLineData
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.loadImage
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import kotlin.math.roundToInt

class BuildStrategyAdapter(val handle: (position: Int) -> Unit = { _ -> }) :
    BaseAdapter<AddedAsset>() {

    inner class ViewHolder(val bind: ItemAddedAssetBinding) : RecyclerView.ViewHolder(bind.root) {
        init {
            bind.llAllocation.setOnClickListener {
                handle(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddedAssetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind.apply {
            itemList[position]?.let {
                if (it.addAsset.priceServiceResumeData.change.roundFloat().toFloat() > 0) {
                    tvAssetVariation.text = "+${it.addAsset.priceServiceResumeData.change.roundFloat().commaFormatted}%"
                    //tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))
                } else {
                    tvAssetVariation.text = "${it.addAsset.priceServiceResumeData.change.roundFloat().commaFormatted}%"
                    //tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))

                }
                val urlLineChart = it.addAsset.priceServiceResumeData.squiggleURL
                lineChart.loadImage(urlLineChart)
                val asset =
                    BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == itemList[position]!!.addAsset.id } }

                ivAsset.loadCircleCrop(asset!!.imageUrl)


                if (it.isChangedManually) {
                    tvAuto.text = ""
                    tvAllocationValue.text = "${it.allocation.roundToInt().commaFormatted}%"
                }else{
                    tvAllocationValue.text = "(${it.allocation.roundToInt().commaFormatted}%)"
                    tvAuto.text = tvAuto.context.getString(R.string.auto )
                }
                tvAssetValue.text = "${
                    it.addAsset.priceServiceResumeData.lastPrice.toString()
                        .roundFloat().commaFormatted
                } €"
                tvAssetName.text = asset.fullName

            }
        }
    }


}