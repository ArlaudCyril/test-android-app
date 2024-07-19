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
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentConfirmInvestmentBinding
import com.Lyber.models.Balance
import com.Lyber.network.RestClient
import com.Lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet2FA
import com.Lyber.utils.App
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
import java.util.ArrayList

class ConfirmWithdrawalFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var viewModelSignup: SignUpViewModel
    private var valueTotal: Double = 0.0
    private var valueTotalEuro: Double = 0.0
    private var isExpand = false
    private var isOtpScreen = false
    private var isResend = false
    private var withdrawUSDC = false
    private var withdrawEuroFee = 0.66

    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModelSignup = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        viewModelSignup.listener = this
        if (arguments != null && requireArguments().containsKey(Constants.FROM) &&
            requireArguments().getString(Constants.FROM) == WithdrawUsdcFragment::class.java.name
        ) {
            withdrawUSDC = true
            binding.title.text = getString(R.string.confirm_withdrawal)
            if (requireArguments().containsKey(Constants.FEE))
                withdrawEuroFee = requireArguments().getDouble(Constants.FEE)
        } else
            withdrawUSDC = false

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
                    if (withdrawUSDC) {
                        viewModel.getBalance()
                        viewModel.selectedOption = Constants.ACTION_WITHDRAW_EURO
                    } else
                        viewModel.selectedOption = Constants.USING_WITHDRAW
                    ConfirmationBottomSheet().show(childFragmentManager, "")
                }
            }
        }
        viewModel.balanceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict?.forEach {
                    val balance = Balance(id = it.key, balanceData = it.value)
                    balances.add(balance)
                }
                com.Lyber.ui.activities.BaseActivity.balances = balances
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
            if (withdrawUSDC) {
                viewModel.createWithdrawalEuroRequest(
                    viewModel.ribDataAddress!!.ribId,
                    viewModel.ribDataAddress!!.iban, viewModel.ribDataAddress!!.bic,
                    valueTotalEuro, code
                )
            } else
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
        if (withdrawUSDC) {
            map["destination"] = viewModel.ribDataAddress!!.iban
            map["asset"] = "usdc"
//            map["amount"] = valueTotal
            map["amount"] = valueTotalEuro
        } else {
            map["asset"] = viewModel.selectedAssetDetail!!.id
            map["amount"] = valueTotal
            map["destination"] = viewModel.withdrawAddress!!.address
            map["network"] = viewModel.selectedNetworkDeposit!!.id
        }
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
        if (withdrawUSDC) {
            if (isResend)
                viewModelSignup.getOtpForWithdraw(Constants.ACTION_WITHDRAW_EURO, encoded)
            else
                viewModel.getOtpForWithdraw(Constants.ACTION_WITHDRAW_EURO, encoded)

        } else {
            if (isResend)
                viewModelSignup.getOtpForWithdraw(Constants.ACTION_WITHDRAW, encoded)
            else
                viewModel.getOtpForWithdraw(Constants.ACTION_WITHDRAW, encoded)
        }
    }

    private fun prepareView() {
        binding.apply {
            if (withdrawUSDC) {
                btnConfirmInvestment.text = getString(R.string.confirm_withdrawal)
                listOf(
                    tvNestedAmount,
                    tvNestedAmountValue,
                    tvExchangeFrom, tvExchangeFromValue, tvExchangeTo, tvExchangeToValue,
                    tvLyberFee,
                    tvValueLyberFee, tvDeposit
                ).visible()
                listOf(
                    tvFrequency,
                    tvValueFrequency,
                    tvAllocation,
                    allocationView,
                    tvAssetPrice,
                    tvValueAssetPrice,
                    tvDepositFee,
                    tvValueDepositFee,
                    tvInfo
                ).gone()
                tvNestedAmount.text = getString(R.string.iban)
                val maxLength = 20 // Adjust this value as needed
                val truncatedText =
                    CommonMethods.getTruncatedText(viewModel.ribDataAddress!!.iban, maxLength)
                tvNestedAmountValue.text = truncatedText
//                tvNestedAmountValue.text = viewModel.ribDataAddress?.iban
                tvExchangeFrom.text = getString(R.string.bic)
                val truncatedTextBic =
                    CommonMethods.getTruncatedText(viewModel.ribDataAddress!!.bic, maxLength)
                binding.tvExchangeFromValue.text = truncatedTextBic
//                tvExchangeFromValue.text = viewModel.ribDataAddress?.bic
                tvExchangeTo.text = getString(R.string.sell)
                tvLyberFee.text = getString(R.string.fee)
                tvValueLyberFee.text = "${withdrawEuroFee} ${Constants.EUR}" //for now static fee
                tvDeposit.text = getString(R.string.receive)
                valueTotal =
                    requireArguments().getString(Constants.EURO, "")
                        .replace(Constants.EURO, "").toDouble()

                tvValueDeposit.text = "~${valueTotal - withdrawEuroFee} " + Constants.EUR

                tvTotal.text = getString(R.string.total)

                tvValueTotal.text = "${valueTotal} ${Constants.EUR}"
                tvAmount.text = "${valueTotal} ${Constants.EUR}"
                valueTotalEuro = requireArguments().getString(
                    Constants.MAIN_ASSET,
                    ""
                ).toDouble()
                tvExchangeToValue.text = requireArguments().getString(
                    Constants.MAIN_ASSET,
                    ""
                ) + Constants.MAIN_ASSET_UPPER

            } else {
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

                binding.apply {
                    tvNestedAmount.text = getString(R.string.amount)

                    viewModel.selectedAssetDetail.let {
                        val balance =
                            com.Lyber.ui.activities.BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                        val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                            .div(balance.balanceData.balance.toDouble() ?: 1.0)
                        tvValueLyberFee.text =
                            viewModel.selectedNetworkDeposit!!.withdrawFee.toString()
                                .formattedAsset(
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
                        val amount =
                            valueTotal - viewModel.selectedNetworkDeposit!!.withdrawFee.toDouble()
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