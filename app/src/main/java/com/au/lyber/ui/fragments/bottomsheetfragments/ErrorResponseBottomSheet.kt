package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.au.lyber.R
import com.au.lyber.databinding.FragmentErrorResponseBottomSheetBinding
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
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