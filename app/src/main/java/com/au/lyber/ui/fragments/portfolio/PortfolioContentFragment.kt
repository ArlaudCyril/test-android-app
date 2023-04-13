package com.au.lyber.ui.fragments.portfolio

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTestPortfolioContentBinding
import com.au.lyber.models.*
import com.au.lyber.ui.adapters.*
import com.au.lyber.ui.fragments.*
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.addTypeface
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.extractAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants

class PortfolioContentFragment : BaseFragment<FragmentTestPortfolioContentBinding>() {

    private val viewModel get() = (requireParentFragment() as PortfolioTestFragment).viewModel

    private val adapterMyAsset get() = (requireParentFragment() as PortfolioTestFragment).adapterMyAsset
    private val adapterAnalyticsAdapter get() = (requireParentFragment() as PortfolioTestFragment).adapterAnalyticsAdapter
    private val adapterRecurring get() = (requireParentFragment() as PortfolioTestFragment).adapterRecurring
    private val adapterAllAsset get() = (requireParentFragment() as PortfolioTestFragment).adapterAllAsset
    private val resourcesAdapter get() = (requireParentFragment() as PortfolioTestFragment).resourcesAdapter
    private val assetBreakdownAdapter get() = (requireParentFragment() as PortfolioTestFragment).assetBreakdownAdapter

    override fun bind() = FragmentTestPortfolioContentBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getAssetResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                if (it.assets.isEmpty()) {
                    binding.rvMyAssets.gone()
                    binding.tvMyAssets.gone()
                    binding.includedEuro.root.gone()
                    binding.rvAnalytics.gone()
                    binding.tvAnalytics.gone()
                } else {

                    viewModel.totalPortfolio = 0.0

                    it.assets.forEach {
                        viewModel.totalPortfolio += (it.coin_detail?.current_price
                            ?: 1.0) * it.total_balance
                    }


                    binding.includedEuro.root.visible()
                    binding.includedEuro.ivDropIcon.gone()
                    adapterMyAsset.setList(it.assets)
                    assetBreakdownAdapter.setList(it.assets)
//                    adapterAnalyticsAdapter.setList(getAnalyticsData())
                    if (!CommonMethods.isProgressShown())
                        binding.rvMyAssets.startLayoutAnimation()
                }

                binding.includedEuro.let { binding ->
                    binding.llFiatWallet.visible()
                    binding.ivAssetIcon.setImageResource(R.drawable.ic_euro)
                    binding.tvAssetNameCenter.text = "Euro"
                    binding.tvAssetAmountCenter.text =
                        it.total_euros_available.commaFormatted + Constants.EURO
                    binding.tvAssetAmountCenter.addTypeface(Typeface.BOLD)
                }

                CommonMethods.dismissProgressDialog()
            }
        }

    }

    fun clickedAnalytics(analyticsData: AnalyticsData) {
        viewModel.screenCount = 2
//        screenOneToThree(analyticsData)
    }

    fun recurringInvestmentClicked(investment: Investment) {
        requireActivity().replaceFragment(
            R.id.flSplashActivity,
            InvestmentDetailFragment.get(investment._id)
        )
    }

    fun investMoneyClicked(toStrategy: Boolean) {
        if (toStrategy)
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                PickYourStrategyFragment(),
                topBottom = true
            )
        else
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                SelectAnAssetFragment(),
                topBottom = true
            )
    }

    fun assetClicked(asset: Assets) {
        CommonMethods.checkInternet(requireContext()) {
            viewModel.selectedAsset = asset
            viewModel.screenCount = 1
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAssetDetail(asset.asset_name)
        }
    }

    fun availableAssetClicked(asset: priceServiceResume) {
        CommonMethods.checkInternet(requireContext()) {
            /*viewModel.selectedAsset = asset
            viewModel.screenCount = 1
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAssetDetail(asset.id)*/
        }

    }

    fun menuOptionSelected(tag: String, option: String) {

        viewModel.selectedAsset

        when (option) {

            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    SwapWithdrawFromFragment(),
                    topBottom = true
                )
            }

            "deposit" -> {

                if (viewModel.screenCount > 0)
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        ChooseAssetForDepositFragment(),
                        topBottom = true
                    )
                else requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    DepositFiatWalletFragment(),
                    topBottom = true
                )
            }

            "exchange" -> {
                viewModel.selectedOption = Constants.USING_EXCHANGE
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    SwapWithdrawFromFragment(),
                    topBottom = true
                )
            }

            "sell" -> {
                viewModel.selectedOption = Constants.USING_SELL
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AddAmountFragment()
                )
            }
        }
    }

}