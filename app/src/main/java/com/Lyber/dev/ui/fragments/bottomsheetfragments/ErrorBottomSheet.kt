package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.ErrorBottomSheetBinding
import com.Lyber.dev.ui.fragments.ChooseAssetForDepositFragment
import com.Lyber.dev.ui.fragments.SelectAssestForBuy
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ErrorBottomSheet(val clickListener: (Boolean) -> Unit) :
    BaseBottomSheet<ErrorBottomSheetBinding>() {

    var goBackToHome = false
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var fromFragment: String
    override fun bind() = ErrorBottomSheetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CommonMethods.getViewModel(requireActivity())
       if(arguments!=null && requireArguments().containsKey(Constants.FROM))
           fromFragment=requireArguments().getString(Constants.FROM).toString()
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
        if ( !goBackToHome) {
            if(::fromFragment.isInitialized){
                when(fromFragment){
                    ChooseAssetForDepositFragment::class.java.name-> findNavController().navigate(
                        R.id.action_preview_my_purchase_to_select_asset_for_deposit
                    )
                    SelectAssestForBuy::class.java.name-> findNavController().navigate(
                        R.id.action_preview_my_purchase_to_select_asset_for_buy
                    )
                }
            }
         else   findNavController().navigate(
                R.id.action_preview_my_purchase_to_detail_fragment
            )
        }
    }

}