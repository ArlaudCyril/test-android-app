package com.Lyber.dev.ui.fragments

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentBuyUsdtBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.BalanceData
import com.Lyber.dev.models.CurrentPriceResponse
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.invisible
import com.Lyber.dev.utils.CommonMethods.Companion.returnErrorCode
import com.Lyber.dev.utils.CommonMethods.Companion.showErrorMessage
import com.Lyber.dev.utils.CommonMethods.Companion.showSnack
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.math.RoundingMode

class BuyUSDTFragment : BaseFragment<FragmentBuyUsdtBinding>(), View.OnClickListener {
    override fun bind() = FragmentBuyUsdtBinding.inflate(layoutInflater)
    private var valueConversion: Double = 1.0
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private lateinit var viewModel: PortfolioViewModel
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private lateinit var from: String
    private var debounceJob: Job? = null // To hold the debounce coroutine job
    private val debounceDelay = 700L // Delay in milliseconds (0.7 seconds)

    private val amount get() = binding.etAmount.text.trim().toString()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        if (arguments != null && requireArguments().containsKey(Constants.FROM))
            from = requireArguments().getString(Constants.FROM).toString()
        binding.tvBackArrow.setOnClickListener(this)
        binding.tvDot.setOnClickListener(this)
        binding.tvZero.setOnClickListener(this)
        binding.tvOne.setOnClickListener(this)
        binding.tvTwo.setOnClickListener(this)
        binding.tvThree.setOnClickListener(this)
        binding.tvFour.setOnClickListener(this)
        binding.tvFive.setOnClickListener(this)
        binding.tvSix.setOnClickListener(this)
        binding.tvSeven.setOnClickListener(this)
        binding.tvEight.setOnClickListener(this)
        binding.tvNine.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        binding.ivRepeat.setOnClickListener(this)
        binding.btnPreviewInvestment.setOnClickListener(this)
        prepareView()
        binding.etAmount.addTextChangedListener(textOnTextChange)
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
        viewModel.getQuoteResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.progress.clearAnimation()
                binding.progress.gone()
                val bundle = Bundle().apply {
                    putString(Constants.DATA_SELECTED, Gson().toJson(it.data))
                    if (::from.isInitialized)
                        putString(Constants.FROM, from)
                }
                findNavController().navigate(R.id.previewMyPurchaseFragment, bundle)
            }
        }
    }


    private val textOnTextChange = object : OnTextChange {


        @SuppressLint("SetTextI18n")
        override fun onTextChange() {
            val valueAmount =
                try {
                    if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
                    else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
                } catch (e: Exception) {
                    0.0
                }
            if (valueAmount == 0.0) {
                debounceJob?.cancel()
                binding.tvAssetConversion.visible()
                binding.ivCircularProgress.gone()
                activateButton(false)
                when (focusedData.currency) {
                    mCurrency -> {
                        binding.tvAssetConversion.text = "0$mConversionCurrency"
                    }

                    else -> {
                        binding.tvAssetConversion.text = "0$mCurrency"
                    }
                }
            } else {
                val input = amount.toString().trim()
                binding.tvAssetConversion.invisible()
                binding.ivCircularProgress.visible()
                debounceJob?.cancel() // Cancel any ongoing debounce job
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(debounceDelay) // Wait for 0.7 seconds
                    fetchPriceAndConvert(input) // Call the function to fetch price and perform conversion
                }
            }

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    private suspend fun fetchAssetPrice(assetId: String): Response<CurrentPriceResponse> {
        // Example API call using a suspend function (use Retrofit, OkHttp, etc.)
        val apiService = RestClient.get() // Replace with your actual API service
        return apiService.getCurrentPrice(assetId)
    }


    private suspend fun fetchPriceAndConvert(amount: String) {
        val valueAmount =
            if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
            else amount.replace(mConversionCurrency, "").pointFormat.toDouble()

        if (amount.isEmpty()) return // Ignore empty input

        try {
            // Make the API call (example using a suspend function)
            val balanceFromPrice = fetchAssetPrice(Constants.MAIN_ASSET)

            // Check if the response is successful
            if (balanceFromPrice.isSuccessful) {
                val body = balanceFromPrice.body()
                if (body?.data?.price != null) {
                    val price = body.data.price.toDouble()
                    valueConversion = 1 / price
                    when {
                        valueAmount > 0 -> {
                            if (focusedData.currency.contains(mCurrency)) {
                                val assetAmount = (valueAmount * valueConversion).toString()
                                viewModel.assetAmount = assetAmount
                                setAssetAmount(assetAmount)
                            } else {
                                val convertedValue = valueAmount / valueConversion
                                setAssetAmount(convertedValue.toString())
                            }
                        }

                        else -> {

                            activateButton(false)

                            viewModel.assetAmount = "0"

                            setAssetAmount("0")
                        }
                    }
                    if (focusedData.currency.contains(mCurrency)) {
                        val valueAmountNew =
                            if (assetConversion.contains(mCurrency)) assetConversion.replace(
                                mCurrency,
                                ""
                            )
                                .replace("~", "").pointFormat.toDouble()
                            else assetConversion.replace(mConversionCurrency, "")
                                .replace("~", "").pointFormat.toDouble()
                        activateButton(true)
                    } else {
                        activateButton(true)
                    }

                } else {
                    // Handle case where price is null or not present
                    println("Error: Price data is null")
                    val errorBody = balanceFromPrice.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    super.onRetrofitError(errorCode.code, errorCode.error)
                }
            } else {
                // Handle API error responses
                println("Error: API call failed with status code ${balanceFromPrice.code()} and message ${balanceFromPrice.message()}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error (e.g., show error message)
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        binding.btnPreviewInvestment.text = getString(R.string.preview_my_purchase)
        when (errorCode) {
            7026 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_7026))
            7027 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_7026))
            7000 -> {
                showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7000, "USDC")
                )
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7001 -> {
                showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7001, "USDC")
                )
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7002 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7002))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7014 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7014))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7015 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7015))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7018 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7018))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7019 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7019))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7020 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7020))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7021 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7021))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7022 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7022))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            7024 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7024))
                findNavController().navigate(R.id.action_buy_usdc_to_home_fragment)
            }

            else -> super.onRetrofitError(errorCode, msg)

        }
    }

    private fun setAssetAmount(assetAmount: String) {
        binding.ivCircularProgress.gone()

        val valueAmount =
            if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
            else amount.replace(mConversionCurrency, "").pointFormat.toDouble()

        if (focusedData.currency.contains(mCurrency)) {
            val priceCoin = valueAmount.toDouble()
                .div(assetAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN)} $mConversionCurrency"
        } else {
            val priceCoin = assetAmount.toDouble()
                .div(valueAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN)} $mCurrency"
        }
        binding.etAmount.visible()
        binding.tvAssetConversion.visible()

    }

    fun activateButton(activate: Boolean) {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }

    private fun prepareView() {

        binding.apply {
            listOf(
                tvTitle,
                etAmount,
                tvAssetConversion,
                ivRepeat
            ).visible()
            binding.etAmount.text = "0€"

            mCurrency = Constants.EURO
            mConversionCurrency = Constants.MAIN_ASSET.uppercase()
            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency

            "${getString(R.string.buy)} ${Constants.MAIN_ASSET.uppercase()}".also {
                tvTitle.text = it
            }
            var balance =
                com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
            if (balance == null) {
                val balanceData = BalanceData("0", "0")
                balance = Balance("0", balanceData)
            }


            setAssetAmount("0.0")

            viewModel.selectedNetworkDeposit.let {
                var balance =
                    com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
                if (balance == null) {
                    val balanceData = BalanceData("0", "0")
                    balance = Balance("0", balanceData)
                }


            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                ivTopAction -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                tvBackArrow -> backspace()
                tvDot -> type('.')
                tvOne -> type('1')
                tvTwo -> type('2')
                tvThree -> type('3')
                tvFour -> type('4')
                tvFive -> type('5')
                tvSix -> type('6')
                tvSeven -> type('7')
                tvEight -> type('8')
                tvNine -> type('9')
                tvZero -> type('0')
                ivRepeat -> swapConversion()

                btnPreviewInvestment -> {

                    hitAPi()
                }
            }
        }
    }

    private fun hitAPi() {
        binding.progress.visible()
        binding.progress.animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
        binding.btnPreviewInvestment.text = ""
        val jsonObject = JSONObject()
        jsonObject.put("fromAsset", "eur")
        jsonObject.put("toAsset", Constants.MAIN_ASSET)
        jsonObject.put("fromAmount", amount.split(focusedData.currency)[0].pointFormat)
        val jsonString = jsonObject.toString()
        // Generate the request hash
        val requestHash = CommonMethods.generateRequestHash(jsonString)
        val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
            SplashActivity.integrityTokenProvider?.request(
                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                    .setRequestHash(requestHash)
                    .build()
            )
        integrityTokenResponse1?.addOnSuccessListener { response ->
            Log.d("token", "${response.token()}")
            viewModel.getQuote(
                "eur",
                Constants.MAIN_ASSET,
                amount.split(focusedData.currency)[0].pointFormat
            )

        }?.addOnFailureListener { exception ->
            Log.d("token", "${exception}")
        }


    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
            val builder = StringBuilder()

            val value =
                if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat
                else amount.replace(mConversionCurrency, "").pointFormat

            builder.append(value.dropLast(1))
            if (builder.toString().isEmpty()) {
                builder.append("0")
            }

            if (amount.contains(mCurrency)) builder.append(mCurrency)
            else builder.append(mConversionCurrency)

            binding.etAmount.text = builder.toString()


        } catch (e: Exception) {
            Log.d(TAG, "backspace: ${e.message}\n${e.localizedMessage}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun type(char: Char) {


        binding.apply {
            val currency =
                if (focusedData.currency.contains(mCurrency)) mCurrency else mConversionCurrency
            when {

                amount.length == (currency.length + 1) && amount[0] == '0' -> {
                    if (char == '.') {
                        if (!amount.contains('.')) etAmount.text = ("0$char${currency}")
                    } else etAmount.text = (char + currency)
                }

                else -> {

                    val string = amount.substring(0, amount.count() - currency.length)


                    if (string.contains('.')) {
                        if (char != '.') {
                            //                     etAmount.text = "$string$char${currency}"
                            val decimalPart = string.substringAfter('.')
                            if (decimalPart.length < 2 && char.isDigit()) {
                                etAmount.text = "$string$char$currency"
                            }
                        }

                    } else {
                        if (char == '.') etAmount.text = ("${
                            string.pointFormat
                        }.${currency}")
                        else etAmount.text = ((string.pointFormat
                            .toString() + char) + currency)
                    }
                }

            }
        }


    }

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {
        binding.tvAssetConversion.invisible()
        binding.ivCircularProgress.visible()
        if (focusedData.currency.contains(mCurrency)) {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mCurrency, "").pointFormat.decimalPoint()
            val valueTwo = assetConversion.replace(mConversionCurrency, "")
                .replace("~", "").pointFormat.decimalPoint()
            binding.etAmount.text = ("${valueTwo}$mConversionCurrency")

//            setAssetAmount(valueOne.toString())
        } else {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mConversionCurrency, "").pointFormat.decimalPoint()
            val valueTwo = assetConversion.replace(mCurrency, "")
                .replace("~", "").pointFormat.decimalPoint()

            binding.etAmount.text = ("${valueTwo}$mCurrency")

//            setAssetAmount(valueOne.toString())
        }


    }

    companion object {
        private const val TAG = "WithdrawAmountFragment"
    }

    private val String.pointFormat
        get() = replace(",", "", true)

    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)


}