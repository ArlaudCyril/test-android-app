package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.au.lyber.R
import com.au.lyber.databinding.FragmentInvestAddMoneyBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.FrequencyModel
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint

class InvestAddMoneyFragment: BaseFragment<FragmentInvestAddMoneyBinding>(){
    private var selectedFrequency:String =""
    override fun bind()= FragmentInvestAddMoneyBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddFrequency.setOnClickListener {
            FrequencyModel(::frequencySelected).show(
                parentFragmentManager, ""
            )
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