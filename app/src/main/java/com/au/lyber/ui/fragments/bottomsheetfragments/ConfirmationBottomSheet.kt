package com.au.lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmationBinding
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ConfirmationBottomSheet : BaseBottomSheet<FragmentConfirmationBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmationBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener {
            dismiss()
        }

        binding.btnThanks.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.portfolioHomeFragment)
        }

        when(viewModel.selectedOption){
            Constants.USING_WITHDRAW-> binding.tvInfoOne.text = getString(R.string.your_withdrawal_has_been_taken_into_account)
            else -> binding.tvInfoOne.text = getString(R.string.your_investment_has_been_taken_into_account)
        }


        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().clearBackStack()
        findNavController().navigate(R.id.portfolioHomeFragment)
    }

}