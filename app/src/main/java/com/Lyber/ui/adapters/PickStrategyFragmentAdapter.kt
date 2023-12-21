package com.Lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.Strategy
import com.Lyber.ui.fragments.StrategyView

class PickStrategyFragmentAdapter(val itemClicked: (position: Int) -> Unit) :
    BaseAdapter<Strategy>() {


    inner class ViewHolder(val strategyView: StrategyView) :
        RecyclerView.ViewHolder(strategyView) {
        init {

            strategyView.setOnRadioButtonClickListener {
                itemClicked(adapterPosition)
            }

            strategyView.rootView.setOnClickListener {
                itemClicked(adapterPosition)
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
                        background = if (it.is_chosen == 1) {
                            isStrategySelected = true
                            radioButton.setImageResource(R.drawable.radio_select)
                            getDrawable(context, R.drawable.round_stroke_purple_500)
                        } else {
                            isStrategySelected = false
                            radioButton.setImageResource(R.drawable.radio_unselect)
                            getDrawable(context, R.drawable.round_stroke_gray_100)
                        }

                        topText = it.status ?: ""
                        yeild = "~${it.yield}% ROI"
                        risk = it.risk
                        allocationView.setAssetsList(it.investment_strategy_assets)
                    }
                }
            }
            else -> {}
        }

    }


}
