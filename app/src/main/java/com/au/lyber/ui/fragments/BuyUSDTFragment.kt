package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentBuyUsdtBinding
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import java.math.RoundingMode

class BuyUSDTFragment : BaseFragment<FragmentBuyUsdtBinding>(), View.OnClickListener {
    private var selectedFrequency: String = ""
    private var valueConversion: Double = 1.0
    private var mCurrency: String = " USDT"
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private var mConversionCurrency: String = ""
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private lateinit var viewModel: PortfolioViewModel
    private val amount get() = binding.etAmount.text.trim().toString()
    override fun bind() = FragmentBuyUsdtBinding.inflate(layoutInflater)
    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
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
        binding.etAmount.addTextChangedListener(textOnTextChange)

    }

    private fun prepareView() {
        binding.etAmount.text = "0$mCurrency"
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
            }
        }
    }

    private fun activateButton(activate: Boolean) {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }

    private val textOnTextChange = object : OnTextChange {


        @SuppressLint("SetTextI18n")
        override fun onTextChange() {


            val valueAmount =
                if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
                else amount.replace(mConversionCurrency, "").pointFormat.toDouble()


            when {

                valueAmount > 0 -> {
                    if (focusedData.currency.contains(mCurrency)) {
                        val assetAmount = (valueAmount * valueConversion).toString()
                        viewModel.assetAmount = assetAmount
                        setAssesstAmount(assetAmount)
                    } else {
                        val convertedValue = valueAmount / valueConversion
                        setAssesstAmount(convertedValue.toString())
                    }
                }

                else -> {

                    activateButton(false)

                    viewModel.assetAmount = "0"

                    setAssesstAmount("0")
                }
            }
            if (focusedData.currency.contains(mCurrency)) {
                val valueAmountNew =
                    if (assetConversion.contains(mCurrency)) assetConversion.replace(mCurrency, "")
                        .replace("~", "").pointFormat.toDouble()
                    else assetConversion.replace(mConversionCurrency, "")
                        .replace("~", "").pointFormat.toDouble()

                activateButton(true)

            } else {
                activateButton(true)

            }

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }


    private fun setAssesstAmount(assetAmount: String) {
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


    }

    private fun investment() {
        val finalAmount = amount.replace(mCurrency, "").pointFormat
        val balance = BaseActivity.balances.find { it1 -> it1.id == "usdt" }
        val aount: Float = balance?.balanceData?.balance?.toFloat() ?: 0f

        viewModel.amount = finalAmount
        viewModel.selectedFrequency = selectedFrequency
        val bundle = Bundle()
        bundle.putString(Constants.AMOUNT, finalAmount)
        bundle.putString(Constants.FREQUENCY, selectedFrequency)
        viewModel.selectedOption = Constants.USING_STRATEGY
        findNavController().navigate(R.id.confirmInvestmentFragment, bundle)
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
                        if (char != '.') etAmount.text = "$string$char${currency}"
                    } else {
                        if (char == '.') etAmount.text = ("${
                            string.pointFormat.toDouble().toInt().commaFormatted
                        }.${currency}")
                        else etAmount.text = ((string.pointFormat.toDouble().toInt()
                            .toString() + char).commaFormatted + currency)
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


}