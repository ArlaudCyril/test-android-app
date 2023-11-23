package com.au.lyber.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.Strategy
import com.au.lyber.ui.fragments.StrategyView
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible

class PickStrategyFragmentAdapter(val itemClicked: (position: Int) -> Unit) :
    BaseAdapter<Strategy>() {



    inner class ViewHolder(val strategyView: StrategyView) :
        RecyclerView.ViewHolder(strategyView) {
        init {

            strategyView.setOnRadioButtonClickListener {
               // itemClicked(adapterPosition)
            }

            strategyView.rootView.setOnClickListener {
                Log.d("positionAdapter",adapterPosition.toString())
              itemClicked(adapterPosition)
            }

        }
    }
    fun markSelected(position: Int){
        for (item in itemList){
            item!!.isSelected = false
        }
        itemList[position]!!.isSelected = true
        notifyDataSetChanged()
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
                        background = if (it.isSelected) {
                            isStrategySelected = true
                            radioButton.setImageResource(R.drawable.radio_select)
                            getDrawable(context, R.drawable.round_stroke_purple_500)
                        } else {
                            isStrategySelected = false
                            radioButton.setImageResource(R.drawable.radio_unselect)
                            getDrawable(context, R.drawable.round_stroke_gray_100)
                        }
                        radioButton.gone()
                        topText = it.name ?: ""
                        if (it.expectedYield !=null) {
                            binding.ivRisk.visible()
                            binding.tvRisk.visible()
                            binding.tvValueRisk.visible()
                            risk = it.expectedYield.substring(0, 1)
                                .uppercase() + it.expectedYield.substring(1).lowercase()
                        }else{
                            binding.ivRisk.gone()
                            binding.tvRisk.gone()
                            binding.tvValueRisk.gone()
                            risk = ""
                        }
                        if (it.risk!=null) {
                            binding.ivYield.visible()
                            binding.tvYield.visible()
                            binding.tvValueYield.visible()
                            yeild = it.risk.substring(0, 1).uppercase() + it.risk.substring(1)
                                .lowercase()
                        }else{
                            binding.ivYield.gone()
                            binding.tvYield.gone()
                            binding.tvValueYield.gone()
                            yeild = ""
                        }
                        allocationView.setAssetsList(it.bundle)
                        if (it.activeStrategy!=null) {
                            binding.tvPriceStrategy.visible()
                        }else{
                            binding.tvPriceStrategy.gone()
                        }
                    }
                }
            }
            else -> {}
        }

    }


}
