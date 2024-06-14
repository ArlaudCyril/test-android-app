package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentWithdrawAmountBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.BalanceData
import com.Lyber.dev.models.WithdrawAddress
import com.Lyber.dev.ui.fragments.bottomsheetfragments.WithdrawalAddressBottomSheet
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPointUptoTwoPlaces
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.load
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import java.math.RoundingMode
import kotlin.math.round


class WithdrawAmountFragment : BaseFragment<FragmentWithdrawAmountBinding>(), View.OnClickListener {
    override fun bind() = FragmentWithdrawAmountBinding.inflate(layoutInflater)
    private lateinit var viewModel: PortfolioViewModel
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private var valueConversion: Double = 1.0
    private var minAmount: String = "0.0"
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private var maxValue: Double = 0.0
    private var maxValueOther: Double = 0.0
    private var maxValueAsset: Double = 0.0
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private var addressId: String = ""
    private var activate: Boolean = false
    private var decimal: Int = 3
    private val addresses: MutableList<WithdrawAddress> = mutableListOf()
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
        binding.llAddress.setOnClickListener(this)
        binding.includedAsset.ivCopy.setOnClickListener(this)
        binding.ivMax.setOnClickListener(this)
        binding.btnPreviewInvestment.setOnClickListener(this)
        decimal = viewModel.selectedNetworkDeposit!!.decimals

        prepareView()
        setObservers()
        binding.etAmount.addTextChangedListener(textOnTextChange)
        CommonMethods.checkInternet(requireActivity()) {
            CommonMethods.showProgressDialog(requireActivity())
            viewModel.getWithdrawalAddresses()
        }
    }

    private fun setObservers() {
        viewModel.withdrawalAddresses.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                addresses.clear()
                for (address in it.data!!) {
                    if (address.network == viewModel.selectedNetworkDeposit!!.id) {
                        addresses.add(address)
                    }
                }
                if (addresses.size > 0) {
                    var addressList = addresses
                    if (newAddress) {
                        addressList =
                            addresses.sortedByDescending { it.creationDate }.toMutableList()
                        newAddress = false
                    }
                    binding.includedAsset.apply {
                        val withdrawAddress = addressList[0]
                        viewModel.withdrawAddress = withdrawAddress
                        tvAssetName.text = withdrawAddress.name
                        tvAssetAddress.visible()
                        ivCopy.visible()
                        addressId = withdrawAddress.address.toString()
                        tvAssetAddress.text = withdrawAddress.address
                        val asset =
                            com.Lyber.dev.ui.activities.BaseActivity.networkAddress.firstNotNullOfOrNull { item -> item.takeIf { item.id == withdrawAddress.network } }
                        if (asset != null)
                            ivAssetIcon.load(asset.imageUrl)

                    }
                }
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
                        setAssetAmount(assetAmount)
                    } else {
                        val convertedValue = valueAmount / valueConversion
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

    private fun setAssetAmount(assetAmount: String) {
        val valueAmount =
            if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
            else amount.replace(mConversionCurrency, "").pointFormat.toDouble()

        if (focusedData.currency.contains(mCurrency)) {
            var roundDigits = decimal
            if (mConversionCurrency == Constants.EURO
//                || mConversionCurrency.equals(
//                    "usdt",
//                    ignoreCase = true
//                )
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
//                || mCurrency.equals("usdt", ignoreCase = true)
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

    fun activateButton() {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }

    private fun prepareView() {
        viewModel.withdrawAddress = null
        binding.apply {
            listOf(
                tvTitle, tvSubTitle, etAmount, tvAssetConversion, ivMax, ivRepeat
            ).visible()
            binding.etAmount.text = "0€"
            includedAsset.ivAssetIcon.setImageResource(R.drawable.addaddressicon)
            includedAsset.tvAssetName.text = getString(R.string.add_address)
            includedAsset.tvAssetAddress.text = getString(R.string.unlimited_withdrawl)
            includedAsset.tvAssetAddress.visible()
            viewModel.selectedAssetDetail.let {
                mCurrency = Constants.EURO
                mConversionCurrency = it!!.id.uppercase()
                focusedData.currency = mCurrency
                unfocusedData.currency = mConversionCurrency

                "${getString(R.string.withdraw)} ${it.id.uppercase()}".also { tvTitle.text = it }
                // calculate available balance and value conversion
                var balance =
                    com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                if (balance == null) {
                    val balanceData = BalanceData("0", "0")
                    balance = Balance("0", balanceData)
                }
                val priceCoin = balance.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble())
                var roundDigits = decimal
                if (
//                    viewModel.selectedAssetDetail!!.id.equals("usdt", ignoreCase = true) ||
                    viewModel.selectedAssetDetail!!.id.equals(Constants.EURO, ignoreCase = true))
                    roundDigits = 2
                "${
                    balance.balanceData.balance.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        roundDigits
                    )
                } Available".also { tvSubTitle.text = it }
                valueConversion =
                    (balance.balanceData.balance.toDouble() / balance.balanceData.euroBalance.toDouble()).toString()
                        .formattedAsset(
                            priceCoin,
                            RoundingMode.DOWN,
                            10
                        ).toDouble()
                if (balance.balanceData.balance == "0") {
                    val balanceResume =
                        com.Lyber.dev.ui.activities.BaseActivity.balanceResume.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                    valueConversion =
                        1.0 / balanceResume!!.priceServiceResumeData.lastPrice.toDouble()
                }
                setAssetAmount("0.0")
            }
            viewModel.selectedNetworkDeposit.let {
                var balance =
                    com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                if (balance == null) {
                    val balanceData = BalanceData("0", "0")
                    balance = Balance("0", balanceData)
                }
                val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                    .div(balance!!.balanceData.balance.toDouble())

                "${getString(R.string.fees)} ${
                    it!!.withdrawFee.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        3
                    )
                } ${balance!!.id.uppercase()}".also {
                    tvAssetFees.text = it
                }
                //calculating max value of asset euro
                maxValue = balance!!.balanceData.balance.toDouble() / valueConversion //59.48
                if (maxValue < 0) {
                    maxValue = 0.0
                }
                //calculating max value of other asset
                maxValueOther =
                    (balance!!.balanceData.euroBalance.toDouble() * valueConversion) //64.35370783
                //setting minimum withdrawal amount
                minAmount = it.withdrawMin.formattedAsset(priceCoin, RoundingMode.DOWN)
                (getString(R.string.minimum_withdrawl) + ": " + minAmount + " " + balance!!.id.uppercase()).also {
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
                llAddress -> openAddressSheet()
                btnPreviewInvestment -> {
                    val amountFinal = if (focusedData.currency.contains(mCurrency)) {
                        assetConversion.replace(mConversionCurrency, "").replace("~", "")
                    } else {
                        amount.replace(mConversionCurrency, "")
                    }
                    val balance =
                        com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }

                    if (balance == null) {
                        getString(R.string.you_do_not_have_this_asset).showToast(requireActivity())
                    } else if (activate) {
                        if (focusedData.currency.contains(mCurrency)) {
                            if (maxValueAsset <= balance.balanceData.balance.toDouble()) {
                                if (viewModel.withdrawAddress != null) {
                                    val bundle = Bundle().apply {
                                        putString(Constants.EURO, amountFinal)
                                    }
                                    findNavController().navigate(
                                        R.id.confirmWithdrawalFragment, bundle
                                    )
                                } else {
                                    getString(R.string.select_address).showToast(requireActivity())
                                }
                            } else {
                                getString(R.string.insufficient_balance).showToast(requireActivity())
                            }
                        } else if (focusedData.currency.contains(mConversionCurrency)) {
                            if (maxValueAsset <= maxValue) {
                                if (viewModel.withdrawAddress != null) {
                                    val bundle = Bundle().apply {
                                        putString(Constants.EURO, amountFinal)
                                    }
                                    findNavController().navigate(
                                        R.id.confirmWithdrawalFragment, bundle
                                    )
                                } else {
                                    getString(R.string.select_address).showToast(requireActivity())
                                }
                            } else {
                                getString(R.string.insufficient_balance).showToast(requireActivity())
                            }
                        } else {
                            getString(R.string.insufficient_balance).showToast(requireActivity())
                        }
                    }
                }

                includedAsset.ivCopy -> {
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", addressId)
                    clipMan?.setPrimaryClip(clip)
                    getString(R.string.copied).showToast(requireContext())
                }
            }
        }
    }

    private fun openAddressSheet() {
        if (addresses.size == 0) {
            val bundle = Bundle().apply {
                putBoolean(Constants.ACTION_WITHDRAW, true)
                putString(Constants.ID, viewModel.selectedNetworkDeposit!!.id)
            }
            findNavController().navigate(R.id.addCryptoAddress, bundle)
        } else {

            WithdrawalAddressBottomSheet(
                addresses, viewModel.selectedNetworkDeposit!!.id, ::handle
            ).show(childFragmentManager, "")
        }
    }

    private fun handle(withdrawAddress: WithdrawAddress?, s: String?) {
        binding.includedAsset.apply {
            viewModel.withdrawAddress = withdrawAddress
            tvAssetName.text = withdrawAddress!!.name
            tvAssetAddress.text = withdrawAddress.address
            addressId = withdrawAddress.address.toString()
            ivCopy.visible()
            val asset =
                com.Lyber.dev.ui.activities.BaseActivity.networkAddress.firstNotNullOfOrNull { item -> item.takeIf { item.id == withdrawAddress.network } }
            if (asset != null)
                ivAssetIcon.load(asset.imageUrl)
        }
    }

    private fun setMaxValue() {
        var balance =
            com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
        if (balance == null) {
            val balanceData = BalanceData("0", "0")
            balance = Balance("0", balanceData)
        }
        val priceCoin = balance.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble())
        if (focusedData.currency.contains(mCurrency)) {
            val spaceGap = if (mCurrency == Constants.EURO)
                ""
            else
                " "
            if (maxValue > 0) {
                var roundDigits = decimal
                if (mCurrency == Constants.EURO)
                    roundDigits = 2
                binding.etAmount.text = "${
                    maxValue.toString().formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
                }" + spaceGap + mCurrency
            } else {
                binding.etAmount.text = "0$spaceGap$mCurrency"
            }
        } else {
            val spaceGap = if (mConversionCurrency == Constants.EURO)
                ""
            else
                " "
            if (maxValueOther > 0) {
                var roundDigits = decimal
                if (mConversionCurrency == Constants.EURO)
                    roundDigits = 2

                binding.etAmount.text = "${
                    maxValueOther.toString()
                        .formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
                }$spaceGap${mConversionCurrency}"

            } else {
                binding.etAmount.text = "${0}$spaceGap${mConversionCurrency}"
            }
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
            Log.d(TAG, "backspace: ${e.message}\n${e.localizedMessage}")
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
                    .replace("~", "").pointFormat.decimalPoint().formattedAsset(1.06, RoundingMode.DOWN, decimal)
                binding.etAmount.text = ("${valueTwo}$mCurrency")
            }
            setAssetAmount(valueOne)
        }


    }

    companion object {
        private const val TAG = "WithdrawAmountFragment"
        var newAddress = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isAdded)
            newAddress = false
    }

    private val String.pointFormat
        get() = replace(",", "", true)

    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)

}