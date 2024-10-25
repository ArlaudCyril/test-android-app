package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentSendAmountBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.BalanceData
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.fragments.bottomsheetfragments.EnterPhoneNumberBottomSheetFragment
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPointUptoTwoPlaces
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import java.math.RoundingMode


class SendAmountFragment : BaseFragment<FragmentSendAmountBinding>(), OnClickListener {
    override fun bind() = FragmentSendAmountBinding.inflate(layoutInflater)

    private val focusedData: ValueHolder =
        ValueHolder()
    private val unfocusedData: ValueHolder =
        ValueHolder()
    private var decimal: Int = 3
    private lateinit var viewModel: PortfolioViewModel
    private var valueConversionEuroToSol: Double = 1.0
    private var activate: Boolean = false
    private var maxValueOther: Double = 0.0
    private var maxValueAsset: Double = 0.0
    private var maxValueEuro: Double = 0.0
    private var assetId = ""
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private var minAmount: String = "0"


    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)

    private val amount get() = binding.etAmount.text.trim().toString()

    private var mCurrency: String = Constants.EURO
    private var mConversionCurrency: String = "USDC"
    private lateinit var balance: Balance
//    private lateinit var phnNo: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

        CommonMethods.showProgressDialog(requireContext())
        assetId = requireArguments().getString(Constants.ID).toString()
        balance =
            com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == assetId } }!!
        (balance.balanceData.euroBalance + Constants.EURO + " Available").also {
            binding.tvSubTitle.text = it
        }
        // calculate available balance and value conversion
        if (balance == null) {
            val balanceData = BalanceData("0", "0")
            balance = Balance("0", balanceData)
        }
        val priceCoin = balance.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble())
        "${
            balance.balanceData.euroBalance + Constants.EURO
        } Available".also { binding.tvSubTitle.text = it }
        maxValueEuro = balance.balanceData.euroBalance.toDouble()
        maxValueOther = balance.balanceData.balance.toDouble()
        valueConversionEuroToSol =
            (balance.balanceData.euroBalance.toDouble() / balance.balanceData.balance.toDouble()).toString()
                .formattedAsset(
                    priceCoin,
                    RoundingMode.DOWN,
                    10
                ).toDouble()
        mCurrency = Constants.EURO
        mConversionCurrency = assetId.uppercase()
        focusedData.currency = mCurrency
        unfocusedData.currency = mConversionCurrency

        checkInternet(binding.root, requireContext()) {
            viewModel.getAssetDetailIncludeNetworks(assetId)
        }
        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                decimal = it.data.decimals.toInt()
            }
        }


        binding.etAmount.addTextChangedListener(textOnTextChange)



        setAssetAmount("0.0")

        binding.apply {
            ivTopAction.setOnClickListener(this@SendAmountFragment)
            btnNext.setOnClickListener(this@SendAmountFragment)
            tvBackArrow.setOnClickListener(this@SendAmountFragment)
            tvDot.setOnClickListener(this@SendAmountFragment)
            tvOne.setOnClickListener(this@SendAmountFragment)
            tvTwo.setOnClickListener(this@SendAmountFragment)
            tvThree.setOnClickListener(this@SendAmountFragment)
            tvFour.setOnClickListener(this@SendAmountFragment)
            tvFive.setOnClickListener(this@SendAmountFragment)
            tvSix.setOnClickListener(this@SendAmountFragment)
            tvSeven.setOnClickListener(this@SendAmountFragment)
            tvEight.setOnClickListener(this@SendAmountFragment)
            tvNine.setOnClickListener(this@SendAmountFragment)
            tvZero.setOnClickListener(this@SendAmountFragment)
            ivMax.setOnClickListener(this@SendAmountFragment)
            ivSwap.setOnClickListener(this@SendAmountFragment)
        }

        viewModel.userByPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                val bundle = Bundle().apply {
                    var euroAmount = ""
                    var otherAssetAmount = ""
                    if (focusedData.currency.contains(Constants.EURO)) {
                        euroAmount = amount.replace(Constants.EURO, "")
                        otherAssetAmount =
                            assetConversion.replace(mConversionCurrency, "").replace("~", "")
                    } else {
                        euroAmount =
                            assetConversion.replace(Constants.EURO, "").replace("~", "")
                        otherAssetAmount = amount.replace(mConversionCurrency, "")

                    }


                    putString("firstName", it.data.firstName)
                    putString("lastName", it.data.lastName)
                    putString("asset", assetId.uppercase())
                    putString("assetAmount", otherAssetAmount)
                    putString("euroAmount", euroAmount)
                    putString("phoneNo", it.data.phoneNo)
                    putString(Constants.FROM, SendAmountFragment::class.java.name)
                }
                findNavController().navigate(
                    R.id.confirmWithdrawalFragment, bundle
                )
            }
        }

    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        when (errorCode) {
            26 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.account_cant_be_found)
            )
            else -> super.onRetrofitError(errorCode, msg)
        }
    }

    private fun setAssetAmount(assetAmount: String) {
        val valueAmount =
            if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
            else amount.replace(mConversionCurrency, "").pointFormat.toDouble()

        if (focusedData.currency.contains(mCurrency)) {
            var roundDigits = decimal
            if (mConversionCurrency == Constants.EURO
            )
                roundDigits = 2
            val priceCoin = valueAmount.div(assetAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${
                    assetAmount.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        roundDigits
                    )
                } $mConversionCurrency"
            maxValueAsset =
                assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits).toDouble()
        } else {
            var roundDigits = decimal
            if (mCurrency == Constants.EURO
            )
                roundDigits = 2
            val priceCoin = assetAmount.toDouble().div(valueAmount)
            binding.tvAssetConversion.text =
                "~${
                    assetAmount.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        roundDigits
                    )
                } $mCurrency"
            maxValueAsset =
                assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits).toDouble()
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
                        val assetAmount = (valueAmount / valueConversionEuroToSol).toString()
                        viewModel.assetAmount = assetAmount
                        setAssetAmount(assetAmount)
                    } else {
                        val convertedValue = valueAmount * valueConversionEuroToSol
                        setAssetAmount(convertedValue.toString())
                    }
                }

                else -> {
                    activate = false
                    activateButton()
                    viewModel.assetAmount = "0"
                    setAssetAmount("0")
                }
            }
            if (focusedData.currency.contains(mCurrency)) {
                val valueAmountNew =
                    if (assetConversion.contains(mCurrency)) assetConversion.replace(mCurrency, "")
                        .replace("~", "").pointFormat.toDouble()
                    else assetConversion.replace(mConversionCurrency, "")
                        .replace("~", "").pointFormat.toDouble()
                if (valueAmountNew >= minAmount.toDouble()) {
                    activate = true
                    activateButton()
                } else {
                    activate = false
                    activateButton()
                }
            } else {
                if (valueAmount >= minAmount.toDouble()) {
                    activate = true
                    activateButton()
                } else {
                    activate = false
                    activateButton()
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    fun activateButton() {
        binding.btnNext.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
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
                ivSwap -> swapConversion()
                btnNext -> {
                    if (activate) {
                        if (focusedData.currency.contains(Constants.EURO)) {
                            if (maxValueAsset <= balance.balanceData.balance.toDouble()) {
                                if (requireArguments().containsKey(Constants.SELECTED_METHOD) && requireArguments().getString(
                                        Constants.SELECTED_METHOD
                                    ) == Constants.QR
                                ) {
//                                        handle(phnNo)//TODO
//                                    findNavController().navigate(R.id.codeScannerFragment)
                                    CodeScannerFragment(::handle).show(
                                        requireActivity().supportFragmentManager,
                                        ""
                                    )
//                                    binding.scannerView.visible()
                                } else EnterPhoneNumberBottomSheetFragment(::handle).show(
                                    requireActivity().supportFragmentManager,
                                    ""
                                )
                            } else {
                                getString(R.string.insufficient_balance).showToast(
                                    binding.root,
                                    requireActivity()
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    private fun setMaxValue() {
        val priceCoin = balance.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble())

        if (focusedData.currency.contains(Constants.EURO)) {
            if (maxValueEuro > 0) {
                var roundDigits = decimal
                if (mCurrency == Constants.EURO)
                    roundDigits = 2
                binding.etAmount.text = "${
                    maxValueEuro.toString()
                        .formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
                }" + mCurrency
            } else {
                binding.etAmount.text = "0$mCurrency"
            }
        } else {
            if (maxValueOther > 0) {
                var roundDigits = decimal
                if (mConversionCurrency == Constants.EURO)
                    roundDigits = 2

                binding.etAmount.text = "${
                    maxValueOther.toString()
                        .formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
                } ${mConversionCurrency}"

            } else {
                binding.etAmount.text = "${0} ${mConversionCurrency}"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {
        if (focusedData.currency.contains(mCurrency)) {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = if (mCurrency == Constants.EURO)
                amount.replace(mCurrency, "").pointFormat.decimalPoint()
                    .decimalPointUptoTwoPlaces()
            else
                amount.replace(mCurrency, "").pointFormat.decimalPoint()
                    .formattedAsset(1.06, RoundingMode.DOWN, decimal)

            val valueTwo = if (mConversionCurrency == Constants.EURO)
                assetConversion.replace(mConversionCurrency, "")
                    .replace("~", "").pointFormat.decimalPoint().decimalPointUptoTwoPlaces()
            else
                assetConversion.replace(mConversionCurrency, "")
                    .replace("~", "").pointFormat.decimalPoint()
                    .formattedAsset(1.06, RoundingMode.DOWN, decimal)
            binding.etAmount.text = ("$valueTwo $mConversionCurrency")

            setAssetAmount(valueOne)
        } else {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mConversionCurrency, "").pointFormat.decimalPoint()
            if (focusedData.currency == Constants.EURO) {
                val valueTwo = assetConversion.replace(mCurrency, "")
                    .replace("~", "").pointFormat.decimalPointUptoTwoPlaces()
                binding.etAmount.text = ("${valueTwo}$mCurrency")
            } else {
                val valueTwo = assetConversion.replace(mCurrency, "")
                    .replace("~", "").pointFormat.decimalPoint()
                    .formattedAsset(1.06, RoundingMode.DOWN, decimal)
                binding.etAmount.text = ("${valueTwo}$mCurrency")
            }
            setAssetAmount(valueOne)
        }


    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
            val builder = StringBuilder()
            val value =
                if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.trim()
                else amount.replace(mConversionCurrency, "").pointFormat.trim()

            builder.append(value.dropLast(1))
            if (builder.toString().isEmpty()) {
                builder.append("0")
            }
            val spaceGapCurrency = if (mCurrency == Constants.EURO)
                mCurrency
            else
                " $mCurrency"
            val spaceGapConversion = if (mConversionCurrency == Constants.EURO)
                mConversionCurrency
            else
                " $mConversionCurrency"

            if (amount.contains(mCurrency)) builder.append(spaceGapCurrency)
            else builder.append(spaceGapConversion)

            binding.etAmount.text = builder.toString()


        } catch (e: Exception) {
            Log.d("SendAmountFragment.TAG", "backspace: ${e.message}\n${e.localizedMessage}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun type(char: Char) {

        binding.apply {
            val trimmedText = amount.replace("\\s+".toRegex(), "") // Removes all whitespace
            val currency =
                if (focusedData.currency.contains(mCurrency)) mCurrency else mConversionCurrency
            val spaceGap = if (currency == Constants.EURO)
                ""
            else
                " "
            when {
                trimmedText.length == (currency.length + 1) && trimmedText[0] == '0' -> {
                    if (char == '.') {
                        if (!trimmedText.contains('.')) etAmount.text =
                            ("0$char$spaceGap${currency}")
                    } else etAmount.text = (char + spaceGap + currency)
                }

                else -> {

                    val string = amount.substring(0, amount.count() - currency.length).trim()

                    if (string.contains('.')) {
                        if (char != '.')  //FOR NOW
                        {
                            val decimalPart = string.substringAfter('.')
                            if (currency == Constants.EURO
//                                || currency.equals(
//                                    "usdt",
//                                    ignoreCase = true
//                                )
                            ) {
                                if (decimalPart.length < 2 && char.isDigit()) {
                                    etAmount.text = "$string$char$spaceGap$currency"
                                }
                            } else if (decimalPart.length < decimal && char.isDigit())
                                etAmount.text = "$string$char$spaceGap$currency"
                        }
                    } else {
                        if (char == '.') etAmount.text = ("${
                            string.pointFormat
                        }.$spaceGap${currency}")
                        else etAmount.text = ((string.pointFormat + char) + spaceGap + currency)
                    }
                }

            }
        }


    }

    private fun handle(phoneNo: String, error: Boolean) {
        if (error)
            phoneNo.showToast(binding.root, requireContext())
        else {
            CommonMethods.checkInternet(binding.root, requireActivity()) {
                val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                    SplashActivity.integrityTokenProvider?.request(
                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                            .build()
                    )
                integrityTokenResponse1?.addOnSuccessListener { response ->
                    Log.d("token", "${response.token()}")
                    CommonMethods.showProgressDialog(requireActivity())
//                viewModel.getWalletRib(response.token())
                    viewModel.getUserNameByPhone(phoneNo, response.token())
                }?.addOnFailureListener { exception ->
                    Log.d("token", "${exception}")
                }
            }
        }
    }


    private val String.pointFormat
        get() = replace(",", "", true)

}