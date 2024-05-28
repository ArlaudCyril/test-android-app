package com.Lyber.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.ItemRecurringInvestmentBinding
import com.Lyber.models.ActiveStrategyData
import com.Lyber.models.Investment
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.toFormat
import com.Lyber.utils.Constants

class RecurringInvestmentAdapter(private val clickListener: (ActiveStrategyData) -> Unit = { _ -> }, private val context:Context) :
    BaseAdapter<ActiveStrategyData>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RecurringViewHolder(
            ItemRecurringInvestmentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemList[position] != null)

            (holder as RecurringViewHolder).binding.apply {

                itemList[position]?.let {

//                    tvInvestmentTitle.text = if (it.strategyType == Constants.STRATEGY_TYPE) it.asset_id.toString()
//                        .uppercase() else it.user_investment_strategy_id?.strategy_name ?: ""

//                    if (it.strategyType == "SINGULAR") {
//
////                        ivInvestment.loadCircleCrop(it.logo)
//
//                    } else
                        ivInvestment.setImageResource(R.drawable.ic_intermediate_strategy)

                    tvInvestmentTitle.text=it.strategyName
                    tvInvestmentAmount.text = "${it.amount}${Constants.EURO}"
                    when(it.frequency){
                        "1d"->tvInvestmentFrequency.text=context.getString(R.string.daily)
                        "1w"->tvInvestmentFrequency.text=context.getString(R.string.weekly)
                        "1m"->tvInvestmentFrequency.text=context.getString(R.string.monthly)
                        else -> tvInvestmentFrequency.text=it.frequency
                    }
//                    tvInvestmentFrequency.text = it.frequency
                    tvInvestmentUpcomingPayment.text =  context.getString(R.string.upcoming_payment)+it.nextExecution.toFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'","MMMM dd")


                }
            }
    }

    inner class RecurringViewHolder(val binding: ItemRecurringInvestmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemList[adapterPosition]?.let(clickListener)
            }

        }
    }

}