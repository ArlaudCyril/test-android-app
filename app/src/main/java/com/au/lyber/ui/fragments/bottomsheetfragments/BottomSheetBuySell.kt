package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import com.au.lyber.databinding.BottomSheetBuySellBinding

class BottomSheetBuySell(private val handle: (String) -> Unit = { _ -> }) :
    BaseBottomSheet<BottomSheetBuySellBinding>() {

    override fun bind() = BottomSheetBuySellBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            ivTopAction.setOnClickListener { dismiss() }

            tvSubTitleBuy.setOnClickListener(buy)
            tvTitleBuy.setOnClickListener(buy)
            ivRightArrowBuy.setOnClickListener(buy)

            tvSubTitleSell.setOnClickListener(sell)
            tvTitleSell.setOnClickListener(sell)
            ivRightArrowSell.setOnClickListener(sell)

            tvSubTitleWithdraw.setOnClickListener(withdraw)
            tvTitleWithdraw.setOnClickListener(withdraw)
            ivRightArrowWithdraw.setOnClickListener(withdraw)

            tvSubTitleDeposit.setOnClickListener(deposit)
            tvTitleDeposit.setOnClickListener(deposit)
            ivRightArrowDeposit.setOnClickListener(deposit)

            tvSubTitleExchange.setOnClickListener(exchange)
            tvTitleExchange.setOnClickListener(exchange)
            ivRightArrowExchange.setOnClickListener(exchange)
        }
    }

    private val deposit = View.OnClickListener {
        handle("deposit")
        dismiss()
    }
    private val withdraw = View.OnClickListener {
        handle("withdraw")
        dismiss()
    }

    private val exchange = View.OnClickListener {
        handle("exchange")
        dismiss()
    }

    private val buy = View.OnClickListener {
        handle("buy")
        dismiss()
    }

    private val sell = View.OnClickListener {
        handle("sell")
        dismiss()
    }

}