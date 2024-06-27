package com.Lyber.ui.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentConfirmInvestmentBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet2FA
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.viewmodels.SignUpViewModel
import okhttp3.ResponseBody
import org.json.JSONObject
import java.math.RoundingMode


class ConfirmWithdrawalFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var viewModelSignup: SignUpViewModel
    private var valueTotal: Double = 0.0
    private var isExpand = false
    private var isOtpScreen = false
    private var isResend = false

    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModelSignup = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        viewModelSignup.listener = this
        binding.tvTotalAmount.gone()
        binding.ivSingleAsset.gone()
        binding.tvMoreDetails.gone()
        binding.zzInfor.visible()
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
//        binding.tvMoreDetails.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false
        prepareView()
        setObservers()

    }

    private fun setObservers() {
        viewModel.commonResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
                if (isOtpScreen) {
                    openOtpScreen()
                } else {

                    viewModel.selectedOption = Constants.USING_WITHDRAW
                    ConfirmationBottomSheet().show(childFragmentManager, "")
                }
            }
        }

//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
    }

    private fun openOtpScreen() {
        if (!isResend) {
            val transparentView = View(context)
            transparentView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.semi_transparent_dark
                )
            )
            val viewParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )

            val vc = VerificationBottomSheet2FA(::handle)
            vc.viewToDelete = transparentView
            vc.viewModel = viewModelSignup
            vc.mainView = getView()?.rootView as ViewGroup

            vc.show(childFragmentManager, "")


//            val vc = VerificationBottomSheet(::handle)
//            vc.viewToDelete = transparentView
//            vc.mainView = getView()?.rootView as ViewGroup
//            vc.viewModel = viewModel
//            vc.arguments = Bundle().apply {
//                putString(Constants.TYPE, clickedOn)
//            }
//            vc.show(childFragmentManager, App.prefsManager.user?.type2FA)


            // Add the transparent view to the RelativeLayout
            val mainView = getView()?.rootView as ViewGroup
            mainView.addView(transparentView, viewParams)
        }
        isResend = false
    }

    private fun handle(code: String) {
        if (code == "Resend") {
            isResend = true
            confirmButtonClick()
        } else {
            CommonMethods.showProgressDialog(requireActivity())
            isOtpScreen = false
            viewModel.createWithdrawalRequest(
                viewModel.selectedAssetDetail!!.id,
                valueTotal,
                viewModel.withdrawAddress!!.address!!,
                viewModel.selectedNetworkDeposit!!.id,
                code
            )
        }
    }

    //    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmButtonClick() {
        if (!isResend)
            CommonMethods.showProgressDialog(requireActivity())
        val map = HashMap<Any?, Any?>()
        map["asset"] = viewModel.selectedAssetDetail!!.id
        map["amount"] = valueTotal
        map["destination"] = viewModel.withdrawAddress!!.address
        map["network"] = viewModel.selectedNetworkDeposit!!.id
        val jso = JSONObject(map)
        isOtpScreen = true
        var encoded = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            encoded = String(java.util.Base64.getEncoder().encode(jso.toString(4).toByteArray()))
        else {
            val jsonString = jso.toString(4) // Convert JSONObject to string with indentation

            val bytes = jsonString.toByteArray() // Convert string to bytes

            encoded =
                android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        }
        if (isResend)
            viewModelSignup.getOtpForWithdraw(Constants.ACTION_WITHDRAW, encoded)
        else
            viewModel.getOtpForWithdraw(Constants.ACTION_WITHDRAW, encoded)
    }



    private fun prepareView() {
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
                tvValueDepositFee,
                tvInfo
            ).gone()
        }
        binding.apply {
            tvNestedAmount.text = getString(R.string.amount)

            viewModel.selectedAssetDetail.let {
                val balance =
                    com.Lyber.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble() ?: 1.0)
                tvValueLyberFee.text =
                    viewModel.selectedNetworkDeposit!!.withdrawFee.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) + " " + it!!.id.uppercase()


                tvExchangeFrom.text = getString(R.string.address)
                tvExchangeFromValue.text =
                    viewModel.withdrawAddress!!.address!!.substring(0, 7) + "...." +
                            viewModel.withdrawAddress!!.address!!.substring(
                                viewModel.withdrawAddress!!.address
                                !!.length - 4
                            )
                tvExchangeTo.text = getString(R.string.network)
                tvExchangeToValue.text = viewModel.selectedNetworkDeposit!!.fullName
                valueTotal =
                    requireArguments().getString(Constants.EURO, "")
                        .replace(it.id.uppercase(), "").toDouble()
                tvValueTotal.text =
                    "${
                        valueTotal.toString().formattedAsset(
                            price = priceCoin,
                            rounding = RoundingMode.DOWN
                        )
                    } ${it.id.uppercase()}"
                tvAmount.text =
                    "${
                        valueTotal.toString().formattedAsset(
                            price = priceCoin,
                            rounding = RoundingMode.DOWN
                        )
                    } ${it.id.uppercase()}"
                val amount = valueTotal - viewModel.selectedNetworkDeposit!!.withdrawFee.toDouble()
                tvNestedAmountValue.text = "${
                    amount.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    )
                } ${it.id.uppercase()}"

                btnConfirmInvestment.isEnabled = true
                btnConfirmInvestment.text =
                    getString(R.string.confirm_withdrawal)
                title.text = getString(R.string.confirm_withdrawal)
            }


        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnConfirmInvestment -> confirmButtonClick()
                tvMoreDetails -> {
                    if (isExpand) {
                        zzInfor.gone()
                        isExpand = false
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_right_arrow_grey,
                            0,
                            0,
                            0
                        )
                    } else {
                        zzInfor.visible()
                        isExpand = true
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_drop_down,
                            0,
                            0,
                            0
                        )
                    }
                }
            }
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        CommonMethods.dismissProgressDialog()
        CommonMethods.dismissAlertDialog()
        val code = CommonMethods.showErrorMessage(requireContext(), responseBody, binding.root)
        Log.d("errorCode", "$code")
        if (code == 7023 || code == 10041 || code == 7025 || code == 10043)
            customDialog(code)
    }
}