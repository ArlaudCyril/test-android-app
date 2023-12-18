package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentBuyUsdtBinding
import com.au.lyber.models.Balance
import com.au.lyber.models.BalanceData
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.math.RoundingMode

class BuyUSDTFragment : BaseFragment<FragmentBuyUsdtBinding>(), View.OnClickListener,
    RestClient.OnRetrofitError {
    override fun bind() = FragmentBuyUsdtBinding.inflate(layoutInflater)
    private var valueConversion: Double = 1.0
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private lateinit var viewModel: PortfolioViewModel
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private val amount get() = binding.etAmount.text.trim().toString()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.ivRepeat.setOnClickListener(this)
        binding.btnPreviewInvestment.setOnClickListener(this)
        prepareView()
        binding.etAmount.addTextChangedListener(textOnTextChange)
        viewModel.getQuoteResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.progress.clearAnimation()
                binding.progress.gone()
                val bundle = Bundle().apply {
                    putString(Constants.DATA_SELECTED, Gson().toJson(it.data))
                }
                findNavController().navigate(R.id.previewMyPurchaseFragment, bundle)

            }
        }
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

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        binding.btnPreviewInvestment.text = getString(R.string.preview_my_purchase)
    }

    private fun setAssesstAmount(assetAmount: String) {
        val valueAmount =
            if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
            else amount.replace(mConversionCurrency, "").pointFormat.toDouble()

        if (focusedData.currency.contains(mCurrency)) {
            val priceCoin = valueAmount.toDouble()
                .div(assetAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN)}$mConversionCurrency"
        } else {
            val priceCoin = assetAmount.toDouble()
                .div(valueAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN)}$mCurrency"
        }


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
            mConversionCurrency = "usdt".uppercase()
            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency

            "${getString(R.string.buy)} ${"usdt".uppercase()}".also { tvTitle.text = it }
            var balance =
                BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == "usdt" } }
            if (balance == null) {
                val balanceData = BalanceData("0", "0")
                balance = Balance("0", balanceData)
            }
            var priceCoin = balance!!.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())

            valueConversion =
                (balance.balanceData.balance.toDouble() / balance.balanceData.euroBalance.toDouble())
            if (balance.balanceData.balance == "0") {
                val balanceResume =
                    BaseActivity.balanceResume.firstNotNullOfOrNull { item -> item.takeIf { item.id == "usdt" } }

                valueConversion = 1.0 / balanceResume!!.priceServiceResumeData.lastPrice.toDouble()
            }
            setAssesstAmount("0.0")

            viewModel.selectedNetworkDeposit.let {
                var balance =
                    BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == "usdt" } }
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
        viewModel.getQuote(
            "eur",
            "usdt",
            amount.split(focusedData.currency)[0].pointFormat
        )


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
                        if (char != '.') etAmount.text = "$string$char${currency}"
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
        if (focusedData.currency.contains(mCurrency)) {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mCurrency, "").pointFormat.decimalPoint()
            val valueTwo = assetConversion.replace(mConversionCurrency, "")
                .replace("~", "").pointFormat.decimalPoint()
            binding.etAmount.text = ("${valueTwo}$mConversionCurrency")

            setAssesstAmount(valueOne.toString())
        } else {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mConversionCurrency, "").pointFormat.decimalPoint()
            val valueTwo = assetConversion.replace(mCurrency, "")
                .replace("~", "").pointFormat.decimalPoint()

            binding.etAmount.text = ("${valueTwo}$mCurrency")

            setAssesstAmount(valueOne.toString())
        }


    }

    companion object {
        private const val TAG = "WithdrawAmountFragment"
    }

    private val String.pointFormat
        get() = replace(",", "", true)

    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)
}