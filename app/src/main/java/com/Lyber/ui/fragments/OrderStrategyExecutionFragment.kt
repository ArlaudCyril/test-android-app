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
import com.Lyber.R
import com.Lyber.databinding.FragmentOrderStrategyExecutionBinding
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
private var executionID=""
    private lateinit var viewModel: PortfolioViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener=this
        executionID = requireArguments().getString("executionId").toString()
        startRepeatingTask()
        viewModel.strategyExecutionResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                stopRepeatingTask()
                Log.d("response","${it.data}")
            }
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        stopRepeatingTask()
        binding.frameLayout.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),
            R.color.red_500))
        binding.ivStatus.visibility=View.VISIBLE
        binding.ivCircularProgress.visibility=View.GONE
    }
    fun startRepeatingTask() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Your statement to be executed every 5 seconds
                // For example, print a log statement
                println("Executing statement every 5 seconds")
                CommonMethods.checkInternet(requireContext()){
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