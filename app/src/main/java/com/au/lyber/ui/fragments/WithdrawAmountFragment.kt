package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.content.ContextCompat
import com.au.lyber.R
import com.au.lyber.databinding.FragmentWithdrawAmountBinding
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
import java.math.RoundingMode

class WithdrawAmountFragment : BaseFragment<FragmentWithdrawAmountBinding>(), View.OnClickListener {
    override fun bind() = FragmentWithdrawAmountBinding.inflate(layoutInflater)
    private var valueConversion: Double = 1.0
    private var minAmount: Double = 0.0
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private var maxValue: Double = 0.0
    private lateinit var viewModel: PortfolioViewModel
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private val amount get() = binding.etAmount.text.trim().toString()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
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
        binding.ivMax.setOnClickListener(this)
        prepareView()
        binding.etAmount.addTextChangedListener(textOnTextChange)
    }

    private val textOnTextChange = object : OnTextChange {


        @SuppressLint("SetTextI18n")
        override fun onTextChange() {


            val valueAmount =
                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
                else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()

            if (valueAmount > minAmount) {
                activateButton(true)
            }
            when {

                valueAmount > 0 -> {

                    when (focusedData.currency) {

                        mCurrency -> {

                            val assetAmount = (valueAmount * valueConversion).toString()
                            viewModel.assetAmount = assetAmount

                            binding.tvAssetConversion.text =
                                "${assetAmount.decimalPoint().commaFormatted}$mConversionCurrency"


                        }

                        else -> {

                            val convertedValue = valueAmount / valueConversion
                            binding.tvAssetConversion.text = "${
                                convertedValue.toString().decimalPoint().commaFormatted
                            }$mCurrency"
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

        }

        override fun afterTextChanged(s: Editable?) {

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
                tvSubTitle,
                etAmount,
                tvAssetConversion,
                ivMax,
                ivRepeat
            ).visible()

            viewModel.selectedAssetDetail.let {
                mCurrency = Constants.EURO
                mConversionCurrency = it!!.id.uppercase()
                focusedData.currency = mCurrency
                unfocusedData.currency = mConversionCurrency

                "${getString(R.string.withdraw)} ${it.id.uppercase()}".also { tvTitle.text = it }
                val balance =
                    BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                maxValue = balance!!.balanceData.balance.toDouble()
                val priceCoin = balance.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble())
                "${
                    balance.balanceData.balance.formattedAsset(priceCoin, RoundingMode.DOWN)
                } ${it.id.uppercase()} Available".also { tvSubTitle.text = it }
                valueConversion = (balance.balanceData.balance.toDouble() / balance.balanceData.euroBalance.toDouble())
                ("0.0 " + it.id.uppercase()).also { tvAssetConversion.text = it }
            }
            viewModel.selectedNetworkDeposit.let {
                val balance =
                    BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble())
                "${getString(R.string.fees)} ${it!!.withdrawFee} ${it.id.uppercase()}".also {
                    tvAssetFees.text = it
                }
                minAmount = it.withdrawMin.toString().formattedAsset(priceCoin, RoundingMode.DOWN)
                    .toDouble()
                (getString(R.string.minimum_withdrawl) + ": " + minAmount + " " + it.id.uppercase()).also {
                    tvMinAmount.text = it
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
                ivMax -> setMaxValue()
                ivRepeat -> swapConversion()
            }
        }
    }

    private fun setMaxValue() = if (amount.contains(focusedData.currency)) {
        val balance =
            BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
        val priceCoin = balance!!.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble() ?: 1.0)
        binding.etAmount.setText(
            maxValue.toString().formattedAsset(priceCoin, RoundingMode.DOWN) + focusedData.currency
        )
    } else {
        val balance =
            BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo!!.id } }
        val priceCoin = balance!!.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble() ?: 1.0)
        binding.etAmount.setText(
            "${
                maxValue.toString().formattedAsset(priceCoin, RoundingMode.DOWN)
            }${unfocusedData.currency}"
        )
    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {

            val builder = StringBuilder()

            val value =
                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat
                else amount.split(unfocusedData.currency)[0].pointFormat


            when {

                value.toDouble() > 9 -> {
                    builder.append(value.dropLast(1).commaFormatted)

                    if (amount.contains(focusedData.currency)) builder.append(focusedData.currency)
                    else builder.append(unfocusedData.currency)

                    binding.etAmount.setText(builder.toString())

                    return
                }

                else -> {
                    if (amount.contains(focusedData.currency)) binding.etAmount.setText("0${focusedData.currency}")
                    else binding.etAmount.setText("0${unfocusedData.currency}")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun type(char: Char) {


        binding.apply {

            when {

                amount.length == (focusedData.currency.length + 1) && amount[0] == '0' -> {
                    if (char == '.') {
                        if (!amount.contains('.')) etAmount.setText(("0$char${focusedData.currency}"))
                    } else etAmount.setText((char +""+ focusedData.currency))
                }

                else -> {

                    val string = amount.substring(0, amount.count() - focusedData.currency.length)

                    if (string.contains('.')) {
                        if (char != '.') etAmount.setText("$string$char${focusedData.currency}")
                    } else {
                        if (char == '.') etAmount.setText(
                            ("${
                                string.pointFormat.toDouble().toInt().commaFormatted
                            }.${focusedData.currency}")
                        )
                        else etAmount.setText(
                            ((string.pointFormat.toDouble().toInt()
                                .toString() + char).commaFormatted + ""+focusedData.currency)
                        )
                    }
                }

            }
        }


    }

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {
        if (focusedData.currency == mCurrency){
            val valueOne = amount.split(mCurrency)[0].pointFormat.decimalPoint()
            val valueTwo = assetConversion.split(mConversionCurrency)[0].pointFormat.decimalPoint()
            binding.etAmount.setText(("${valueTwo.commaFormatted}$mConversionCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mCurrency")
        }else{
            val valueOne = amount.split(mConversionCurrency)[0].pointFormat.decimalPoint()
            val valueTwo = assetConversion.split(mCurrency)[0].pointFormat.decimalPoint()
            binding.etAmount.setText(("${valueTwo.commaFormatted}$mCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mConversionCurrency")
        }
        val currency = focusedData.currency
        focusedData.currency = unfocusedData.currency
        unfocusedData.currency = currency



    }

    private val String.pointFormat
        get() = replace(",", "", true)

    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)
}