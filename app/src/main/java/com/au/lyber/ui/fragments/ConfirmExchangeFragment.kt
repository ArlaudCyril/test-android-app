package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmInvestmentBinding
import com.au.lyber.databinding.LottieViewBinding
import com.au.lyber.models.DataQuote
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.github.jinatonic.confetti.CommonConfetti
import okhttp3.ResponseBody


class ConfirmExchangeFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private var timer = 25
    private var dialog: Dialog? = null
    private var orderId: String = ""
    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false

        getData()
        viewModel.getQuoteResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                prepareView(it.data)


            }
        }
        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                loadAnimation()
                showLottieProgressDialog(requireActivity(), Constants.LOADING_SUCCESS)
                Handler().postDelayed({
                    dismissProgressDialog()
                    viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!.id)
                    viewModel.selectedBalance =
                        BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetTo!!.id }
                    findNavController().navigate(R.id.action_confirmExchangeFragment_to_deatil_fragment)
                }, 2000)

            }
        }


    }

    private fun loadAnimation() {
        val array = IntArray(2)
        array[0] = R.color.purple_400
        array[1] = R.color.white_transparent
        val confetti = CommonConfetti.rainingConfetti(binding.root, array)
            .infinite()
        confetti.setAccelerationY(500f)
        confetti.setEmissionRate(500f)
        confetti.setVelocityY(500f)
            .animate()
    }

    private fun getData() {
        binding.apply {
            listOf(
                tvNestedAmount,
                tvNestedAmountValue,
                tvExchangeFrom, tvExchangeFromValue, tvExchangeTo, tvExchangeToValue,
                tvLyberFee,
                tvValueLyberFee
            ).visible()

            listOf(
                tvFrequency,
                tvValueFrequency,
                tvAllocation,
                allocationView,
                tvAssetPrice,
                tvValueAssetPrice,
                tvDeposit,
                tvDepositFee,
                tvValueDeposit,
                tvValueDepositFee
            ).gone()
        }

        CommonMethods.showProgressDialog(requireActivity())
        viewModel.getQuote(
            viewModel.exchangeAssetFrom?.id ?: "",
            viewModel.exchangeAssetTo?.id ?: "",
            viewModel.exchangeFromAmount
        )
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressed()
                btnConfirmInvestment -> {
                    CommonMethods.checkInternet(requireContext()) {
                        showLottieProgressDialog(requireContext(), Constants.LOADING)
                        timer = 0
                        viewModel.confirmOrder(
                            orderId
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {
            tvNestedAmount.text = getString(R.string.ratio)
            tvNestedAmountValue.text = "1 : " + data!!.ratio
            tvValueLyberFee.text =
                data.fees.decimalPoint() + data.fromAsset.uppercase()
            tvAmount.text =
                "${data.toAmount.decimalPoint()} ${data.toAsset.uppercase()}"
            orderId = data.orderId
            tvExchangeFromValue.text =
                "${data.fromAmount} ${data.fromAsset.uppercase()}"
            tvExchangeToValue.text =
                "${data.toAmount.decimalPoint()} ${data.toAsset.uppercase()}"

            val valueTotal = data.fees.toDouble() + data.fromAmount.toDouble()
            tvValueTotal.text =
                "${valueTotal.toString().decimalPoint()} ${data.fromAsset.uppercase()}"
            btnConfirmInvestment.isEnabled = true
            startTimer()
            btnConfirmInvestment.text =
                getString(R.string._25_sec, getString(R.string.confirm_exchange), "25")
            title.text = getString(R.string.confirm_exchange)


        }
    }


    private fun startTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
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
    }

    private fun showLottieProgressDialog(context: Context, typeOfLoader: Int) {

        if (dialog == null) {
            dialog = Dialog(context)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog!!.window!!.setDimAmount(0.2F)
            dialog!!.setCancelable(false)
            dialog!!.getWindow()!!.setLayout(500, 500);
            dialog!!.setContentView(LottieViewBinding.inflate(LayoutInflater.from(context)).root)
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

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        if (dialog != null) {
            showLottieProgressDialog(requireActivity(), Constants.LOADING_FAILURE)
            Handler().postDelayed({
                dismissProgressDialog()
            }, 1000)
        }
    }

}