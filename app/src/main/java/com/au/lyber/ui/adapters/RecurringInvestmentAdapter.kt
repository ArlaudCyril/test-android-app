package com.au.lyber.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemRecurringInvestmentBinding
import com.au.lyber.models.Investment
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.Constants

class RecurringInvestmentAdapter(private val clickListener: (Investment) -> Unit = { _ -> }) :
    BaseAdapter<Investment>() {

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

                    tvInvestmentTitle.text = if (it.type == "SINGULAR") it.asset_id.toString()
                        .uppercase() else it.user_investment_strategy_id?.strategy_name ?: ""

                    if (it.type == "SINGULAR") {

                        ivInvestment.loadCircleCrop(it.logo)

                    } else ivInvestment.setImageResource(R.drawable.ic_intermediate_strategy)

                    tvInvestmentAmount.text = "${it.amount}${Constants.EURO}"
                    tvInvestmentFrequency.text = it.frequency
                    tvInvestmentUpcomingPayment.text = "Upcoming Payment August 27"

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