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
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.google.gson.Gson
import java.math.RoundingMode


class ConfirmExchangeFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
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

    }

    override fun onResume() {
        super.onResume()
        getData()
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

        if (arguments!=null && requireArguments().containsKey(Constants.DATA_SELECTED)){
            val data = Gson().fromJson(requireArguments().getString(Constants.DATA_SELECTED),DataQuote::class.java)
            prepareView(data)
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnConfirmInvestment -> {
                    viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!)
                    val bundle = Bundle().apply {
                        putString(Constants.ORDER_ID,orderId)
                    }
                    findNavController().navigate(R.id.action_confirmExchangeFragment_to_deatil_fragment
                    ,bundle)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {
            tvNestedAmount.text = getString(R.string.ratio)
            val balance =BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
            var priceCoin =balance!!.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            tvNestedAmountValue.text = "1 : "+data!!.ratio
            tvValueLyberFee.text =
                data.fees.formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                ) + data.fromAsset.uppercase()

            orderId = data.orderId
            tvExchangeFromValue.text =
                "${data.fromAmount} ${data.fromAsset.uppercase()}"
            val balanceFrom =BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetFrom}
            val balanceTo =BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetTo}
            val balanceFromPrice = balanceFrom!!.priceServiceResumeData.lastPrice
            val balanceToPrice = balanceTo!!.priceServiceResumeData.lastPrice
            val valuesInEurosToAsset =
                (if (data.fromAsset == viewModel.exchangeAssetTo!!) balanceToPrice else balanceFromPrice).toDouble()
            priceCoin = valuesInEurosToAsset
                .div(data.toAmount.toDouble())
            tvExchangeToValue.text =
                "${data.toAmount.formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                )} ${data.toAsset.uppercase()}"
            tvAmount.text =
                "${data.toAmount.formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                ) } ${data.toAsset.uppercase()}"
            val valueTotal = data.fees.toDouble()+data.fromAmount.toDouble()
            tvValueTotal.text =
                "${valueTotal.toString().formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                ) } ${data.fromAsset.uppercase()}"
            btnConfirmInvestment.isEnabled = true
            startTimer()
            btnConfirmInvestment.text =
                getString(R.string._25_sec, getString(R.string.confirm_exchange), "25")
            title.text = getString(R.string.confirm_exchange)


        }
    }


    private fun startTimer() {
       try {
           Handler(Looper.getMainLooper()).postDelayed({
               if (lifecycle.currentState == Lifecycle.State.RESUMED)
               if (timer == 0) {
                   binding.btnConfirmInvestment.isEnabled = true
                   binding.btnConfirmInvestment.text = getString(
                       R.string._25_sec, getString(R.string.confirm_exchange), timer.toString()
                   )
                   binding.btnConfirmInvestment.isEnabled = false
                   binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                       requireContext(),
                       R.drawable.button_purple_400
                   )
               } else {
                   timer -= 1
                   binding.btnConfirmInvestment.text = getString(
                       R.string._25_sec, getString(R.string.confirm_exchange), timer.toString()
                   )
                   binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                       requireContext(),
                       R.drawable.button_purple_500
                   )
                   startTimer()
               }
           }, 1000)
       }catch (e:Exception){
           e.printStackTrace()
       }
    }





}