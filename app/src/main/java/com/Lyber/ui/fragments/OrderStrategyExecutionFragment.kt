package com.Lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.R
import com.Lyber.databinding.FragmentOrderStrategyExecutionBinding
import com.Lyber.models.Balance
import com.Lyber.models.BalanceData
import com.Lyber.ui.adapters.BalanceAdapter
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import okhttp3.ResponseBody


/**
 * A simple [Fragment] subclass.
 * Use the [OrderStrategyExecutionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderStrategyExecutionFragment : BaseFragment<FragmentOrderStrategyExecutionBinding>() {
    override fun bind() = FragmentOrderStrategyExecutionBinding.inflate(layoutInflater)
    var gotResponse = false
    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 5000L // 5 seconds in milliseconds
    private var executionID = ""
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapterBalance: BalanceAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        executionID = requireArguments().getString("executionId").toString()
        startRepeatingTask()

        adapterBalance = BalanceAdapter(false)
        binding.rvMyAssets.let {
            it.adapter = adapterBalance
            it.layoutManager = LinearLayoutManager(requireContext())
            it.isNestedScrollingEnabled = false
        }
        viewModel.strategyExecutionResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                Log.d("response", "${it.data}")
                when (it.data.status) {
                    "PENDING" -> {

                    }

                    "SUCCESS" -> {
                        CommonMethods.dismissProgressDialog()
                        stopRepeatingTask()
                        binding.tvTitle.text=getString(R.string.strategy_executed)
                        binding.tvDetail.visibility=View.GONE
                        binding.btnThanks.visibility=View.VISIBLE

                        val balanceDataDict = it.data.successfulBundleEntries
                        val balances = ArrayList<Balance>()
                        balanceDataDict?.forEach {
                            var dt=BalanceData(it.stableAmount,it.assetAmount)
                            val balance = Balance(id = it.asset, balanceData = dt)
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
                        binding.ivStatus.visibility = View.VISIBLE
                        binding.ivCircularProgress.visibility = View.GONE
                        binding.tvTitle.text=getString(R.string.strategy_rejected)
                        binding.tvDetail.visibility=View.GONE
                        binding.btnThanks.visibility=View.VISIBLE
                    }

                    "partially successful", "PARTIALLY SUCCESSFUL" -> {
                        CommonMethods.dismissProgressDialog()
                        stopRepeatingTask()
                        binding.tvTitle.text=getString(R.string.strategy_partially_executed)
                        binding.tvDetail.visibility=View.GONE
                        binding.btnThanks.visibility=View.VISIBLE
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
}