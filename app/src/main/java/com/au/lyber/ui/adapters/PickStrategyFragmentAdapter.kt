package com.au.lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.Strategy
import com.au.lyber.ui.fragments.StrategyView

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
                        background = if (false) {
                            isStrategySelected = true
                            radioButton.setImageResource(R.drawable.radio_select)
                            getDrawable(context, R.drawable.round_stroke_purple_500)
                        } else {
                            isStrategySelected = false
                            radioButton.setImageResource(R.drawable.radio_unselect)
                            getDrawable(context, R.drawable.round_stroke_gray_100)
                        }

                        topText = it.name ?: ""
                        yeild = "~${it.expectedYield}% ROI"
                        risk = it.risk
                        allocationView.setAssetsList(it.bundle)
                    }
                }
            }
            else -> {}
        }

    }


}
