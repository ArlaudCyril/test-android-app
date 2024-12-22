package com.Lyber.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.ItemMyAssetBinding
import com.Lyber.models.Balance
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.currencyFormatted
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.visible
import java.math.RoundingMode

class BalanceAdapter(
    private val isFromWithdraw:Boolean = false,private val listener: (Balance) -> Unit = { _ ->
    },private val hideBalanceFeature:Boolean=false
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
                    val currency = com.Lyber.ui.activities.BaseActivity.assets.find { it.id == balanceId }
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
                        if (currency!=null && currency.isTradeActive) {
                            tvAssetNameCode.gone()
                        } else {
                            tvAssetNameCode.visible()
                        }
                    }
                    if(hideBalanceFeature && App.prefsManager.hideAmount) {
                        tvAssetAmount.text = "*****"
                        tvAssetAmountInCrypto.text = "*****"
                    }
                    else {
                        tvAssetAmount.text = balance.euroBalance.commaFormatted.currencyFormatted
                        tvAssetAmountInCrypto.text =
                            balance.balance.formattedAsset(
                                priceCoin,
                                rounding = RoundingMode.DOWN, currency?.decimals ?: 4
                            )
                    }
//                    +" ${balanceId.uppercase()}"

                    try {
                        val balanceId = it.id
                        val currency = com.Lyber.ui.activities.BaseActivity.assets.find { it.id == balanceId }
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
                            if (currency!=null && currency.isTradeActive) {
                                tvAssetNameCode.gone()
                            } else {
                                tvAssetNameCode.visible()
                            }
                        }
                        if(hideBalanceFeature && App.prefsManager.hideAmount) {
                            tvAssetAmount.text = "*****"
                            tvAssetAmountInCrypto.text = "*****"
                        } else {
                            tvAssetAmount.text =
                                balance.euroBalance.commaFormatted.currencyFormatted
                            tvAssetAmountInCrypto.text =
                                balance.balance.formattedAsset(
                                    priceCoin,
                                    rounding = RoundingMode.DOWN, currency!!.decimals
                                )
                        }
//                        +" ${balanceId.uppercase()}"
                    }catch (_:Exception){

                    }
                }
            }
    }

    inner class AssetViewHolder(val binding: ItemMyAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

            binding.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            binding.ivDropIcon.visible()
            binding.root.setOnClickListener {
                itemList[absoluteAdapterPosition]?.let { it1 ->
                    listener(it1)
                }
            }
        }
    }
}