package com.au.lyber.ui.portfolio.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ListPopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentPortfolioHomeBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.*
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.ui.adapters.*
import com.au.lyber.ui.fragments.BaseFragment
import com.au.lyber.ui.fragments.bottomsheetfragments.InvestBottomSheet
import com.au.lyber.ui.portfolio.action.PortfolioFragmentActions
import com.au.lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDots
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.*
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.fadeOut
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.CommonMethods.Companion.toMilli
import com.au.lyber.utils.CommonMethods.Companion.visibleFromLeft
import com.au.lyber.utils.CommonMethods.Companion.zoomIn
import com.google.android.material.tabs.TabLayout


class PortfolioHomeFragment : BaseFragment<FragmentPortfolioHomeBinding>(), ActivityCallbacks,
    View.OnClickListener, PortfolioFragmentActions {

    /* adapters */
    private lateinit var adapterBalance: BalanceAdapter
    private lateinit var adapterRecurring: RecurringInvestmentAdapter
    private lateinit var adapterAllAsset: AvailableAssetAdapter
    private lateinit var resourcesAdapter: ResourcesAdapter
    private lateinit var assetBreakdownAdapter: BalanceAdapter

    private lateinit var assetPopUpWindow: ListPopupWindow
    private lateinit var assetPopupAdapter: AssetsPopupAdapter

    private lateinit var viewModel: PortfolioViewModel
    private var apiStarted = false
    private lateinit var navController : NavController
    override fun bind() = FragmentPortfolioHomeBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        SplashActivity.activityCallbacks = this
        App.prefsManager.savedScreen = javaClass.name

        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        requireActivity().window.statusBarColor =
            getColor(requireContext(), android.R.color.transparent)

        /* initializing adapters for recycler views */

        adapterBalance = BalanceAdapter(::assetClicked)
        adapterAllAsset = AvailableAssetAdapter(::availableAssetClicked)
        adapterRecurring = RecurringInvestmentAdapter(::recurringInvestmentClicked)
        resourcesAdapter = ResourcesAdapter()
        assetBreakdownAdapter = BalanceAdapter(isAssetBreakdown = true)

        /* setting up recycler views */
        binding.apply {

            rvMyAssets.let {
                it.adapter = adapterBalance
                it.layoutManager = LinearLayoutManager(requireContext())
                it.isNestedScrollingEnabled = false
                it.setOnClickListener(this@PortfolioHomeFragment)
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
        }

        /* setting up tabs */
        binding.tabLayout.let {

            it.addTab(it.newTab().apply { text = "1H" })
            it.addTab(it.newTab().apply { text = "4H" })
            it.addTab(it.newTab().apply { text = "1D" })
            it.addTab(it.newTab().apply { text = "1W" })
            it.addTab(it.newTab().apply { text = "1M" })
            it.addTab(it.newTab().apply { text = "1Y" })

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
        binding.llThreeDot.setOnClickListener(this)
        binding.btnPlaceOrder.setOnClickListener(this)
        binding.ivProfile.setOnClickListener(this)
        binding.screenContent.setOnClickListener(this)
//        binding.includedEuro.root.setOnClickListener(this)
//        binding.tvAssetName.setOnClickListener(this)

        // to retain the state of this fragment

        /* pop up initialization */
        assetPopUpWindow = ListPopupWindow(requireContext()).apply {
            assetPopupAdapter = AssetsPopupAdapter()
            setAdapter(assetPopupAdapter)
            //anchorView = binding.tvAssetName
            width = 240.px
            height = 420.px
            setOnItemClickListener { _, _, position, _ ->
                assetPopupAdapter.getItemAt(position)?.let {
                   // binding.tvAssetName.text = "${it.asset_name} (${it.symbol.uppercase()})"
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
            viewModel.getAllAssets()


        }
        if (viewModel.screenCount == 0) {
            //My assets Part (waiting API)
            viewModel.getBalance()

            //Analytics Part (waiting API)


            // Recurring Investment Part (waiting API)


            // All assets available part
            viewModel.getAllPriceResume()


        }



    }

    private fun addObservers() {

        viewModel.newsResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                resourcesAdapter.setList(it.data)
            }
        }

        viewModel.allAssets.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                BaseActivity.assets = it.data as ArrayList<AssetBaseData>
            }
        }

        viewModel.priceServiceResumes.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                adapterAllAsset.setList(it.subList(0, 6))
            }
        }

        viewModel.balanceResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict?.forEach {
                    val balance = Balance(id = it.key, balanceData = it.value)
                    balances.add(balance)
                }
                BaseActivity.balances = balances
                adapterBalance.setList(balances)
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
        lastUpdate: String, tf: String = "1h"
    ): MutableList<List<Double>> {
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

    private fun getLineData(value: Double, straightLine: Boolean = false): MutableList<List<Double>> {
        val list = mutableListOf<List<Double>>()
        if (!straightLine) list.add(listOf(System.currentTimeMillis().toDouble(), 0.0))
        for (i in 0..8) list.add(listOf(System.currentTimeMillis().toDouble(), value))
        return list
    }

    override fun recurringInvestmentClicked(investment: Investment) {
        /*requireActivity().replaceFragment(
            R.id.flSplashActivity, InvestmentDetailFragment.get(investment._id)
        )*/
        val arguments = Bundle().apply {
            putString("investmentId", investment._id)
        }
        navController.navigate(R.id.investmentDetailFragment, arguments)
    }

    override fun investMoneyClicked(toStrategy: Boolean) {
        if (toStrategy) navController.navigate(R.id.pickYourStrategyFragment)
        else navController.navigate(R.id.selectAnAssetFragment)
    }

    override fun assetClicked(balance: Balance) {
        checkInternet(requireContext()) {

            navController.navigate(R.id.portfolioDetailFragment)
            viewModel.selectedAsset = CommonMethods.getAsset(balance.id)
            viewModel.selectedBalance = balance
        }
    }

    override fun availableAssetClicked(priceResume: PriceServiceResume) {
        checkInternet(requireContext()) {
            navController.navigate(R.id.portfolioDetailFragment)
            viewModel.selectedAsset = CommonMethods.getAsset(priceResume.id)
            viewModel.selectedBalance = CommonMethods.getBalance(priceResume.id)
        }

    }

    override fun menuOptionSelected(tag: String, option: String) {

        viewModel.selectedAsset

        when (option) {

            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                if (viewModel.screenCount == 0) {
                   navController.navigate(R.id.swapWithdrawFromFragment)
                } else {
                    viewModel.withdrawAsset = viewModel.selectedAsset
                    navController.navigate(R.id.addAmountFragment)
                }
            }

            "deposit" -> {

                if (viewModel.screenCount > 0) navController.navigate(R.id.chooseAssetForDepositFragment)
                else navController.navigate(R.id.depositFiatWalletFragment)
            }

            "exchange" -> {
                viewModel.selectedOption = Constants.USING_EXCHANGE
                navController.navigate(R.id.swapWithdrawFromFragment)
            }

            "sell" -> {
                viewModel.selectedOption = Constants.USING_SELL
               navController.navigate(R.id.addAmountFragment)
            }
        }
    }


    private fun screenThreeToOne() {
        binding.apply {
            apiStarted = false
            binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)

            llTabLayout.fadeOut()
            ivProfile.fadeIn()

            llPortfolio.visibleFromLeft()

            tvInvestMoney.text = "Invest Money"
            tvPortfolioAssetPrice.text = "Portfolio"
            "${viewModel.totalPortfolio.commaFormatted} ${Constants.EURO}".also { tvValuePortfolioAndAssetPrice.text = it }

            zoomIn(llThreeDot)
            zoomIn(btnPlaceOrder)

        }
    }



    //Analytics data
    private fun getAnalyticsData(): List<AnalyticsData> {
        val list = mutableListOf<AnalyticsData>()
        list.add(AnalyticsData("Total earnings", "0.00€", CommonMethods.getLineData()))
        list.add(AnalyticsData("Return on Investment", "0.00%", CommonMethods.getLineData()))
        return list
    }

    override fun onBackPressed(): Boolean {
        apiStarted = false
       return false
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
                    when (viewModel.screenCount) {

                        1 -> {

                            viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
                            viewModel.selectedAsset
                           navController.navigate(R.id.addAmountFragment)

                        }
                        else -> {
                            InvestBottomSheet(
                                ::investMoneyClicked
                            ).show(childFragmentManager, "")
                            // Create a transparent color view
                            _fragmentPortfolio = this@PortfolioHomeFragment
                            val transparentView = View(context)
                            transparentView.setBackgroundColor(getColor(requireContext(), R.color.semi_transparent_dark))

                            // Set layout parameters for the transparent view
                            val viewParams = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT
                            )

                            // Add the transparent view to the RelativeLayout
                            screenContent.addView(transparentView, viewParams)


                        }


                    }
                }

                ivProfile -> navController.navigate(R.id.profileFragment)

                llThreeDot -> {
                    PortfolioThreeDots(::menuOptionSelected).show(
                        childFragmentManager,
                        ""
                    )
                    // Create a transparent color view
                    _fragmentPortfolio = this@PortfolioHomeFragment
                    val transparentView = View(context)
                    transparentView.setBackgroundColor(getColor(requireContext(), R.color.semi_transparent_dark))

                    // Set layout parameters for the transparent view
                    val viewParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )

                    // Add the transparent view to the RelativeLayout
                    screenContent.addView(transparentView, viewParams)
                }

                tvViewAll ->  navController.navigate(R.id.allAssetFragment)

                rvMyAssets -> {
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                }
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


    private class AssetsPopupAdapter : BaseAdapter() {

        private val list = mutableListOf<GetAssetsResponseItem?>()

        fun getItemAt(position: Int): GetAssetsResponseItem? {
            return list[position]
        }

        fun hasNoData(): Boolean {
            return list.isEmpty()
        }

        fun addProgress() {
            if (!list.contains(null)) list.add(null)
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
            if (list[position] == null) LoaderViewBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            ).let {
                it.ivLoader.animation =
                    AnimationUtils.loadAnimation(it.ivLoader.context, R.anim.rotate_drawable)
                return it.root
            }
            else TextView(parent?.context, null, 0, R.style.SubheadMabry).apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                gravity = Gravity.CENTER
                updatePadding(20.px, 12.px, 20.px, 12.px)
                list[position]?.let { data ->
                    "${data.asset_name} (${data.symbol.uppercase()})".also { text = it }
                }

                return this

            }

        }
    }
    companion object {
        private const val TAG = "PortfolioHomeFragment"

        private var _fragmentPortfolio: PortfolioHomeFragment? = null
        val fragmentPortfolio get() = _fragmentPortfolio!!
    }

}


