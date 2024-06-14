package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.marginTop
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentConfirmInvestmentBinding
import com.Lyber.dev.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.addFragment
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormattedDecimal
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import java.util.*

class ConfirmInvestmentFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    private var decimal = 2
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false
        if (arguments != null && requireArguments().containsKey(Constants.DECIMAL))
            decimal = requireArguments().getInt(Constants.DECIMAL)
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
                                    if (freq == "none") {
                                        viewModel.oneTimeOrderStrategy(
                                            viewModel.selectedStrategy!!.name,
                                            viewModel.amount.toFloat().toDouble(),
                                            it.ownerUuid,
                                        )
                                    } else {
                                        if (arguments != null && requireArguments().getBoolean(
                                                Constants.EDIT_ACTIVE_STRATEGY
                                            )
                                        )
                                            viewModel.editEnabledStrategy(
                                                it.ownerUuid,
                                                freq,
                                                viewModel.amount.toFloat().toDouble(),
                                                viewModel.selectedStrategy!!.name
                                            )
                                        else

                                            viewModel.investStrategy(
                                                it.ownerUuid,
                                                freq,
                                                viewModel.amount.toFloat().toDouble(),
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
            val buyValue = (viewModel.amount.toDouble() * (0.08)).toDouble()
            tvNestedAmountValue.text =
                viewModel.amount.decimalPoint().commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"
            tvValueTotal.text =
                (viewModel.amount.toFloat() + buyValue).toString()
                    .decimalPoint().commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"
            tvValueLyberFee.text =
                buyValue.toString().decimalPoint().commaFormattedDecimal(decimal)+ " ${Constants.MAIN_ASSET_UPPER}"

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
                    var fee = ((viewModel.amount.toFloat() * 0.5f) / 100.0f)
                    fee = String.format(Locale.US, "%.${decimal}f", fee).toFloat()

                    tvNestedAmount.text = getString(R.string.invest)

                    if (viewModel.selectedFrequency == "none")
                        tvValueFrequency.text = getString(R.string.immediate)
                    else
                        tvValueFrequency.text = viewModel.selectedFrequency

                    tvNestedAmountValue.text = (viewModel.amount.toFloat() - fee).toString()
                        .decimalPoint().commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"



//                    viewModel.amount.decimalPoint().commaFormatted + " ${Constants.MAIN_ASSET_UPPER}"
                    tvValueTotal.text =
                        (viewModel.amount.toFloat()).toString()
                            .decimalPoint().commaFormatted + " ${Constants.MAIN_ASSET_UPPER}"
                    tvLyberFee.text = getString(R.string.fee)
                    tvValueLyberFee.text =
                        "~" + fee.toString().decimalPoint() + " ${Constants.MAIN_ASSET_UPPER}"


                    tvAmount.text =
                        (viewModel.amount.toFloat()).toString() + " ${Constants.MAIN_ASSET_UPPER}"

                }


            }

        }
    }


}