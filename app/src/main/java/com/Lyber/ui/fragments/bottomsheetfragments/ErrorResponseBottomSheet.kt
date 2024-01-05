package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Lyber.R
import com.Lyber.databinding.FragmentErrorResponseBottomSheetBinding
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
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