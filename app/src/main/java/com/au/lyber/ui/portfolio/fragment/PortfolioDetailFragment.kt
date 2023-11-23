package com.au.lyber.ui.portfolio.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.airbnb.lottie.LottieAnimationView
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentPortfolioDetailBinding
import com.au.lyber.databinding.LottieViewBinding
import com.au.lyber.models.Balance
import com.au.lyber.models.Duration
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.ui.adapters.BalanceAdapter
import com.au.lyber.ui.adapters.ResourcesAdapter
import com.au.lyber.ui.fragments.AddAmountFragment
import com.au.lyber.ui.fragments.BaseFragment
import com.au.lyber.ui.fragments.PickYourStrategyFragment
import com.au.lyber.ui.fragments.SearchAssetsFragment
import com.au.lyber.ui.fragments.SelectAnAssetFragment
import com.au.lyber.ui.fragments.SwapWithdrawFromFragment
import com.au.lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDots
import com.au.lyber.ui.portfolio.bottomSheetFragment.PortfolioThreeDotsDismissListener
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.toMilli
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.ItemOffsetDecoration
import com.github.jinatonic.confetti.CommonConfetti
import com.github.jinatonic.confetti.ConfettiManager
import com.google.android.material.tabs.TabLayout
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

class PortfolioDetailFragment : BaseFragment<FragmentPortfolioDetailBinding>(),
    View.OnClickListener, PortfolioThreeDotsDismissListener {
    private var confetti: ConfettiManager?=null
    private var dialog: Dialog? = null

    /* adapters */
    private lateinit var adapterBalance: BalanceAdapter
    private lateinit var resourcesAdapter: ResourcesAdapter
    private lateinit var assetBreakdownAdapter: BalanceAdapter

    private lateinit var viewModel: PortfolioViewModel

    private lateinit var webSocket: WebSocket
    private var socketOpen = false
    private val client = OkHttpClient()
    private var timer = Timer()
    private var grayOverlay: View? = null
    override fun bind() = FragmentPortfolioDetailBinding.inflate(layoutInflater)

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: ")
        SplashActivity.activityCallbacks = null
        webSocket.close(1000, "Goodbye !")
        this.stopTimer()
        super.onDestroyView()
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
            includedMyAsset.root.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.curved_button)
            includedMyAsset.root.setBackgroundTint(R.color.purple_gray_50)

            tvInvestMoney.text = "Invest in ${viewModel.selectedAsset?.id?.uppercase()}"
            tvAssetName.text = "${viewModel.selectedAsset?.fullName}"
            tvAssetName.typeface = context?.resources?.getFont(R.font.mabry_pro_medium)

            val request = Request.Builder().url(
                Constants.SOCKET_BASE_URL
                        + "${viewModel.selectedAsset?.id}eur"
            ).build()
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
                        viewModel.getPrice(asset.id, (tab?.text ?: "1h").toString().lowercase())
                        stopTimer()
                        setTimer((tab?.text ?: "1h").toString().lowercase())
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

            if (arguments != null && requireArguments().containsKey(Constants.ORDER_ID)) {
                showLottieProgressDialog(requireActivity(), Constants.LOADING)
            } else {
                CommonMethods.showProgressDialog(requireActivity())
            }
            viewModel.getPrice(viewModel.selectedAsset?.id ?: "btc")
            viewModel.getNews(viewModel.selectedAsset?.id ?: "btc")
        }

    }

    private fun loadAnimation() {
        val array = IntArray(2)
        array[0] = R.color.purple_400
        array[1] = R.color.white_transparent
        confetti = CommonConfetti.rainingConfetti(binding.root, array)
            .infinite()
        confetti!!.setAccelerationY(500f)
        confetti!!.setEmissionRate(500f)
        confetti!!.setVelocityY(500f)
            .animate()
    }

    private fun addObservers() {
        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
              Handler().postDelayed({
                  viewModel.getBalance()
              },4000)

            }
        }
        viewModel.balanceResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict.forEach {
                    val balance = Balance(id = it.key, balanceData = it.value)
                    balances.add(balance)
                }
                BaseActivity.balances = balances
                loadAnimation()
                showLottieProgressDialog(requireActivity(), Constants.LOADING_SUCCESS)
                Handler().postDelayed({
                    dismissProgressDialog()
                    if (confetti!=null){
                        confetti!!.terminate()
                    }
                        setData()
                }, 2000)
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

                binding.lineChart.timeSeries =
                    it.data.prices.toTimeSeries(it.data.lastUpdate, timeFrame)

                binding.tvValuePortfolioAndAssetPrice.text =
                    "${it.data.prices.last().currencyFormatted}"

                this.setTimer("1h")// le code ne s'exécute pas

                if (arguments != null && requireArguments().containsKey(Constants.ORDER_ID)) {
                    viewModel.confirmOrder(requireArguments().getString(Constants.ORDER_ID, ""))
                }
            }
        }

        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                viewModel.selectedAsset?.imageUrl?.let { it1 ->
                    binding.includedMyAsset.ivAssetIcon.loadCircleCrop(
                        it1
                    )
                }
                viewModel.selectedAssetDetail = it.data
                binding.includedMyAsset.tvAssetName.text = viewModel.selectedAssetDetail?.fullName
                binding.tvValueAbout.text = viewModel.selectedAssetDetail?.about?.en
                //
            }
        }

    }

    private fun setData() {
        binding.apply {
            includedMyAsset.let {
                val balance = BaseActivity.balances.find { it1 -> it1.id == viewModel.selectedAsset!!.id }
                viewModel.selectedBalance = balance
                val priceCoin =balance?.balanceData?.euroBalance?.toDouble()
                    ?.div(balance.balanceData.balance.toDouble() ?: 1.0)
                it.tvAssetAmount.text =
                    balance?.balanceData?.euroBalance?.currencyFormatted
                it.tvAssetAmountInCrypto.text =
                    balance?.balanceData?.balance?.formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    )
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

    private fun getLineData(
        value: Double,
        straightLine: Boolean = false
    ): MutableList<List<Double>> {
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


    private fun menuOptionSelected(tag: String, option: String) {

        viewModel.selectedAsset

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
            "buy"->{
                val balance =BaseActivity.balances.find { it1 -> it1.id == "usdt"}
                if (balance!=null){
                    viewModel.exchangeAssetTo = viewModel.selectedAsset!!.id
                    viewModel.exchangeAssetFrom = "usdt"
                    findNavController().navigate(R.id.addAmountForExchangeFragment)
                }else{
                    showDialog()
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
                    getString(R.string.you_don_t_have_balance_to_exchange).showToast(requireActivity())
                }
            }

            "sell" -> {
                viewModel.selectedOption = Constants.USING_SELL
                requireActivity().replaceFragment(
                    R.id.flSplashActivity, AddAmountFragment()
                )
            }
        }
    }

    private fun showDialog() {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_PHONE
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.buy_usdt)
                it.tvMessage.text =
                    getString(R.string.usdt_error)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.buy_usdt)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    findNavController().navigate(R.id.buyUsdt)

                }
            }
        }
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
                    val portfolioThreeDotsFragment = PortfolioThreeDots(::menuOptionSelected)
                    portfolioThreeDotsFragment.dismissListener = this@PortfolioDetailFragment
                    if (CommonMethods.getBalance(viewModel.selectedAsset!!.id!!) != null){
                        portfolioThreeDotsFragment.typePopUp = "AssetPopUpWithdraw"
                    }else{
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
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity, SearchAssetsFragment(), topBottom = true
                    )


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
                    val price = jsonObject.getString("Price")
                    binding.tvValuePortfolioAndAssetPrice.text = price.currencyFormatted
                    binding.lineChart.updateValueLastPoint(price.toFloat())
                }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            socketOpen = false
        }
    }

    //MARK: - Timer
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
                    binding.lineChart.addPoint()
                }
            }
        }, date, (interval * 1000).toLong())

    }

    private fun stopTimer() {
        timer.cancel()
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
            dialog!!.getWindow()!!.setLayout(width.toInt(), height.toInt());
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

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        if (dialog != null) {
            showLottieProgressDialog(requireActivity(), Constants.LOADING_FAILURE)
            Handler().postDelayed({
                dismissProgressDialog()
            }, 1000)
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
}