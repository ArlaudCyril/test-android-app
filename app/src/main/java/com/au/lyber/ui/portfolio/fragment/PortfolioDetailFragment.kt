package com.au.lyber.ui.portfolio.fragment

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentPortfolioDetailBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.Duration
import com.au.lyber.models.Resources
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.ui.adapters.AvailableAssetAdapter
import com.au.lyber.ui.adapters.BalanceAdapter
import com.au.lyber.ui.adapters.ResourcesAdapter
import com.au.lyber.ui.fragments.AddAmountFragment
import com.au.lyber.ui.fragments.BaseFragment
import com.au.lyber.ui.fragments.ChooseAssetForDepositFragment
import com.au.lyber.ui.fragments.DepositFiatWalletFragment
import com.au.lyber.ui.fragments.PickYourStrategyFragment
import com.au.lyber.ui.fragments.SearchAssetsFragment
import com.au.lyber.ui.fragments.SelectAnAssetFragment
import com.au.lyber.ui.fragments.SwapWithdrawFromFragment
import com.au.lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDots
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.toMilli
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.CommonMethods.Companion.visibleFromRight
import com.au.lyber.utils.Constants
import com.au.lyber.utils.ItemOffsetDecoration
import com.google.android.material.tabs.TabLayout

class PortfolioDetailFragment : BaseFragment<FragmentPortfolioDetailBinding>(),
    View.OnClickListener {

    /* adapters */
    private lateinit var adapterBalance: BalanceAdapter
    private lateinit var resourcesAdapter: ResourcesAdapter
    private lateinit var assetBreakdownAdapter: BalanceAdapter

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentPortfolioDetailBinding.inflate(layoutInflater)

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

        App.prefsManager.savedScreen = javaClass.name


        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), android.R.color.transparent)

        /* initializing adapters for recycler views */
        adapterBalance = BalanceAdapter()
        resourcesAdapter = ResourcesAdapter()
        assetBreakdownAdapter = BalanceAdapter(isAssetBreakdown = true)

        /* setting up recycler views */
        binding.apply {

            rvResources.let {
                it.adapter = resourcesAdapter
                it.layoutManager = LinearLayoutManager(requireContext()).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                it.addItemDecoration(ItemOffsetDecoration(8))
            }

            includedMyAsset.let {


                it.tvAssetAmount.text = viewModel.selectedBalance?.balanceData?.euroBalance
                it.tvAssetAmountInCrypto.text = viewModel.selectedBalance?.balanceData?.balance
            }

            viewModel.selectedAsset?.id?.let { viewModel.getAssetDetail(it) }


            includedMyAsset.root.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)
            includedMyAsset.root.setBackgroundTint(R.color.purple_gray_50)
            includedMyAsset.root.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)
            includedMyAsset.root.setBackgroundTint(R.color.purple_gray_50)

            tvInvestMoney.text = "Invest in ${viewModel.selectedAsset?.id?.uppercase()}"
            tvAssetName.text = "${viewModel.selectedAsset?.fullName}"
            tvAssetName.typeface = context?.resources?.getFont(R.font.mabry_pro_medium)



        }

        /* setting up tabs */
        binding.tabLayout.let {

//            it.addTab(it.newTab().apply { text = "1H" })

            it.addTab(it.newTab().apply { text = "1H" })
            it.addTab(it.newTab().apply { text = "4H" })
            it.addTab(it.newTab().apply { text = "1D" })
            it.addTab(it.newTab().apply { text = "1W" })
            it.addTab(it.newTab().apply { text = "1M" })
            it.addTab(it.newTab().apply { text = "1Y" })
//            it.addTab(it.newTab().apply { text = "All" })

            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewModel.selectedAsset?.let { asset ->
                        viewModel.getPrice(asset.id, (tab?.text ?: "1h").toString().lowercase())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })
        }

        /* onclick listeners */
        binding.ivTopAction.setOnClickListener(this)
//        binding.includedMyAsset.root.setOnClickListener(this)
        binding.llThreeDot.setOnClickListener(this)
        binding.btnPlaceOrder.setOnClickListener(this)
        binding.screenContent.setOnClickListener(this)
//        binding.includedEuro.root.setOnClickListener(this)
//        binding.tvAssetName.setOnClickListener(this)

        // to retain the state of this fragment

        /* pop up initialization */

        binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)

        binding.tvInvestMoney.text = "Invest Money"
        binding.tvValuePortfolioAndAssetPrice.text =
            "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"

        addObservers()

        CommonMethods.checkInternet(requireContext()) {

            viewModel.getPrice(viewModel.selectedAsset?.id ?: "btc")
            viewModel.getNews(viewModel.selectedAsset?.id ?: "btc")
        }

    }


    private fun addObservers() {

        viewModel.newsResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                resourcesAdapter.setList(it.data)
            }
        }

        viewModel.priceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()

                val timeFrame =
                    (binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)?.text
                        ?: "1h").toString().lowercase()

                binding.lineChart.timeSeries =
                    it.data.prices.toTimeSeries(it.data.lastUpdate, timeFrame)

                binding.tvValuePortfolioAndAssetPrice.text = "${it.data.prices.last().currencyFormatted}"
            }
        }

        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                viewModel.selectedAssetDetail = it.data
                binding.includedMyAsset.tvAssetName.text = viewModel.selectedAssetDetail?.fullName
                binding.tvValueAbout.text = viewModel.selectedAssetDetail?.about?.en
                viewModel.selectedAssetDetail?.image?.let { it1 -> binding.includedMyAsset.ivAssetIcon.loadCircleCrop(it1) }
            }
        }

    }

    private fun List<String>.toTimeSeries(
        lastUpdate: String, tf: String = "1h"
    ): List<List<Double>> {
        val last = lastUpdate.toMilli()
        val timeSeries = mutableListOf<List<Double>>()
        val timeInterval = when (tf) {
            "1h" -> 60 * 1000
            "4h" -> 5 * 60 * 1000
            "1d" -> 30 * 60 * 1000
            "1w" -> 4 * 60 * 60 * 1000
            "1m" -> 12 * 60 * 60 * 1000
            else -> 7 * 24 * 60 * 60 * 1000
        }
        for (i in 0 until count()) {
            val date = (last - (count() - i) * timeInterval).toDouble()
            timeSeries.add(listOf(date, this[i].toDouble()))
        }
        return timeSeries
    }

    private fun List<String>.toFloats(): MutableList<Float> {
        val list = mutableListOf<Float>()
        forEach {
            list.add(it.roundFloat().toFloat())
        }
        return list

    }

    private fun getLineData(value: Double, straightLine: Boolean = false): List<List<Double>> {
        val list = mutableListOf<List<Double>>()
        if (!straightLine) list.add(listOf(System.currentTimeMillis().toDouble(), 0.0))
        for (i in 0..8) list.add(listOf(System.currentTimeMillis().toDouble(), value))
        return list
    }

    fun investMoneyClicked(toStrategy: Boolean) {
        if (toStrategy) requireActivity().replaceFragment(
            R.id.flSplashActivity, PickYourStrategyFragment(), topBottom = true
        )
        else requireActivity().replaceFragment(
            R.id.flSplashActivity, SelectAnAssetFragment(), topBottom = true
        )
    }


    fun menuOptionSelected(tag: String, option: String) {

        viewModel.selectedAsset

        when (option) {

            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                if (viewModel.screenCount == 0) {
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity, SwapWithdrawFromFragment(), topBottom = true
                    )
                } else {
                    viewModel.withdrawAsset = viewModel.selectedAsset
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity, AddAmountFragment(), topBottom = true
                    )
                }
            }

            "deposit" -> {

                if (viewModel.screenCount > 0) requireActivity().replaceFragment(
                    R.id.flSplashActivity, ChooseAssetForDepositFragment(), topBottom = true
                )
                else requireActivity().replaceFragment(
                    R.id.flSplashActivity, DepositFiatWalletFragment(), topBottom = true
                )
            }

            "exchange" -> {
                viewModel.selectedOption = Constants.USING_EXCHANGE
                requireActivity().replaceFragment(
                    R.id.flSplashActivity, SwapWithdrawFromFragment(), topBottom = true
                )
            }

            "sell" -> {
                viewModel.selectedOption = Constants.USING_SELL
                requireActivity().replaceFragment(
                    R.id.flSplashActivity, AddAmountFragment()
                )
            }
        }
    }


    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: ")
        SplashActivity.activityCallbacks = null
        super.onDestroyView()
    }

    @SuppressLint("InflateParams")
    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                btnPlaceOrder -> {
                    viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
                    viewModel.selectedAsset
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity, AddAmountFragment()
                    )
                }


                llThreeDot -> {
                    PortfolioThreeDots(::menuOptionSelected).show(
                        childFragmentManager,
                        ""
                    )
                    // Create a transparent color view
                   // _fragmentPortfolio = this@PortfolioHomeFragment
                    var transparentView = View(context)
                    transparentView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.semi_transparent_dark
                        )
                    )

                    // Set layout parameters for the transparent view
                    val viewParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )

                    // Add the transparent view to the RelativeLayout
                    screenContent.addView(transparentView, viewParams)
                }

                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()

                tvAssetName -> {
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity, SearchAssetsFragment(), topBottom = true)


                }
            }
        }
    }

    private fun getPriceChart(assetId: String, duration: Duration) {
        CommonMethods.checkInternet(requireContext()) {
            binding.lineChart.animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
            viewModel.getPriceGraph(assetId, duration)
        }
    }

}