package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import com.Lyber.dev.databinding.FragmentErrorResponseBottomSheetBinding
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.google.android.material.bottomsheet.BottomSheetBehavior


class ErrorResponseBottomSheet :  BaseBottomSheet<FragmentErrorResponseBottomSheetBinding>() {

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentErrorResponseBottomSheetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val marginInDp = 50
        val marginInPx = (marginInDp * displayMetrics.density).toInt()
        val bottomSheet = binding.root.parent as ViewGroup
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = screenHeight - marginInPx
        bottomSheet.layoutParams = layoutParams

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener {
            dismiss()
        }

        binding.btnTryAgain.setOnClickListener {
            dismiss()
        }

    }
}