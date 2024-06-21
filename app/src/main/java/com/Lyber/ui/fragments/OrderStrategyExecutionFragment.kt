package com.Lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentOrderStrategyExecutionBinding
import com.Lyber.databinding.ItemMyAssetBinding
import com.Lyber.models.BalanceStrategy
import com.Lyber.models.BalanceStrategyData
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.App
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.currencyFormatted
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import okhttp3.ResponseBody
import java.math.RoundingMode


/**
 * A simple [Fragment] subclass.
 * Use the [OrderStrategyExecutionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderStrategyExecutionFragment : BaseFragment<FragmentOrderStrategyExecutionBinding>(),
    OnClickListener {
    override fun bind() = FragmentOrderStrategyExecutionBinding.inflate(layoutInflater)
   private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 5000L // 5 seconds in milliseconds
    private var executionID = ""
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapterBalance: BundleEntriesAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        executionID =  requireArguments().getString("executionId").toString()
        startRepeatingTask()
        binding.ivTopAction.setOnClickListener(this)
        binding.btnThanks.setOnClickListener(this)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.btnThanks.isVisible)
                    findNavController().popBackStack(R.id.pickYourStrategyFragment, false)
                else
                    findNavController().popBackStack(R.id.confirmInvestmentFragment, false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)



        adapterBalance = BundleEntriesAdapter()
        binding.rvMyAssets.let {
            it.adapter = adapterBalance
            it.layoutManager = LinearLayoutManager(requireContext())
            it.isNestedScrollingEnabled = false
        }
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
        viewModel.strategyExecutionResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                Log.d("response", "${it.data}")
                when (it.data.status) {
                    "PENDING" -> {

                    }

                    "SUCCESS" -> {
                        CommonMethods.dismissProgressDialog()
                        stopRepeatingTask()
                        binding.frameLayout.setBackgroundTintList(
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.greenBackColor
                            )
                        )
                        binding.ivStatus.setImageResource(R.drawable.ic_green_large_tick)

                        binding.ivCircularProgress.visibility = View.GONE
                        binding.ivStatus.visibility = View.VISIBLE
                        binding.ivTopAction.visibility = View.VISIBLE
                        binding.ivEnd.visibility = View.VISIBLE
                        binding.tvTitle.text = getString(R.string.strategy_executed)
                        binding.tvDetail.visibility = View.GONE
                        binding.btnThanks.visibility = View.VISIBLE
                        val balanceDataDict = it.data.successfulBundleEntries
                        val balances = ArrayList<BalanceStrategy>()
                        balanceDataDict.forEach {
                            val balance = BalanceStrategy(
                                id = it.asset,
                                balanceData = BalanceStrategyData(
                                    it.stableAmount,
                                    it.assetAmount,
                                    true
                                )
                            )
                            balances.add(balance)
                        }
                        adapterBalance.setList(balances)
                    }

                    "failure", "FAILURE" -> {
                        CommonMethods.dismissProgressDialog()
                        stopRepeatingTask()
                        binding.frameLayout.setBackgroundTintList(
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.red_500
                            )
                        )
                        binding.ivStatus.setImageResource(R.drawable.red_cross)
                        binding.ivStatus.visibility = View.VISIBLE
                        binding.ivTopAction.visibility = View.VISIBLE
                        binding.ivEnd.visibility = View.VISIBLE
                        binding.ivCircularProgress.visibility = View.GONE
                        binding.tvTitle.text = getString(R.string.strategy_rejected)
                        binding.tvDetail.visibility = View.GONE
                        binding.btnThanks.visibility = View.VISIBLE
                        val balanceDataDict = it.data.failedBundleEntries
                        val balances = ArrayList<BalanceStrategy>()
                        balanceDataDict.forEach {
                            val balance = BalanceStrategy(
                                id = it.asset,
                                balanceData = BalanceStrategyData("", "", false)
                            )
                            balances.add(balance)
                        }
                        adapterBalance.setList(balances)
                    }

                    "partially successful", "PARTIALLY SUCCESSFUL" -> {
                        CommonMethods.dismissProgressDialog()
                        stopRepeatingTask()
                        binding.frameLayout.setBackgroundTintList(
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.orangeBackColor
                            )
                        )
                        binding.ivStatus.setImageResource(R.drawable.partially_success)
                        binding.ivCircularProgress.visibility = View.GONE
                        binding.ivTopAction.visibility = View.GONE
                        binding.ivEnd.visibility = View.GONE
                        binding.ivStatus.visibility = View.VISIBLE
                        binding.tvTitle.text = getString(R.string.strategy_partially_executed)
                        binding.tvDetail.visibility = View.GONE
                        binding.btnThanks.visibility = View.VISIBLE

                        val balanceDataDictS = it.data.successfulBundleEntries
                        val balanceDataDict = it.data.failedBundleEntries
                        val balances = ArrayList<BalanceStrategy>()
                        balanceDataDictS.forEach {
                            val balance = BalanceStrategy(
                                id = it.asset,
                                balanceData = BalanceStrategyData(
                                    it.stableAmount,
                                    it.assetAmount,
                                    true
                                )
                            )
                            balances.add(balance)
                        }
                        balanceDataDict.forEach {
                            val balance = BalanceStrategy(
                                id = it.asset,
                                balanceData = BalanceStrategyData("", "", false)
                            )
                            balances.add(balance)
                        }
                        adapterBalance.setList(balances)
                    }
                }

            }
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        stopRepeatingTask()
    }

    private fun startRepeatingTask() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Your statement to be executed every 5 seconds
                CommonMethods.checkInternet(requireContext()) {
                    viewModel.strategyStatus(executionID)
                }
                // Schedule the next execution after the delay
                handler.postDelayed(this, delayMillis)
            }
        }, delayMillis)
    }

    // Call this method to stop the repeating task
    fun stopRepeatingTask() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRepeatingTask()
    }

    inner class BundleEntriesAdapter(
    ) :
        BaseAdapter<BalanceStrategy>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AssetViewHolder(
                ItemMyAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (itemList[position] != null)
                (holder as AssetViewHolder).binding.apply {
                    itemList[position]?.let {
                        try {
                            val balanceId = it.id
                            val currency =
                                com.Lyber.ui.activities.BaseActivity.assets.find { it.id == balanceId }
                            ivAssetIcon.loadCircleCrop(currency?.imageUrl ?: "")
                            tvAssetName.visible()
                            tvAssetName.text = currency?.fullName
                            if (it.balanceData.success) {
                                val balance = it.balanceData
                                val priceCoin = balance.stableAmount.toDouble()
                                    .div(balance.assetAmount.toDouble() ?: 1.0)

                                if (currency != null && currency.isTradeActive) {
                                    tvAssetNameCode.gone()
                                } else {
                                    tvAssetNameCode.visible()
                                }

                                var ts =
                                    (balance.stableAmount.commaFormatted.currencyFormatted).toString()
                                        .replace("€", " ${Constants.MAIN_ASSET_UPPER}")
                                tvAssetAmount.text = ts
                                tvAssetAmountInCrypto.text =
                                    balance.assetAmount.formattedAsset(
                                        priceCoin,
                                        rounding = RoundingMode.DOWN
                                    ) + " ${it.id.uppercase()}"
                                ivDropIcon.setImageResource(R.drawable.green_mini)

                            } else {
                                tvAssetAmount.visibility = View.GONE
                                tvAssetAmountInCrypto.visibility = View.GONE
                                ivDropIcon.setImageResource(R.drawable.red_mini)
                            }
                        } catch (_: Exception) {

                        }
                    }
                }
        }

        inner class AssetViewHolder(val binding: ItemMyAssetBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnThanks, ivTopAction -> {
                    findNavController().popBackStack(R.id.pickYourStrategyFragment, false)
                }

            }
        }
    }
}