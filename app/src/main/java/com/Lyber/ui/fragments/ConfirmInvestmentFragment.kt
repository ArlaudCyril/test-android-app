package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.marginTop
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentConfirmInvestmentBinding
import com.Lyber.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods.Companion.addFragment
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import java.util.*

class ConfirmInvestmentFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false

        prepareView()


        viewModel.investStrategyResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                findNavController().popBackStack(R.id.pickYourStrategyFragment, false)

//                findNavController().navigate(R.id.pickYourStrategyFragment)
            }
        }

        viewModel.oneTimeStrategyDataResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                Log.d("dataa", "${it.data.id}")
                val bundle = Bundle()
                bundle.putString("executionId", it.data.id)
                findNavController().navigate(R.id.orderStrategyExecutionFragment, bundle)
            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressed()

                btnConfirmInvestment -> {

                    when (viewModel.selectedOption) {
                        Constants.USING_STRATEGY -> {
                            viewModel.selectedStrategy?.let {
                                checkInternet(requireContext()) {
                                    /*frequency = "now" || "1d" || "1w" || "1m"*/
                                    val freq = when (viewModel.selectedFrequency) {
                                        "Once" -> null     //"now"
                                        "Daily" -> "1d"
                                        "Weekly" -> "1w"
                                        "none" -> "none"
                                        else -> "1m"
                                    }
                                    showProgressDialog(requireContext())
//                                    viewModel.investStrategy(
//                                        it.ownerUuid,
//                                        freq,
//                                        viewModel.amount.toFloat().toInt()
//                                        ,viewModel.selectedStrategy!!.name)


//                                    showProgressDialog(requireContext())
                                    if (freq == "none") {
                                        viewModel.oneTimeOrderStrategy(
                                            viewModel.selectedStrategy!!.name,
                                            viewModel.amount.toFloat().toDouble(),
                                            it.ownerUuid,
                                        )
                                    } else {
                                        showProgressDialog(requireContext())
                                        if (arguments != null && requireArguments().getBoolean(
                                                Constants.EDIT_ACTIVE_STRATEGY
                                            )
                                        )
                                            viewModel.editEnabledStrategy(
                                                it.ownerUuid,
                                                freq,
                                                viewModel.amount.toFloat().toInt(),
                                                viewModel.selectedStrategy!!.name
                                            )
                                        else

                                            viewModel.investStrategy(
                                                it.ownerUuid,
                                                freq,
                                                viewModel.amount.toFloat().toInt(),
                                                viewModel.selectedStrategy!!.name
                                            )
                                    }
                                }
                            }
                        }

                        else -> {
                            requireActivity().clearBackStack()
                            requireActivity().addFragment(
                                R.id.flSplashActivity,
                                PortfolioHomeFragment()
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView() {
        binding.apply {
            val buyValue = (viewModel.amount.toFloat().toInt() * (0.08)).toFloat()
            tvNestedAmountValue.text =
                viewModel.amount.decimalPoint().commaFormatted + " USDT"
            tvValueTotal.text =
                (viewModel.amount.toFloat() + buyValue).toString()
                    .decimalPoint().commaFormatted + " USDT"
            tvValueLyberFee.text =
                buyValue.toString().decimalPoint().commaFormatted + " USDT"

            when (viewModel.selectedOption) {

                Constants.USING_STRATEGY -> {

                    listOf(
                        zzInfor,
                        tvFrequency,
                        tvValueFrequency,
                        tvAllocation,
                        allocationView,
                        tvLyberFee,
                        tvValueLyberFee
                    ).visible()

                    viewModel.selectedStrategy?.bundle?.let {
                        binding.allocationView.setAssetsList(it)
                    }

                    listOf(
                        ivSingleAsset,
                        tvTotalAmount,
                        tvMoreDetails,
                        tvAssetPrice,
                        tvValueAssetPrice,
                        tvDeposit,
                        tvDepositFee,
                        tvValueDeposit,
                        tvValueDepositFee
                    ).gone()
                    //changed fee to 1 percent of the amount
                    var fee = ((viewModel.amount.toFloat() * 0.5) / 100.0)
                    fee = String.format("%.2f", fee).toFloat().toDouble()

                    tvNestedAmount.text = getString(R.string.invest)

                    if (viewModel.selectedFrequency == "none")
                        tvValueFrequency.text = getString(R.string.immediate)
                    else
                        tvValueFrequency.text = viewModel.selectedFrequency

                    tvNestedAmountValue.text = (viewModel.amount.toFloat() - fee).toString()
                        .decimalPoint().commaFormatted + " USDT"
                    viewModel.amount.decimalPoint().commaFormatted + " USDT"
                    tvValueTotal.text =
                        (viewModel.amount.toFloat()).toString()
                            .decimalPoint().commaFormatted + " USDT"
                    tvLyberFee.text = getString(R.string.fee)
                    tvValueLyberFee.text =
                        "~" + fee.toString().decimalPoint() + " USDT"


                    tvAmount.text =
                        (viewModel.amount.toFloat()).toString() + " USDT"

                }


            }

        }
    }


}