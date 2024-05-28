package com.Lyber.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.Strategy
import com.Lyber.ui.fragments.StrategyView
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants


class PickStrategyFragmentAdapter(val itemClicked: (position: Int, view: StrategyView) -> Unit) :
        BaseAdapter<Strategy>() {


    inner class ViewHolder(val strategyView: StrategyView) :
            RecyclerView.ViewHolder(strategyView) {
        init {

            strategyView.setOnRadioButtonClickListener {
                // itemClicked(adapterPosition)
            }

            strategyView.rootView.setOnClickListener {
                for (i in 0..itemList.size - 1) {
                    if (itemList[absoluteAdapterPosition]!!.name.equals(itemList[i]!!.name))
                        itemList[i]!!.isSelected = !itemList[i]!!.isSelected
                    else
                        itemList[i]!!.isSelected = false
                }
                itemClicked(absoluteAdapterPosition, strategyView)
                notifyDataSetChanged()
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ORDINARY_VIEW -> ViewHolder(StrategyView(parent.context))
            else -> {
                LoaderViewHolder(
                        LoaderViewBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                        )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ORDINARY_VIEW -> {
                (holder as ViewHolder).strategyView.apply {
                    itemList[position]?.let {
                        if (itemList[position]!!.isSelected)
                            (holder as ViewHolder).strategyView.setBackgroundResource(R.drawable.round_stroke_purple_500)
                        else
                            (holder as ViewHolder).strategyView.setBackgroundResource(R.drawable.round_stroke_gray_100)
//                        background = if (false) {
//                            isStrategySelected = true
////                            radioButton.setImageResource(R.drawable.radio_select)
//                            getDrawable(context, R.drawable.round_stroke_purple_500)
//                        } else {
//                            isStrategySelected = false
//                            radioButton.setImageResource(R.drawable.radio_unselect)
//                            getDrawable(context, R.drawable.round_stroke_gray_100)
//                        }
                        radioButton.gone()
                        topText = it.name ?: ""
                        if (it.expectedYield != null) {
                            binding.ivRisk.visible()
                            binding.tvRisk.visible()
                            binding.tvValueRisk.visible()
                            risk = it.expectedYield.substring(0, 1)
                                    .uppercase() + it.expectedYield.substring(1).lowercase()
                        } else {
                            binding.ivRisk.gone()
                            binding.tvRisk.gone()
                            binding.tvValueRisk.gone()
                            risk = ""
                        }
                        if (it.risk != null) {
                            binding.ivYield.visible()
                            binding.tvYield.visible()
                            binding.tvValueYield.visible()
                            yeild = it.risk.substring(0, 1).uppercase() + it.risk.substring(1)
                                    .lowercase()
                        } else {
                            binding.ivYield.gone()
                            binding.tvYield.gone()
                            binding.tvValueYield.gone()
                            yeild = ""
                        }
                        binding.ivMinInvest.visible()
                        binding.tvMinInvest.visible()
                        binding.tvMinInvestValue.visible()
                        binding.tvMinInvestValue.text = "${it.minAmount} USDT"

                        allocationView.setAssetsList(it.bundle)
                        if (it.activeStrategy != null) {
                            binding.tvPriceStrategy.visible()
                            binding.ivFrequency.visible()
                            binding.tvFrequency.visible()
                            binding.ivAmount.visible()
                            binding.tvAmount.visible()
                            val freq = when(it.activeStrategy!!.frequency){
                                 "1d"->context.getString(R.string.daily)
                                 "1w"->context.getString(R.string.weekly)
                                 else -> context.getString(R.string.monthly)
                            }
                            binding.tvFrequencyValue.text=": "+freq
                            binding.tvFrequencyValue.visible()
                            binding.tvAmountValue.text=": "+it.activeStrategy!!.amount+Constants.EURO
                            binding.tvAmountValue.visible()
                        } else {
                            binding.tvPriceStrategy.gone()
                            binding.ivFrequency.gone()
                            binding.tvFrequency.gone()
                            binding.tvFrequencyValue.gone()
                            binding.ivAmount.gone()
                            binding.tvAmount.gone()
                            binding.tvAmountValue.gone()
                        }
                    }
                }
            }

            else -> {}
        }

    }


}

