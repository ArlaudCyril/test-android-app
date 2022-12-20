package com.au.lyber.ui.fragments.bottomsheetfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import com.au.lyber.R
import com.au.lyber.databinding.PortfolioThreeDotsBinding
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PortfolioViewModel

class PortfolioThreeDots(private val listenItemClicked: (String, String) -> Unit = { _, _ -> }) :
    BaseBottomSheet<PortfolioThreeDotsBinding>(), View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = PortfolioThreeDotsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())

        prepareView()

        binding.llWithdraw.root.setOnClickListener(this)
        binding.llExchange.root.setOnClickListener(this)
        binding.llDepositFiat.root.setOnClickListener(this)
        binding.llSell.root.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)

    }

    @SuppressLint("SetTextI18n")
    private fun prepareView() {

        val isLyberAsset =
            Constants.LYBER_ASSETS.contains(viewModel.selectedAsset?.asset_id)
                    || Constants.LYBER_ASSETS.contains(viewModel.selectedAsset?.asset_id?.uppercase())
                    || viewModel.selectedAsset?.asset_name in Constants.LYBER_ASSETS


        binding.llWithdraw.let {
            it.root.updatePadding(left = 0, right = 0)
            if (!isLyberAsset && viewModel.screenCount == 1) it.root.gone()
            it.ivItem.setImageResource(R.drawable.ic_withdraw)
            it.tvStartTitle.text = "Withdraw"
            it.tvStartSubTitle.text = "Send assets to your bank account"
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        }

        binding.llExchange.let {
            it.root.updatePadding(left = 0, right = 0)
            it.ivItem.setImageResource(R.drawable.ic_exchange)
            it.tvStartTitle.text = "Exchange"
            it.tvStartSubTitle.text = "Trade one asset for another"
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)

        }

        binding.llDepositFiat.let {

            it.root.updatePadding(left = 0, right = 0)
            it.ivItem.setImageResource(R.drawable.ic_exchange)
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            if (viewModel.screenCount == 1) {
                if (!isLyberAsset) it.root.gone()
                it.tvStartTitle.text = "Deposit ${viewModel.selectedAsset?.asset_id?.uppercase()}"
                it.tvStartSubTitle.text = "To your Lyber wallet"
            } else {
                it.tvStartTitle.text = "Deposit"
                it.tvStartSubTitle.text = "Add money on Lyber"
            }
        }

        binding.llSell.let {
            if (viewModel.screenCount == 1) {
                it.root.updatePadding(left = 0, right = 0)
                it.ivItem.setImageResource(R.drawable.ic_exchange)
                it.tvStartTitle.text = "Sell ${viewModel.selectedAsset?.asset_id?.uppercase()}"
                it.tvStartSubTitle.text = "For fiat currency"
                it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            } else it.root.gone()
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                llWithdraw.root -> {
                    listenItemClicked(tag ?: "", "withdraw")
                    dismiss()
                }
                llExchange.root -> {
                    listenItemClicked(tag ?: "", "exchange")
                    dismiss()
                }
                llDepositFiat.root -> {
                    listenItemClicked(tag ?: "", "deposit")
                    dismiss()
                }
                llSell.root -> {
                    listenItemClicked(tag ?: "", "sell")
                    dismiss()
                }
                ivTopAction -> dismiss()
            }
        }
    }


}