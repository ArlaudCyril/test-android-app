package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.R
import com.au.lyber.databinding.FragmentInvestMoneyBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.DepositOrSingularAsset
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel

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

                topText = it.status ?: ""
                yeild = it.yield.toString()
                risk = it.risk
                allocationView.setAssetsList(it.investment_strategy_assets)

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