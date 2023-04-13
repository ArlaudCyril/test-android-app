package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentPortfolioBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.*
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.ui.adapters.*
import com.au.lyber.ui.fragments.bottomsheetfragments.InvestBottomSheet
import com.au.lyber.ui.fragments.bottomsheetfragments.PortfolioBalanceBottomSheet
import com.au.lyber.ui.fragments.bottomsheetfragments.PortfolioThreeDots
import com.au.lyber.utils.*
import com.au.lyber.utils.CommonMethods.Companion.addTypeface
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.extractAsset
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.fadeOut
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.goneToLeft
import com.au.lyber.utils.CommonMethods.Companion.goneToRight
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.toMilli
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.CommonMethods.Companion.visibleFromLeft
import com.au.lyber.utils.CommonMethods.Companion.visibleFromRight
import com.au.lyber.utils.CommonMethods.Companion.zoomIn
import com.au.lyber.utils.CommonMethods.Companion.zoomOut
import com.au.lyber.viewmodels.PortfolioViewModel
import com.google.android.material.tabs.TabLayout

class PortfolioFragment : BaseFragment<FragmentPortfolioBinding>(), ActivityCallbacks,
    View.OnClickListener, PortfolioFragmentActions {

    /* adapters */
    private lateinit var adapterMyAsset: MyAssetAdapter
    private lateinit var adapterAnalyticsAdapter: AnalyticsAdapter
    private lateinit var adapterRecurring: RecurringInvestmentAdapter
    private lateinit var adapterAllAsset: AvailableAssetAdapter
    private lateinit var resourcesAdapter: ResourcesAdapter
    private lateinit var assetBreakdownAdapter: MyAssetAdapter

    private lateinit var assetPopUpWindow: ListPopupWindow
    private lateinit var assetPopupAdapter: AssetsPopupAdapter

    private lateinit var viewModel: PortfolioViewModel
    private var apiStarted = false

    override fun bind() = FragmentPortfolioBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        SplashActivity.activityCallbacks = this
        App.prefsManager.savedScreen = javaClass.name

        /* line chart */
        binding.lineChart.clearYAxis()
        binding.lineChart.lineWidth = 6F
        binding.lineChart.selectorPointSize = 24.px
        binding.lineChart.textSize = 16F
        binding.lineChart.heightFraction = 0.6F
        binding.lineChart.horizontalPadding = 32.px
        binding.lineChart.lineColor = getColor(requireContext(), R.color.purple_500_)
        binding.lineChart.selectorLineColor = getColor(requireContext(), R.color.purple_500_)

        ResourcesCompat.getFont(requireContext(), R.font.mabry_pro)?.let {
            binding.lineChart.textTypeface = it
        }

        requireActivity().window.statusBarColor =
            getColor(requireContext(), android.R.color.transparent)

        /* initializing adapters for recycler views */

        adapterMyAsset = MyAssetAdapter(::assetClicked)
        adapterAllAsset = AvailableAssetAdapter(::availableAssetClicked)
        adapterAnalyticsAdapter = AnalyticsAdapter(::clickedAnalytics)
        adapterRecurring = RecurringInvestmentAdapter(::recurringInvestmentClicked)
        resourcesAdapter = ResourcesAdapter()
        assetBreakdownAdapter = MyAssetAdapter(isAssetBreakdown = true)

        /* setting up recycler views */
        binding.apply {

            rvMyAssets.let {
                it.adapter = adapterMyAsset
                it.layoutManager = LinearLayoutManager(requireContext())
                it.isNestedScrollingEnabled = false
            }

            rvAnalytics.let {
                it.adapter = adapterAnalyticsAdapter
                it.layoutManager = GridLayoutManager(requireContext(), 2)
                it.addItemDecoration(ItemOffsetDecoration(8))
                it.isNestedScrollingEnabled = false
            }

            rvRecurringInvestments.let {
                it.adapter = adapterRecurring
                it.layoutManager = LinearLayoutManager(requireContext())
                it.isNestedScrollingEnabled = false
            }

            rvAllAssetsAvailable.let {
                it.adapter = adapterAllAsset
                it.isNestedScrollingEnabled = false
                it.layoutManager = GridLayoutManager(requireContext(), 3)
                it.addItemDecoration(ItemOffsetDecoration(8))
            }

            rvResources.let {
                it.adapter = resourcesAdapter
                it.layoutManager = LinearLayoutManager(requireContext()).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                it.addItemDecoration(ItemOffsetDecoration(8))
            }

            rvAssetBreaddown.let {
                it.adapter = assetBreakdownAdapter
                it.layoutManager = LinearLayoutManager(requireContext())
                binding.includedBreakDown.llFiatWallet.visible()
                binding.includedBreakDown.ivDropIcon.gone()
                binding.includedBreakDown.ivAssetIcon.setImageResource(R.drawable.ic_euro)
                binding.includedBreakDown.tvAssetAmountCenter.addTypeface(Typeface.BOLD)
                binding.includedBreakDown.tvAssetNameCenter.text = "Euro"
//                binding.includedBreakDown.tvAssetAmountCenter.text =
//                    "${App.prefsManager.user?.balance.commaFormatted}${Constants.EURO}"
            }

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
                    viewModel.chosenAssets?.let { asset ->
                        viewModel.getPrice(asset.id, (tab?.text ?: "1h").toString().lowercase())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })
        }

        /* onclick listeners */
//        binding.ivTopAction.setOnClickListener(this)
//        binding.includedMyAsset.root.setOnClickListener(this)
        binding.tvViewAll.setOnClickListener(this)
//        binding.llThreeDot.setOnClickListener(this)
//        binding.btnPlaceOrder.setOnClickListener(this)
        binding.ivProfile.setOnClickListener(this)
//        binding.includedEuro.root.setOnClickListener(this)
//        binding.tvAssetName.setOnClickListener(this)

        // to retain the state of this fragment

        /* pop up initialization */
        assetPopUpWindow = ListPopupWindow(requireContext()).apply {
            assetPopupAdapter = AssetsPopupAdapter()
            setAdapter(assetPopupAdapter)
            anchorView = binding.tvAssetName
            width = 240.px
            height = 420.px
            setOnItemClickListener { _, _, position, _ ->
                assetPopupAdapter.getItemAt(position)?.let {
                    binding.tvAssetName.text = "${it.asset_name} (${it.symbol.uppercase()})"
                    dismiss()
                    /*checkInternet(requireContext()) {
                        showProgressDialog(requireContext())
                        viewModel.getAssetDetail(it.asset_id)
                    }*/
                }
            }
        }

        binding.lineChart.clearYAxis()
        binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)

        binding.tvInvestMoney.text = "Invest Money"
        binding.tvPortfolioAssetPrice.text = "Portfolio"
        binding.tvValuePortfolioAndAssetPrice.text =
            "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"

        addObservers()

        checkInternet(requireContext()) {
            if (viewModel.screenCount == 1) {
                viewModel.getPrice(viewModel.chosenAssets?.id ?: "btc")
                viewModel.getNews(viewModel.chosenAssets?.id ?: "btc")
            }
            viewModel.getUser()
            viewModel.getAllAssetsDetail()
        }
        if(viewModel.screenCount == 0){
            //My assets Part (waiting API)
//            var listAssets = ArrayList<AssetBaseData>()
//            var asset = AssetBase(id = "btc", fullName = "Bitcoin",
//            image = "https://static.staging.lyber.com/assets/btc.png",
//            isUIActive = true,
//            isTradeActive = true,
//            isDepositActive = false,
//            isWithdrawalActive = false)
//            listAssets.add(asset)
//            adapterMyAsset.addList(listAssets)

            //Analytics Part (waiting API)


            // Recurring Investment Part (waiting API)


            // All assets available part
            viewModel.getAllAssets()



        }
        if (viewModel.screenCount == 1) {
            binding.apply {

                llTabLayout.fadeIn()
                nestedScrollView.scrollTo(0, root.top)

                includedMyAsset.root.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)

                includedMyAsset.root.setBackgroundTint(R.color.purple_gray_50)

                includedMyAsset.apply {

//                    if (!asset.image.isNullOrEmpty())
//                        ivAssetIcon.loadCircleCrop(asset.image)
//
//                    tvAssetNameCenter.text = asset.asset_name
//                    tvAssetAmount.text =
//                        (asset.euro_amount * asset.total_balance).commaFormatted + Constants.EURO
//                    tvAssetAmountInCrypto.text =
//                        "${asset.total_balance.commaFormatted} ${asset.asset_id.uppercase()}"

                }

//                tvInvestMoney.text = "Invest in ${asset.asset_id.uppercase()}"
                tvAssetVariation.visible()

                if (false) {
                    llAssetBreakdown.goneToRight()
                    llAssetPortfolio.visibleFromLeft()
                    zoomIn(btnPlaceOrder)
                    zoomIn(llThreeDot)
                } else {
                    llPortfolio.goneToLeft()
                    llAssetPortfolio.visibleFromRight()
                    ivProfile.fadeOut()
                    zoomIn(llTopToolbar)
                }

//                tvAssetName.text = "${asset.asset_name} (${asset.asset_id.uppercase()})"
//                tvPortfolioAssetPrice.text = "Price"
//                tvValuePortfolioAndAssetPrice.text =
//                    "${asset.euro_amount.commaFormatted} ${Constants.EURO}"

            }
        }


    }


    private fun addObservers() {

        viewModel.newsResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                resourcesAdapter.setList(it.data)
            }
        }

        viewModel.allAssetsDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                BaseActivity.currencies = it.data as ArrayList<AssetBaseData>
            }
        }

        viewModel.allAssets.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                adapterAllAsset.setList(it.data.subList(0,6))
            }
        }

        viewModel.priceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()

                val timeFrame =
                    (binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)?.text
                        ?: "1h").toString().lowercase()

                binding.lineChart.timeSeries =
                    it.data.prices.toTimeSeries(it.data.lastUpdate, timeFrame)
            }
        }

        viewModel.getUserResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                App.prefsManager.user = it.data
            }
        }

    }

    private fun List<String>.toTimeSeries(
        lastUpdate: String,
        tf: String = "1h"
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
        if (!straightLine)
            list.add(listOf(System.currentTimeMillis().toDouble(), 0.0))
        for (i in 0..8) list.add(listOf(System.currentTimeMillis().toDouble(), value))
        return list
    }

    /* portfolio interface implementation */

    override fun clickedAnalytics(analyticsData: AnalyticsData) {
        viewModel.screenCount = 2
        screenOneToThree(analyticsData)
    }

    override fun recurringInvestmentClicked(investment: Investment) {
        requireActivity().replaceFragment(
            R.id.flSplashActivity,
            InvestmentDetailFragment.get(investment._id)
        )
    }

    override fun investMoneyClicked(toStrategy: Boolean) {
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

    override fun assetClicked(asset: Assets) {
        checkInternet(requireContext()) {
            viewModel.selectedAsset = asset
            viewModel.screenCount = 1
            showProgressDialog(requireContext())
            viewModel.getAssetDetail(asset.asset_name)
        }
    }

    override fun availableAssetClicked(asset: priceServiceResume) {
        checkInternet(requireContext()) {
         /*   viewModel.selectedAsset = asset
            viewModel.screenCount = 1
            showProgressDialog(requireContext())
            viewModel.getAssetDetail(asset._id)*/
        }

    }

    override fun menuOptionSelected(tag: String, option: String) {

        viewModel.selectedAsset

        when (option) {

            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                if (viewModel.screenCount == 0) {
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        SwapWithdrawFromFragment(),
                        topBottom = true
                    )
                } else {
                    viewModel.withdrawAsset = viewModel.selectedAsset
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        AddAmountFragment(),
                        topBottom = true
                    )
                }
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

    /* screen transitions */

    @SuppressLint("SetTextI18n")
    private fun screenOne() {
        binding.apply {
            apiStarted = false
            llTabLayout.fadeOut()
            ivProfile.fadeIn()
            zoomOut(llTopToolbar)
            tvAssetVariation.gone()

            llAssetPortfolio.goneToRight()
            llPortfolio.visibleFromLeft()

            tvInvestMoney.text = "Invest Money"
            tvPortfolioAssetPrice.text = "Portfolio"
            tvValuePortfolioAndAssetPrice.text =
                "${viewModel.totalPortfolio.commaFormatted} ${Constants.EURO}"

            binding.lineChart.clearYAxis()
            binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun screenTwo(asset: Assets, fromThree: Boolean = false) {

        binding.apply {

            llTabLayout.fadeIn()
            nestedScrollView.scrollTo(0, root.top)
            includedMyAsset.root.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)
            includedMyAsset.root.setBackgroundTint(R.color.purple_gray_50)

            includedMyAsset.apply {

                if (!asset.image.isNullOrEmpty())
                    ivAssetIcon.loadCircleCrop(asset.image)

                tvAssetNameCenter.text = asset.asset_name
                tvAssetAmount.text =
                    (asset.euro_amount * asset.total_balance).commaFormatted + Constants.EURO
                tvAssetAmountInCrypto.text =
                    "${asset.total_balance.commaFormatted} ${asset.asset_id.uppercase()}"

            }

            tvInvestMoney.text = "Invest in ${asset.asset_id.uppercase()}"
            tvAssetVariation.visible()

            if (fromThree) {
                llAssetBreakdown.goneToRight()
                llAssetPortfolio.visibleFromLeft()
                zoomIn(btnPlaceOrder)
                zoomIn(llThreeDot)
            } else {
                llPortfolio.goneToLeft()
                llAssetPortfolio.visibleFromRight()
                ivProfile.fadeOut()
                zoomIn(llTopToolbar)
            }

            tvAssetName.text = "${asset.asset_name} (${asset.asset_id.uppercase()})"
            tvPortfolioAssetPrice.text = "Price"
            tvValuePortfolioAndAssetPrice.text =
                "${asset.euro_amount.commaFormatted} ${Constants.EURO}"

        }
    }

    private fun screenThreeToOne() {
        binding.apply {
            apiStarted = false
            binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)

            llTabLayout.fadeOut()
            ivProfile.fadeIn()
            tvAssetVariation.fadeOut()
            llAssetBreakdown.goneToRight()
            llPortfolio.visibleFromLeft()

            tvInvestMoney.text = "Invest Money"
            tvPortfolioAssetPrice.text = "Portfolio"
            tvValuePortfolioAndAssetPrice.text =
                "${viewModel.totalPortfolio.commaFormatted} ${Constants.EURO}"

            zoomIn(llThreeDot)
            zoomIn(btnPlaceOrder)
            zoomOut(llTopToolbar)

        }
    }

    private fun screenOneToThree(info: AnalyticsData) {
        binding.apply {
            apiStarted = false
            binding.lineChart.timeSeries = getLineData(0.0, true)

            nestedScrollView.scrollTo(0, binding.root.top)
            if (info.title == "Return on Investment") {
                tvPortfolioAssetPrice.text = "Yield"
                tvValuePortfolioAndAssetPrice.text = "5.01%"
                tvAssetVariation.text = "Total earnings: 0.00€"
                tvAssetName.text = "Return on Investment"
            } else {
                llTabLayout.fadeOut()
                tvPortfolioAssetPrice.text = "Total earnings"
                tvValuePortfolioAndAssetPrice.text = "0.00€"
                tvAssetVariation.text = "Yield: 5.01%"
                tvAssetName.text = "Total Earnings"
            }

            llPortfolio.goneToLeft()
            llAssetBreakdown.visibleFromRight()

            ivProfile.fadeOut()
            tvAssetVariation.fadeIn()
            zoomIn(llTopToolbar)
            zoomOut(llThreeDot)
            zoomOut(btnPlaceOrder)

            binding.lineChart.timeSeries = getLineData(0.0, true)
        }

    }

    //Analytics data
    private fun getAnalyticsData(): List<AnalyticsData> {
        val list = mutableListOf<AnalyticsData>()
        list.add(AnalyticsData("Total earnings", "0.00€", CommonMethods.getLineData()))
        list.add(AnalyticsData("Return on Investment", "0.00%", CommonMethods.getLineData()))
        return list
    }

    // Resources data
    private fun getResourcesData(): List<Resources> {
        val list = mutableListOf<Resources>()
        list.add(Resources(R.drawable.resources_one, "Bitcoin 101, from basics to advance ..."))
        list.add(Resources(R.drawable.resources_two, "Elon Musk tips on trading bitcoins"))
        list.add(Resources(R.drawable.resources_three, "What’s Dollar Cost Averaging ?"))
        return list
    }

    override fun onBackPressed(): Boolean {
        apiStarted = false
        return when (viewModel.screenCount) {
            1 -> {
                screenOne()
                viewModel.screenCount--
                false
            }
            2 -> {
                screenThreeToOne()
                viewModel.screenCount = 0
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: ")
        SplashActivity.activityCallbacks = null
        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                includedEuro.root -> requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AddPaymentMethodFragment(),
                    topBottom = true
                )


                btnPlaceOrder -> {
                    when (viewModel.screenCount) {

                        1 -> {

                            viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
                            viewModel.selectedAsset
                            requireActivity().replaceFragment(
                                R.id.flSplashActivity,
                                AddAmountFragment()
                            )

                        }

                        else -> InvestBottomSheet(
                            ::investMoneyClicked
                        ).show(childFragmentManager, "")


                    }
                }

                ivProfile -> requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    ProfileFragment(),
                    topBottom = true
                )

                llThreeDot ->
                    PortfolioThreeDots(::menuOptionSelected).show(childFragmentManager, "")

                tvViewAll ->
                    requireActivity().replaceFragment(R.id.flSplashActivity, AllAssetFragment())

                includedMyAsset.root ->
                    PortfolioBalanceBottomSheet().show(childFragmentManager, "")


                ivTopAction -> requireActivity().onBackPressed()

                tvAssetName -> {
                    if (viewModel.screenCount == 1)
                        requireActivity().replaceFragment(
                            R.id.flSplashActivity,
                            SearchAssetsFragment(),
                            topBottom = true
                        )

//                    if (assetPopupAdapter.hasNoData()) {
//                        assetPopupAdapter.addProgress()
//                        assetPopUpWindow.show()
//                    } else
//                        if (assetPopUpWindow.isShowing)
//                            assetPopUpWindow.dismiss()
//                        else assetPopUpWindow.show()

                }

            }
        }
    }

    private fun getPriceChart(assetId: String, duration: Duration) {
        checkInternet(requireContext()) {
            binding.lineChart.animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
            viewModel.getPriceGraph(assetId, duration)
        }
    }

    private class AssetsPopupAdapter : BaseAdapter() {

        private val list = mutableListOf<GetAssetsResponseItem?>()

        fun getItemAt(position: Int): GetAssetsResponseItem? {
            return list[position]
        }

        fun hasNoData(): Boolean {
            return list.isEmpty()
        }

        fun addProgress() {
            if (!list.contains(null))
                list.add(null)
            notifyDataSetChanged()
        }

        fun removeProgress() {
            list.last()?.let {
                list.remove(it)
                notifyDataSetChanged()
            }
        }


        fun setData(items: List<GetAssetsResponseItem?>) {
            list.clear()
            list.addAll(items)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return list.count()
        }

        override fun getItem(position: Int): Any? {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return System.currentTimeMillis()
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (list[position] == null)
                LoaderViewBinding.inflate(LayoutInflater.from(parent?.context), parent, false).let {
                    it.ivLoader.animation =
                        AnimationUtils.loadAnimation(it.ivLoader.context, R.anim.rotate_drawable)
                    return it.root
                }
            else
                TextView(parent?.context, null, 0, R.style.SubheadMabry).apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    gravity = Gravity.CENTER
                    updatePadding(20.px, 12.px, 20.px, 12.px)
                    list[position]?.let { data ->
                        text = "${data.asset_name} (${data.symbol.uppercase()})"
                    }

                    return this

                }

        }
    }

    companion object {
        private const val TAG = "PortfolioFragment"
    }

}


