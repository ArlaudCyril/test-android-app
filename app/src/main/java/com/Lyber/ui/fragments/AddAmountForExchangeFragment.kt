package com.Lyber.ui.fragments

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentAddAmountBinding
import com.Lyber.models.CurrentPriceResponse
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.utils.CommonMethods.Companion.fadeIn
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.invisible
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.returnErrorCode
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.CommonMethods.Companion.shake
import com.Lyber.utils.CommonMethods.Companion.showErrorMessage
import com.Lyber.utils.CommonMethods.Companion.showSnack
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
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

class AddAmountForExchangeFragment : BaseFragment<FragmentAddAmountBinding>(),
    View.OnClickListener {

    /* display conversion */
    private val amount get() = binding.etAmount.text!!.trim().toString()

    private var selectedFrequency: String = ""
    private var canPreview: Boolean = false
    private var onMaxClick: Boolean = false

    private var minAmount: Double = 0.0
    private var minPriceExchange: Double = 1.05
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private var maxValue: String = "1.0"
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private lateinit var viewModel: PortfolioViewModel
    var assetAvail = 0.0
    var decimalFrom = 3
    var decimalTo = 3
    private var debounceJob: Job? = null // To hold the debounce coroutine job
    private val debounceDelay = 700L // Delay in milliseconds (0.7 seconds)
    private var exchangeFromPrice: Double = 0.0
    private var exchangeToPrice: Double = 0.0


    override fun bind() = FragmentAddAmountBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filter = IntentFilter()
        filter.addAction("AssestUpdateChanges")
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

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
        binding.btnAddFrequency.setOnClickListener(this)
        binding.rlCardInfo.setOnClickListener(this)
        binding.btnPreviewInvestment.setOnClickListener(this)
        binding.rlSwapFrom.setOnClickListener(this)
        binding.rlSwapTo.setOnClickListener(this)
        binding.ivSwapBetween.setOnClickListener(this)
        binding.ivRepeat.setOnClickListener(this)
        binding.ivMax.setOnClickListener(this)
        binding.rlBalance.gone()

        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                requireActivity().clearBackStack()
            }
        }

        viewModel.getQuoteResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.progress.clearAnimation()
                binding.progress.gone()
                val bundle = Bundle().apply {
                    putString(Constants.DATA_SELECTED, Gson().toJson(it.data))
                    putDouble("FromPrice", exchangeFromPrice)
                    putDouble("ToPrice", exchangeToPrice)
                }
                findNavController().navigate(R.id.confirmExchangeFragment, bundle)

            }
        }
        viewModel.withdrawFiatResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                viewModel.allMyPortfolio = ""
                requireActivity().clearBackStack()
            }
        }
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
        if (viewModel.selectedFrequency.isNotEmpty()) frequencySelected(viewModel.selectedFrequency)



        binding.etAmount.addTextChangedListener(textOnTextChange)


    }

    override fun onResume() {
        super.onResume()
        prepareView()
    }


    @SuppressLint("SetTextI18n")
    private fun prepareView() {
        binding.apply {
            btnAddFrequency.gone()
            ivMax.visible()
            tvAssetConversion.visible()
            tvEuro.visible()
            rlCardInfo.gone()
            btnPreviewInvestment.text = getString(R.string.preview_exchange)
            llSwapLayout.visible()
            val balance =
                com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
            val asset =
                com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetFrom } }

            Log.d("Balance", "$balance")
            if (balance != null) {
                decimalFrom = asset!!.decimals
                mCurrency = " " + balance.id.uppercase()
                tvTitle.text = "Exchange ${balance.id.uppercase()}"
                val priceCoin = balance.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble())
                tvSubTitle.text = "${
                    balance.balanceData.balance.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        decimalFrom
                    ) //removed decimal for now
                } Available"
//                } ${balance.id.uppercase()} Available"
                assetAvail =
                    balance.balanceData.balance.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        decimalFrom
                    ) //removed decimal for now
                        .toDouble()
                val currency =
                    com.Lyber.ui.activities.BaseActivity.assets.find { it1 -> it1.id == balance.id }
                ivAssetSwapFrom.loadCircleCrop(currency!!.imageUrl)
                tvSwapAssetFrom.text = balance.id.uppercase()
                val exchangeFromCoinPrice =
                    (balance.balanceData.euroBalance.toDouble() / balance.balanceData.balance.toDouble())
                minAmount = minPriceExchange / (exchangeFromCoinPrice ?: 1.0)
                tvMinAmount.text = getString(
                    R.string.the_minimum_amount_to_be_exchanged_1,
                    minAmount.toString().decimalPoint(), balance.id.uppercase()
                )
                maxValue = balance.balanceData.balance
            } else {
                val data1 =
                    com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetFrom } }
                mCurrency = " " + data1!!.id.uppercase()
                tvTitle.text = "Exchange ${data1.id.uppercase()}"
                assetAvail = 0.0
                tvSubTitle.text = "0 Available"
                val currency =
                    com.Lyber.ui.activities.BaseActivity.assets.find { it1 -> it1.id == data1.id }
                ivAssetSwapFrom.loadCircleCrop(currency!!.imageUrl)
                tvSwapAssetFrom.text = data1.id.uppercase()
//                val exchangeFromCoinPrice =
//                    (balance.balanceData.euroBalance.toDouble() / balance.balanceData.balance.toDouble())
//            minAmount = (data1.balanceData.balance.toDouble() /balance.balanceData.euroBalance.toDouble())
                minAmount = minPriceExchange / (1.0)
                tvMinAmount.text = getString(
                    R.string.the_minimum_amount_to_be_exchanged_1,
                    minAmount.toString().decimalPoint(), data1.id.uppercase()
                )
                maxValue = "0"
                binding.tvEuro.text = "~0 ${Constants.EURO}"
            }
            val balanceTo =
                com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetTo }
//            tvAssetConversion.text = balanceTo!!.priceServiceResumeData
//                .lastPrice.commaFormatted
            mConversionCurrency = " " + balanceTo!!.id.uppercase()
            val data =
                com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo } }
            decimalTo = data!!.decimals
            Log.d("assetTo", "$data")
            ivAssetSwapTo.loadCircleCrop(data.imageUrl)
            tvSwapAssetTo.text = balanceTo.id.uppercase()
            binding.etAmount.text = "${0.commaFormatted}$mCurrency"
            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency
            tvAssetConversion.text = "0.00${unfocusedData.currency}"
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

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

                btnAddFrequency -> FrequencyModel(::frequencySelected).show(
                    parentFragmentManager, ""
                )

                btnPreviewInvestment -> {

//                    if(etAmount.text.)
                    if (!ivCircularProgress.isVisible && !ivCenterProgress.isVisible)
                        if (canPreview) {
                            if (assetAvail == 0.0)
                                getString(R.string.do_not_have).showToast(
                                    binding.root,
                                    requireActivity()
                                )
                            else
                                hitAPi()
                        }
                }

                rlSwapFrom -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.FROM_SWAP)
                    findNavController().navigate(R.id.exchangeFromFragment, bundle)
                }

                rlSwapTo -> {
//                    val bundle = Bundle()
//                    bundle.putString(Constants.TYPE, Constants.TO_SWAP)
//                    findNavController().navigate(R.id.exchangeFromFragment, bundle)
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.Exchange)
                    bundle.putString(Constants.FROM, AddAmountForExchangeFragment::class.java.name)
                    findNavController().navigate(R.id.allAssetFragment, bundle)
                }
//                    requireActivity().onBackPressedDispatcher.onBackPressed()

                ivSwapBetween, ivRepeat -> {
//                    if (CommonMethods.getBalance(viewModel.exchangeAssetTo!!) != null) {
                    swapConversion()
//                    } else {
//                        getString(R.string.you_don_t_have_balance_to_exchange).showToast(
//                            requireActivity()
//                        )
//                    }
                }

                ivMax -> setMaxValue()

            }
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        CommonMethods.dismissProgressDialog()
        binding.btnPreviewInvestment.text = getString(R.string.preview_exchange)
        when (errorCode) {
            7003 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_7003))
            7026 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_7026))
            7015 -> {
                showSnack(binding.root, requireContext(), getString(R.string.error_code_7015))
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            7000 -> {
                val data1 =
                    com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetFrom } }

                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7000, data1!!.fullName)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7001 -> {
                val data1 =
                    com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo } }

                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7001, data1!!.fullName)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7002 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7002)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7018 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7018)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7019 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7019)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7020 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7020)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7021 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7021)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7022 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7022)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            7024 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7024)
                )
                findNavController().navigate(
                    R.id.action_add_amount_exchange_to_home_fragment

                )
            }

            else -> super.onRetrofitError(errorCode, msg)
        }
    }

    private fun hitAPi() {
        val valueAmount =
            if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
            else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
        val balance =
            com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
        Log.d("Balance", "$balance")

//        val priceCoin = balance!!.balanceData.euroBalance.toDouble()
//            .div(balance.balanceData.balance.toDouble())
//        val maxAmount = balance.balanceData.balance.formattedAsset(priceCoin, RoundingMode.DOWN)

        if (valueAmount <= maxValue.toDouble()) {
            binding.progress.visible()
            binding.progress.animation =
                AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
            binding.btnPreviewInvestment.text = ""
            val jsonObject = JSONObject()
            jsonObject.put("fromAsset", focusedData.currency.lowercase())
            jsonObject.put("toAsset", unfocusedData.currency.lowercase())
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
                    focusedData.currency.lowercase(),
                    unfocusedData.currency.lowercase(),
                    amount.split(focusedData.currency)[0].pointFormat
                )

            }?.addOnFailureListener { exception ->
                Log.d("token", "${exception}")
            }
        } else {
            getString(R.string.insufficient_balance).showToast(binding.root, requireActivity())
        }


    }

    /* bottom sheet callbacks */

//    private fun conversionFormula(): String {
//        val valueAmount =
//            if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
//            else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
//        val balanceFromPrice =
//            com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
//        val balanceToPrice =
//            com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetTo }
//        val valueInEurosFromAsset = balanceFromPrice!!.priceServiceResumeData.lastPrice.toDouble()
//        val valuesInEurosToAsset = balanceToPrice!!.priceServiceResumeData.lastPrice.toDouble()
//        val numberToAssets = (valueAmount * valueInEurosFromAsset) / (valuesInEurosToAsset)
//        val priceCoin = valuesInEurosToAsset
//            .div(numberToAssets)
//        Log.d("FromLast", "$valueInEurosFromAsset")
//        Log.d("FromLast", "$valuesInEurosToAsset")
//        Log.d(
//            "FromLast", "${
//                numberToAssets.toString().formattedAsset(priceCoin, RoundingMode.DOWN, decimalTo)
//            }"
//        )
//
//        return numberToAssets.toString().formattedAsset(priceCoin, RoundingMode.DOWN, decimalTo)
//
//    }

    private fun frequencySelected(
        frequency: String
    ) {
        binding.apply {


            btnAddFrequency.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)
            btnAddFrequency.setBackgroundTint(R.color.purple_gray_50)

            tvAddFrequency.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_calendar_black, 0, R.drawable.ic_drop_down, 0
            )
            tvAddFrequency.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.purple_gray_800
                )
            )
            tvAddFrequency.text = frequency
            selectedFrequency = frequency
        }
    }

    @SuppressLint("SetTextI18n")
    private fun type(char: Char) {


        binding.apply {

            when {

                amount.length == (focusedData.currency.length + 1) && amount[0] == '0' -> {
                    if (char == '.') {
                        if (!amount.contains('.')) etAmount.text = ("0$char${focusedData.currency}")
                    } else etAmount.text = (char + focusedData.currency)
                }

                else -> {

                    val string = amount.substring(0, amount.count() - focusedData.currency.length)

                    if (string.contains('.')) {
                        if (char != '.') {
                            val decimalPart = string.substringAfter('.')
                            if (decimalPart.length < decimalFrom && char.isDigit()) {
                                etAmount.text = "$string$char${focusedData.currency}"
                            }
                        }
//                            etAmount.text = "$string$char${focusedData.currency}" //for now
                    } else {
                        if (char == '.') etAmount.text = ("${
                            string.pointFormat
                        }.${focusedData.currency}")
                        else etAmount.text = ((string.pointFormat
                            .toString() + char) + focusedData.currency)
                    }
                }

            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
            val builder = StringBuilder()

            val value =
                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat
                else amount.split(unfocusedData.currency)[0].pointFormat

            builder.append(value.dropLast(1))
            if (builder.toString().isEmpty()) {
                builder.append("0")
            }

            if (amount.contains(focusedData.currency)) builder.append(focusedData.currency)
            else builder.append(unfocusedData.currency)

            binding.etAmount.text = builder.toString()


        } catch (e: Exception) {
            Log.d(TAG, "backspace: ${e.message}\n${e.localizedMessage}")
        }
    }

    fun activateButton(activate: Boolean) {
        if (activate) binding.tvMinAmount.gone() else binding.tvMinAmount.visible()
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
        canPreview = activate
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
                binding.tvEuro.text = "~0 ${Constants.EURO}"
                debounceJob?.cancel()
                binding.tvAssetConversion.visible()
                binding.tvEuro.visible()
                binding.ivCircularProgress.gone()
                binding.ivCenterProgress.gone()
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
                binding.tvEuro.invisible()
                if (!onMaxClick)
                    binding.ivCircularProgress.visible()
                onMaxClick = false

                debounceJob?.cancel() // Cancel any ongoing debounce job
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(debounceDelay) // Wait for 0.7 seconds
                    fetchPriceAndConvert(input) // Call the function to fetch price and perform conversion
                }
            }

            //todo for trial
//            val valueAmount =
//                try {
//                    if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
//                    else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
//                } catch (e: Exception) {
//                    0.0
//                }
//
//            if (valueAmount >= minAmount) {
//                activateButton(true)
//            }
//            when {
//
//                valueAmount > 0 -> {
//
//                    when (focusedData.currency) {
//
//                        mCurrency -> {
//
//                            val assetAmount = conversionFormula()
//                            viewModel.assetAmount = assetAmount
//
////                            binding.tvAssetConversion.text =
//                         val ts=       "${
//                                    assetAmount.formattedAsset(
//                                        1.03,
//                                        RoundingMode.DOWN,
//                                        decimalTo
//                                    )
//                                }$mConversionCurrency"
////                                "${assetAmount}$mConversionCurrency"
//                            val balanceFromPrice =
//                                com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
////                            binding.tvEuro.text =
//                                val tt="~${
//                                (valueAmount * balanceFromPrice!!.priceServiceResumeData.lastPrice.toDouble()).toString()
//                                    .formattedAsset(0.0, RoundingMode.DOWN, 2)
//                            } ${Constants.EURO}"
//                            Log.d("FromLast", "$ts")
//                            Log.d("FromLast", "$tt")
//                        }
//
//                        else -> {
//
//                            val convertedValue = conversionFormula()
////                            binding.tvAssetConversion.text = "$convertedValue$mCurrency"
////                            binding.tvAssetConversion.text = "${
////                                convertedValue.formattedAsset(1.03, RoundingMode.DOWN, decimalTo)
////                            }$mCurrency"
//                            val balanceFromPrice =
//                                com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
////                            binding.tvEuro.text =
//                                val tt="~${
//                                (valueAmount * balanceFromPrice!!.priceServiceResumeData.lastPrice.toDouble()).toString()
//                                    .formattedAsset(0.0, RoundingMode.DOWN, 2)
//                            } ${Constants.EURO}"
//                            Log.d("FromLast", "$tt")
//
//                        }
//
//                    }
//
//                }
//
//                else -> {
//
//                    activateButton(false)
//
//                    viewModel.assetAmount = "0"
//
////                    binding.tvAssetConversion.text =
////                        if (focusedData.currency == mCurrency) "${"0".commaFormatted}$mConversionCurrency"
////                        else "${"0".commaFormatted}$mCurrency"
//                }
//            }
            //For Trial


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
            try {
                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
                else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
            } catch (e: Exception) {
                0.0
            }
        if (amount.isEmpty()) return // Ignore empty input

        try {
            // Make the API call (example using a suspend function)
            val balanceFromPrice = fetchAssetPrice(viewModel.exchangeAssetFrom.toString())
            val balanceToPrice = fetchAssetPrice(viewModel.exchangeAssetTo.toString())

            // Check if the response is successful
            if (balanceFromPrice.isSuccessful && balanceToPrice.isSuccessful ) {
                val body = balanceToPrice.body()
                val body2 = balanceToPrice.body()
                if (body?.data?.price != null && body2?.data?.price!=null) {
                    exchangeFromPrice=     balanceFromPrice.body()!!.data.price.toDouble() // Assume 'price' is the fetched value
                    exchangeToPrice=    balanceToPrice.body()!!.data.price.toDouble() // Assume 'price' is the fetched value
                    Log.d("FromApi", "$exchangeFromPrice")
                    Log.d("FromApi", "$exchangeToPrice")

                    val numberToAssets = (valueAmount * exchangeFromPrice) / (exchangeToPrice)
                    val priceCoin = exchangeToPrice
                        .div(numberToAssets)
                    val conversion =
                        numberToAssets.toString().formattedAsset(priceCoin, RoundingMode.DOWN, decimalTo)
                    Log.d("FromApi", "$conversion")
                    if (valueAmount >= minAmount) {
                        activateButton(true)
                    }
                    when {

                        valueAmount > 0 -> {

                            when (focusedData.currency) {

                                mCurrency -> {

                                    val assetAmount = conversion
                                    viewModel.assetAmount = assetAmount

                                    binding.tvAssetConversion.text =
                                        "${
                                            assetAmount.formattedAsset(
                                                1.03,
                                                RoundingMode.DOWN,
                                                decimalTo
                                            )
                                        }$mConversionCurrency"
                                    binding.tvEuro.text = "~${
//                                (valueAmount * exchangeToPrice).toString()
                                        (valueAmount * exchangeFromPrice).toString()
                                            .formattedAsset(0.0, RoundingMode.DOWN, 2)
                                    } ${Constants.EURO}"
                                    Log.d("FromApi", "${binding.tvAssetConversion.text}")
                                    Log.d("FromApi", "${binding.tvEuro.text}")
                                    if (valueAmount > maxValue.toDouble()) {
                                        binding.etAmount.visible()
                                        binding.etAmount.shake()
                                        binding.ivCircularProgress.gone()
                                        binding.ivCenterProgress.gone()
                                        binding.tvAssetConversion.visible()
                                        binding.tvEuro.visible()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            setMaxValue()
                                        }, 700)


                                    }

                                }

                                else -> {

//                            val convertedValue = conversionFormula()
                                    val convertedValue = conversion
                                    binding.tvAssetConversion.text = "${
                                        convertedValue.formattedAsset(1.03, RoundingMode.DOWN, decimalTo)
                                    }$mCurrency"
                                    binding.tvEuro.text = "~${
                                        (valueAmount * exchangeFromPrice).toString()
                                            .formattedAsset(0.0, RoundingMode.DOWN, 2)
                                    } ${Constants.EURO}"
                                    Log.d("FromApi", "${binding.tvEuro.text}")
                                }

                            }

                        }

                        else -> {

                            activateButton(false)

                            viewModel.assetAmount = "0"

                            binding.tvAssetConversion.text =
                                if (focusedData.currency == mCurrency) "${"0".commaFormatted}$mConversionCurrency"
                                else "${"0".commaFormatted}$mCurrency"
                        }
                    }
                    if (valueAmount <= maxValue.toDouble()) {
                        binding.ivCircularProgress.gone()
                        binding.ivCenterProgress.gone()
                        binding.tvAssetConversion.visible()
                        binding.tvEuro.visible()
                        binding.etAmount.visible()
                    }


                } else {
                    // Handle case where price is null or not present
                    println("Error: Price data is null")
                    val errorBody = balanceToPrice.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    super.onRetrofitError(errorCode.code, errorCode.error)
                }
            } else {
                // Handle API error responses
                println("Error: API call failed with status code ${balanceToPrice.code()} and message ${balanceToPrice.message()}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error (e.g., show error message)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {
        binding.rlSwapFrom.fadeIn()
        binding.rlSwapTo.fadeIn()
        val toId = viewModel.exchangeAssetTo
        val fromId = viewModel.exchangeAssetFrom
        viewModel.exchangeAssetTo = fromId
        viewModel.exchangeAssetFrom = toId
        decimalFrom = decimalTo.also { decimalTo = decimalFrom }

        binding.tvEuro.text = "~0 ${Constants.EURO}"
        prepareView()
    }

    @SuppressLint("SetTextI18n")
    private fun setMaxValue() {
        if (assetAvail == 0.0) {
            binding.etAmount.text = "0${focusedData.currency}"
            binding.tvEuro.text = "~0 ${Constants.EURO}"
        } else {
            val balance =
                com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom!! }
            val asset =
                com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetFrom } }


            val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            binding.etAmount.invisible()
            binding.tvEuro.invisible()
            onMaxClick = true
            binding.ivCenterProgress.visible()
            binding.tvAssetConversion.invisible()
            binding.etAmount.text = "${
                maxValue.formattedAsset(priceCoin, RoundingMode.DOWN, asset!!.decimals)
            }${focusedData.currency}"

        }

    }

    private val String.pointFormat
        get() = replace(",", "", true)


    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)


    companion object {
        private const val TAG = "AddAmountForExchangeFragment"
    }

}