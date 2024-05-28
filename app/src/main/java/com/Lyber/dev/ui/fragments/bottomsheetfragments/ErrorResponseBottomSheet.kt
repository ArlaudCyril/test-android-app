package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import com.Lyber.dev.databinding.FragmentErrorResponseBottomSheetBinding
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.google.android.material.bottomsheet.BottomSheetBehavior


class ErrorResponseBottomSheet :  BaseBottomSheet<FragmentErrorResponseBottomSheetBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentErrorResponseBottomSheetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener {
            dismiss()
        }

        binding.btnTryAgain.setOnClickListener {
            dismiss()
        }


        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}