package com.Lyber.ui.portfolio.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.R
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.databinding.FragmentPortfolioDetailBinding
import com.Lyber.databinding.LottieViewBinding
import com.Lyber.databinding.ProgressBarNewBinding
import com.Lyber.models.Balance
import com.Lyber.models.Duration
import com.Lyber.ui.activities.BaseActivity
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.ui.adapters.BalanceAdapter
import com.Lyber.ui.adapters.ResourcesAdapter
import com.Lyber.ui.fragments.AddAmountFragment
import com.Lyber.ui.fragments.BaseFragment
import com.Lyber.ui.fragments.PickYourStrategyFragment
import com.Lyber.ui.fragments.SelectAnAssetFragment
import com.Lyber.ui.fragments.SendMoneyOptionsFragment
import com.Lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDots
import com.Lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDotsDismissListener
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.AppLifeCycleObserver
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.currencyFormatted
import com.Lyber.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.returnErrorCode
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.CommonMethods.Companion.showErrorMessage
import com.Lyber.utils.CommonMethods.Companion.showSnack
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.toMilli
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.ItemOffsetDecoration
import com.airbnb.lottie.LottieAnimationView
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.github.jinatonic.confetti.CommonConfetti
import com.github.jinatonic.confetti.ConfettiManager
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.integrity.StandardIntegrityManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class PortfolioDetailFragment : BaseFragment<FragmentPortfolioDetailBinding>(),
    View.OnClickListener, PortfolioThreeDotsDismissListener {
    private var confetti: ConfettiManager? = null
    private var dialog: Dialog? = null

    /* adapters */
    private lateinit var adapterBalance: BalanceAdapter
    private lateinit var resourcesAdapter: ResourcesAdapter
    private lateinit var assetBreakdownAdapter: BalanceAdapter

    private lateinit var viewModel: PortfolioViewModel

    private lateinit var webSocket: WebSocket
    private var socketOpen = false
    private var timer = Timer()
    private var grayOverlay: View? = null
    private var firstPrice = 0.0
    private var selectedTab = "1h"
    private var updateSocketValue = false
    override fun bind() = FragmentPortfolioDetailBinding.inflate(layoutInflater)

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: ")
        updateSocketValue = false
        com.Lyber.ui.activities.SplashActivity.activityCallbacks = null
        webSocket.close(1000, "Goodbye !")
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        if (AppLifeCycleObserver.fromBack) {
            AppLifeCycleObserver.fromBack = false
            setView()
        }
    }

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
        assetBreakdownAdapter = BalanceAdapter()
        binding.btnSell.gone()
//        binding.btnBuy.gone()

        binding.apply {

            rvResources.let {
                it.adapter = resourcesAdapter
                it.layoutManager = LinearLayoutManager(requireContext()).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                it.addItemDecoration(ItemOffsetDecoration(8))
            }

            setData()

            viewModel.selectedAsset?.id?.let { viewModel.getAssetDetail(it) }


            includedMyAsset.root.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)
            includedMyAsset.root.setBackgroundTint(R.color.purple_gray_50)
            includedMyAsset.ivDropIcon.gone()
            tvAssetName.text = "${viewModel.selectedAsset?.fullName}"
            tvAssetName.typeface = context?.resources?.getFont(R.font.mabry_pro_medium)


            var customUrl = ""
            if (viewModel.selectedAsset?.id == "usdt")
                customUrl = Constants.SOCKET_BASE_URL + "eurusdt"
            else
                customUrl = Constants.SOCKET_BASE_URL + "${viewModel.selectedAsset?.id}eur"
            val request = Request.Builder()
                .url(customUrl)
                .build()
            val client = OkHttpClient.Builder()
                .pingInterval(0, TimeUnit.MILLISECONDS) // Adjust if necessary
                .build()

            webSocket = client.newWebSocket(request, PortfolioDetailWebSocketListener())
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
                    viewModel.selectedAsset?.let { asset ->
                        selectedTab = tab?.text.toString().lowercase()
                        updateSocketValue = false
                        viewModel.getPrice(asset.id, (tab?.text ?: "1h").toString().lowercase())
//                        stopTimer()
//                        setTimer((tab?.text ?: "1h").toString().lowercase())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })
        }

        /* onclick listeners */
        binding.ivTopAction.setOnClickListener(this)
        binding.llThreeDot.setOnClickListener(this)
        binding.btnSell.setOnClickListener(this)
        binding.screenContent.setOnClickListener(this)
        binding.tvAssetName.setOnClickListener(this)
        binding.btnBuy.setOnClickListener(this)

        /* pop up initialization */

        binding.lineChart.timeSeries = getLineData(viewModel.totalPortfolio)
        binding.lineChart.hideAmount = false

        binding.tvValuePortfolioAndAssetPrice.text =
            "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"

        addObservers()
        setView()
    }

    private fun setView() {
        CommonMethods.checkInternet(binding.root, requireContext()) {

            if (arguments != null && requireArguments().containsKey(Constants.ORDER_ID)) {
                updateSocketValue = false
                showProgress(requireActivity())
            } else {
                CommonMethods.showProgressDialog(requireActivity())
            }
            viewModel.getPrice(viewModel.selectedAsset?.id ?: "btc", selectedTab)
            viewModel.getNews(viewModel.selectedAsset?.id ?: "btc")
            viewModel.getPriceResumeById(viewModel.selectedAsset?.id ?: "btc")
        }
    }

    private fun loadAnimation() {
        val array = IntArray(1)
//        array[0] = R.color.purple_300
//        array[1] = R.color.purple_500
//        array[2] = R.color.purple_200
//        array[3] = R.color.white_transparent
        array[0] = R.color.red_500
        confetti = CommonConfetti.rainingConfetti(binding.root, array)
            .infinite()
        confetti!!.setAccelerationY(400f)
        confetti!!.setEmissionRate(300f)
        confetti!!.setVelocityY(400f)
            .animate()
    }

    private fun addObservers() {
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.getBalance()
                }, 4000)
            }
        }
        viewModel.balanceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                arguments = null
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict.forEach {
                    val balance = Balance(id = it.key, balanceData = it.value)
                    balances.add(balance)
                }
                com.Lyber.ui.activities.BaseActivity.balances = balances
                loadAnimation()
//                showLottieProgressDialog(requireActivity(), Constants.LOADING_SUCCESS)
                dismissProgress()
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissProgressDialog()
                    if (isAdded) {
                        if (confetti != null) {
                            confetti!!.terminate()
                        }
                        setData()
                    }
                }, 3000)
            }
        }
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

//                binding.lineChart.clearLineData()
                binding.lineChart.timeSeries =
                    it.data.prices.toTimeSeries(it.data.lastUpdate, timeFrame)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) {
                        updateSocketValue = true
                    }
                }, 2000)
//                Log.d("timeSeries", "${it.data.prices.toTimeSeries(it.data.lastUpdate, timeFrame)}")
                binding.tvValuePortfolioAndAssetPrice.text =
                    "${it.data.prices.last().currencyFormatted}"
                firstPrice = it.data.prices.first().toDouble()

                if (arguments != null && requireArguments().containsKey(Constants.ORDER_ID)) {
                    if (requireArguments().containsKey(Constants.FROM_SWAP) || requireArguments().containsKey(
                            Constants.TO_SWAP
                        )
                    ) {
                        viewModel.getOrderApi(
                            requireArguments().getString(
                                Constants.ORDER_ID,
                                ""
                            )
                        )
                    } else {
                        val jsonObject = JSONObject()
                        jsonObject.put(
                            "orderId",
                            requireArguments().getString(Constants.ORDER_ID, "")
                        )
                        val jsonString =  CommonMethods.sortAndFormatJson(jsonObject)
//                            jsonObject.toString()
                        // Generate the request hash
                        val requestHash = CommonMethods.generateRequestHash(jsonString)
                        val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                            SplashActivity.integrityTokenProvider?.request(
                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                    .setRequestHash(requestHash)
                                    .build()
                            )
                        integrityTokenResponse1?.addOnSuccessListener { response ->
                            viewModel.confirmOrder(
                                requireArguments().getString(
                                    Constants.ORDER_ID,
                                    ""
                                ), response.token()
                            )
                        }?.addOnFailureListener { exception ->
                            Log.d("token", "${exception}")
                        }

                        val eventValues = HashMap<String, Any>()
                        eventValues[AFInAppEventParameterName.CONTENT_TYPE] =
                            Constants.APP_FLYER_TYPE_CRYPTO
                        eventValues[AFInAppEventParameterName.CONTENT_ID] =
                            requireArguments().getString(Constants.ORDER_ID, "")
                        AppsFlyerLib.getInstance().logEvent(requireContext().applicationContext,
                            AFInAppEventType.PURCHASE, eventValues,
                            object : AppsFlyerRequestListener {
                                override fun onSuccess() {
                                    Log.d("LOG_TAG", "Event sent successfully")
                                }

                                override fun onError(errorCode: Int, errorDesc: String) {
                                    Log.d(
                                        "LOG_TAG", "Event failed to be sent:\n" +
                                                "Error code: " + errorCode + "\n"
                                                + "Error description: " + errorDesc
                                    )
                                }
                            })
                    }
                }
            }
        }

        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                Log.d("data", "${it.data}")
                viewModel.selectedAsset?.imageUrl?.let { it1 ->
                    binding.includedMyAsset.ivAssetIcon.loadCircleCrop(
                        it1
                    )
                }
                viewModel.selectedAssetDetail = it.data
                binding.includedMyAsset.tvAssetName.text = viewModel.selectedAssetDetail?.fullName
                binding.tvValueAbout.text = viewModel.selectedAssetDetail?.about?.en
                binding.tvValueMarketCap.text =
                    viewModel.selectedAssetDetail?.marketCap + Constants.EURO
                binding.tvValueVolume.text =
                    viewModel.selectedAssetDetail?.volume24h + Constants.EURO
                binding.tvValueCirculatingSupply.text =
                    viewModel.selectedAssetDetail?.circulatingSupply + " " + viewModel.selectedAssetDetail?.id!!.uppercase()
                binding.tvValuePopularity.text =
                    "#" + viewModel.selectedAssetDetail?.marketRank.toString()

            }
        }
        viewModel.orderResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (it.data.orderStatus == "PENDING") {
                    viewModel.getOrderApi(
                        requireArguments().getString(Constants.ORDER_ID, "")
                    )
                } else if (it.data.orderStatus == "VALIDATED")
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.getBalance()
                    }, 4000)
                else  //TODO for now
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.getBalance()
                    }, 4000)
            }
        }
        viewModel.priceResumeIdResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                firstPrice = it.data.firstPrice.toDouble()
            }
        }
    }

    private fun setData() {
        binding.apply {
            includedMyAsset.let {
                val balance =
                    BaseActivity.balances.find { it1 -> it1.id == viewModel.selectedAsset!!.id }
                viewModel.selectedBalance = balance
                val priceCoin = balance?.balanceData?.euroBalance?.toDouble()
                    ?.div(balance.balanceData.balance.toDouble() ?: 1.0)
                if (!App.prefsManager.hideAmount) {
                    if (balance?.balanceData?.euroBalance == null)
                        it.tvAssetAmount.text = "0.0 ${Constants.EURO}"
                    else
                        it.tvAssetAmount.text =
                            balance.balanceData.euroBalance.currencyFormatted

                    if (balance?.balanceData?.balance == null)
                        it.tvAssetAmountInCrypto.text = "0.00"
                    else it.tvAssetAmountInCrypto.text =
                        balance.balanceData.balance.formattedAsset(
                            price = priceCoin,
                            rounding = RoundingMode.DOWN, viewModel.selectedAsset!!.decimals
                        )
                } else {
                    it.tvAssetAmount.text = "*****"
                    it.tvAssetAmountInCrypto.text = "*****"
                }

                try {
                    it.tvAssetName.text = viewModel.selectedAsset!!.fullName
                    viewModel.selectedAsset?.imageUrl?.let { it1 ->
                        binding.includedMyAsset.ivAssetIcon.loadCircleCrop(
                            viewModel.selectedAsset!!.imageUrl
                        )
                    }
                } catch (_: Exception) {
                }
                if (balance != null)
                    btnSell.visible()
                if ((viewModel.selectedAsset!!.id.equals(
                        "usdt",
                        ignoreCase = true
                    ))
                )
                    btnBuy.gone()
//                else if (viewModel.selectedAsset!!.id.equals(
//                        Constants.MAIN_ASSET,
//                        ignoreCase = true
//                    )
//                )
//                    btnBuy.visible()
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
            "1m" -> 12 * 60 * 60 * 1000L
            else -> 7 * 24 * 60 * 60 * 1000L
        }
        for (i in 0 until count()) {
            val date = (last - (count() - i) * timeInterval).toDouble()
            timeSeries.add(listOf(date, this[i].toDouble()))
        }
        return timeSeries
    }

    private fun getLineData(
        value: Double,
        straightLine: Boolean = false
    ): MutableList<List<Double>> {
        val list = mutableListOf<List<Double>>()
        if (!straightLine) list.add(listOf(System.currentTimeMillis().toDouble(), 0.0))
        for (i in 0..8) list.add(listOf(System.currentTimeMillis().toDouble(), value))
        return list
    }

    private fun menuOptionSelected(tag: String, option: String) {
//        viewModel.selectedAsset
        when (option) {
            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                val currency = viewModel.selectedAsset
                //if (currency!!.isWithdrawalActive){
                val bundle = Bundle()
                bundle.putString(Constants.ID, currency!!.id)
                findNavController().navigate(R.id.withdrawlNetworksFragment, bundle)
                //  }else{
                //   getString(R.string.withdrawal_is_deactivated_on_this_asset).showToast(requireActivity())
                //  }
            }

            "buy" -> {
                val balance =
                    com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == Constants.MAIN_ASSET }
                viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
                if (viewModel.selectedAsset!!.id == Constants.MAIN_ASSET) {
                    if (checkKyc())
                        findNavController().navigate(R.id.buyUsdt)
                } else if (balance != null) {
                    viewModel.exchangeAssetTo = viewModel.selectedAsset!!.id
                    viewModel.exchangeAssetFrom = Constants.MAIN_ASSET
                    findNavController().navigate(R.id.addAmountForExchangeFragment)
                } else {
                    if (App.prefsManager.user!!.kycStatus == "OK" && App.prefsManager.user!!.yousignStatus == "SIGNED")
                        showDialog()
                    else checkKyc()
                }
            }

            "deposit" -> {

                val bundle = Bundle().apply {
                    putString(Constants.DATA_SELECTED, viewModel.selectedAsset!!.id)
                }
                findNavController().navigate(R.id.chooseAssetForDepositFragment, bundle)
            }

            "exchange" -> {
                if (CommonMethods.getBalance(viewModel.selectedAsset!!.id) != null) {
                    viewModel.selectedOption = Constants.USING_EXCHANGE
                    val bundle = Bundle()
                    viewModel.exchangeAssetFrom = viewModel.selectedBalance!!.id
                    bundle.putString(Constants.TYPE, Constants.Exchange)
                    findNavController().navigate(R.id.allAssetFragment, bundle)
                } else {
                    getString(R.string.you_don_t_have_balance_to_exchange).showToast(
                        binding.root,
                        requireActivity()
                    )
                }
            }

            "sell" -> {
                viewModel.selectedOption = Constants.USING_SELL
                requireActivity().replaceFragment(
                    R.id.flSplashActivity, AddAmountFragment()
                )
            }

            Constants.USING_SEND_MONEY -> {
                viewModel.selectedOption = Constants.USING_SEND_MONEY
                val bundle = Bundle()
                bundle.putString(Constants.ID, viewModel.selectedAsset?.id)
                bundle.putString(Constants.FROM, PortfolioDetailFragment::class.java.name)
                findNavController().navigate(R.id.sendMoneyOptionsFragment,bundle)
//                val bundle = Bundle()
//                bundle.putString(Constants.ID, viewModel.selectedAsset?.id)
//                                bundle.putString(Constants.FROM, PortfolioDetailFragment::class.java.name)
//                findNavController().navigate(R.id.sendAmountFragment,bundle)

            }
        }
    }

    private fun showDialog() {
        BottomSheetDialog(requireContext(), R.style.CustomDialogBottomSheet).apply {
            CustomDialogVerticalLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_PHONE
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.buy_usdc)
                it.tvMessage.text =
                    getString(R.string.usdt_error)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.buy_usdc)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    findNavController().navigate(R.id.buyUsdt)

                }
                show()
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnSell -> {
                    if (viewModel.selectedAsset!!.id.equals(
                            Constants.MAIN_ASSET,
                            ignoreCase = true
                        )
                    ) {
                        if (BaseActivity.ribWalletList.isEmpty()) {
                            val bundle = Bundle().apply {
                                putString(Constants.FROM, PortfolioDetailFragment::class.java.name)
                            }
                            findNavController().navigate(R.id.addRibFragment, bundle)
                        } else {
                            findNavController().navigate(R.id.ribListingFragment)

                        }
                    } else {
                        viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
                        if (viewModel.selectedAsset!!.id == Constants.MAIN_ASSET) {
//                        findNavController().navigate(R.id.buyUsdt)
                            val balance =
                                com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == Constants.MAIN_ASSET }
                            if (balance != null) {
                                viewModel.exchangeAssetTo = Constants.MAIN_ASSET
                                viewModel.exchangeAssetFrom = viewModel.selectedAsset!!.id
                                findNavController().navigate(R.id.addAmountForExchangeFragment)
                            }
                        } else {
                            val balance =
                                com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.selectedAsset!!.id }
                            if (balance != null) {
                                viewModel.exchangeAssetTo = Constants.MAIN_ASSET
                                viewModel.exchangeAssetFrom = viewModel.selectedAsset!!.id
                                findNavController().navigate(R.id.addAmountForExchangeFragment)
                            } else {
                                showDialog()
                            }
                        }
                    }

                }

                llThreeDot -> {
                    val portfolioThreeDotsFragment = PortfolioThreeDots(::menuOptionSelected)
                    portfolioThreeDotsFragment.dismissListener = this@PortfolioDetailFragment
                    if (CommonMethods.getBalance(viewModel.selectedAsset!!.id) != null) {
                        portfolioThreeDotsFragment.typePopUp = "AssetPopUpWithdraw"
                    } else {
                        portfolioThreeDotsFragment.typePopUp = "AssetPopUpWithdraw"
                    }

                    portfolioThreeDotsFragment.show(
                        childFragmentManager,
                        "PortfolioThreeDots"
                    )
                    grayOverlay = View(requireContext())
                    grayOverlay?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.semi_transparent_dark
                        )
                    )
                    grayOverlay?.alpha = 1.0f
                    screenContent.addView(grayOverlay)
                }

                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                tvAssetName -> {
                    findNavController().popBackStack()
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, "assets")
                    findNavController().navigate(R.id.allAssetFragment, bundle)
                }

                btnBuy -> {
                    val balance =
                        com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == Constants.MAIN_ASSET }
                    viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
                    if (viewModel.selectedAsset!!.id == Constants.MAIN_ASSET) {
                        if (checkKyc())
                            findNavController().navigate(R.id.buyUsdt)
                    } else if (balance != null) {
                        viewModel.exchangeAssetTo = viewModel.selectedAsset!!.id
                        viewModel.exchangeAssetFrom = Constants.MAIN_ASSET
                        findNavController().navigate(R.id.addAmountForExchangeFragment)
                    } else {
                        if (App.prefsManager.user!!.kycStatus == "OK" && App.prefsManager.user!!.yousignStatus == "SIGNED")
                            showDialog()
                        else checkKyc()
                    }
                }
            }
        }
    }

    //MARK:- Web Socket Listener
    private inner class PortfolioDetailWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            socketOpen = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (!socketOpen) return
            requireActivity().runOnUiThread {
                val jsonObject = JSONObject(text)
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    var price = jsonObject.getString("Price")
//                    Log.d("price", "$price")
                    if (viewModel.selectedAsset?.id == Constants.MAIN_ASSET)
                        price = (1.0 / price.toFloat()).toString()
//                    Log.d("price", "$price")
                    if (updateSocketValue) {
                        binding.tvValuePortfolioAndAssetPrice.text = price.currencyFormatted
                        binding.lineChart.updateValueLastPoint(price.toFloat())
                    }
                    if (firstPrice != 0.0) {
                        val percentChange = ((price.toDouble() / firstPrice) - 1) * 100
                        val euroChange = price.toDouble() - firstPrice

                        if (percentChange >= 0.0) {
                            binding.tvAssetVariation.text =
                                "▲ + ${percentChange.absoluteValue.commaFormatted}% (${euroChange.commaFormatted}€)"
                            binding.tvAssetVariation.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_500
                                )
                            )

                        } else {
                            binding.tvAssetVariation.text =
                                "▼ - ${percentChange.absoluteValue.commaFormatted}% (${euroChange.commaFormatted}€)"
                            binding.tvAssetVariation.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red_500
                                )
                            )

                        }
//                        Log.d("Price", "$price")
//                        Log.d("firstPrice", "$firstPrice")
//                        Log.d("percent", "${percentChange.commaFormatted}")
                    }
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.d("socket", "failed $response")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            socketOpen = false
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        updateSocketValue = true
        dismissAlertDialog()
        arguments = null
        if (dialog != null) {
//            showLottieProgressDialog(requireActivity(), Constants.LOADING_FAILURE)
            dismissProgress()
            Handler().postDelayed({
                dismissProgressDialog()
            }, 1000)
        }
        when (errorCode) {
            7007 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_7007))
            else -> super.onRetrofitError(errorCode, msg)

        }
    }

    fun dismissProgressDialog() {
        dialog?.let {
            try {
                it.findViewById<ImageView>(R.id.progressImage).clearAnimation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            it.dismiss()
            dialog = null
        }
    }

    override fun onPortfolioThreeDotsDismissed() {
        // Code to remove the overlay view from the parent fragment's layout
        binding.screenContent.removeView(grayOverlay)
    }

    private fun showProgress(context: Context) {

        if (dialog == null) {
            dialog = Dialog(context)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog!!.window!!.setDimAmount(0.6F)
            dialog!!.setCancelable(false)
            dialog!!.setContentView(ProgressBarNewBinding.inflate(LayoutInflater.from(context)).root)
        }
        try {
            dialog?.findViewById<ImageView>(R.id.progressImage)?.animation =
                AnimationUtils.loadAnimation(context, R.anim.rotate_drawable)
            dialog!!.show()
        } catch (e: WindowManager.BadTokenException) {
            Log.d("Exception", "showProgressDialog: ${e.message}")
            dialog?.dismiss()
            dialog = null
        } catch (e: Exception) {
            Log.d("Exception", "showProgressDialog: ${e.message}")
        }

    }

    private fun dismissProgress() {
        dialog?.let {
            try {
                it.findViewById<ImageView>(R.id.progressImage).clearAnimation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            it.dismiss()
        }
    }

    private fun setTimer(timeFrame: String) {
        var interval = 0.0
        var date = Date(binding.lineChart.timeSeries.last()[0].toLong())
        when (timeFrame) {
            "1h" -> {
                // Every minute
                date = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MINUTE, 1)
                }.time
                interval = 60.0
            }

            "4h" -> {
                // Every 5 minutes
                date = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MINUTE, 5)
                }.time
                interval = 60.0 * 5.0
            }

            "1d" -> {
                // Every 30 minutes
                date = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MINUTE, 30)
                }.time
                interval = 60.0 * 30.0
            }

            "1w" -> {
                // Every 4 hours
                date = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.HOUR_OF_DAY, 4)
                }.time
                interval = 60.0 * 60.0 * 4.0
            }

            "1m" -> {
                // Every 12 hours
                date = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.HOUR_OF_DAY, 12)
                }.time
                interval = 60.0 * 60.0 * 12.0
            }

            "1y" -> {
                // Every 7 days
                date = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_YEAR, 7)
                }.time
                interval = 60.0 * 60.0 * 24.0 * 7.0
            }

            else -> {
                println("timeFrame not recognized")
            }
        }

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread() {
                    if (isAdded)
                        binding.lineChart.addPoint()
                }
            }
        }, date, (interval * 1000).toLong())

    }

    private fun getPriceChart(assetId: String, duration: Duration) {
        CommonMethods.checkInternet(binding.root, requireContext()) {
            binding.lineChart.animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
            viewModel.getPriceGraph(assetId, duration)
        }
    }

    private fun showLottieProgressDialog(context: Context, typeOfLoader: Int) {

        if (dialog == null) {
            dialog = Dialog(context)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog!!.window!!.setDimAmount(0.2F)
            dialog!!.setCancelable(false)
            val height = resources.getDimension(R.dimen.px_200)
            val width = resources.getDimension(R.dimen.px_300)
            dialog!!.setContentView(LottieViewBinding.inflate(LayoutInflater.from(context)).root)
//            dialog!!.getWindow()!!.setLayout(width.toInt(), height.toInt());
        }
        try {
            val viewImage = dialog?.findViewById<LottieAnimationView>(R.id.animationView)
            val imageView = dialog?.findViewById<ImageView>(R.id.ivCorrect)!!
            when (typeOfLoader) {
                Constants.LOADING -> {
                    viewImage!!.setMinAndMaxProgress(0f, .32f)
                }

                Constants.LOADING_SUCCESS -> {
                    imageView.visible()
                    imageView.setImageResource(R.drawable.baseline_done_24)
                }

                Constants.LOADING_FAILURE -> {
                    imageView.visible()
                    imageView.setImageResource(R.drawable.baseline_clear_24)
                }
            }
            /*(0f,.32f) for loader
            * (0f,.84f) for success
            * (0.84f,1f) for failure*/
            viewImage!!.playAnimation()


            dialog!!.show()
        } catch (e: WindowManager.BadTokenException) {
            Log.d("Exception", "showProgressDialog: ${e.message}")
            dialog?.dismiss()
            dialog = null
        } catch (e: Exception) {
            Log.d("Exception", "showProgressDialog: ${e.message}")
        }

    }

    private fun stopTimer() {
        timer.cancel()
    }

    fun investMoneyClicked(toStrategy: Boolean) {
        if (toStrategy) requireActivity().replaceFragment(
            R.id.flSplashActivity, PickYourStrategyFragment(), topBottom = true
        )
        else requireActivity().replaceFragment(
            R.id.flSplashActivity, SelectAnAssetFragment(), topBottom = true
        )
    }

}