package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.ErrorBottomSheetBinding
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ErrorBottomSheet(val clickListener: (Boolean) -> Unit) :
    BaseBottomSheet<ErrorBottomSheetBinding>() {

    var goBackToHome = false
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
        }
        binding.btnYes.setOnClickListener {
            clickListener.invoke(true)
            goBackToHome = true
            dismiss()
        }


        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun clearBackStack1() {
        val navController = findNavController()
        navController.popBackStack(navController.graph.startDestinationId, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (tag!!.isEmpty() && !goBackToHome) {
            clearBackStack1()
            findNavController().navigate(R.id.portfolioDetailFragment)
        }
    }

}