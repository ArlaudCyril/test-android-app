package com.au.lyber.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmInvestmentBinding
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import java.math.RoundingMode

class ConfirmWithdrawalFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private var timer = 25
    private var dialog: Dialog? = null
    private var orderId: String = ""
    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false
        prepareView()
    }

    private fun prepareView() {
        binding.apply {
            listOf(
                tvNestedAmount,
                tvNestedAmountValue,
                tvExchangeFrom, tvExchangeFromValue, tvExchangeTo, tvExchangeToValue,
                tvLyberFee,
                tvValueLyberFee
            ).visible()

            listOf(
                tvFrequency,
                tvValueFrequency,
                tvAllocation,
                allocationView,
                tvAssetPrice,
                tvValueAssetPrice,
                tvDeposit,
                tvDepositFee,
                tvValueDeposit,
                tvValueDepositFee,
                tvInfo
            ).gone()
        }
        binding.apply {
            tvNestedAmount.text = getString(R.string.amount)
            tvNestedAmountValue.text = requireArguments().getString(Constants.EURO)
            viewModel.selectedAssetDetail.let {
                val balance =
                    BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                    .div(balance!!.balanceData.balance.toDouble() ?: 1.0)
                tvValueLyberFee.text =
                    viewModel.selectedNetworkDeposit!!.withdrawFee.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) + it!!.id.uppercase()
                tvAmount.text =
                    "${balance.balanceData.balance.formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) } ${it.id.uppercase()}"

                tvExchangeFrom.text = getString(R.string.address)
                tvExchangeFromValue.text = viewModel.withdrawAddress!!.address.substring(0,5)+"...."+
                        viewModel.withdrawAddress!!.address.last()
                tvExchangeTo.text = getString(R.string.network)
                tvExchangeToValue.text = viewModel.withdrawAddress!!.network.uppercase()
                val valueTotal = viewModel.selectedNetworkDeposit!!.withdrawFee.toDouble()+
                        requireArguments().getString(Constants.EURO,"")
                            .replace(it.id.uppercase(),"").toDouble()
                tvValueTotal.text =
                    "${valueTotal.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) } ${it.id.uppercase()}"
                btnConfirmInvestment.isEnabled = true
                btnConfirmInvestment.text =
                    getString(R.string.confirm_withdrawal)
                title.text = getString(R.string.confirm_withdrawal)
            }





        }

    }

    override fun onClick(v: View?) {

    }
}