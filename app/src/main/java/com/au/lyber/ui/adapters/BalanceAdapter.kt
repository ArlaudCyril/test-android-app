package com.au.lyber.ui.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemMyAssetBinding
import com.au.lyber.models.Balance
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.bumptech.glide.Glide
import java.math.RoundingMode

class BalanceAdapter(
    private val isFromWithdraw:Boolean = false,private val listener: (Balance) -> Unit = { _ ->
    },
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
                    val currency = BaseActivity.assets.find { it.id == balanceId }
                    ivAssetIcon.loadCircleCrop(currency?.imageUrl ?: "")
                    tvAssetName.visible()
                    tvAssetName.text = currency?.fullName
                    val balance = it.balanceData
                    val priceCoin = balance.euroBalance.toDouble()
                        .div(balance.balance.toDouble() ?: 1.0)
                    if (isFromWithdraw){
                        if (currency!!.isWithdrawalActive) {
                            tvAssetNameCode.gone()
                        } else {
                            tvAssetNameCode.visible()
                        }
                    }else {
                        if (currency!!.isTradeActive) {
                            tvAssetNameCode.gone()
                        } else {
                            tvAssetNameCode.visible()
                        }
                    }
                    tvAssetAmount.text = balance.euroBalance.commaFormatted.currencyFormatted
                    tvAssetAmountInCrypto.text =
                        balance.balance.formattedAsset(
                            price = priceCoin,
                            rounding = RoundingMode.DOWN
                        )

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