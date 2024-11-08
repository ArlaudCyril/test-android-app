package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentMyPurchaseBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.BalanceData
import com.Lyber.dev.models.DataQuote
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.fragments.bottomsheetfragments.ErrorBottomSheet
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAssetForInverseRatio
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.button.ButtonConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.gson.Gson
import com.stripe.android.PaymentConfiguration
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale


class PreviewMyPurchaseFragment : BaseFragment<FragmentMyPurchaseBinding>(),
    View.OnClickListener {
    private var timer = 25
    private var isExpand = false
    private lateinit var clientSecret: String
    private var orderId: String = ""
    private lateinit var googlePayLauncher: GooglePayLauncher
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var fromFragment: String
    private var isTimerRunning = false
    private lateinit var handler: Handler
    private var isGpayInstalled = false
    var isGpayHit = false
    var isApiHit = false
    val hashMap: HashMap<String, Any> = hashMapOf()

    override fun bind() = FragmentMyPurchaseBinding.inflate(layoutInflater)


    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put(
                "parameters", JSONObject()
                    .put("allowedAuthMethods", allowedCardAuthMethods)
                    .put("allowedCardNetworks", JSONArray(listOf("*")))
                    .put("billingAddressRequired", false)
            )

    private val allowedCardNetworks = JSONArray(
        listOf(
            "AMEX",
            "DISCOVER",
            "INTERAC",
            "JCB",
            "MASTERCARD",
            "VISA"
        )
    )

    private val allowedCardAuthMethods = JSONArray(
        listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS"
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PaymentConfiguration.init(
            requireActivity(), Constants.STRIKE_KEY
        )

        val payButton: PayButton = binding.googlePayPaymentButton
        val paymentMethods: JSONArray = JSONArray().put(baseCardPaymentMethod())
        payButton.initialize(
            ButtonOptions.newBuilder()
                .setButtonType(ButtonConstants.ButtonType.BUY)
                .setCornerRadius(24)
                .setAllowedPaymentMethods(paymentMethods.toString())
                .build()
        )
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this



        binding.ivTopAction.setOnClickListener(this)
        binding.tvMoreDetails.setOnClickListener(this)
//        Handler(Looper.getMainLooper()).postDelayed({ binding.googlePayPaymentButton.visible() }, 1000)


        handler = Handler(Looper.getMainLooper())
        googlePayLauncher = GooglePayLauncher(
            fragment = this@PreviewMyPurchaseFragment,
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Test, //  change environment
                merchantCountryCode = "FR",
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
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                isApiHit = false
                CommonMethods.dismissProgressDialog()
            }
        }

        getData()
        payButton.setOnClickListener {
            if (isGpayInstalled) {
                isGpayHit = true
                googlePayLauncher.presentForPaymentIntent(clientSecret)
            } else
                getString(R.string.you_must_install_gpay).showToast(binding.root,requireContext())
        }
    }

    private fun onGooglePayReady(isReady: Boolean) {
        //   binding.btnConfirmInvestment.isEnabled = isReady
        // implemented below
        Log.d("isGpayReady", "$isReady")
        if (isReady)
            isGpayInstalled = true
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                Log.d("GooglePayLauncher", "$isGpayHit")
                stopTimer()
                isGpayHit = false
                viewModel.selectedAsset = CommonMethods.getAsset(Constants.MAIN_ASSET)
                val eventValues = HashMap<String, Any>()
                eventValues[AFInAppEventParameterName.CONTENT_TYPE] = Constants.APP_FLYER_TYPE_GPAY
                eventValues[AFInAppEventParameterName.CONTENT_ID] = orderId
                AppsFlyerLib.getInstance().logEvent( requireContext().applicationContext,
                    AFInAppEventType.PURCHASE, eventValues,
                    object : AppsFlyerRequestListener {
                        override fun onSuccess() {
                            Log.d("LOG_TAG", "Event sent successfully")
                        }
                        override fun onError(errorCode: Int, errorDesc: String) {
                            Log.d("LOG_TAG", "Event failed to be sent:\n" +
                                    "Error code: " + errorCode + "\n"
                                    + "Error description: " + errorDesc)
                        }
                    })
                val bundle = Bundle().apply {
                    putString(Constants.ORDER_ID, orderId)
                    putString(Constants.FROM_SWAP, PreviewMyPurchaseFragment::class.java.name)
                }
                findNavController().navigate(
                    R.id.action_preview_my_purchase_to_detail_fragment, bundle
                )
//                result.
//                val paymentMethodId = result.paymentMethod.id
            }

            GooglePayLauncher.Result.Canceled -> {
                // User canceled the operation
                isGpayHit = false
                Log.d("isGpayReady", "Cancelled")
            }

            is GooglePayLauncher.Result.Failed -> {
                // Operation failed; inspect `result.error` for the exception
                isGpayHit = false
//                val paymentMethodId = result.paymentMethod.id
                Log.d("isGpayReady", "${result.error}")
//                result.error.showToast(requireContext())
            }
        }
    }


    private fun getData() {


        if (arguments != null && requireArguments().containsKey(Constants.DATA_SELECTED)) {
            val data = Gson().fromJson(
                requireArguments().getString(Constants.DATA_SELECTED),
                DataQuote::class.java
            )
            prepareView(data)
            hashMap.clear()
            hashMap["paymentIntentId"] = data.paymentIntentId
            hashMap["orderId"] = data.orderId
            hashMap["userUuid"] = App.prefsManager.user?.uuid.toString()
            if (requireArguments().containsKey(Constants.FROM))
                fromFragment = requireArguments().getString(Constants.FROM).toString()
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
                    if (isGpayInstalled) {
                        isGpayHit = true
                        googlePayLauncher.presentForPaymentIntent(clientSecret)
                    } else
                        getString(R.string.you_must_install_gpay).showToast(binding.root,requireContext())
                }

                tvMoreDetails -> {
                    if (isExpand) {
                        zzInfor.gone()
                        isExpand = false
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_right_arrow_grey,
                            0,
                            0,
                            0
                        )
                    } else {
                        zzInfor.visible()
                        isExpand = true
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_drop_down,
                            0,
                            0,
                            0
                        )
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
                com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
            if (balance == null) {
                val balanceData = BalanceData("0", "0")
                balance = Balance("0", balanceData)
            }
            val priceCoin = balance.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            tvValueDepositFee.text =
                BigDecimal.valueOf(data.fromAmount.toDouble())
                    .subtract(BigDecimal.valueOf(data.fromAmountDeductedFees.toDouble()))
                    .toString()
//               (data.fromAmount.toDouble() - data.fromAmountDeductedFees.toDouble()).toString().formattedAsset(priceCoin, RoundingMode.DOWN,8)+Constants.EURO
//                data.fees + Constants.EURO TODO
            tvValueTotal.text = "${data.fromAmount}${Constants.EURO}"
            tvValuePrice.text = data.inverseRatio.formattedAssetForInverseRatio(
                priceCoin,
                RoundingMode.DOWN
            ) + Constants.EURO
//            tvTotalAmount.text =  "${data.fromAmount+" "+ Constants.EURO}"
            tvTotalAmount.text =
                "${String.format(Locale.US, "%.2f", data.fromAmount.toFloat()) + Constants.EURO}"
            tvValueDeposit.text = data.fromAmountDeductedFees + Constants.EURO
//                "" + (data.fromAmount.toDouble() - data.fees.toDouble()) + Constants.EURO TODO
            tvAmount.text = "${
                data.toAmount.formattedAsset(
                    priceCoin,
                    RoundingMode.DOWN
                ) + Constants.MAIN_ASSET_UPPER
            }"
            btnConfirmInvestment.isEnabled = true
            timer =
                ((data.validTimestamp.toLong() - System.currentTimeMillis()) / 1000).toInt()
            Log.d("time", "$timer")
            handler.removeCallbacks(runnable)
            binding.googlePayPaymentButton.visible()
            startTimer()
            title.text = getString(R.string.confirm_purchase)


        }
    }


    private fun startTimer() {

        try {
            handler.postDelayed(runnable, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private val runnable = Runnable {
        isTimerRunning = true
        if (timer == 0) {
            try {
                val errorBottomSheet = ErrorBottomSheet(::dismissList)
                if (::fromFragment.isInitialized)
                    errorBottomSheet.arguments = Bundle().apply {
                        putString(Constants.FROM, fromFragment)
                    }
                if (isGpayHit)
                    errorBottomSheet.show(childFragmentManager, "GpaySheet")
                else
                    errorBottomSheet.show(childFragmentManager, "")
            } catch (_: Exception) {

            }
        } else {
            timer -= 1
//            Log.d("timer", "$timer")
            if (timer < 2) {
                if (!isApiHit) {
                    CommonMethods.checkInternet(binding.root,requireContext()) {
                        isApiHit = true
//                        CommonMethods.showProgressDialog(requireActivity())
                        viewModel.cancelQuote(hashMap)
//                        val jsonObject = JSONObject()
//                        jsonObject.put("paymentIntentId", hashMap["paymentIntentId"])
//                        jsonObject.put("orderId",hashMap["orderId"])
//                        jsonObject.put("userUuid", App.prefsManager.user?.uuid.toString())
//                        val jsonString = jsonObject.toString()
//                        // Generate the request hash
//                        val requestHash = CommonMethods.generateRequestHash(jsonString)
//                        val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
//                            SplashActivity.integrityTokenProvider?.request(
//                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
//                                    .setRequestHash(requestHash)
//                                    .build()
//                            )
//                        integrityTokenResponse1?.addOnSuccessListener { response ->
//                            Log.d("token", "${response.token()}")
//                            viewModel.cancelQuote(hashMap)
//
//                        }?.addOnFailureListener { exception ->
//                            Log.d("token", "${exception}")
//
//                        }
                    }
                }
            }
            if (timer > 0) {
                binding.tvTimer.text =
                    getString(R.string.you_have_seconds_to_confirm_this_purchase, timer.toString())
            } else {
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
        if(isAdded) {
            handler.removeCallbacks(runnable)
            isTimerRunning = false
        }
    }

    private fun dismissList(clicked: Boolean) {
        if (clicked) {
            checkInternet(binding.root,requireActivity()) {
                val data = Gson().fromJson(
                    requireArguments().getString(Constants.DATA_SELECTED),
                    DataQuote::class.java
                )
                CommonMethods.showProgressDialog(requireActivity())
//                val jsonObject = JSONObject()
//                jsonObject.put("fromAsset","eur")
//                jsonObject.put("toAsset",Constants.MAIN_ASSET)
//                jsonObject.put("fromAmount",data.fromAmount)
//                val jsonString = jsonObject.toString()
//                // Generate the request hash
//                val requestHash = CommonMethods.generateRequestHash(jsonString)
//                val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
//                    SplashActivity.integrityTokenProvider?.request(
//                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
//                            .setRequestHash(requestHash)
//                            .build()
//                    )
//                integrityTokenResponse1?.addOnSuccessListener { response ->
//                    Log.d("token", "${response.token()}")
                    viewModel.getQuote(
                        "eur",
                        Constants.MAIN_ASSET,
                        data.fromAmount
                    )
//
//                }?.addOnFailureListener { exception ->
//                    Log.d("token", "${exception}")
//                }
            }
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        super.onRetrofitError(errorCode, msg)
        CommonMethods.dismissProgressDialog()
        dismissAlertDialog()
        if (isApiHit)
            isApiHit = false

//        if(code==7023 || code == 10041)
//            customDialog(code)
    }
}