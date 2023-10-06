package com.au.lyber.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmInvestmentBinding
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.au.lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet2FA
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import org.json.JSONObject
import java.math.RoundingMode
import java.util.Base64


class ConfirmWithdrawalFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private lateinit var viewModel: PortfolioViewModel
    private  var valueTotal : Double =0.0
    private var isOtpScreen = false

    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false
        prepareView()
        setObservers()

    }

    private fun setObservers() {
        viewModel.commonResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (isOtpScreen) {
                    openOtpScreen()
                } else {
                    viewModel.selectedOption = Constants.USING_WITHDRAW
                    ConfirmationBottomSheet().show(childFragmentManager, "")
                }
            }
        }
    }

    private fun openOtpScreen() {
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

        val vc  = VerificationBottomSheet2FA(::handle)
        vc.viewToDelete = transparentView
        vc.mainView = getView()?.rootView as ViewGroup
        vc.viewModel = viewModel
        vc.show(childFragmentManager, "")

        // Add the transparent view to the RelativeLayout
        val mainView = getView()?.rootView as ViewGroup
        mainView.addView(transparentView, viewParams)
    }

    private fun handle() {
        CommonMethods.showProgressDialog(requireActivity())
        isOtpScreen = false
        viewModel.createWithdrawalRequest(viewModel.selectedAssetDetail!!.id
            ,valueTotal,viewModel.withdrawAddress!!.address,viewModel.selectedNetworkDeposit!!.id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmButtonClick() {
        //openOtpScreen()
     /* val isScope2FA = App.prefsManager.user!!.scope2FA.contains("withdrawal")
        if (isScope2FA){*/
            CommonMethods.showProgressDialog(requireActivity())
            val map = HashMap<Any?,Any?>()
            map["asset"] = viewModel.selectedAssetDetail!!.id
            map["amount"] = valueTotal
            map["destination"] = viewModel.withdrawAddress!!.address
            map["network"] = viewModel.selectedNetworkDeposit!!.id
            val jso = JSONObject(map)
            isOtpScreen = true
            val encoded = String(Base64.getEncoder().encode(jso.toString(4).toByteArray()))
            viewModel.getOtpForWithdraw(Constants.ACTION_WITHDRAW,encoded)
        /*}else{
            CommonMethods.showProgressDialog(requireActivity())
            isOtpScreen = false
            viewModel.createWithdrawalRequest(viewModel.selectedAssetDetail!!.id
            ,valueTotal,viewModel.withdrawAddress!!.address,viewModel.selectedNetworkDeposit!!.id)
        }*/
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
                    BaseActivity.balances.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.selectedAssetDetail!!.id } }
                val priceCoin = balance!!.balanceData.euroBalance.toDouble()
                    .div(balance.balanceData.balance.toDouble() ?: 1.0)
                tvNestedAmountValue.text = requireArguments().getString(Constants.EURO)+" "+it!!.id.uppercase()
                tvValueLyberFee.text =
                    viewModel.selectedNetworkDeposit!!.withdrawFee.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) + " "+it!!.id.uppercase()


                tvExchangeFrom.text = getString(R.string.address)
                tvExchangeFromValue.text = viewModel.withdrawAddress!!.address.substring(0,7)+"...."+
                        viewModel.withdrawAddress!!.address.substring(viewModel.withdrawAddress!!.address
                            .length-4)
                tvExchangeTo.text = getString(R.string.network)
                tvExchangeToValue.text = viewModel.selectedNetworkDeposit!!.fullName
                 valueTotal = viewModel.selectedNetworkDeposit!!.withdrawFee.toDouble()+
                        requireArguments().getString(Constants.EURO,"")
                            .replace(it.id.uppercase(),"").toDouble()
                tvValueTotal.text =
                    "${valueTotal.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) } ${it.id.uppercase()}"
                tvAmount.text =
                    "${valueTotal.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN
                    ) } ${it.id.uppercase()}"
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
            when(v!!){
                ivTopAction->requireActivity().onBackPressedDispatcher.onBackPressed()
                btnConfirmInvestment-> confirmButtonClick()
        }
        }
    }
}