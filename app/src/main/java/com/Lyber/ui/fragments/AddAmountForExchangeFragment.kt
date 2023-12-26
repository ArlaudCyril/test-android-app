package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentAddAmountBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.BaseActivity
import com.Lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.utils.CommonMethods.Companion.fadeIn
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.invisible
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.math.RoundingMode

class AddAmountForExchangeFragment : BaseFragment<FragmentAddAmountBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {

    /* display conversion */
    private val amount get() = binding.etAmount.text!!.trim().toString()
    private val assetConversion get() = binding.tvAssetConversion.text.trim().toString()

    private var selectedFrequency: String = ""
    private var canPreview: Boolean = false

    private var minAmount: Double = 0.0
    private var mCurrency: String = ""
    private var mConversionCurrency: String = ""
    private var maxValue: String = "1.0"
    private val focusedData: ValueHolder = ValueHolder()
    private val unfocusedData: ValueHolder = ValueHolder()
    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentAddAmountBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filter = IntentFilter()
        filter.addAction("AssestUpdateChanges")
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

        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                requireActivity().clearBackStack()
            }
        }

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
            val balance = com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
            mCurrency = " "+balance!!.id.uppercase()
            tvTitle.text = "Exchange ${balance.id.uppercase()}"
            val priceCoin =balance.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            tvSubTitle.text = "${
                balance.balanceData.balance.formattedAsset(priceCoin, RoundingMode.DOWN)
            } ${balance.id.uppercase()} Available"
            val currency = com.Lyber.ui.activities.BaseActivity.assets.find { it1 -> it1.id ==balance.id }
            ivAssetSwapFrom.loadCircleCrop(currency!!.imageUrl)
            tvSwapAssetFrom.text = balance.id.uppercase()
            minAmount = (balance.balanceData.balance.toDouble() /balance.balanceData.euroBalance.toDouble())
            tvMinAmount.text = getString(
                R.string.the_minimum_amount_to_be_exchanged_1,
                minAmount.toString().decimalPoint(), balance.id.uppercase()
            )
            val balanceTo = com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetTo }
            tvAssetConversion.text = balanceTo!!.priceServiceResumeData
                .lastPrice.commaFormatted
            mConversionCurrency =" "+balanceTo.id.uppercase()
            val data =
                com.Lyber.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo} }
            ivAssetSwapTo.loadCircleCrop(data!!.imageUrl)
            tvSwapAssetTo.text =balanceTo.id.uppercase()
            maxValue = balance.balanceData.balance
            binding.etAmount.text = "${0.commaFormatted}$mCurrency"
            focusedData.currency = mCurrency
            unfocusedData.currency = mConversionCurrency
            tvAssetConversion.text = "0.00${unfocusedData.currency}"
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

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

                btnAddFrequency -> FrequencyModel(::frequencySelected).show(
                    parentFragmentManager, ""
                )

                btnPreviewInvestment -> {

                    if (canPreview) {
                        hitAPi()
                    }
                }

                rlSwapFrom -> {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.FROM_SWAP)
                    findNavController().navigate(R.id.exchangeFromFragment, bundle)
                }

                rlSwapTo -> requireActivity().onBackPressedDispatcher.onBackPressed()

                ivSwapBetween, ivRepeat -> {
                    if (CommonMethods.getBalance(viewModel.exchangeAssetTo!!) != null) {
                        swapConversion()
                    } else {
                        getString(R.string.you_don_t_have_balance_to_exchange).showToast(
                            requireActivity()
                        )
                    }
                }
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
            focusedData.currency.lowercase(),
            unfocusedData.currency.lowercase(),
            amount.split(focusedData.currency)[0].pointFormat
        )


    }

    /* bottom sheet callbacks */

    private fun conversionFormula(): String {
        val valueAmount =
            if (amount.contains(focusedData.currency)) amount.split(focusedData.currency)[0].pointFormat.toDouble()
            else amount.split(unfocusedData.currency)[0].pointFormat.toDouble()
        val balanceFromPrice =  com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
        val balanceToPrice = com.Lyber.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetTo }
        val valueInEurosFromAsset =balanceFromPrice!!.priceServiceResumeData.lastPrice.toDouble()
        val valuesInEurosToAsset =balanceToPrice!!.priceServiceResumeData.lastPrice.toDouble()
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

                    val string = amount.substring(0, amount.count() - focusedData.currency.length)

                    if (string.contains('.')) {
                        if (char != '.') etAmount.text = "$string$char${focusedData.currency}"
                    } else {
                        if (char == '.') etAmount.text = ("${
                            string.pointFormat
                        }.${focusedData.currency}")
                        else etAmount.text = ((string.pointFormat
                            .toString() + char) + focusedData.currency)
                    }
                }

            }
        }


    }

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
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

            binding.etAmount.text = builder.toString()


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

                            val assetAmount = conversionFormula()
                            viewModel.assetAmount = assetAmount

                            binding.tvAssetConversion.text =
                                "${assetAmount}$mConversionCurrency"


                        }

                        else -> {

                            val convertedValue = conversionFormula()
                            binding.tvAssetConversion.text = "$convertedValue$mCurrency"
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

    @SuppressLint("SetTextI18n")
    private fun swapConversion() {
        binding.rlSwapFrom.fadeIn()
        binding.rlSwapTo.fadeIn()
        val toId = viewModel.exchangeAssetTo
        val fromId = viewModel.exchangeAssetFrom
        viewModel.exchangeAssetTo = fromId
        viewModel.exchangeAssetFrom = toId
        prepareView()
    }

    @SuppressLint("SetTextI18n")
    private fun setMaxValue() {
        val balance = com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom!!}
        val priceCoin = balance!!.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble())
        binding.etAmount.text = "${
            maxValue.formattedAsset(priceCoin, RoundingMode.DOWN)
        }${focusedData.currency}"
    }

    private val String.pointFormat
        get() = replace(",", "", true)


    data class ValueHolder(var value: Double = 0.0, var currency: String = Constants.EURO)


    companion object {
        private const val TAG = "AddAmountForExchangeFragment"
    }

}

