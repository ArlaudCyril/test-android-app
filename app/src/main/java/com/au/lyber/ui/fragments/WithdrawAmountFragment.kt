package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentWithdrawAmountBinding
import com.au.lyber.models.WithdrawAddress
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.WithdrawalAddressBottomSheet
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.load
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import java.math.RoundingMode

class WithdrawAmountFragment : BaseFragment<FragmentWithdrawAmountBinding>(), View.OnClickListener {
    override fun bind() = FragmentWithdrawAmountBinding.inflate(layoutInflater)
    private var valueConversion: Double = 1.0
    private var minAmount: String = "0.0"
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()
    private var maxValue: Double = 0.0
    private lateinit var viewModel: PortfolioViewModel
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private val addresses : MutableList<WithdrawAddress> = mutableListOf()
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
        binding.btnAddFrequency.setOnClickListener(this)
        binding.ivMax.setOnClickListener(this)
        binding.btnPreviewInvestment.setOnClickListener(this)
        prepareView()
        setObservers()
        binding.etAmount.addTextChangedListener(textOnTextChange)
        CommonMethods.checkInternet(requireActivity()) {
            CommonMethods.showProgressDialog(requireActivity())
            viewModel.getWithdrawalAddresses()
        }
    }
    private fun setObservers() {
        viewModel.withdrawalAddresses.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                addresses.clear()
                for (address in it.data) {
                    if (address.network == viewModel.selectedNetworkDeposit!!.id) {
                        addresses.add(address)
                    }
                }
                if (addresses.size>0){
                    binding.includedAsset.apply {
                        val withdrawAddress = addresses[0]
                        viewModel.withdrawAddress = withdrawAddress
                        tvAssetName.text = withdrawAddress!!.name
                        tvAssetNameCode.text = withdrawAddress.address
                        val assest =
                            BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == withdrawAddress.network } }
                        ivAssetIcon.load(assest!!.imageUrl)
                    }
                }
            }
        }
    }

    private val textOnTextChange = object : OnTextChange {


        @SuppressLint("SetTextI18n")
        override fun onTextChange() {


            val valueAmount =
                if (amount.contains(mCurrency)) amount.replace(mCurrency,"").pointFormat.toDouble()
                else amount.replace(mConversionCurrency,"").pointFormat.toDouble()


            when {

                valueAmount > 0 -> {
                    if (focusedData.currency.contains(mCurrency)){
                        val assetAmount = (valueAmount * valueConversion).toString()
                        viewModel.assetAmount = assetAmount
                        setAssesstAmount(assetAmount)
                    }else{
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
            if (focusedData.currency.contains(mCurrency)){
                val valueAmountNew =
                    if (assetConversion.contains(mCurrency)) assetConversion.replace(mCurrency,"")
                        .replace("~","").pointFormat.toDouble()
                    else assetConversion.replace(mConversionCurrency,"")
                        .replace("~","").pointFormat.toDouble()
                if (valueAmountNew >= minAmount.toDouble()) {
                    activateButton(true)
                }else{
                    activateButton(false)
                }
            }else{
                if (valueAmount >= minAmount.toDouble()) {
                    activateButton(true)
                }else{
                    activateButton(false)
                }
            }

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    private fun setAssesstAmount(assetAmount: String) {
        val valueAmount =
            if (amount.contains(mCurrency)) amount.replace(mCurrency,"").pointFormat.toDouble()
            else amount.replace(mConversionCurrency,"").pointFormat.toDouble()

        if (focusedData.currency.contains(mCurrency)) {
            val priceCoin = valueAmount.toDouble()
                .div(assetAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${assetAmount.formattedAsset(priceCoin,RoundingMode.DOWN)} $mConversionCurrency"
        }else{
            val priceCoin = assetAmount.toDouble()
                .div(valueAmount.toDouble())
            binding.tvAssetConversion.text =
                "~${assetAmount.formattedAsset(priceCoin,RoundingMode.DOWN)} $mCurrency"
        }


    }

    fun activateButton(activate: Boolean) {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }

    private fun prepareView() {
        viewModel.withdrawAddress = null
        binding.apply {
            listOf(
                tvTitle,
                tvSubTitle,
                etAmount,
                tvAssetConversion,
                ivMax,
                ivRepeat
            ).visible()
            binding.etAmount.text = "0 €"
            includedAsset.ivAssetIcon.setImageResource(R.drawable.addaddressicon)
            includedAsset.tvAssetName.text = getString(R.string.add_an_address)
            includedAsset.tvAssetNameCode.text = getString(R.string.unlimited_withdrawl)
            includedAsset.tvAssetNameCode.visible()
            viewModel.selectedAssetDetail.let {
                mCurrency = " "+Constants.EURO
                mConversionCurrency = " "+it!!.id.uppercase()
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
                valueConversion =
                    (balance.balanceData.balance.toDouble() / balance.balanceData.euroBalance.toDouble())
                setAssesstAmount("0.0")
            }
            viewModel.selectedNetworkDeposit.let {
                val balance =
                    BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble())
                "${getString(R.string.fees)} ${it!!.withdrawFee} ${balance.id.uppercase()}".also {
                    tvAssetFees.text = it
                }
                minAmount = it.withdrawMin.toString().formattedAsset(priceCoin, RoundingMode.DOWN)
                (getString(R.string.minimum_withdrawl) + ": " + minAmount + " " + balance.id.uppercase()).also {
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
                btnAddFrequency -> openAddressSheet()
                btnPreviewInvestment -> {
                    val amountFinal = if (focusedData.currency.contains(mCurrency)) {
                        assetConversion.replace(mConversionCurrency,"").replace("~","")
                    } else {
                        amount.replace(mConversionCurrency,"")
                    }
                    if (maxValue >= amountFinal.toDouble()) {
                        if (viewModel.withdrawAddress!=null) {
                            val bundle = Bundle().apply {
                                putString(Constants.EURO, amountFinal)
                            }
                            findNavController().navigate(
                                R.id.confirmWithdrawalFragment, bundle
                            )
                        }else{
                            getString(R.string.select_address).showToast(requireActivity())
                        }
                    } else {
                        getString(R.string.insufficient_balance).showToast(requireActivity())
                    }
                }
            }
        }
    }

    private fun openAddressSheet() {
        WithdrawalAddressBottomSheet(addresses,::handle).show(childFragmentManager, "")
    }

    private fun handle(withdrawAddress: WithdrawAddress?, s: String?) {
        binding.includedAsset.apply {
            viewModel.withdrawAddress = withdrawAddress
            tvAssetName.text = withdrawAddress!!.name
            tvAssetNameCode.text = withdrawAddress.address
            val assest =
                BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == withdrawAddress.network } }
            ivAssetIcon.load(assest!!.imageUrl)
        }
    }

    private fun setMaxValue() {
        val balance =
            BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }

        if (focusedData.currency.contains(mCurrency)) {
            val maxinEuro = maxValue/valueConversion
            val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                .div(maxinEuro)
            binding.etAmount.setText(
                maxinEuro.toString()
                    .formattedAsset(priceCoin, RoundingMode.DOWN) + mCurrency
            )
        } else {
            val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            binding.etAmount.setText(
                "${
                    maxValue.toString().formattedAsset(priceCoin, RoundingMode.DOWN)
                }${mConversionCurrency}"
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
            val builder = StringBuilder()

            val value =
                if (amount.contains(mCurrency)) amount.replace(mCurrency,"").pointFormat
                else amount.replace(mConversionCurrency,"").pointFormat

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
            val currency = if (focusedData.currency.contains(mCurrency)) mCurrency else mConversionCurrency
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

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {
        if (focusedData.currency.contains(mCurrency)) {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mCurrency,"").pointFormat.decimalPoint()
            val valueTwo = assetConversion.replace(mConversionCurrency,"")
                .replace("~","").pointFormat.decimalPoint()
            binding.etAmount.text = ("${valueTwo}$mConversionCurrency")

            setAssesstAmount(valueOne.toString())
        } else {
            val currency = focusedData.currency
            focusedData.currency = unfocusedData.currency
            unfocusedData.currency = currency
            val valueOne = amount.replace(mConversionCurrency,"").pointFormat.decimalPoint()
            val valueTwo = assetConversion.replace(mCurrency,"")
                .replace("~","").pointFormat.decimalPoint()

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