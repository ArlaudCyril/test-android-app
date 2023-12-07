package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentInvestAddMoneyBinding
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.Constants

class InvestAddMoneyFragment: BaseFragment<FragmentInvestAddMoneyBinding>(),View.OnClickListener{
    private var selectedFrequency:String =""
    private var mCurrency: String = " USDT"
    private var minInvestPerAsset = 20f
    private var requiredAmount =0f
    private lateinit var viewModel: PortfolioViewModel
    private val amount get() = binding.etAmount.text.trim().toString()
    override fun bind()= FragmentInvestAddMoneyBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        prepareView()
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
        binding.btnPreviewInvestment.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        binding.btnAddFrequency.setOnClickListener {
            FrequencyModel(::frequencySelected).show(
                parentFragmentManager, ""
            )
        }
    }

    private fun prepareView() {
        for (asset in viewModel.selectedStrategy?.bundle!!){
            val newAmount = minInvestPerAsset / (asset.share/100)
            if(newAmount > requiredAmount){
                requiredAmount = newAmount
            }
        }
        binding.etAmount.text = "0$mCurrency"
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
                btnPreviewInvestment-> investment()
            }
        }
    }
    private fun activateButton(activate: Boolean) {
        binding.btnPreviewInvestment.background = ContextCompat.getDrawable(
            requireContext(),
            if (activate) R.drawable.button_purple_500 else R.drawable.button_purple_400
        )
    }
    private fun investment() {
        val finalAmount = amount.replace(mCurrency,"").pointFormat
        val balance = BaseActivity.balances.find { it1 -> it1.id == "usdt" }
       val aount :Float= balance?.balanceData?.balance?.toFloat() ?: 0f
        if (finalAmount.toFloat()< requiredAmount){
            getString(
                R.string.you_need_to_invest_at_least_per_asset_in_the_strategy,
                requiredAmount.toString(),
                mCurrency.uppercase()
            ).showToast(requireActivity())
        }else if (finalAmount.toFloat() > aount){
            getString(R.string.you_don_t_have_enough_to_perform_this_action, mCurrency.uppercase()).showToast(requireActivity())
        }else if (selectedFrequency.trim().isEmpty()){
            getString(R.string.please_select_the_frequency).showToast(requireActivity())
        }else{
            viewModel.amount = finalAmount
            viewModel.selectedFrequency = selectedFrequency
            val bundle = Bundle()
            bundle.putString(Constants.AMOUNT,finalAmount)
            bundle.putString(Constants.FREQUENCY,selectedFrequency)
            viewModel.selectedOption = Constants.USING_STRATEGY
            findNavController().navigate(R.id.confirmInvestmentFragment,bundle)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun type(char: Char) {

        activateButton(true)
        binding.apply {
            val currency =  mCurrency
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
    private val String.pointFormat
        get() = replace(",", "", true)

    @SuppressLint("SetTextI18n")
    private fun backspace() {
        try {
            val builder = StringBuilder()

            val value =
                amount.replace(mCurrency,"").pointFormat


            builder.append(value.dropLast(1))
            if (builder.toString().isEmpty()) {
                builder.append("0")
            }

            builder.append(mCurrency)

            binding.etAmount.text = builder.toString()


        } catch (e: Exception) {
           e.printStackTrace()
        }
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

}