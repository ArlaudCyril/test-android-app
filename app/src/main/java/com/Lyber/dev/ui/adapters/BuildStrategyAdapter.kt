package com.Lyber.dev.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.ItemAddedAssetBinding
import com.Lyber.dev.models.AddedAsset
import com.Lyber.dev.ui.fragments.BuildStrategyFragment
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.dev.utils.CommonMethods.Companion.loadImage
import com.Lyber.dev.utils.CommonMethods.Companion.roundFloat
import kotlin.math.roundToInt

class BuildStrategyAdapter(
        val rv: RecyclerView,
        val callback: (position: Int, String) -> Unit) :
        BaseAdapter<AddedAsset>() {

    inner class ViewHolder(val bind: ItemAddedAssetBinding) : RecyclerView.ViewHolder(bind.root) {
        init {
            bind.root.setOnClickListener {
                if (BuildStrategyFragment.isAnyItemSwiped(rv))
                    callback.invoke(absoluteAdapterPosition, "setView")
            }
            bind.rlDelete.setOnClickListener {
                callback.invoke(absoluteAdapterPosition, "delete")
            }
            bind.llAllocation.setOnClickListener {
                callback(absoluteAdapterPosition, "allocation")
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
                        com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == itemList[position]!!.addAsset.id } }
                if (asset != null)
                    ivAsset.loadCircleCrop(asset!!.imageUrl)


                if (it.isChangedManually) {
                    tvAuto.text = ""
                    tvAllocationValue.text = "${it.allocation.roundToInt().commaFormatted}%"
                } else {
                    tvAllocationValue.text = "(${it.allocation.roundToInt().commaFormatted}%)"
                    tvAuto.text = tvAuto.context.getString(R.string.auto)
                }
                tvAssetValue.text = "${
                    it.addAsset.priceServiceResumeData.lastPrice.toString()
                            .roundFloat().commaFormatted
                } €"
                tvAssetName.text = asset!!.fullName

            }
        }
    }
//    fun showButton(position: Int) {
//        // Implement logic to show the button at the specified position
//     Log.d("show","Button")
//    }
//
//    fun removeItem(position: Int) {
//        // Implement logic to remove the item at the specified position
//        Log.d("show","Remove")
//    }

}
