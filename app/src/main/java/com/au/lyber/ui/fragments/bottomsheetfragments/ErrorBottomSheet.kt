package com.au.lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.ErrorBottomSheetBinding
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ErrorBottomSheet(val clickListener: (Boolean) -> Unit) : BaseBottomSheet<ErrorBottomSheetBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = ErrorBottomSheetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener {
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.portfolioHomeFragment)
        }
        binding.btnYes.setOnClickListener {
            clickListener.invoke(true)
            dismiss()
        }


        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().clearBackStack()
        findNavController().navigate(R.id.portfolioHomeFragment)
    }

}