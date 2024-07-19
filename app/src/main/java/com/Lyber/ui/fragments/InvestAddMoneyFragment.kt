package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentInvestAddMoneyBinding
import com.Lyber.models.Balance
import com.Lyber.models.BalanceData
import com.Lyber.models.Strategy
import com.Lyber.models.TransactionData
import com.Lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.round

class InvestAddMoneyFragment : BaseFragment<FragmentInvestAddMoneyBinding>(), View.OnClickListener {
    private var selectedFrequency: String = ""
    private var mCurrency: String = " ${Constants.MAIN_ASSET_UPPER}"
    private var valueConversion: Double = 1.0
    private var minInvestPerAsset = 10f
    private var requiredAmount = 0f
    private lateinit var viewModel: PortfolioViewModel
    private var editEnabledStrategy = false
    private var decimal = 2
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
            com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
        if(selectedAsset!=null)
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
            com.Lyber.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
        if (balance == null) {
            val balanceData = BalanceData("0", "0")
            balance = Balance("0", balanceData)
        }

        "${
            balance.balanceData.balance.formattedAsset(0.0, RoundingMode.DOWN, decimal)
        } USDC Available".also { binding.tvSubTitle.text = it }
        valueConversion =
            (balance.balanceData.euroBalance.toDouble() / balance.balanceData.balance.toDouble())

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
                etAmount.text = "${data.activeStrategy!!.amount.toString().formattedAsset(0.0, RoundingMode.DOWN, decimal)
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
                    var balance =
                        com.Lyber.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == Constants.MAIN_ASSET } }
                    if (balance == null) {
                        val balanceData = BalanceData("0", "0")
                        balance = Balance("0", balanceData)
                    }
                    var maxValue = balance.balanceData.balance.toDouble()
                    if (maxValue > 0) {
                        binding.etAmount.text = "${
                            maxValue.toString().formattedAsset(0.0, RoundingMode.DOWN, decimal)
                        }" + mCurrency
                        activateButton(true)
                    } else {
                        binding.etAmount.text = "0" + mCurrency
                    }
                }
            }
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
            com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == Constants.MAIN_ASSET }
        val amount: Float = balance?.balanceData?.balance?.toFloat() ?: 0f

        if (finalAmount.toFloat() < requiredAmount) {
            getString(
                R.string.you_need_to_invest_at_least_per_asset_in_the_strategy,
                requiredAmount.toString(),
                mCurrency.uppercase()
            ).showToast(requireActivity())
        } else if (finalAmount.toFloat() > amount) {
            getString(
                R.string.you_don_t_have_enough_to_perform_this_action,
                mCurrency.uppercase()
            ).showToast(requireActivity())
        } else if (selectedFrequency.trim().isEmpty()) {
            getString(R.string.please_select_the_frequency).showToast(requireActivity())
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
        val valueAmount =
            amount.replace(mCurrency, "").pointFormat.toDouble()

        Log.d("valueAmount", "$valueAmount")
        val priceCoin = valueAmount.toDouble().div(assetAmount.toDouble())
        binding.tvAssetConversion.text =
            "~${assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN, 2)} ${Constants.EURO}"
    }

    private val textOnTextChange = object : OnTextChange {
        @SuppressLint("SetTextI18n")
        override fun onTextChange() {
            val valueAmount = amount.replace(mCurrency, "").pointFormat.toDouble()


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

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }
}