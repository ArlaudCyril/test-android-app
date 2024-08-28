package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentEnterWalletAddressBinding
import com.Lyber.dev.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.viewmodels.PortfolioViewModel

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
            if (address.isNotEmpty()) {
                checkInternet(binding.root,requireContext()) {
                    showProgressDialog(requireContext())
                    if (viewModel.allMyPortfolio.isNotEmpty())
                        viewModel.withdrawFiat(viewModel.amount)
                    else {
                        viewModel.selectedAsset?.let {
                            viewModel.withdraw(it.id,viewModel.amount,viewModel.assetAmount.toFloat(),address)
                        }
                    }
                }
            } else {
                getString(R.string.please_enter_wallet_address).showToast(binding.root,requireContext())
                binding.etWalletAddress.requestKeyboard()
            }
        }
    }

}