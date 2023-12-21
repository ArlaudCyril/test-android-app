package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.FragmentInvestMoneyBinding
import com.Lyber.ui.fragments.bottomsheetfragments.DepositOrSingularAsset
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel

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