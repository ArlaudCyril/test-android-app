package com.Lyber.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.Strategy
import com.Lyber.ui.fragments.StrategyView
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible

class PickStrategyFragmentAdapter(val itemClicked: (position: Int, view: StrategyView) -> Unit) :
    BaseAdapter<Strategy>() {


    inner class ViewHolder(val strategyView: StrategyView) :
        RecyclerView.ViewHolder(strategyView) {
        init {

            strategyView.setOnRadioButtonClickListener {
                // itemClicked(adapterPosition)
            }

            strategyView.rootView.setOnClickListener {
//                Log.d("dataaa", "${itemList[absoluteAdapterPosition]!!.isSelected}")
//                Log.d("dataaa", "${!itemList[absoluteAdapterPosition]!!.isSelected}")
//                itemList[absoluteAdapterPosition]!!.isSelected =
//                    !itemList[absoluteAdapterPosition]!!.isSelected
//                Log.d("dataaa", "${itemList[absoluteAdapterPosition]!!.isSelected}")
                for (i in 0..itemList.size-1) {
                    if (itemList[absoluteAdapterPosition]!!.name.equals(itemList[i]!!.name))
                        itemList[i]!!.isSelected = !itemList[i]!!.isSelected
                    else
                        itemList[i]!!.isSelected = false
                }
                itemClicked(adapterPosition,strategyView)
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
                        allocationView.setAssetsList(it.bundle)
                        if (it.activeStrategy != null) {
                            binding.tvPriceStrategy.visible()
                        } else {
                            binding.tvPriceStrategy.gone()
                        }
                    }
                }
            }

            else -> {}
        }

    }


}
