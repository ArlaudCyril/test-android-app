package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.databinding.FragmentEnterWalletAddressBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel

class EnterWalletAddressFragment : BaseFragment<FragmentEnterWalletAddressBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    private val address: String get() = binding.etWalletAddress.text.trim().toString()
    override fun bind() = FragmentEnterWalletAddressBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

//        viewModel.withdrawResponse.observe(viewLifecycleOwner) {
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                ConfirmationBottomSheet().show(childFragmentManager, "")
//            }
//        }

        viewModel.withdrawFiatResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                ConfirmationBottomSheet().show(childFragmentManager, "")
            }
        }

        viewModel.withdrawResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                ConfirmationBottomSheet().show(childFragmentManager, "")
            }
        }

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.btnWithdraw.setOnClickListener {
            if (address.isNotEmpty())
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    if (viewModel.allMyPortfolio.isNotEmpty())
                        viewModel.withdrawFiat(viewModel.amount)
                    else {
                        viewModel.selectedAsset?.let {
                            viewModel.withdraw(it.id,viewModel.amount,viewModel.assetAmount.toFloat(),address)
                        }
                    }
                }
            else {
                "Please enter wallet address".showToast(requireContext())
                binding.etWalletAddress.requestKeyboard()
            }
        }
    }

}