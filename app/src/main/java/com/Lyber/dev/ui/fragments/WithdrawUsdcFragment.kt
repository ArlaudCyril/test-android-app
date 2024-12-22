package com.Lyber.dev.ui.fragments
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentWithdrawAmountBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.BalanceData
import com.Lyber.dev.models.CurrentPriceResponse
import com.Lyber.dev.models.RIBData
import com.Lyber.dev.models.WithdrawEuroData
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.ui.fragments.bottomsheetfragments.WithdrawalUsdcAddressBottomSheet
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPointUptoTwoPlaces
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.invisible
import com.Lyber.dev.utils.CommonMethods.Companion.shake
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.PortfolioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode


class WithdrawUsdcFragment : BaseFragment<FragmentWithdrawAmountBinding>(), OnClickListener {
    private lateinit var viewModel: PortfolioViewModel
    private val focusedData: WithdrawAmountFragment.ValueHolder =
        WithdrawAmountFragment.ValueHolder()
    private val unfocusedData: WithdrawAmountFragment.ValueHolder =
        WithdrawAmountFragment.ValueHolder()
    private var valueConversion: BigDecimal = 1.toBigDecimal()
//    private var lastPriceFromApi: Double = 1.0
    private var lastPriceFromApi: BigDecimal = 1.toBigDecimal()
    private var minAmount = 10.0
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private var maxValue: BigDecimal = 0.toBigDecimal()
    private var maxValueOther: BigDecimal = 0.toBigDecimal()
    private var maxValueAsset: BigDecimal = 0.toBigDecimal()
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""

    //    private var addressId: String = ""
    private var activate: Boolean = false
    private var decimal: Int = 2
    private val ribDataList: MutableList<RIBData> = mutableListOf()
    private lateinit var withdrawFeeData: WithdrawEuroData
    private var debounceJob: Job? = null // To hold the debounce coroutine job
    private val debounceDelay = 700L // Delay in milliseconds (0.7 seconds)
    private var onMaxClick: Boolean = false
    private var onMax: Boolean = false


    private val amount get() = binding.etAmount.text.trim().toString()

    override fun bind() = FragmentWithdrawAmountBinding.inflate(layoutInflater)

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

        val asset =
            com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id ==viewModel.selectedAssetDetail!!.id } }
        if (asset != null) {
            decimal = asset.decimals
        }
        prepareView()
        setObservers()
        binding.etAmount.addTextChangedListener(textOnTextChange)
        CommonMethods.checkInternet(binding.root, requireActivity()) {
            CommonMethods.showProgressDialog(requireActivity())
            viewModel.getCurrentPrice(viewModel.selectedAssetDetail!!.id)
            viewModel.getWalletRib()
            viewModel.getWithdrawEuroFee()

        }
    }

    private fun prepareView() {
        viewModel.ribDataAddress = null
        binding.apply {
            listOf(
                tvTitle, tvSubTitle, etAmount, tvAssetConversion, ivMax, ivRepeat
            ).visible()
            binding.tvAssetFees.gone()
            binding.tvAssetConversion.text = "0 USDC"
            binding.etAmount.text = "0€"
//            includedAsset.ivAssetIcon.setImageResource(R.drawable.addaddressicon)
            includedAsset.tvAssetName.text = getString(R.string.add_address)
            includedAsset.tvAssetAddress.text = getString(R.string.unlimited_withdrawl)
            includedAsset.tvAssetAddress.visible()
        }
    }

    private fun setObservers() {
        viewModel.currentPriceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                val lastPrice = it.data.price.toBigDecimal()
                lastPriceFromApi=it.data.price.toBigDecimal()
                valueConversion = BigDecimal(1 / it.data.price.toDouble())
                setValues()
            }
        }
        viewModel.walletRibResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                ribDataList.clear()
                for (rib in it.data)
                    if (rib.ribStatus.lowercase() == "validated")
                        ribDataList.add(rib)
//                ribDataList.addAll(it.data)

                if (ribDataList.size > 0) {
                    var addressList = ribDataList
                    if (WithdrawAmountFragment.newAddress) {
                        addressList =
                            ribDataList.sortedByDescending { it.creationDate }.toMutableList()
                        WithdrawAmountFragment.newAddress = false
                    }
                    binding.includedAsset.apply {
                        val withdrawAddress = addressList[0]
                        viewModel.ribDataAddress = withdrawAddress
                        tvAssetName.text = withdrawAddress.name
                        ivCopy.gone()
                        ivDropIcon.gone()
                        tvAssetAddress.visible()
                        ivDropIcon2.visible()
//                        addressId = withdrawAddress.address.toString()
                        val maxLength = 20 // Adjust this value as needed
                        val truncatedText =
                            CommonMethods.getTruncatedText(withdrawAddress.iban, maxLength)
                        tvAssetAddress.text = truncatedText
//                        tvAssetAddress.text = withdrawAddress.iban

                    }
                }
            }
        }
        viewModel.withdrawEuroFeeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (it.data != null) {
                    withdrawFeeData = it.data
                    minAmount = withdrawFeeData.withdrawEuroMin
                    (getString(R.string.minimum_withdrawl) + ": ${minAmount}${Constants.EURO}").also {
                        binding.tvMinAmount.text = it
                    }
                    var balance =
                        com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                    if (balance == null) {
                        val balanceData = BalanceData("0", "0")
                        balance = Balance("0", balanceData)
                    }
                    val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                        .div(balance!!.balanceData.balance.toDouble())

                    "${getString(R.string.fees)} ${
                        withdrawFeeData.withdrawEuroFees.toString().formattedAsset(
                            priceCoin,
                            RoundingMode.DOWN,
                            3
                        )
                    } ${Constants.EURO}".also {
                        binding.tvAssetFees.text = it
                    }
                    binding.tvAssetFees.visible()
                }
            }
        }

    }
    private fun setValues() {
        binding.apply {
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
                ("${
                    balance.balanceData.balance.formattedAsset(
                        priceCoin,
                        RoundingMode.DOWN,
                        roundDigits
                    )
                } " + getString(R.string.available)).also { tvSubTitle.text = it }
                maxValue = balance.balanceData.balance.toBigDecimal() / valueConversion
                if (maxValue < 0.toBigDecimal()) {
                    maxValue = 0.toBigDecimal()
                }
                //calculating max value of other asset
//                maxValueOther =   balance.balanceData.balance.toBigDecimal()/lastPriceFromApi
                maxValueOther =   maxValue/lastPriceFromApi
//                / lastPriceFromApi

//                (balance.balanceData.euroBalance.toDouble() * valueConversion)
               //setting minimum withdrawal amount

                //setting minimum withdrawal amount

//                valueConversion =
//                    (balance.balanceData.balance.toDouble() / balance.balanceData.euroBalance.toDouble()).toString()
//                        .formattedAsset(
//                            priceCoin,
//                            RoundingMode.DOWN,
//                            10
//                        ).toDouble()
//                Log.d("Conversion Price", "$valueConversion")
//                if (balance.balanceData.balance == "0") {
//                    val balanceResume =
//                        com.Lyber.dev.ui.activities.BaseActivity.balanceResume.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
//                    valueConversion =
//                        1.0 / balanceResume!!.priceServiceResumeData.lastPrice.toDouble()
//                }
                setAssetAmount("0.0")
            }
        }
    }

    private val textOnTextChange = object : OnTextChange {
        @SuppressLint("SetTextI18n")
        override fun onTextChange() {
            val valueAmount =
                if (amount.contains(mCurrency)) amount.replace(mCurrency, "").pointFormat.toDouble()
                else amount.replace(mConversionCurrency, "").pointFormat.toDouble()

            if (valueAmount.toBigDecimal() == 0.toBigDecimal()) {
                activate = false
                activateButton()
                debounceJob?.cancel()
                binding.tvAssetConversion.visible()
//                binding.tvAssetFees.visible()
                binding.ivCircularProgress.gone()
                binding.ivCenterProgress.gone()
//                activateButton(false)
                when (focusedData.currency) {
                    mCurrency -> {
                        binding.tvAssetConversion.text = "0$mConversionCurrency"
                    }

                    else -> {
                        binding.tvAssetConversion.text = "0$mCurrency"
                    }
                }
            } else {
//                val input = amount.toString().trim()
                val input = valueAmount
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

    fun activateButton() {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
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
                assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits).toBigDecimal()

            if (valueAmount > maxValue.toDouble()) {
                binding.etAmount.visible()
                binding.etAmount.shake()
                binding.ivCircularProgress.gone()
                binding.ivCenterProgress.gone()
                binding.tvAssetConversion.visible()
                Handler(Looper.getMainLooper()).postDelayed({
                    setMaxValue()
                }, 700)
            }
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
                assetAmount.formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits).toBigDecimal()
            if (valueAmount > maxValueOther.toDouble()) {
                binding.etAmount.visible()
                binding.etAmount.shake()
                binding.ivCircularProgress.gone()
                binding.ivCenterProgress.gone()
                binding.tvAssetConversion.visible()
                Handler(Looper.getMainLooper()).postDelayed({
                    setMaxValue()
                }, 700)
            }
        }
    }

    private val String.pointFormat
        get() = replace(",", "", true)

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
                    //todo for now in case of Euro Withdrawal replacing amountFinal with amountEuro as we have min value for Euro
                    val amountEuro = if (focusedData.currency.contains(mCurrency)) {
//                        amount.replace(mConversionCurrency, "")
                        amount.replace(mCurrency, "")
                    } else {
//                        assetConversion.replace(mConversionCurrency, "").replace("~", "")
                        assetConversion.replace(mCurrency, "").replace("~", "")
                    }
                    val amountFinal = if (focusedData.currency.contains(mCurrency)) {
                        assetConversion.replace(mConversionCurrency, "").replace("~", "")
                    } else {
                        amount.replace(mConversionCurrency, "")
                    }

                    val balance =
                        com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }

                    if (balance == null) {
                        getString(R.string.you_do_not_have_this_asset).showToast(
                            binding.root,
                            requireActivity()
                        )
                    } else if (activate) {
                        if (focusedData.currency.contains(mCurrency)) {
                            if (maxValueAsset <= balance.balanceData.balance.toBigDecimal()) {
//                                if (minAmount.toDouble() >= amountFinal.toDouble())
                                if (minAmount.toDouble() > amountEuro.toDouble())

                                    getString(R.string.withdrawal_amount_is_inferior).showToast(
                                        binding.root,
                                        requireContext()
                                    )
                                else if (viewModel.ribDataAddress != null) {
                                    val bundle = Bundle().apply {
                                        putString(Constants.MAIN_ASSET, amountFinal)
                                        putString(Constants.EURO, amountEuro)
                                        if (::withdrawFeeData.isInitialized)
                                            putDouble(
                                                Constants.FEE,
                                                withdrawFeeData.withdrawEuroFees
                                            )
                                        putString(
                                            Constants.FROM,
                                            WithdrawUsdcFragment::class.java.name
                                        )
                                    }
                                    findNavController().navigate(
                                        R.id.confirmWithdrawalFragment, bundle
                                    )
                                } else {
                                    getString(R.string.select_address).showToast(
                                        binding.root,
                                        requireActivity()
                                    )
                                }
                            } else {
                                getString(R.string.withdrawal_amount_exceeds_balance).showToast(
                                    binding.root,
                                    requireContext()
                                )

                            }
                        } else if (focusedData.currency.contains(mConversionCurrency)) {
                            if (maxValueAsset <= maxValue) {
//                                if (minAmount.toDouble() >= amountFinal.toDouble())
                                if (minAmount.toDouble() > amountEuro.toDouble())
                                    getString(R.string.withdrawal_amount_is_inferior).showToast(
                                        binding.root,
                                        requireContext()
                                    )
                                else
                                    if (viewModel.ribDataAddress != null) {
                                        val bundle = Bundle().apply {
                                            putString(Constants.MAIN_ASSET, amountFinal)
                                            putString(Constants.EURO, amountEuro)
                                            if (::withdrawFeeData.isInitialized)
                                                putDouble(
                                                    Constants.FEE,
                                                    withdrawFeeData.withdrawEuroFees
                                                )
                                            putString(
                                                Constants.FROM,
                                                WithdrawUsdcFragment::class.java.name
                                            )
                                        }
                                        findNavController().navigate(
                                            R.id.confirmWithdrawalFragment, bundle
                                        )
                                    } else {
                                        getString(R.string.select_address).showToast(
                                            binding.root,
                                            requireActivity()
                                        )
                                    }
                            } else {
                                getString(R.string.withdrawal_amount_exceeds_balance).showToast(
                                    binding.root,
                                    requireContext()
                                )
                            }
                        } else {
                            getString(R.string.withdrawal_amount_exceeds_balance).showToast(
                                binding.root,
                                requireContext()
                            )
                        }
                    }
                }

            }
        }
    }

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
            Log.d("TAG", "backspace: ${e.message}\n${e.localizedMessage}")
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

//            setAssetAmount(valueOne)
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
//            setAssetAmount(valueOne)

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
            if (maxValue > 0.toBigDecimal()) {
                var roundDigits = decimal
                if (mCurrency == Constants.EURO)
                    roundDigits = 2
                binding.etAmount.invisible()
                onMaxClick = true
                onMax = true
                binding.ivCenterProgress.visible()
                binding.tvAssetConversion.invisible()
//                binding.etAmount.text = "${
//                    maxValue.toString().formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
//                }" + spaceGap + mCurrency

                val convertedValue = maxValueOther / valueConversion
                binding.etAmount.text = "${
                    convertedValue.toString()
                        .formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
                }" + spaceGap + mCurrency


                binding.tvAssetConversion.text =
                    "~${
                        maxValueOther.toString()
                            .formattedAsset(priceCoin, RoundingMode.DOWN, decimal)
                    } $mConversionCurrency"
                maxValueAsset = (maxValueOther)

            } else {
                binding.etAmount.text = "0$spaceGap$mCurrency"
            }
        } else {
            val spaceGap = if (mConversionCurrency == Constants.EURO)
                ""
            else
                " "
            if (maxValueOther > 0.toBigDecimal()) {
                var roundDigits = decimal
                if (mConversionCurrency == Constants.EURO)
                    roundDigits = 2

                binding.etAmount.invisible()
                onMaxClick = true
                binding.ivCenterProgress.visible()
                binding.tvAssetConversion.invisible()
                binding.etAmount.text = "${
                    maxValueOther.toString()
                        .formattedAsset(priceCoin, RoundingMode.DOWN, roundDigits)
                }$spaceGap${mConversionCurrency}"

            } else {
                binding.etAmount.text = "${0}$spaceGap${mConversionCurrency}"
            }
        }
    }

    private fun openAddressSheet() {

        if (ribDataList.size == 0) {
            //this case will not occur
            findNavController().navigate(R.id.addRibFragment)
        } else {

            WithdrawalUsdcAddressBottomSheet(
                ribDataList, "", ::handle
            ).show(childFragmentManager, "")
        }
    }

    private fun handle(withdrawAddress: RIBData?, s: String?) {
        binding.includedAsset.apply {
            viewModel.ribDataAddress = withdrawAddress
            tvAssetName.text = withdrawAddress!!.name
            val maxLength = 20
            val truncatedText = CommonMethods.getTruncatedText(withdrawAddress.iban, maxLength)
            tvAssetAddress.text = truncatedText
//            addressId = withdrawAddress.address.toString()
            ivCopy.gone()
        }
    }

    private suspend fun fetchAssetPrice(assetId: String): Response<CurrentPriceResponse> {
        // Example API call using a suspend function (use Retrofit, OkHttp, etc.)
        val apiService = RestClient.get() // Replace with your actual API service
        return apiService.getCurrentPrice(assetId)
    }


    private suspend fun fetchPriceAndConvert(amount: Double) {
        val valueAmount =amount
//            try {
//                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toBigDecimal()
//                else amount.split(unfocusedData.currency)[0].pointFormat.toBigDecimal()
//            } catch (e: Exception) {
//                0.toBigDecimal()
//            }

        if (amount==0.0) return // Ignore empty input

        try {
            val balanceToPrice = fetchAssetPrice(viewModel.selectedAssetDetail!!.id)

            // Check if the response is successful
            if (balanceToPrice.isSuccessful) {
                val body = balanceToPrice.body()

                if (body?.data?.price != null) {
                    val price = body.data.price.toBigDecimal()
                    valueConversion =BigDecimal(1.0 / body.data.price.toDouble())
                    var balance =
                        com.Lyber.dev.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                    if (balance == null) {
                        val balanceData = BalanceData("0", "0")
                        balance = Balance("0", balanceData)
                    }
                    maxValue = balance.balanceData.balance.toBigDecimal() / valueConversion
                    if (maxValue < 0.toBigDecimal()) {
                        maxValue = 0.toBigDecimal()
                    }
                    //calculating max value of other asset
                        maxValueOther = maxValue / price
                    if (maxValue < 0.toBigDecimal()) {
                        maxValue = 0.toBigDecimal()
                    }
                    //calculating max value of other asset
//                    maxValueOther = maxValue / price
                    var maxbal= binding.tvSubTitle.text.toString().replace(getString(R.string.available),"").trim()
                    maxValueOther = BigDecimal(maxbal)


//                        maxValueOther = balance.balanceData.balance.toBigDecimal() / price
//                        (balance.balanceData.euroBalance.toDouble() * valueConversion)

                    when {
                        valueAmount > 0.0 -> {
                            if (focusedData.currency.contains(mCurrency)) {
                                if (!onMax) {
                                    val assetAmount = (valueAmount.toBigDecimal() * valueConversion).toString()
                                    viewModel.assetAmount = assetAmount
                                    setAssetAmount(assetAmount)
                                } else {
                                    if (valueAmount > maxValue.toDouble()) {
                                        binding.etAmount.visible()
                                        binding.etAmount.shake()
                                        binding.ivCircularProgress.gone()
                                        binding.ivCenterProgress.gone()
                                        binding.tvAssetConversion.visible()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            setMaxValue()
                                        }, 700)
                                    }
                                }
                                onMax = false
                            } else {
                                val convertedValue = valueAmount.toBigDecimal() / valueConversion
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
                            if (assetConversion.contains(mCurrency)) assetConversion.replace(
                                mCurrency,
                                ""
                            )
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
                        if (valueAmount.toBigDecimal() <= maxValue) {
                            binding.ivCircularProgress.gone()
                            binding.ivCenterProgress.gone()
                            binding.tvAssetConversion.visible()
//                binding.tvAssetFees.visible()
                            binding.etAmount.visible()
                        }
                    }
                    else {
                        if (valueAmount.toBigDecimal() >= minAmount.toBigDecimal()) {
                            activate = true
                            activateButton()
                        } else {
                            activate = false
                            activateButton()
                        }
                        if (valueAmount.toBigDecimal() <= maxValueOther) {
                            binding.ivCircularProgress.gone()
                            binding.ivCenterProgress.gone()
                            binding.tvAssetConversion.visible()
//                binding.tvAssetFees.visible()
                            binding.etAmount.visible()
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