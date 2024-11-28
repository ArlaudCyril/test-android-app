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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentInvestAddMoneyBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.BalanceData
import com.Lyber.dev.models.CurrentPriceResponse
import com.Lyber.dev.models.Strategy
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.ui.fragments.bottomsheetfragments.FrequencyModel
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.invisible
import com.Lyber.dev.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.math.RoundingMode
import kotlin.math.ceil

class InvestAddMoneyFragment : BaseFragment<FragmentInvestAddMoneyBinding>(), View.OnClickListener {
    private var selectedFrequency: String = ""
    private var mCurrency: String = " ${Constants.MAIN_ASSET_UPPER}"
    private var valueConversion: Double = 1.0
    private var maxValue: Double = 1.0
    private var minInvestPerAsset = 10f
    private var requiredAmount = 0f
    private lateinit var viewModel: PortfolioViewModel
    private var editEnabledStrategy = false
    private var decimal = 2
    private var debounceJob: Job? = null // To hold the debounce coroutine job
    private val debounceDelay = 700L // Delay in milliseconds (0.7 seconds)
    private var onMaxClick: Boolean = false

    private val amount get() = binding.etAmount.text.trim().toString()
    override fun bind() = FragmentInvestAddMoneyBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        if (arguments != null) {
            if (requireArguments().containsKey(Constants.EDIT_ACTIVE_STRATEGY) && requireArguments().getBoolean(
                    Constants.EDIT_ACTIVE_STRATEGY
                )
            ) {
                editEnabledStrategy = true
            } else {
                editEnabledStrategy = false
                binding.btnAddFrequency.visibility = View.GONE
                selectedFrequency = "none"
            }
        }
//        viewModel.getCurrentPrice(Constants.MAIN_ASSET)
//        viewModel.currentPriceResponse.observe(viewLifecycleOwner) {
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                val lastPrice = it.data.price.toDouble()
//                valueConversion = 1.0 / lastPrice
//                Log.d("Conversion Price 2", "$valueConversion")
//            }
//        }
        prepareView()
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
        binding.btnPreviewInvestment.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        binding.ivMax.setOnClickListener(this)
        binding.etAmount.addTextChangedListener(textOnTextChange)

        binding.btnAddFrequency.setOnClickListener {
            FrequencyModel(::frequencySelected).show(
                parentFragmentManager, ""
            )
        }
    }

    private fun prepareView() {
        val selectedAsset =
            com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
        if (selectedAsset != null)
            decimal = selectedAsset.decimals
        for (asset in viewModel.selectedStrategy?.bundle!!) {
            val newAmount = minInvestPerAsset / (asset.share / 100)
            if (newAmount > requiredAmount) {
                requiredAmount = newAmount
            }
        }
        requiredAmount = ceil(requiredAmount)
        if (ceil(requiredAmount) != viewModel.selectedStrategy?.minAmount?.toFloat())
            requiredAmount = viewModel.selectedStrategy?.minAmount?.toFloat()!!
        var balance =
            com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
        if (balance == null) {
            val balanceData = BalanceData("0", "0")
            balance = Balance("0", balanceData)
        }

        "${
            balance.balanceData.balance.formattedAsset(0.0, RoundingMode.DOWN, decimal)
        } USDC Available".also { binding.tvSubTitle.text = it }
//        valueConversion =
//            (balance.balanceData.euroBalance.toDouble() / balance.balanceData.balance.toDouble())
        maxValue = balance.balanceData.balance.toDouble()

        binding.etAmount.text = "0$mCurrency"
        if (editEnabledStrategy) {
            val data = Gson().fromJson<Strategy>(
                requireArguments().getString("data"),
                object :
                    TypeToken<Strategy>() {}.type
            )
            val frequency = when (data.activeStrategy!!.frequency) {
                "1d" -> getString(R.string.daily)
                "1w" -> getString(R.string.weekly)
                else -> getString(R.string.monthly)
            }
            binding.apply {
                etAmount.text = "${
                    data.activeStrategy!!.amount.toString()
                        .formattedAsset(0.0, RoundingMode.DOWN, decimal)
                }$mCurrency"
                val assetAmount =
                    (data.activeStrategy!!.amount!!.toDouble() * valueConversion).toString()
                viewModel.assetAmount = assetAmount

                setAssetAmount(assetAmount)
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
                if (frequency == "none")
                    tvAddFrequency.text = getString(R.string.once)
                else
                    tvAddFrequency.text = frequency
            }
            selectedFrequency = frequency
            activateButton(true)
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
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
                btnPreviewInvestment -> investment()
                ivMax -> {
                    setMaxValue()
                }
            }
        }
    }

    private fun setMaxValue() {
        if (maxValue > 0) {
            binding.etAmount.invisible()
            onMaxClick = true
            binding.ivCenterProgress.visible()
            binding.tvAssetConversion.invisible()
            onMaxClick = true
            binding.etAmount.text = "${
                maxValue.toString().formattedAsset(0.0, RoundingMode.DOWN, decimal)
            }" + mCurrency
            activateButton(true)
        } else {
            binding.etAmount.text = "0" + mCurrency
        }
    }

    private fun activateButton(activate: Boolean) {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }

    private fun investment() {
        val finalAmount = amount.replace(mCurrency, "").pointFormat
        val balance =
            com.Lyber.dev.ui.activities.BaseActivity.balances.find { it1 -> it1.id == Constants.MAIN_ASSET }
        val amount: Float = balance?.balanceData?.balance?.toFloat() ?: 0f

        if (finalAmount.toFloat() < requiredAmount) {
            getString(
                R.string.you_need_to_invest_at_least_per_asset_in_the_strategy,
                requiredAmount.toString(),
                mCurrency.uppercase()
            ).showToast(binding.root, requireActivity())
        } else if (finalAmount.toFloat() > amount) {
            getString(
                R.string.you_don_t_have_enough_to_perform_this_action,
                mCurrency.uppercase()
            ).showToast(binding.root, requireActivity())
        } else if (selectedFrequency.trim().isEmpty()) {
            getString(R.string.please_select_the_frequency).showToast(
                binding.root,
                requireActivity()
            )
        } else {
            viewModel.amount = finalAmount
            viewModel.selectedFrequency = selectedFrequency
            val bundle = Bundle()
            bundle.putString(Constants.AMOUNT, finalAmount)
            bundle.putString(Constants.FREQUENCY, selectedFrequency)
            bundle.putInt(Constants.DECIMAL, decimal)
            bundle.putBoolean(Constants.EDIT_ACTIVE_STRATEGY, editEnabledStrategy)
            viewModel.selectedOption = Constants.USING_STRATEGY
            findNavController().navigate(R.id.confirmInvestmentFragment, bundle)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun type(char: Char) {

        activateButton(true)
        binding.apply {
            val currency = mCurrency
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
                            val decimalPart = string.substringAfter('.')
                            if (decimalPart.length < decimal && char.isDigit()) {
                                etAmount.text = "$string$char$currency"
                            }
                        }
//                            etAmount.text = "$string$char${currency}"
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

    private val String.pointFormat
        get() = replace(",", "", true)

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
            val builder = StringBuilder()

            val value =
                amount.replace(mCurrency, "").pointFormat


            builder.append(value.dropLast(1))
            if (builder.toString().isEmpty()) {
                builder.append("0")
            }

            builder.append(mCurrency)

            binding.etAmount.text = builder.toString()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
            if (frequency == "none")
                tvAddFrequency.text = getString(R.string.once)
            else
                tvAddFrequency.text = frequency
            selectedFrequency = frequency
        }
    }

    private fun setAssetAmount(assetAmount: String) {
        binding.ivCenterProgress.gone()
        binding.ivCircularProgress.gone()
        val valueAmount =
            amount.replace(mCurrency, "").pointFormat.toDouble()

        Log.d("valueAmount", "$valueAmount")
        val priceCoin = valueAmount.toDouble().div(assetAmount.toDouble())
        binding.tvAssetConversion.text =
            "~${assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN, 2)} ${Constants.EURO}"
        binding.etAmount.visible()
        binding.tvAssetConversion.visible()
        if (valueAmount > maxValue) {
            activateButton(false)
//            binding.etAmount.visible()
            binding.etAmount.shake()
            binding.ivCircularProgress.gone()
            binding.ivCenterProgress.gone()
            binding.tvAssetConversion.visible()
            Handler(Looper.getMainLooper()).postDelayed({
                setMaxValue()
            }, 700)
        }
    }

    private val textOnTextChange = object : OnTextChange {
        @SuppressLint("SetTextI18n")
        override fun onTextChange() {
            val valueAmount = amount.replace(mCurrency, "").pointFormat.toDouble()

            if (valueAmount == 0.0) {
                activateButton(false)
                debounceJob?.cancel()
                binding.tvAssetConversion.text = "~0 ${Constants.EURO}"
                binding.tvAssetConversion.visible()
                binding.ivCircularProgress.gone()
                binding.ivCenterProgress.gone()

            } else {
                val input = amount.toString().trim()
                binding.tvAssetConversion.invisible()
//                binding.tvAssetFees.invisible()
                if (!onMaxClick)
                    binding.ivCircularProgress.visible()
                onMaxClick = false

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

    fun View.shake() {
        val keyframe1 = Keyframe.ofFloat(0f, 0f)
        val keyframe2 = Keyframe.ofFloat(0.25f, 30f)
        val keyframe3 = Keyframe.ofFloat(0.5f, -30f)
        val keyframe4 = Keyframe.ofFloat(0.75f, 30f)
        val keyframe5 = Keyframe.ofFloat(1f, 0f)

        val propertyValuesHolder = PropertyValuesHolder.ofKeyframe(
            "translationX",
            keyframe1,
            keyframe2,
            keyframe3,
            keyframe4,
            keyframe5
        )
        val animator = ObjectAnimator.ofPropertyValuesHolder(this, propertyValuesHolder)
        animator.duration = 500 // Duration in milliseconds
        animator.start()
    }

    private suspend fun fetchPriceAndConvert(amount: String) {
        val valueAmount = amount.replace(mCurrency, "").pointFormat.toDouble()

        if (amount.isEmpty()) return // Ignore empty input

        try {
            val balanceToPrice = fetchAssetPrice(Constants.MAIN_ASSET)

            // Check if the response is successful
            if (balanceToPrice.isSuccessful) {
                val body = balanceToPrice.body()

                if (body?.data?.price != null) {
                    val price = body.data.price.toDouble()
                    valueConversion = 1 / price

                    when {
                        valueAmount > 0 -> {
                            val assetAmount = (valueAmount * valueConversion).toString()
                            viewModel.assetAmount = assetAmount
                            setAssetAmount(assetAmount)
                        }

                        else -> {
                            activateButton(false)
                            viewModel.assetAmount = "0"
                            setAssetAmount("0")
                        }
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


}