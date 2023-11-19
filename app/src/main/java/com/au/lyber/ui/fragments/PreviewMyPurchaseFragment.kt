package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentMyPurchaseBinding
import com.au.lyber.models.DataQuote
import com.au.lyber.network.RestClient
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.Constants
import com.google.gson.Gson

class PreviewMyPurchaseFragment : BaseFragment<FragmentMyPurchaseBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private var timer = 25

    private var orderId: String = ""
    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentMyPurchaseBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        getData()
    }


    private fun getData() {


        if (arguments!=null && requireArguments().containsKey(Constants.DATA_SELECTED)){
            val data = Gson().fromJson(requireArguments().getString(Constants.DATA_SELECTED),
                DataQuote::class.java)
            prepareView(data)
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnConfirmInvestment -> {
                    val bundle = Bundle().apply {
                        putString(Constants.ORDER_ID,orderId)
                    }
                    findNavController().navigate(
                        R.id.action_confirmExchangeFragment_to_deatil_fragment
                        ,bundle)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {

            orderId = data!!.orderId

            tvTotalAmount.text = "${data.fromAmount+" "+ Constants.EURO}"
            tvAmount.text = "${data.toAmount+" "+ "USDT"}"
            btnConfirmInvestment.isEnabled = true
            startTimer()
            btnConfirmInvestment.text =
                getString(R.string._25_sec, getString(R.string.confirm_exchange), "25")
            title.text = getString(R.string.confirm_purchase)


        }
    }


    private fun startTimer() {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                if (lifecycle.currentState == Lifecycle.State.RESUMED)
                    if (timer == 0) {
                        binding.btnConfirmInvestment.isEnabled = true
                        binding.btnConfirmInvestment.text = getString(
                            R.string._25_sec, getString(R.string.confirm_exchange), timer.toString()
                        )
                        binding.btnConfirmInvestment.isEnabled = false
                        binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button_purple_400
                        )
                    } else {
                        timer -= 1
                        binding.btnConfirmInvestment.text = getString(
                            R.string._25_sec, getString(R.string.confirm_exchange), timer.toString()
                        )
                        binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button_purple_500
                        )
                        startTimer()
                    }
            }, 1000)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }





}