package com.au.lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.databinding.ItemMyAssetBinding
import com.au.lyber.models.Assets
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoints
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import kotlin.math.roundToInt

class MyAssetAdapter(
    private val listener: (Assets) -> Unit = { _ -> },
    private val isAssetBreakdown: Boolean = false
) :
    BaseAdapter<Assets>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AssetViewHolder(
            ItemMyAssetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (itemList[position] != null)
            (holder as AssetViewHolder).binding.apply {
                itemList[position]?.let {

                    ivAssetIcon.loadCircleCrop(it.image?:"")

                    if (isAssetBreakdown) {
                        tvAssetNameCenter.gone()
                        tvAssetVariation.visible()
                        tvAssetName.visible()
                        tvAssetName.text = it.name
                    } else {
                        tvAssetNameCenter.visible()
                        tvAssetNameCenter.text = it.name
                        tvAssetVariation.gone()
                    }

                    val value = it.total_balance * it.euro_amount
                    tvAssetAmount.text = "${value.commaFormatted}${Constants.EURO}"
                    tvAssetAmountInCrypto.text =
                        "${it.total_balance.commaFormatted} ${it.asset_id.uppercase()}"

                }
            }
    }

    inner class AssetViewHolder(val binding: ItemMyAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

            binding.ivDropIcon.gone()
            binding.root.setOnClickListener {
                itemList[adapterPosition]?.let { it1 ->
                    listener(it1)
                }
            }
        }
    }
}