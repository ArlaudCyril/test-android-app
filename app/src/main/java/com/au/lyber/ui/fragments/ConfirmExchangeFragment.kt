package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmInvestmentBinding
import com.au.lyber.models.DataQuote
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible

class ConfirmExchangeFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener {
    private var timer = 25
    private var orderId: String = ""
    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false
        getData()
        viewModel.getQuoteResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED){
                CommonMethods.dismissProgressDialog()
                prepareView(it.data)
            }
        }
        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!.id)
                viewModel.selectedBalance =
                    BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetTo!!.id }
                findNavController().navigate(R.id.portfolioDetailFragment)

            }
        }

    }

    private fun getData() {
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
              tvValueDepositFee
          ).gone()
      }

        CommonMethods.showProgressDialog(requireActivity())
        viewModel.getQuote(
            viewModel.exchangeAssetFrom?.id ?: "",
            viewModel.exchangeAssetTo?.id ?: "",
            viewModel.exchangeFromAmount
        )
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressed()
                btnConfirmInvestment -> {
                    CommonMethods.checkInternet(requireContext()) {
                        CommonMethods.showProgressDialog(requireContext())
                        timer =0
                        viewModel.confirmOrder(
                            orderId
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {
            tvNestedAmount.text = getString(R.string.ratio)
            tvNestedAmountValue.text = data!!.ratio
            tvValueLyberFee.text =
                data.fees.decimalPoint() + data.fromAsset.uppercase()
            tvAmount.text =
                "${data.toAmount.decimalPoint()} ${data.toAsset.uppercase()}"
            orderId = data.orderId
            tvExchangeFromValue.text =
                "${data.fromAmount} ${data.fromAsset.uppercase()}"
            tvExchangeToValue.text =
                "${data.toAmount.decimalPoint()} ${data.toAsset.uppercase()}"

            val valueTotal = data.fees.toDouble()+data.fromAmount.toDouble()
            tvValueTotal.text =
                "${valueTotal.toString().decimalPoint()} ${data.fromAsset.uppercase()}"
            btnConfirmInvestment.isEnabled = true
            startTimer()
            btnConfirmInvestment.text = getString(R.string._25_sec, getString(R.string.confirm_exchange),"25")
            title.text = getString(R.string.confirm_exchange)




        }
    }


    private fun startTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
               if (timer ==0){
                   binding.btnConfirmInvestment.isEnabled = true
                   binding.btnConfirmInvestment.text =getString(R.string._25_sec, getString(R.string.confirm_exchange)
                       ,timer.toString())
                   binding.btnConfirmInvestment.isEnabled = false
                   binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                       requireContext(),
                       R.drawable.button_purple_400
                   )
               }else{
                   timer -=1
                   binding.btnConfirmInvestment.text = getString(R.string._25_sec, getString(R.string.confirm_exchange)
                   ,timer.toString())
                   binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                       requireContext(),
                       R.drawable.button_purple_500
                   )
                   startTimer()
               }
        },1000)
    }

}