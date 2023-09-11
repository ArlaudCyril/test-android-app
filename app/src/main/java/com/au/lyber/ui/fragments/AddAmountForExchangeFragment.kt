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
import com.au.lyber.databinding.FragmentAddAmountBinding
import com.au.lyber.models.Whitelistings
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.au.lyber.ui.fragments.bottomsheetfragments.PayWithModel
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.invisible
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.math.RoundingMode

class AddAmountForExchangeFragment : BaseFragment<FragmentAddAmountBinding>(),
    View.OnClickListener,RestClient.OnRetrofitError {

    /* display conversion */
    private val amount get() = binding.etAmount.text!!.trim().toString()
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()

    private var selectedFrequency: String = ""
    private var canPreview: Boolean = false

    private var minAmount: Double = 0.0
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""


    private var maxValue: Double = 1.0

    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()

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



        binding.etAmount.addTextChangedListener(textOnTextChange)

    }

    override fun onResume() {
        super.onResume()
        prepareView()
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView() {
        binding.apply {

            btnAddFrequency.gone()
            ivMax.visible()
            tvAssetConversion.visible()
            rlCardInfo.gone()

            btnPreviewInvestment.text = getString(R.string.preview_exchange)
            llSwapLayout.visible()

            viewModel.exchangeAssetFrom?.let { it ->

                mCurrency = " "+it.id.uppercase()
                tvTitle.text = "Exchange ${it.id.uppercase()}"
                val priceCoin = it.balanceData.euroBalance.toDouble()
                    .div(it.balanceData.balance.toDouble())
                tvSubTitle.text = "${
                    it.balanceData.balance.formattedAsset(priceCoin, RoundingMode.DOWN)
                } ${it.id.uppercase()} Available"
                val currency = BaseActivity.assets.find { it1 -> it1.id == it.id }
                ivAssetSwapFrom.loadCircleCrop(currency!!.imageUrl)
                tvSwapAssetFrom.text = it.id.uppercase()
                minAmount =
                    (it.balanceData.balance.toDouble() / it.balanceData.euroBalance.toDouble())
                tvMinAmount.text = getString(
                    R.string.the_minimum_amount_to_be_exchanged_1,
                    minAmount.toString().decimalPoint(), it.id.uppercase()
                )

            }
            viewModel.exchangeAssetTo?.let {
                tvAssetConversion.text = viewModel.exchangeAssetTo!!.priceServiceResumeData
                    .lastPrice.commaFormatted
                mConversionCurrency =" "+it.id.uppercase()
                val data =
                    BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo!!.id } }
                ivAssetSwapTo.loadCircleCrop(data!!.imageUrl)
                tvSwapAssetTo.text = it.id.uppercase()
            }

            maxValue = viewModel.exchangeAssetFrom?.balanceData!!.balance.toDouble()
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
                            amount.split(focusedData.currency)[0].pointFormat
                        } else {
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

                        if (maxValue >= viewModel.amount.toDouble()) {
                            hitAPi()
                        } else {
                            getString(R.string.insufficient_balance).showToast(requireActivity())
                        }
                    }
                }

                rlSwapFrom -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.FROM_SWAP)
                    findNavController().navigate(R.id.exchangeFromFragment, bundle)
                }

                rlSwapTo -> requireActivity().onBackPressed()

                ivSwapBetween, ivRepeat -> {
                    if (CommonMethods.getBalance(viewModel.exchangeAssetTo!!.id) != null) {
                        swapConversion()
                    } else {
                        getString(R.string.you_don_t_have_balance_to_exchange).showToast(
                            requireActivity()
                        )
                    }
                }

                rlCardInfo -> payMethodBottomSheet.show(childFragmentManager, "")

                ivMax -> setMaxValue()

            }
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        binding.btnPreviewInvestment.text = getString(R.string.preview_exchange)
    }
    private fun hitAPi() {
        binding.progress.visible()
        binding.progress.animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
        binding.btnPreviewInvestment.text = ""
        viewModel.getQuote(
            viewModel.exchangeAssetFrom?.id ?: "",
            viewModel.exchangeAssetTo?.id ?: "",
            viewModel.exchangeFromAmount
        )
        viewModel.getQuoteResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.progress.clearAnimation()
                binding.progress.gone()
                val bundle = Bundle().apply {
                    putString(Constants.DATA_SELECTED,Gson().toJson(it.data))
                }
                findNavController().navigate(R.id.confirmExchangeFragment,bundle)

            }
        }

    }

    /* bottom sheet callbacks */

    private fun conversionFormula(): String {

        val valueAmount =
            if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
            else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
        val balanceFromPrice =  viewModel.exchangeAssetFromResume!!
        val balanceToPrice =
            viewModel.exchangeAssetTo!!.priceServiceResumeData.lastPrice
        val valueInEurosFromAsset =
            (if (focusedData.currency == mCurrency) balanceFromPrice else balanceToPrice).toDouble()
        val valuesInEurosToAsset =
            (if (focusedData.currency == mCurrency) balanceToPrice else balanceFromPrice).toDouble()

        val numberToAssets = (valueAmount * valueInEurosFromAsset) / (valuesInEurosToAsset)
        val priceCoin = valuesInEurosToAsset
            .div(numberToAssets)
        return numberToAssets.toString().formattedAsset(priceCoin,RoundingMode.DOWN)

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

            builder.append(value.dropLast(1))
            if (builder.toString().isEmpty()) {
                builder.append("0")
            }

            if (amount.contains(focusedData.currency)) builder.append(focusedData.currency)
            else builder.append(unfocusedData.currency)

            binding.etAmount.setText(builder.toString())


        } catch (e: Exception) {
            Log.d(TAG, "backspace: ${e.message}\n${e.localizedMessage}")
        }
    }

    fun activateButton(activate: Boolean) {
        if (activate) binding.tvMinAmount.invisible() else binding.tvMinAmount.visible()
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
                try {
                    if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
                    else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
                }catch (e:Exception){
                    0.0
                }

            if (valueAmount > minAmount) {
                activateButton(true)
            }
            when {

                valueAmount > 0 -> {

                    when (focusedData.currency) {

                        mCurrency -> {

                            val assetAmount = conversionFormula().toString()
                            viewModel.assetAmount = assetAmount

                            binding.tvAssetConversion.text =
                                "${assetAmount}$mConversionCurrency"


                        }

                        else -> {

                            val convertedValue = conversionFormula()
                            binding.tvAssetConversion.text = "${
                                convertedValue.toString()
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
            /*if (valueAmount > maxValue) {
                binding.etAmount.removeTextChangedListener(this)
               binding.etAmount.addTextChangedListener(this)
            }*/

        }
    }

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {

        var valueOne: String
        var valueTwo: String

        binding.rlSwapFrom.fadeIn()
        binding.rlSwapTo.fadeIn()

        if (focusedData.currency == mCurrency) {
            valueOne = 0.00.toString()
            valueTwo = 0.00.toString()



            viewModel.exchangeAssetTo?.let {
                val balance =
                    BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetTo!!.id }!!
                maxValue = balance.balanceData.balance.toDouble()
                minAmount =
                    (balance.balanceData.balance.toDouble() / balance.balanceData.euroBalance.toDouble())
                binding.tvMinAmount.text = getString(
                    R.string.the_minimum_amount_to_be_exchanged_1,
                    minAmount.toString().decimalPoint(), it.id.uppercase()
                )
            }


            focusedData.currency = mConversionCurrency
            unfocusedData.currency = mCurrency

            binding.etAmount.setText(("${valueTwo.commaFormatted}$mConversionCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mCurrency")

            viewModel.exchangeAssetFrom?.let { from ->
                viewModel.exchangeAssetTo?.let { to ->

                    val balance =
                        BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == to.id } }

                    val data1 =
                        BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo!!.id } }
                    val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                        .div(balance.balanceData.balance.toDouble())
                    binding.tvSubTitle.text = "${
                        balance.balanceData.balance.formattedAsset(priceCoin, RoundingMode.DOWN)
                    } ${balance.id.uppercase()} Available"
                    binding.ivAssetSwapFrom.loadCircleCrop(data1!!.imageUrl ?: "")

                    binding.tvSwapAssetFrom.text = to.id.uppercase()
                    val currencyFrom = BaseActivity.assets.find { it1 -> it1.id == from.id }
                    binding.ivAssetSwapTo.loadCircleCrop(currencyFrom!!.imageUrl ?: "")

                    binding.tvSwapAssetTo.text = from.id.uppercase()


                }
            }


        } else {
            valueOne = 0.00.toString()
            valueTwo = 0.00.toString()
            maxValue = viewModel.exchangeAssetFrom!!.balanceData.balance.toDouble()
            val priceCoin = viewModel.exchangeAssetFrom!!.balanceData.euroBalance.toDouble()
                .div(viewModel.exchangeAssetFrom!!.balanceData.balance.toDouble())
            binding.tvSubTitle.text = "${
                viewModel.exchangeAssetFrom!!.balanceData.balance.formattedAsset(
                    priceCoin,
                    RoundingMode.DOWN
                )
            } ${viewModel.exchangeAssetFrom!!.id.uppercase()} Available"

            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency
            minAmount =
                (viewModel.exchangeAssetFrom!!.balanceData.balance.toDouble() / viewModel.exchangeAssetFrom!!.balanceData.euroBalance.toDouble())
            binding.tvMinAmount.text = getString(
                R.string.the_minimum_amount_to_be_exchanged_1,
                minAmount.toString().decimalPoint(), viewModel.exchangeAssetFrom!!.id.uppercase()
            )
            binding.etAmount.setText(("${valueTwo.commaFormatted}$mCurrency"))
            binding.tvAssetConversion.text = ("${valueOne.commaFormatted}$mConversionCurrency")

            viewModel.exchangeAssetFrom?.let { from ->
                viewModel.exchangeAssetTo?.let { to ->
                    val currencyFrom = BaseActivity.assets.find { it1 -> it1.id == from.id }
                    binding.ivAssetSwapFrom.loadCircleCrop(currencyFrom!!.imageUrl ?: "")

                    binding.tvSwapAssetFrom.text = from.id.uppercase()
                    val data =
                        BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo!!.id } }

                    binding.ivAssetSwapTo.loadCircleCrop(data!!.imageUrl ?: "")

                    binding.tvSwapAssetTo.text = to.id.uppercase()

                }
            }


        }

    }

    @SuppressLint("SetTextI18n")
    private fun setMaxValue() {

        if (amount.contains(focusedData.currency)) {
            val priceCoin = viewModel.exchangeAssetFrom!!.balanceData.euroBalance.toDouble()
                .div(viewModel.exchangeAssetFrom!!.balanceData.balance.toDouble() ?: 1.0)
            binding.etAmount.setText(
                "${
                    maxValue.toString().formattedAsset(priceCoin, RoundingMode.DOWN)
                }${focusedData.currency}"
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

    }

    private val String.pointFormat
        get() = replace(",", "", true)


    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)

    data class DataHolder(var focusedData: ValueHolder, var unfocusedData: ValueHolder)

    companion object {
        private const val TAG = "AddAmountForExchangeFragment"
    }

}

