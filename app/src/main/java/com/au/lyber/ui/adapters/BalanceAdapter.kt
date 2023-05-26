package com.au.lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemMyAssetBinding
import com.au.lyber.models.Balance
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants

class BalanceAdapter(
    private val listener: (Balance) -> Unit = { _ ->
    },
    private val isAssetBreakdown: Boolean = false
) :
    BaseAdapter<Balance>() {

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
                    val balanceId = it.id
                    val currency = BaseActivity.currencies.find { it.id == balanceId}
                    ivAssetIcon.loadCircleCrop(currency?.image?:"")

                    if (isAssetBreakdown) {
                        tvAssetName.visible()
                        tvAssetName.text = currency?.fullName
                    }
                    val value = it.balanceData.euroBalance
                    tvAssetAmount.text = "${value.commaFormatted}${Constants.EURO}"
                    tvAssetAmountInCrypto.text =
                        "${it.balanceData.balance} ${it.id.uppercase()}"

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