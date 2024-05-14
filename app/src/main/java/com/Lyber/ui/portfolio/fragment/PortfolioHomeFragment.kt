package com.Lyber.ui.portfolio.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.Lyber.R
import com.Lyber.databinding.FragmentPortfolioHomeBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.*
import com.Lyber.ui.adapters.*
import com.Lyber.ui.fragments.BaseFragment
import com.Lyber.ui.fragments.bottomsheetfragments.InvestBottomSheet
import com.Lyber.ui.portfolio.action.PortfolioFragmentActions
import com.Lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDots
import com.Lyber.utils.*
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.fadeIn
import com.Lyber.utils.CommonMethods.Companion.fadeOut
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.px
import com.Lyber.utils.CommonMethods.Companion.setProfile
import com.Lyber.utils.CommonMethods.Companion.toFormat
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.CommonMethods.Companion.visibleFromLeft
import com.Lyber.utils.CommonMethods.Companion.zoomIn
import com.Lyber.viewmodels.PortfolioViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


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
    private var kycOK = false
    private var verificationVisible = false
    private lateinit var navController: NavController
    private var limit = 1
    var daily = false
    private lateinit var timeGraphList: MutableList<List<Double>>
    private var lastValueUpdated = false
    private var responseFrom = ""

    override fun bind() = FragmentPortfolioHomeBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        com.Lyber.ui.activities.SplashActivity.activityCallbacks = this
        App.prefsManager.savedScreen = javaClass.name

        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        requireActivity().window.statusBarColor =
            getColor(requireContext(), android.R.color.transparent)


        /* initializing adapters for recycler views */

        adapterBalance = BalanceAdapter(false, ::assetClicked)
        adapterAllAsset = AvailableAssetAdapter(::availableAssetClicked)
        adapterRecurring =
            RecurringInvestmentAdapter(::recurringInvestmentClicked, requireActivity())
        resourcesAdapter = ResourcesAdapter()
        assetBreakdownAdapter = BalanceAdapter()

        binding.rvRefresh.setOnRefreshListener {
            binding.rvRefresh.isRefreshing = true
            responseFrom = ""
            lastValueUpdated = false
            viewModel.getWalletHistoryPrice(daily, limit)
            viewModel.getUser()
            viewModel.getBalance()
            viewModel.getAllPriceResume()
        }

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

            it.addTab(it.newTab().apply { text = "1D" })
            it.addTab(it.newTab().apply { text = "1W" })
            it.addTab(it.newTab().apply { text = "1M" })
//            it.addTab(it.newTab().apply { text = "1Y" })
            it.addTab(it.newTab().apply { text = "All" })

            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.text) {
                        "1D" -> {
                            limit = 1
                            daily = false
                        }

                        "1W" -> {
                            limit = 7
                            daily = true
                        }

                        "1M" -> {
                            limit = 30
                            daily = true
                        }

                        "1Y" -> {
                            limit = 365
                            daily = true
                        }

                        else -> {
                            limit = 500
                            daily = true
                        }
                    }
                    viewModel.getWalletHistoryPrice(daily, limit)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })
        }

        /* onclick listeners */
        binding.tvViewAll.setOnClickListener(this)
        binding.llThreeDot.setOnClickListener(this)
        binding.btnPlaceOrder.setOnClickListener(this)
        binding.ivProfile.setOnClickListener(this)
        binding.screenContent.setOnClickListener(this)
        binding.tvActivateStrategy.setOnClickListener(this)
        binding.llVerifyIdentity.setOnClickListener(this)
        binding.llContract.setOnClickListener(this)

        /* pop up initialization */
        assetPopUpWindow = ListPopupWindow(requireContext()).apply {
            assetPopupAdapter = AssetsPopupAdapter()
            setAdapter(assetPopupAdapter)
            //anchorView = binding.tvAssetName
            width = 240.px
            height = 420.px
            setOnItemClickListener { _, _, position, _ ->
                assetPopupAdapter.getItemAt(position)?.let {
                    dismiss()
                }
            }
        }

        binding.lineChart.clearYAxis()
//        binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)

        binding.tvInvestMoney.text = getString(R.string.invest_money)
        binding.tvPortfolioAssetPrice.text = getString(R.string.portfolio)
        binding.tvValuePortfolioAndAssetPrice.text =
            "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"

        addObservers()

        checkInternet(requireContext()) {
            if (viewModel.screenCount == 1) {
                viewModel.getPrice(viewModel.chosenAssets?.id ?: "btc")
                viewModel.getNews(viewModel.chosenAssets?.id ?: "btc")
            }
            if (arguments != null && requireArguments().containsKey("showLoader")) {
                isKyc = true
                showDocumentDialog(App.appContext, Constants.LOADING, false)
                arguments = null
            } else
                CommonMethods.showProgressDialog(requireActivity())
            lastValueUpdated=false
            responseFrom=""
            viewModel.getUser()
            viewModel.getAllAssets()
            viewModel.getNetworks()
            viewModel.getActiveStrategies()
            viewModel.getWalletHistoryPrice(daily, limit)
            viewModel.getBalance()
            viewModel.getAllPriceResume()
        }


        if (App.prefsManager.user?.kycStatus != "OK" || App.prefsManager.user?.yousignStatus != "SIGNED") {
            GlobalScope.launch {
                // Run a loop infinitely
                while (!kycOK) {
                    // Call the function to fetch user data
//                    Log.d("hitting Api","$kycOK")
                    if (App.prefsManager.accessToken.isNotEmpty()) {
                        viewModel.getUser()
                        // Delay for 10 seconds
//                        if (App.prefsManager.user?.kycStatus == "STARTED" || App.prefsManager.user?.kycStatus == "NOT_STARTED")
                        delay(3 * 1000)
//                        else
//                            delay(10 * 1000)
                    }
                }
            }
            // Prevent the program from terminating immediately
            readLine()
        }

    }

    private fun addObservers() {

        viewModel.newsResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
                resourcesAdapter.setList(it.data)
            }
        }

        viewModel.allAssets.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.rvRefresh.isRefreshing = false
//                dismissProgressDialog()
                App.prefsManager.assetBaseDataResponse = it
                com.Lyber.ui.activities.BaseActivity.assets = it.data as ArrayList<AssetBaseData>
            }
        }


        viewModel.priceServiceResumes.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.rvRefresh.isRefreshing = false
//                dismissProgressDialog()
                com.Lyber.ui.activities.BaseActivity.balanceResume.clear()
                com.Lyber.ui.activities.BaseActivity.balanceResume.addAll(it)
                adapterAllAsset.setList(it.subList(0, 6))
            }
        }

        viewModel.balanceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
                binding.rvRefresh.isRefreshing = false
                if (it.data.isNotEmpty()) {
                    binding.tvNoAssets.gone()
                    val balanceDataDict = it.data
                    val balances = ArrayList<Balance>()
                    balanceDataDict.forEach {
                        val balance = Balance(id = it.key, balanceData = it.value)
                        balances.add(balance)
                    }
                    var totalBalance = 0.0
                    for (i in it.data)
                        totalBalance += i.value.euroBalance.toDoubleOrNull()!!
                    viewModel.totalPortfolio = totalBalance
                    if (!(::timeGraphList.isInitialized && timeGraphList.isNotEmpty()))
                        binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)
                    else if (responseFrom.isNotEmpty() && responseFrom == "history") {
                        if (lastValueUpdated)
                            timeGraphList.removeAt(timeGraphList.size - 1)
                        timeGraphList.add(
                            listOf(
                                System.currentTimeMillis().toDouble(),
                                viewModel.totalPortfolio
                            )
                        )
                        binding.lineChart.timeSeries = timeGraphList
                    }
                    binding.tvValuePortfolioAndAssetPrice.text =
                        "${totalBalance.commaFormatted}${Constants.EURO}"
//                    binding.lineChart.updateValueLastPoint(totalBalance.commaFormatted.toFloat())
                    com.Lyber.ui.activities.BaseActivity.balances = balances
                    balances.sortByDescending { it.balanceData.euroBalance.toDoubleOrNull() }
                    adapterBalance.setList(balances)
                    responseFrom = "balance"

                } else
                    binding.tvNoAssets.visible()
            }
        }

        viewModel.walletHistoryResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.rvRefresh.isRefreshing = false
                val dates = it.data.map { it.date }
                var dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                if (!daily)
                    dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val timeSeries1 = dates.mapIndexed { index, dateString ->
                    val totalValue =
                        it.data[index].total.toDouble() // Get the total value from the corresponding index
                    var ds = dateString
                    if (!daily)
                        ds = dateString.toFormat("yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm")
                    val dateInMillis = dateFormatter.parse(ds).time.toDouble()
                    listOf(dateInMillis, totalValue)
                } as MutableList
                Log.d("timeseries", "$timeSeries1")
                timeGraphList = timeSeries1
                if (responseFrom.isNotEmpty() && responseFrom == "balance" && viewModel.totalPortfolio != 0.0) {
                    lastValueUpdated = true
                    timeGraphList.add(
                        listOf(
                            System.currentTimeMillis().toDouble(),
                            viewModel.totalPortfolio
                        )
                    )
                }
                if (timeSeries1.isEmpty())
                    binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)
                else
                    binding.lineChart.timeSeries = timeGraphList

                responseFrom = "history"
            }
        }

        viewModel.getUserResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (!isKyc)
                    dismissProgressDialog()
                binding.rvRefresh.isRefreshing = false
                App.prefsManager.user = it.data
                App.prefsManager.defaultImage = it.data.avatar
                binding.ivProfile.setProfile
                App.prefsManager.withdrawalLockSecurity = it.data.withdrawalLock
                if (it.data.kycStatus == "OK" && it.data.yousignStatus == "SIGNED" && !verificationVisible) {
                    dismissProgressDialog()
                    kycOK = true
                    binding.tvVerification.gone()
                    binding.llVerification.gone()
                } else {
                    verificationVisible = true
                    binding.tvVerification.visible()
                    binding.llVerification.visible()
                    when (it.data.kycStatus) {
                        "NOT_STARTED", "STARTED" -> binding.ivKyc.setImageResource(R.drawable.arrow_right_purple)
                        "FAILED", "CANCELED" -> {
                            dismissProgressDialog()
                            if (isKyc) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showDocumentDialog(
                                        App.appContext,
                                        Constants.LOADING_FAILURE,
                                        isSign
                                    )
                                }, 1500)
                                isKyc = false
                            }
                            binding.ivKyc.setImageResource(R.drawable.arrow_right_purple)
                        }

                        "OK" -> {
                            dismissProgressDialog()
                            if (isKyc) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showDocumentDialog(
                                        App.appContext,
                                        Constants.LOADING_SUCCESS,
                                        isSign
                                    )
                                }, 1500)
                                isKyc = false
                            }
                            binding.ivKyc.setImageResource(R.drawable.accepted_indicator)
                        }

                        "REVIEW" -> {
                            dismissProgressDialog()
                            if (isKyc) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showDocumentDialog(
                                        App.appContext,
                                        Constants.LOADING_SUCCESS,
                                        isSign
                                    )
                                }, 1500)
                                isKyc = false
                            }
                            binding.ivKyc.setImageResource(R.drawable.pending_indicator)
                        }

                    }
                    if (it.data.kycStatus == "OK")
                        when (it.data.yousignStatus) {
                            "SIGNED" -> {
                                if (isSign) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        showDocumentDialog(
                                            App.appContext,
                                            Constants.LOADING_SUCCESS,
                                            isSign
                                        )
                                    }, 1500)
                                    isSign = false
                                }
                                binding.ivSign.setImageResource(R.drawable.accepted_indicator)
                            }

                            "NOT_SIGNED" -> binding.ivSign.setImageResource(R.drawable.arrow_right_purple)
                        }
                    if (it.data.kycStatus == "OK" && it.data.yousignStatus == "SIGNED") {
                        kycOK = true
                        verificationVisible = false
                    }

                }

                if (it.data.language.isNotEmpty()) {
                    App.prefsManager.setLanguage(it.data.language)
                    val locale = Locale(it.data.language)
                    Locale.setDefault(locale)
                    val resources: Resources = resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                }
            }
        }

        viewModel.networkResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
                com.Lyber.ui.activities.BaseActivity.networkAddress = it.data as ArrayList<Network>
            }
        }
        viewModel.activeStrategyResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
                if (it.data.isNotEmpty()) {
                    binding.llNoActiveStrategy.gone()
                    adapterRecurring.setList(it.data)
                } else {
                    binding.llNoActiveStrategy.visible()
                }
            }
        }

    }

    private fun String.toMilliS(): Long {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date =
            dateFormatter.parse(this.replace("T", " ").substring(0, 16)) // Adjust format and length
        return date?.time ?: 0L
    }

//    private fun List<String>.toTimeSeries(
//        lastUpdate: String, tf: String = "1h"
//    ): MutableList<List<Double>> {
//        val last = lastUpdate.toMilliS()
//        val timeSeries = mutableListOf<List<Double>>()
//        val timeInterval = when (tf) {
//            "1h" -> 60 * 60 * 1000L
//            "4h" -> 4 * 60 * 60 * 1000L
//            "1d" -> 24 * 60 * 60 * 1000L
//            "1w" -> 7 * 24 * 60 * 60 * 1000L
//            "1m" -> 30 * 24 * 60 * 60 * 1000L
//            else -> 7 * 24 * 60 * 60 * 1000L
//        }
//
//        for (i in indices) {
//            val date = last - (size - i) * timeInterval
//            timeSeries.add(listOf(date.toDouble(), this[i].toDouble()))
//        }
//        return timeSeries
//    }

//    private fun List<String>.toFloats(): MutableList<Float> {
//        val list = mutableListOf<Float>()
//        forEach {
//            list.add(it.roundFloat().toFloat())
//        }
//        return list
//
//    }

    private fun getLineData(
        value: Double,
        straightLine: Boolean = false
    ): MutableList<List<Double>> {
        val list = mutableListOf<List<Double>>()
        if (!straightLine) list.add(listOf(System.currentTimeMillis().toDouble(), 0.0))
        for (i in 0..8) list.add(listOf(System.currentTimeMillis().toDouble(), value))
        return list
    }


    override fun recurringInvestmentClicked(investment: ActiveStrategyData) {
        navController.navigate(R.id.pickYourStrategyFragment)
    }

    override fun investMoneyClicked(toStrategy: Boolean) {
        if (toStrategy) navController.navigate(R.id.pickYourStrategyFragment)
        else navController.navigate(R.id.selectAssestForBuy)
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
            viewModel.selectedAsset = CommonMethods.getAsset(priceResume.id)
            viewModel.selectedBalance = CommonMethods.getBalance(priceResume.id)
            navController.navigate(R.id.portfolioDetailFragment)
        }

    }

    override fun menuOptionSelected(tag: String, option: String) {

        viewModel.selectedAsset

        when (option) {
            "buy" -> {
                viewModel.selectedOption = Constants.USING_BUY
                navController.navigate(R.id.selectAssestForBuy)
            }

            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                navController.navigate(R.id.swapWithdrawFromFragment)
            }

            "deposit" -> {
                navController.navigate(R.id.selectAssestForDepositFragment)
            }

            "exchange" -> {
                viewModel.selectedOption = Constants.USING_EXCHANGE
                navController.navigate(R.id.exchangeFromFragment)
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

            tvInvestMoney.text = getString(R.string.invest_money)
            tvPortfolioAssetPrice.text = getString(R.string.portfolio)
            "${viewModel.totalPortfolio.commaFormatted} ${Constants.EURO}".also {
                tvValuePortfolioAndAssetPrice.text = it
            }

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
        com.Lyber.ui.activities.SplashActivity.activityCallbacks = null
        super.onDestroyView()
    }

    @SuppressLint("InflateParams")
    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnPlaceOrder -> {
                    InvestBottomSheet(
                        ::investMoneyClicked
                    ).show(childFragmentManager, "")
                    // Create a transparent color view
                    _fragmentPortfolio = this@PortfolioHomeFragment
                    val transparentView = View(context)
                    transparentView.setBackgroundColor(
                        getColor(
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

                ivProfile -> navController.navigate(R.id.profileFragment)
                ivProfile -> navController.navigate(R.id.profileFragment)
                llThreeDot -> {
                    PortfolioThreeDots(::menuOptionSelected).show(
                        childFragmentManager,
                        ""
                    )
                    // Create a transparent color view
                    _fragmentPortfolio = this@PortfolioHomeFragment
                    val transparentView = View(context)
                    transparentView.setBackgroundColor(
                        getColor(
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

                tvViewAll -> {
                    val arguments = Bundle().apply {
                        putString("type", "assets")
                    }
                    navController.navigate(R.id.allAssetFragment, arguments)
                }

                rvMyAssets -> {
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                }

                tvActivateStrategy -> {
                    navController.navigate(R.id.pickYourStrategyFragment)
                }

                llContract -> {
                    if (App.prefsManager.user?.kycStatus == "OK" && App.prefsManager.user?.yousignStatus != "SIGNED")
                        customDialog(7025)
                }

                llVerifyIdentity -> {
                    if (App.prefsManager.user?.kycStatus == "CANCELED" || App.prefsManager.user?.kycStatus == "FAILED"
                        || App.prefsManager.user?.kycStatus == "STARTED" || App.prefsManager.user?.kycStatus == "NOT_STARTED"
                    )
                        customDialog(7023)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.ivProfile.setProfile
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


