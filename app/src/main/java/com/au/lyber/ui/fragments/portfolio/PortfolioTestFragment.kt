package com.au.lyber.ui.fragments.portfolio

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTestPortfolioBinding
import com.au.lyber.models.AnalyticsData
import com.au.lyber.models.Assets
import com.au.lyber.models.Data
import com.au.lyber.models.Investment
import com.au.lyber.ui.adapters.*
import com.au.lyber.ui.fragments.*
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.extractAsset
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PortfolioViewModel

class PortfolioTestFragment : BaseFragment<FragmentTestPortfolioBinding>() {

    lateinit var viewModel: PortfolioViewModel

    lateinit var adapterMyAsset: MyAssetAdapter
    lateinit var adapterAnalyticsAdapter: AnalyticsAdapter
    lateinit var adapterRecurring: RecurringInvestmentAdapter
    lateinit var adapterAllAsset: AvailableAssetAdapter
    lateinit var resourcesAdapter: ResourcesAdapter
    lateinit var assetBreakdownAdapter: MyAssetAdapter



    override fun bind() = FragmentTestPortfolioBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        adapterMyAsset = MyAssetAdapter(::assetClicked)
        assetBreakdownAdapter = MyAssetAdapter(isAssetBreakdown = true)
        adapterAnalyticsAdapter = AnalyticsAdapter(::clickedAnalytics)
        adapterRecurring = RecurringInvestmentAdapter(::recurringInvestmentClicked)
        adapterAllAsset = AvailableAssetAdapter(::availableAssetClicked)
        resourcesAdapter = ResourcesAdapter()

        replaceFragment(R.id.flHead, PortfolioHeadFragment())
        replaceFragment(R.id.flContent, PortfolioContentFragment())

        checkInternet(requireContext()) {
            if (viewModel.notHaveData())
                showProgressDialog(requireContext())
            viewModel.getAssets()
            viewModel.getCoins(1, 6, Constants.VOLUME_DESC)
            viewModel.getRecurringInvestments()
            viewModel.getTransactions()
            viewModel.assetsToChoose()
        }

    }


    private fun clickedAnalytics(analyticsData: AnalyticsData) {
        viewModel.screenCount = 2
//        screenOneToThree(analyticsData)
    }

    private fun recurringInvestmentClicked(investment: Investment) {
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

    private fun assetClicked(asset: Assets) {
        CommonMethods.checkInternet(requireContext()) {
            viewModel.selectedAsset = asset
            viewModel.screenCount = 1
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAssetDetail(asset.asset_name)
        }
    }

    private fun availableAssetClicked(asset: Data) {
        CommonMethods.checkInternet(requireContext()) {
            viewModel.selectedAsset = asset.extractAsset()
            viewModel.screenCount = 1
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAssetDetail(asset.id)
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



    fun replaceFragment(id: Int, fragment: Fragment) {
        childFragmentManager.beginTransaction().replace(id, fragment).commit()
    }


}