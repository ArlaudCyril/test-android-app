package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmInvestmentBinding
import com.au.lyber.databinding.LottieViewBinding
import com.au.lyber.models.DataQuote
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import java.math.RoundingMode
import com.au.lyber.utils.Constants
import com.github.jinatonic.confetti.CommonConfetti
import com.google.gson.Gson
import okhttp3.ResponseBody


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
                ivTopAction -> requireActivity().onBackPressed()
                btnConfirmInvestment -> {
                    viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!.id)
                    viewModel.selectedBalance =
                        BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetTo!!.id }
                    val bundle = Bundle().apply {
                        putString(Constants.ORDER_ID,orderId)
                    }
                    findNavController().navigate(R.id.action_confirmExchangeFragment_to_deatil_fragment
                    ,bundle)

                /* CommonMethods.checkInternet(requireContext()) {
                        showLottieProgressDialog(requireContext(), Constants.LOADING)
                        timer = 0
                        viewModel.confirmOrder(
                            orderId
                        )
                    }*/
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {
            tvNestedAmount.text = getString(R.string.ratio)
            tvNestedAmountValue.text = "1 : " + data!!.ratio
            val priceCoin = viewModel.exchangeAssetFrom!!.balanceData.euroBalance.toDouble()
                .div(viewModel.exchangeAssetFrom!!.balanceData.balance.toDouble() ?: 1.0)
            tvNestedAmountValue.text = "1:"+data!!.ratio
            tvValueLyberFee.text =
                data.fees.formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                ) + data.fromAsset.uppercase()
            tvAmount.text =
                "${data.toAmount.formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                ) } ${data.toAsset.uppercase()}"
            orderId = data.orderId
            tvExchangeFromValue.text =
                "${data.fromAmount} ${data.fromAsset.uppercase()}"
            tvExchangeToValue.text =
                "${data.toAmount.formattedAsset(
                    price = priceCoin,
                    rounding = RoundingMode.DOWN
                )} ${data.toAsset.uppercase()}"

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
        Handler(Looper.getMainLooper()).postDelayed({
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
    }





}