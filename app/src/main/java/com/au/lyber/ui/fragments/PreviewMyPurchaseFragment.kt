package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentMyPurchaseBinding
import com.au.lyber.models.Balance
import com.au.lyber.models.BalanceData
import com.au.lyber.models.DataQuote
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.ErrorBottomSheet
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.google.gson.Gson
import com.stripe.android.PaymentConfiguration
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import java.math.RoundingMode

class PreviewMyPurchaseFragment : BaseFragment<FragmentMyPurchaseBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private var timer = 25
    private var isExpand = false
    private lateinit var clientSecret: String
    private var orderId: String = ""
    private lateinit var googlePayLauncher: GooglePayLauncher
    private lateinit var viewModel: PortfolioViewModel
    private var isTimerRunning = false
    private lateinit var handler:Handler
    override fun bind() = FragmentMyPurchaseBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PaymentConfiguration.init(requireActivity(), "pk_test_51NVVY7F2A3romcuHdC3JDD9evsFhQvyZ5cYS6wpy9OznXgmYzLvWTG81Zfj2nWGQFZ2zs8RboA3uMLCNPpPV08Zk00McUdiPAt")

        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.tvMoreDetails.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        handler = Handler(Looper.getMainLooper())
         googlePayLauncher = GooglePayLauncher(
            fragment = this@PreviewMyPurchaseFragment,
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Test,
                merchantCountryCode = "US",
                merchantName = "Widget Store",
                        existingPaymentMethodRequired = false
            ),
            readyCallback = ::onGooglePayReady,
            resultCallback = ::onGooglePayResult
        )
        viewModel.getQuoteResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                val bundle = Bundle().apply {
                    putString(Constants.DATA_SELECTED, Gson().toJson(it.data))
                }
                this.arguments = bundle
                getData()
            }
        }
        getData()
    }

    private fun onGooglePayReady(isReady: Boolean) {
     //   binding.btnConfirmInvestment.isEnabled = isReady
        // implemented below
        Log.d("isGpayReady","$isReady")
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                stopTimer()
                viewModel.selectedAsset = CommonMethods.getAsset("usdt")
                val bundle = Bundle().apply {
                    putString(Constants.ORDER_ID,orderId)
                    putString(Constants.FROM_SWAP,PreviewMyPurchaseFragment::class.java.name)
                }
                findNavController().navigate(R.id.action_preview_my_purchase_to_detail_fragment
                    ,bundle)
            }
            GooglePayLauncher.Result.Canceled -> {
                // User canceled the operation
                Log.d("isGpayReady","Cancelled")
            }
            is GooglePayLauncher.Result.Failed -> {
                // Operation failed; inspect `result.error` for the exception
                Log.d("isGpayReady","${result.error}")
                result.error.showToast(requireContext())
            }
        }
    }


    private fun getData() {


        if (arguments!=null && requireArguments().containsKey(Constants.DATA_SELECTED)){
            val data = Gson().fromJson(requireArguments().getString(Constants.DATA_SELECTED),
                DataQuote::class.java)
            prepareView(data)
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> {
                    stopTimer()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                btnConfirmInvestment -> {
                    if(CommonMethods.isAppInstalled(requireActivity(),"com.google.android.apps.nbu.paisa.user"))
                    googlePayLauncher.presentForPaymentIntent(clientSecret)
                    else
                        getString(R.string.you_must_install_gpay).showToast(requireContext())
                }
                tvMoreDetails->{
                    if (isExpand){
                        zzInfor.gone()
                        isExpand = false
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_right_arrow_grey,0,0,0)
                    }else{
                        zzInfor.visible()
                        isExpand = true
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_drop_down,0,0,0)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {
            clientSecret = data!!.clientSecret
            orderId = data!!.orderId
            var balance =
                BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == "usdt"} }
            if (balance == null) {
                val balanceData = BalanceData("0", "0")
                balance = Balance("0", balanceData)
            }
            val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            tvValueDepositFee.text = data.fees+" "+Constants.EURO
            tvValueTotal.text = "${data.fromAmount+" "+ Constants.EURO}"
            tvValuePrice.text = data.ratio.formattedAsset(priceCoin,RoundingMode.DOWN)+" "+Constants.EURO
            tvTotalAmount.text = "${data.fromAmount+" "+ Constants.EURO}"
            tvValueDeposit.text = ""+(data.fromAmount.toDouble() - data.fees.toDouble())+" "+Constants.EURO
            tvAmount.text = "${data.toAmount.formattedAsset(priceCoin,RoundingMode.DOWN)+" "+ "USDT"}"
            btnConfirmInvestment.isEnabled = true
            timer = ((data.validTimestamp.toLong() - System.currentTimeMillis())/1000).toInt()

            handler.removeCallbacks(runnable)
            startTimer()
            title.text = getString(R.string.confirm_purchase)


        }
    }


    private fun startTimer() {

        try {
            handler.postDelayed(runnable, 1000)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private val runnable = Runnable {
        isTimerRunning = true
        if (timer == 0) {

            ErrorBottomSheet(::dismissList).show(childFragmentManager, "")


        } else {
            timer -= 1
            if (timer>0) {
                binding.tvTimer.text =
                    getString(R.string.you_have_seconds_to_confirm_this_purchase, timer.toString())
            }else{
                binding.tvTimer.text =
                    getString(R.string.you_have_seconds_to_confirm_this_purchase, "0")
            }
            startTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false
    }

    private fun dismissList(clicked:Boolean){
        if (clicked){
            checkInternet(requireActivity()) {
                val data = Gson().fromJson(
                    requireArguments().getString(Constants.DATA_SELECTED),
                    DataQuote::class.java
                )
                CommonMethods.showProgressDialog(requireActivity())
                viewModel.getQuote(
                    "eur",
                    "usdt",
                    data.fromAmount
                )
            }
        }
    }




}