package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.ConfirmationDialogBinding
import com.Lyber.databinding.FragmentAddAmountBinding
import com.Lyber.models.Whitelistings
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.Lyber.ui.fragments.bottomsheetfragments.PayWithModel
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.fadeIn
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager

class AddAmountFragment : BaseFragment<FragmentAddAmountBinding>(), View.OnClickListener {

    /* display conversion */
    private val amount get() = binding.etAmount.text!!.trim().toString()
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()

    private var selectedFrequency: String = ""
    private var canPreview: Boolean = false

    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""

    private var valueConversion: Double = 1.0

    private var maxValue: Double = 1.0

    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private val dataHolder: DataHolder = DataHolder(focusedData, unfocusedData)

    private lateinit var payMethodBottomSheet: PayWithModel
    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentAddAmountBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
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


//        binding.etAmount.showSoftInputOnFocus = false


        payMethodBottomSheet =
            PayWithModel.getToWithdraw(viewModel.withdrawAsset, ::payMethodSelected, true)

        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                requireActivity().clearBackStack()
            }
        }


        viewModel.withdrawFiatResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                viewModel.allMyPortfolio = ""
                requireActivity().clearBackStack()
            }
        }

        if (viewModel.selectedFrequency.isNotEmpty()) frequencySelected(viewModel.selectedFrequency)

        prepareView()

        binding.etAmount.addTextChangedListener(textOnTextChange)

    }

    @SuppressLint("SetTextI18n")
    private fun prepareView() {

        binding.apply {

//            tvBalance.text = "${App.prefsManager.getBalance()}${Constants.EURO}"

            when (viewModel.selectedOption) {

                Constants.USING_DEPOSIT -> {
                    btnAddFrequency.gone()
                    tvAssetConversion.gone()
                    tvSubTitle.gone()
                    tvTitle.text = requireContext().getString(R.string.euro_deposit)
//                    maxValue = App.prefsManager.getBalance().toDouble()
                }

                Constants.USING_STRATEGY -> {

                    btnAddFrequency.visible()
                    tvAssetConversion.gone()
                    tvSubTitle.visible()
                    tvTitle.text = requireContext().getString(R.string.invest_in_my_strat)
                    mCurrency = Constants.EURO
                    mConversionCurrency = Constants.EURO
                    valueConversion = 1.0
//                    maxValue = App.prefsManager.getBalance().toDouble()
                }

                Constants.USING_SINGULAR_ASSET -> {


                    btnAddFrequency.visible()
                    tvAssetConversion.visible()
                    tvSubTitle.gone()
                    ivMax.visible()
                    rlCardInfo.gone()
                    ivRepeat.visible()

                    tvAddFrequency.text =
                        requireContext().getString(R.string.add_a_frequency_optional)

                    mCurrency = Constants.EURO
                    mConversionCurrency = viewModel.selectedAsset?.id?.uppercase() ?: ""
                    //valueConversion = viewModel.selectedAsset?.euro_amount.toString().toDouble() TODO

//                    maxValue = App.prefsManager.getBalance().toDouble()

                    tvTitle.text =
                        getString(R.string.invest_in) + viewModel.selectedAsset?.id?.uppercase()
                    tvAssetConversion.text =
                        "0.00 ${viewModel.selectedAsset?.id?.uppercase()}"


                }

                Constants.USING_EXCHANGE -> {


                    rlBalance.visible()
                    btnAddFrequency.gone()
                    ivMax.visible()
                    tvAssetConversion.visible()
                    rlCardInfo.gone()

                    btnPreviewInvestment.text = getString(R.string.preview_exchange)
                    llSwapLayout.visible()

                    viewModel.exchangeAssetFrom?.let {
/*

                        mCurrency = it.id.uppercase()
                        tvTitle.text = "Exchange ${it.id.uppercase()}"
*/

                        /* tvSubTitle.text = "${
                             it.total_balance.toString().roundFloat().commaFormatted
                         } ${it.id.uppercase()} Available"TODO*/

                        //  ivAssetSwapFrom.loadCircleCrop(it.imageUrl?:"")

                        //  tvSwapAssetFrom.text = it.id.uppercase()

                        //maxValue = it.total_balance TODO
                    }

                    viewModel.exchangeAssetTo?.let {

                        /*tvAssetConversion.text =
                            it.euro_amount.toString().roundFloat().commaFormatted TODO*/
                        //   mConversionCurrency = it.id.uppercase()

                        // ivAssetSwapTo.loadCircleCrop(it.imageUrl?:"")
                        //  tvSwapAssetTo.text = it.id.uppercase()
                    }

                    /*valueConversion = viewModel.exchangeAssetTo?.euro_amount.toString()
                        .toDouble() / viewModel.exchangeAssetFrom?.euro_amount.toString().toDouble()

                    maxValue = viewModel.exchangeAssetFrom?.total_balance ?: 1.0* TODO*/

                    binding.etAmount.setText("${0.commaFormatted}$mCurrency")

                }

                Constants.USING_WITHDRAW -> {

                    checkInternet(binding.root,requireContext()) {
                          viewModel.getWhiteListings()
                    }

                    btnAddFrequency.gone()
                    ivMax.visible()
                    ivRepeat.visible()
                    tvAssetConversion.visible()

                    mCurrency = Constants.EURO

                    rlCardInfo.visible()

                    btnPreviewInvestment.text = getString(R.string.next)

                    if (viewModel.allMyPortfolio.isNotEmpty()) {

                        tvAssetConversion.gone()
                        tvTitle.text = getString(R.string.withdraw_all_my_portfolio)
                        etAmount.setTextColor(getColor(requireContext(), R.color.purple_300))
                        ivMax.gone()
                        ivRepeat.gone()
                        rlBalance.gone()
                        groupWithdrawAllPortfolio.gone()
                        etAmount.setText(viewModel.totalPortfolio.commaFormatted + Constants.EURO)
                        btnPreviewInvestment.text = getString(R.string.preview_withdraw)

                        payMethodBottomSheet = PayWithModel.getToWithdraw(
                            viewModel.withdrawAsset, ::payMethodSelected, true, true
                        )

                        maxValue = viewModel.totalPortfolio

                        mConversionCurrency = Constants.EURO
                        valueConversion = 1.0
                        activateButton(true)
                        canPreview = true

                    } else {

                        rlBalance.gone()

                        mConversionCurrency = viewModel.withdrawAsset?.id?.uppercase() ?: ""

                        //valueConversion = (viewModel.withdrawAsset?.euro_amount ?: 1).toDouble() TODO


                        /* maxValue =
                             (viewModel.withdrawAsset?.total_balance!!) * (viewModel.withdrawAsset?.euro_amount!!)
                         TODO */
                        tvTitle.text =
                            getString(R.string.withdraw_from) + " ${viewModel.withdrawAsset?.id?.uppercase()}"

                        /* maxValue =
                             (viewModel.withdrawAsset?.total_balance!!) * (viewModel.withdrawAsset?.euro_amount!!)
                         TODO */

                        /*tvSubTitle.text = "${
                            viewModel.withdrawAsset?.total_balance.toString().commaFormatted
                        } " + "${viewModel.withdrawAsset?.id?.uppercase()} Available"
                            TODO*/
                        tvAssetConversion.text =
                            "0.00${viewModel.withdrawAsset?.id?.uppercase()}"

                    }

                }

                Constants.USING_WITHDRAW_FIAT -> {
                    checkInternet(binding.root,requireContext()) {
                        viewModel.getWhiteListings()
                    }
                    btnAddFrequency.gone()
                    ivMax.visible()
                    tvAssetConversion.gone()

                    mCurrency = Constants.EURO
                    rlCardInfo.gone()
                    btnPreviewInvestment.text = getString(R.string.next)
                    mConversionCurrency = Constants.EURO
                    valueConversion = 1.0
//                    maxValue = App.prefsManager.getBalance().toDouble()
                    tvTitle.text = getString(R.string.withdraw_from) + " Fiat"
//                    tvSubTitle.text = "${App.prefsManager.getBalance().commaFormatted} Available"

                    tvAssetConversion.text = "0.00${Constants.EURO}"


                }

                Constants.USING_SELL -> {

                    binding.rlCardInfo.gone()
                    binding.btnAddFrequency.gone()
                    binding.ivMax.visible()

                    mCurrency = Constants.EURO

                    mConversionCurrency = viewModel.selectedAsset?.id?.uppercase() ?: ""

                    // valueConversion = (viewModel.selectedAsset?.euro_amount ?: 1).toDouble() TODO

                    /* maxValue =
                         (viewModel.selectedAsset?.total_balance!!) * (viewModel.selectedAsset?.euro_amount!!)
                     */ //TODO
                    tvAssetConversion.text = "0.00${viewModel.selectedAsset?.id?.uppercase()}"

                    btnPreviewInvestment.text =
                        "${getString(R.string.sell)} ${viewModel.selectedAsset?.id?.uppercase()}"
                    binding.tvTitle.text =
                        "${getString(R.string.sell)} ${viewModel.selectedAsset?.fullName?.uppercase()}"
                    /*binding.tvSubTitle.text =
                        "${viewModel.selectedAsset?.total_balance.commaFormatted} ${viewModel.selectedAsset?.asset_id?.uppercase()}"
                    binding.tvAssetConversion.visible() TODO*/
                }

            }

            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency

            tvAssetConversion.text = "0.00${unfocusedData.currency}"


        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressed()
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

                    if (canPreview) { // amount constraint


                        viewModel.amount = if (mCurrency == focusedData.currency) {
//                            viewModel.assetAmount =
//                                assetConversion.split(mConversionCurrency)[0].pointFormat
                            amount.split(focusedData.currency)[0].pointFormat
                        } else {
//                            viewModel.assetAmount =
//                                amount.split(mConversionCurrency)[0].pointFormat
                            assetConversion.split(mCurrency)[0].pointFormat
                        }



                        when (viewModel.selectedOption) {

                            Constants.USING_SINGULAR_ASSET -> {
                                /*if (selectedFrequency.isEmpty()) {
                                    "Please select frequency".showToast(requireContext())
                                    return
                                } else*/ viewModel.selectedFrequency = selectedFrequency
                            }

                            Constants.USING_STRATEGY -> {
                                if (selectedFrequency.isEmpty()) {
                                    getString(R.string.please_select_frequency).showToast(
                                        binding.root,requireContext()
                                    )
                                    return
                                } else viewModel.selectedFrequency = selectedFrequency
                            }

                            Constants.USING_EXCHANGE -> {
                                /*    viewModel.amount = if (mCurrency == focusedData.currency) {
                                 *//*   viewModel.exchangeFromAmount =
                                        amount.split(focusedData.currency)[0].pointFormat
                                    viewModel.exchangeToAmount =
                                        assetConversion.split(mConversionCurrency)[0].pointFormat
                                    amount.split(focusedData.currency)[0].pointFormat*//*
                                } else {
                                    viewModel.exchangeFromAmount =
                                        assetConversion.split(mCurrency)[0].pointFormat
                                    viewModel.exchangeToAmount =
                                        amount.split(mConversionCurrency)[0].pointFormat
                                    assetConversion.split(mCurrency)[0].pointFormat
                                }
                            }*/
                            }

                            Constants.USING_WITHDRAW -> {

                                if (viewModel.allMyPortfolio.isNotEmpty()) {
                                    checkInternet(binding.root,requireContext()) {
                                        showProgressDialog(requireContext())
                                        viewModel.withdrawFiat(viewModel.amount)
                                    }
                                } else {
                                    requireActivity().replaceFragment(
                                        R.id.flSplashActivity, EnterWalletAddressFragment()
//                                    DepositAssetFragment()
                                    )
                                }
                                return

                            }

                            Constants.USING_WITHDRAW_FIAT -> {
                                checkInternet(binding.root,requireContext()) {
                                    showProgressDialog(requireContext())
                                    viewModel.withdrawFiat(viewModel.amount)
                                }
                                return
                            }

                            Constants.USING_SELL -> {

                                /*viewModel.selectedAsset?.let {
                                    showDialog(
                                        it.image ?: "",
                                        it.euro_amount.toString(),
                                        it.id.uppercase()
                                    )
                                }TODO*/
                                return
                            }

                        }


                        requireActivity().replaceFragment(
                            R.id.flSplashActivity, ConfirmInvestmentFragment()
                        )

                    }
                }

                rlSwapFrom -> requireActivity().onBackPressed()

                rlSwapTo -> requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AllAssetFragment().apply {
                        arguments = Bundle().apply { putString("type", "SwapTo") }
                    })

                ivSwapBetween, ivRepeat -> swapConversion()

                rlCardInfo -> payMethodBottomSheet.show(childFragmentManager, "")

                ivMax -> setMaxValue()

            }
        }
    }

    /* bottom sheet callbacks */

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
                getColor(
                    requireContext(), R.color.purple_gray_800
                )
            )
            tvAddFrequency.text = frequency
            selectedFrequency = frequency
        }
    }

    private fun payMethodSelected(
        whitelistings: Whitelistings?, string: String?
    ) {

        binding.apply {
            tvCardSubTitle.visible()
            whitelistings?.let {
                ivCard.loadCircleCrop(it.logo)
                tvCardTitle.text = it.name
                tvCardSubTitle.text = it.address
            }
        }

        payMethodBottomSheet.dismiss()
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
                    try {
                        val string =
                            amount.substring(0, amount.count() - focusedData.currency.length)

                        if (string.contains('.')) {
                            if (char != '.') etAmount.text = "$string$char${focusedData.currency}"
                        } else {
                            if (char == '.') etAmount.text = ("${
                                string.pointFormat
                            }.${focusedData.currency}")
                            else etAmount.text = ((string.pointFormat
                                .toString() + char) + focusedData.currency)
                        }


                    } catch (_: Exception) {

                    }

                }

            }
        }


    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {

//            val selection = binding.etAmount.selectionEnd - 1
            val builder = StringBuilder()

            val value =
                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat
                else amount.split(unfocusedData.currency)[0].pointFormat


            when {

                value.toDouble() > 9 -> {

//                    if (selection > 0)
//                        builder.append(value.toString().dropAt(selection).commaFormatted)
//                    else
//                    if (value.length > 4)
//                        builder.append(value.dropLast(4).commaFormatted)
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
            Log.d(TAG, "backspace: ${e.message}\n${e.localizedMessage}")
        }
    }

    fun activateButton(activate: Boolean) {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
        canPreview = activate
    }

    private fun String.dropAt(position: Int): String {
        if (position <= length) {
            val builder = StringBuilder()
            for (i in 0 until count()) if (i != position) builder.append(get(i))
            return builder.toString()
        }
        return this
    }

    private val textOnTextChange = object : OnTextChange {


        @SuppressLint("SetTextI18n")
        override fun onTextChange() {


            val valueAmount =
                if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
                else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()

            when {

                valueAmount > 0 -> {

                    activateButton(true)

                    when (focusedData.currency) {

                        mCurrency -> {

                            val assetAmount = (valueAmount / valueConversion).toString()
                            viewModel.assetAmount = assetAmount

                            binding.tvAssetConversion.text =
                                "${assetAmount.decimalPoint().commaFormatted}$mConversionCurrency"


                        }

                        else -> {

                            val convertedValue = valueAmount * valueConversion
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

//            if (valueAmount > maxValue) {
//                binding.etAmount.removeTextChangedListener(this)
//                binding.etAmount.setText("${maxValue.commaFormatted}$mCurrency")
//                binding.etAmount.addTextChangedListener(this)
//            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {

        var valueOne: String
        var valueTwo: String

        binding.rlSwapFrom.fadeIn()
        binding.rlSwapTo.fadeIn()

        if (focusedData.currency == mCurrency) {

            valueOne = amount.split(mCurrency)[0].pointFormat.decimalPoint()
            valueTwo = assetConversion.split(mConversionCurrency)[0].pointFormat.decimalPoint()

            when (viewModel.selectedOption) {

                Constants.USING_SINGULAR_ASSET -> {}
                Constants.USING_WITHDRAW -> {}
                Constants.USING_EXCHANGE -> {
                    valueOne = 0.00.toString()
                    valueTwo = 0.00.toString()
                    // maxValue = viewModel.exchangeAssetTo?.total_balance ?: 1.0 TODO
                }

                Constants.USING_SELL -> {}

            }

//            viewModel.selectedAsset?.let {
//                maxValue = it.total_balance * it.euro_amount
//            }


            focusedData.currency = mConversionCurrency
            unfocusedData.currency = mCurrency

            binding.etAmount.setText(("${valueTwo.commaFormatted}$mConversionCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mCurrency")

            viewModel.exchangeAssetFrom?.let { from ->
                viewModel.exchangeAssetTo?.let { to ->

//                    maxValue = to.total_balance

                    // binding.ivAssetSwapFrom.loadCircleCrop(to.imageUrl ?: "")

                    //binding.tvSwapAssetFrom.text = to.id.uppercase()

                    // binding.ivAssetSwapTo.loadCircleCrop(from.imageUrl ?: "")

                    //  binding.tvSwapAssetTo.text = from.id.uppercase()


                }
            }


        } else {

            valueOne = amount.split(mConversionCurrency)[0].pointFormat.decimalPoint()
            valueTwo = assetConversion.split(mCurrency)[0].pointFormat.decimalPoint()

//            viewModel.selectedAsset?.let {
//                maxValue = App.prefsManager.getBalance().toDouble()
//            }


            when (viewModel.selectedOption) {
                Constants.USING_SINGULAR_ASSET -> {}
                Constants.USING_WITHDRAW -> {}
                Constants.USING_EXCHANGE -> {
                    valueOne = 0.00.toString()
                    valueTwo = 0.00.toString()
                    //maxValue = viewModel.exchangeAssetFrom?.total_balance!! TODO
                }

                Constants.USING_SELL -> {}
            }

            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency

            binding.etAmount.setText(("${valueTwo.commaFormatted}$mCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mConversionCurrency")

            viewModel.exchangeAssetFrom?.let { from ->
                viewModel.exchangeAssetTo?.let { to ->

//                    maxValue = from.total_balance

                    //binding.ivAssetSwapFrom.loadCircleCrop(from.imageUrl ?: "")

                    //  binding.tvSwapAssetFrom.text = from.id.uppercase()

                    //binding.ivAssetSwapTo.loadCircleCrop(to.imageUrl ?: "")

                    //  binding.tvSwapAssetTo.text = to.id.uppercase()

                }
            }


        }

    }

    @SuppressLint("SetTextI18n")
    private fun setMaxValue() {
        when (viewModel.selectedOption) {

            Constants.USING_WITHDRAW -> binding.etAmount.setText("${maxValue.commaFormatted}${Constants.EURO}")

            Constants.USING_EXCHANGE -> {

                if (amount.contains(focusedData.currency)) {
                    binding.etAmount.setText("${maxValue.commaFormatted}${focusedData.currency}")
                } else {
                    binding.etAmount.setText("${maxValue.commaFormatted}${unfocusedData.currency}")
                }
//                if (focusedData.currency == mCurrency) binding.etAmount.setText("${maxValue.commaFormatted}$mCurrency")
//                else binding.etAmount.setText("${maxValue.commaFormatted}$mConversionCurrency")
            }

            Constants.USING_SELL -> {
                if (amount.contains(focusedData.currency)) {
                    binding.etAmount.setText("${maxValue.commaFormatted}${focusedData.currency}")
                } else {
                    binding.etAmount.setText("${maxValue.commaFormatted}${unfocusedData.currency}")
                }
//                binding.etAmount.setText("${maxValue.commaFormatted}${Constants.EURO}")
            }

            Constants.USING_STRATEGY -> {
//                binding.etAmount.setText("${App.prefsManager.getBalance().commaFormatted}${Constants.EURO}")
            }

            Constants.USING_WITHDRAW_FIAT -> {
                if (viewModel.allMyPortfolio.isEmpty()) {
//                    binding.etAmount.setText("${App.prefsManager.getBalance().commaFormatted}${Constants.EURO}")
                }
            }

            Constants.USING_SINGULAR_ASSET -> {

                if (amount.contains(focusedData.currency)) {
                    binding.etAmount.setText("${maxValue.commaFormatted}${focusedData.currency}")
                } else {
                    binding.etAmount.setText("${maxValue.commaFormatted}${unfocusedData.currency}")
                }

//                binding.etAmount.setText("${maxValue.commaFormatted}${Constants.EURO}")
            }


        }
    }

    private val String.pointFormat
        get() = replace(",", "", true)

    private fun showDialog(logo: String, amount: String, assetSymbol: String) {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            ConfirmationDialogBinding.inflate(layoutInflater).let {

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(true)
                setContentView(it.root)

                it.ivAsset.loadCircleCrop(logo)

                it.tvTitle.text = getString(R.string.successfully_sold)
                it.tvAssetAmount.text = amount.commaFormatted

                it.tvMessage.text =
                    "${getString(R.string.you_just_sold)} ${assetSymbol.uppercase()} ${amount.commaFormatted}"

                it.root.setOnClickListener {
                    dismiss()
                }

                setOnDismissListener {
                    requireActivity().clearBackStack()
                }

                show()
            }
        }
    }

    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)

    data class DataHolder(var focusedData: ValueHolder, var unfocusedData: ValueHolder)

    companion object {
        private const val TAG = "AddAmountFragment"
    }

}

