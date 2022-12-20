package com.au.lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.au.lyber.databinding.FragmentConfirmationBinding
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PortfolioViewModel
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
        }

        when(viewModel.selectedOption){
            Constants.USING_WITHDRAW-> binding.tvInfoOne.text = "Your withdrawal has been taken into account."
            else -> binding.tvInfoOne.text = "Your investment has been taken into account."
        }


        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().clearBackStack()
    }

}