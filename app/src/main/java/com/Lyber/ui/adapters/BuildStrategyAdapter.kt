package com.Lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.models.AddedAsset
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.formLineData
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.roundFloat
import com.Lyber.databinding.ItemAddedAssetBinding
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
                if (it.addAsset.price_change_percentage_24h > 0) {
                    tvAssetVariation.text =
                        "${it.addAsset.price_change_percentage_24h.toString().roundFloat().commaFormatted}%"
                    tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))
                    lineChart.setLineData(
                        it.addAsset.sparkline_in_7d.price.formLineData(),
                        R.color.green_500,
                        R.drawable.drawable_green_fill_line_chart
                    )
                } else {
                    tvAssetVariation.text =
                        "${it.addAsset.price_change_percentage_24h.toString().roundFloat().commaFormatted}%"
                    tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))
                    lineChart.setLineData(
                        it.addAsset.sparkline_in_7d.price.formLineData(),
                        R.color.red_500,
                        R.drawable.drawable_red_fill_line_chart
                    )
                }


                    ivAsset.loadCircleCrop(it.addAsset.image)

                tvAllocationValue.text = "(${it.allocation.roundToInt().commaFormatted}%)"
                tvAuto.text = "Auto"
                tvAssetValue.text = "${it.addAsset.current_price.toString().roundFloat().commaFormatted} €"
                tvAssetName.text = it.addAsset.name

            }
        }
    }


}