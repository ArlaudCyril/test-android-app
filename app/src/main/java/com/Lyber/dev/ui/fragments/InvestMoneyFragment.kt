package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentInvestMoneyBinding
import com.Lyber.dev.ui.fragments.bottomsheetfragments.DepositOrSingularAsset
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PortfolioViewModel

class InvestMoneyFragment : BaseFragment<FragmentInvestMoneyBinding>() {

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentInvestMoneyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.strategyView.apply {
            viewModel.selectedStrategy?.let {

                radioButton.gone()
                btnInvestUsingStrategy.visible()
                btnPickAnotherStrategy.visible()

                topText = it.name ?: ""
                yeild = it.expectedYield.toString()
                risk = it.risk!!
                allocationView.setAssetsList(it.bundle)

            }

            btnPickAnotherStrategy.setOnClickListener {
                requireActivity().onBackPressed()
            }

            btnInvestUsingStrategy.setOnClickListener {
                viewModel.selectedOption = Constants.USING_STRATEGY
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AddAmountFragment(),
                )
            }
        }

        binding.tvDepositOrSingularBuy.setOnClickListener {
            DepositOrSingularAsset(::methodSelected).show(parentFragmentManager, "")
        }


    }

    private fun methodSelected(method: String) {
        viewModel.selectedOption = method
        if (method == Constants.USING_SINGULAR_ASSET)
            requireActivity().replaceFragment(R.id.flSplashActivity, SelectAnAssetFragment())
        else
            requireActivity().replaceFragment(R.id.flSplashActivity, DepositFragment())
    }


}