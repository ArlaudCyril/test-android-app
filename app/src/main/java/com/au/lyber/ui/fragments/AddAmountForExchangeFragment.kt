package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.ConfirmationDialogBinding
import com.au.lyber.databinding.FragmentAddAmountBinding
import com.au.lyber.models.Whitelistings
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.au.lyber.ui.fragments.bottomsheetfragments.PayWithModel
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange

class AddAmountForExchangeFragment : BaseFragment<FragmentAddAmountBinding>(),
    View.OnClickListener {

    /* display conversion */
    private val amount get() = binding.etAmount.text.trim().toString()
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
        binding.btnAddFrequency.setOnClickListener(this)
        binding.rlCardInfo.setOnClickListener(this)
        binding.btnPreviewInvestment.setOnClickListener(this)
        binding.rlSwapFrom.setOnClickListener(this)
        binding.rlSwapTo.setOnClickListener(this)
        binding.ivSwapBetween.setOnClickListener(this)
        binding.ivRepeat.setOnClickListener(this)
        binding.ivMax.setOnClickListener(this)
        binding.rlBalance.gone()



        payMethodBottomSheet =
            PayWithModel.getToWithdraw(viewModel.withdrawAsset, ::payMethodSelected, true)

        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                requireActivity().clearBackStack()
            }
        }


        viewModel.withdrawFiatResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
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

            btnAddFrequency.gone()
            ivMax.visible()
            tvAssetConversion.visible()
            rlCardInfo.gone()

            btnPreviewInvestment.text = "Preview exchange"
            llSwapLayout.visible()

            viewModel.exchangeAssetFrom?.let { it ->

                mCurrency = it.id.uppercase()
                tvTitle.text = "Exchange ${it.id.uppercase()}"

                tvSubTitle.text = "${
                    it.balanceData.balance.roundFloat().commaFormatted
                } ${it.id.uppercase()} Available"
                val currency = BaseActivity.assets.find { it1 -> it1.id == it.id }
                ivAssetSwapFrom.loadCircleCrop(currency!!.imageUrl ?: "")
                tvSwapAssetFrom.text = it.id.uppercase()
                maxValue = it.balanceData.balance.toDouble()
            }

            viewModel.exchangeAssetTo?.let {
                val currency = BaseActivity.assets.find { it1 -> it1.id == it.id }
                /*tvAssetConversion.text =
                    it.euro_amount.toString().roundFloat().commaFormatted TODO*/
                mConversionCurrency = it.id.uppercase()

                ivAssetSwapTo.loadCircleCrop(currency!!.imageUrl ?: "")
                tvSwapAssetTo.text = it.id.uppercase()
            }

            /*    valueConversion = viewModel.exchangeAssetTo?.euro_amount.toString()
                    .toDouble() / viewModel.exchangeAssetFrom?.euro_amount.toString().toDouble()TODO*/

            maxValue = (viewModel.exchangeAssetFrom?.balanceData!!.balance.toDouble() ?: 1.0)
            binding.etAmount.setText("${0.commaFormatted}$mCurrency")





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
                        viewModel.amount = if (mCurrency == focusedData.currency) {
                            viewModel.exchangeFromAmount =
                                amount.split(focusedData.currency)[0].pointFormat
                            viewModel.exchangeToAmount =
                                assetConversion.split(mConversionCurrency)[0].pointFormat
                            amount.split(focusedData.currency)[0].pointFormat
                        } else {
                            viewModel.exchangeFromAmount =
                                assetConversion.split(mCurrency)[0].pointFormat
                            viewModel.exchangeToAmount =
                                amount.split(mConversionCurrency)[0].pointFormat
                            assetConversion.split(mCurrency)[0].pointFormat
                        }
                        findNavController().navigate(R.id.confirmInvestmentFragment)

                    }
                }

                rlSwapFrom -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.FROM_SWAP)
                    findNavController().navigate(R.id.exchangeFromFragment, bundle)
                }

                rlSwapTo -> requireActivity().onBackPressed()

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
                ContextCompat.getColor(
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
                        if (!amount.contains('.')) etAmount.setText(("0$char${focusedData.currency}"))
                    } else etAmount.setText((char + focusedData.currency))
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
                                .toString() + char).commaFormatted + focusedData.currency)
                        )
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

            valueOne = 0.00.toString()
            valueTwo = 0.00.toString()
            // maxValue = viewModel.exchangeAssetTo?.total_balance ?: 1.0 TODO


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

                    //binding.ivAssetSwapFrom.loadCircleCrop(to.imageUrl ?: "")

                    binding.tvSwapAssetFrom.text = to.id.uppercase()

                    // binding.ivAssetSwapTo.loadCircleCrop(from.imageUrl ?: "")

                    binding.tvSwapAssetTo.text = from.id.uppercase()


                }
            }


        } else {

            valueOne = amount.split(mConversionCurrency)[0].pointFormat.decimalPoint()
            valueTwo = assetConversion.split(mCurrency)[0].pointFormat.decimalPoint()

//            viewModel.selectedAsset?.let {
//                maxValue = App.prefsManager.getBalance().toDouble()
//            }
            valueOne = 0.00.toString()
            valueTwo = 0.00.toString()
            //maxValue = viewModel.exchangeAssetFrom?.total_balance!! TODO


            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency

            binding.etAmount.setText(("${valueTwo.commaFormatted}$mCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mConversionCurrency")

            viewModel.exchangeAssetFrom?.let { from ->
                viewModel.exchangeAssetTo?.let { to ->

//                    maxValue = from.total_balance

                    //binding.ivAssetSwapFrom.loadCircleCrop(from.imageUrl ?: "")

                    binding.tvSwapAssetFrom.text = from.id.uppercase()

                    //binding.ivAssetSwapTo.loadCircleCrop(to.imageUrl ?: "")

                    binding.tvSwapAssetTo.text = to.id.uppercase()

                }
            }


        }

    }

    @SuppressLint("SetTextI18n")
    private fun setMaxValue() {

        if (amount.contains(focusedData.currency)) {
            binding.etAmount.setText("${maxValue.commaFormatted}${focusedData.currency}")
        } else {
            binding.etAmount.setText("${maxValue.commaFormatted}${unfocusedData.currency}")
        }
//                if (focusedData.currency == mCurrency) binding.etAmount.setText("${maxValue.commaFormatted}$mCurrency")
//                else binding.etAmount.setText("${maxValue.commaFormatted}$mConversionCurrency")


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

                it.tvTitle.text = "Successfully sold"
                it.tvAssetAmount.text = amount.commaFormatted

                it.tvMessage.text =
                    "You just sold ${assetSymbol.uppercase()} ${amount.commaFormatted}"

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
        private const val TAG = "AddAmountForExchangeFragment"
    }

}

