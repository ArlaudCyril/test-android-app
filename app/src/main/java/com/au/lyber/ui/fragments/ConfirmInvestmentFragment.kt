package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.ConfirmationDialogBinding
import com.au.lyber.databinding.FragmentConfirmInvestmentBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.au.lyber.ui.portfolio.fragment.PortfolioHomeFragment
import com.au.lyber.utils.CommonMethods.Companion.addFragment
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import java.util.*

class ConfirmInvestmentFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false

        prepareView()

        viewModel.investSingleAssetResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                showDialog(
                    viewModel.selectedAsset?.imageUrl ?: "",
                    viewModel.assetAmount,
                    viewModel.selectedAsset?.id ?: ""
                )
//                ConfirmationBottomSheet().show(childFragmentManager, "")
            }
        }

        viewModel.investStrategyResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                ConfirmationBottomSheet().show(childFragmentManager, "")
            }
        }

        viewModel.exchangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                ConfirmationBottomSheet().show(childFragmentManager, "")
            }
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressed()

                btnConfirmInvestment -> {

                    when (viewModel.selectedOption) {

                        Constants.USING_SINGULAR_ASSET -> {
                            checkInternet(requireContext()) {
                                showProgressDialog(requireContext())
                                viewModel.investSingleAsset(
                                    viewModel.selectedAsset!!,
                                    viewModel.amount.toFloat().toInt(),
                                    viewModel.assetAmount.toFloat(),
                                    viewModel.selectedFrequency.uppercase()
                                )
                            }
                        }

                        Constants.USING_STRATEGY -> {
                            viewModel.selectedStrategy?.let {
                                checkInternet(requireContext()) {
                                    showProgressDialog(requireContext())
                                    viewModel.investStrategy(
                                        it._id,
                                        viewModel.selectedFrequency.uppercase(),
                                        viewModel.amount.toFloat().toInt()
                                    )
                                }
                            }
                        }

                        Constants.USING_EXCHANGE -> {
                            checkInternet(requireContext()) {
                                showProgressDialog(requireContext())
                                viewModel.exchange(
                                    viewModel.exchangeAssetFrom?.id ?: "",
                                    viewModel.exchangeAssetTo?.id ?: "",
                                    viewModel.exchangeFromAmount,
                                    viewModel.exchangeToAmount
                                )
                            }
                        }

                        Constants.USING_WITHDRAW -> {
                            ConfirmationBottomSheet().show(childFragmentManager, "")
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
                viewModel.amount.decimalPoint().commaFormatted + Constants.EURO
            tvValueTotal.text =
                (viewModel.amount.toFloat() + buyValue).toString()
                    .decimalPoint().commaFormatted + Constants.EURO
            tvValueLyberFee.text =
                buyValue.toString().decimalPoint().commaFormatted + Constants.EURO

            when (viewModel.selectedOption) {

                Constants.USING_STRATEGY -> {

                    listOf(
                        tvFrequency,
                        tvValueFrequency,
                        tvAllocation,
                        allocationView,
                        tvLyberFee,
                        tvValueLyberFee
                    ).visible()

                    viewModel.selectedStrategy?.investment_strategy_assets?.let {
                        binding.allocationView.setAssetsList(it)
                    }

                    listOf(
                        tvAssetPrice,
                        tvValueAssetPrice,
                        tvDeposit,
                        tvDepositFee,
                        tvValueDeposit,
                        tvValueDepositFee
                    ).gone()

                    tvValueFrequency.text = viewModel.selectedFrequency

                    tvAmount.text =
                        "${viewModel.amount.decimalPoint().commaFormatted} ${Constants.EURO}"

                }

                Constants.USING_DEPOSIT -> {
                    listOf(
                        tvAssetPrice,
                        tvValueAssetPrice,
                        tvFrequency,
                        tvValueFrequency,
                        tvAllocation,
                        allocationView,
                        tvLyberFee,
                        tvValueLyberFee
                    ).gone()
                    listOf(tvDeposit, tvDepositFee, tvValueDeposit, tvValueDepositFee).visible()

                    tvAmount.text =
                        "${viewModel.amount.decimalPoint().commaFormatted} ${Constants.EURO}"

                }

                Constants.USING_SINGULAR_ASSET -> {

                    listOf(
                        tvAssetPrice,
                        tvValueAssetPrice,
                        tvFrequency,
                        tvValueFrequency,
                        tvLyberFee,
                        tvValueLyberFee
                    ).visible()

                    listOf(
                        tvAllocation,
                        allocationView,
                        tvDeposit,
                        tvDepositFee,
                        tvValueDeposit,
                        tvValueDepositFee
                    ).gone()

                    if (viewModel.selectedFrequency.isEmpty()) {
                        tvFrequency.gone()
                        tvValueFrequency.gone()
                    }


                    tvAssetPrice.text =
                        viewModel.selectedAsset?.fullName?.capitalize(Locale.ROOT) + " Price"
                    /*tvValueAssetPrice.text =
                        ((viewModel.selectedAsset?.euro_amount.toString()
                            .roundFloat().commaFormatted)) + Constants.EURO TODO*/
                    tvValueFrequency.text = viewModel.selectedFrequency

                    tvAmount.text = viewModel.assetAmount.commaFormatted
                    viewModel.selectedAsset?.imageUrl?.let {
                        ivSingleAsset.visible()
                        ivSingleAsset.loadCircleCrop(viewModel.selectedAsset?.imageUrl ?: "")
                    }

                }

                Constants.USING_EXCHANGE -> {

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

                    tvAmount.text =
                        "${viewModel.assetAmount.commaFormatted} ${viewModel.exchangeAssetTo?.id?.uppercase()}"

                    tvNestedAmount.text =
                        "${viewModel.exchangeAssetFrom?.fullName?.capitalize()} Price"
                    /*tvNestedAmountValue.text =
                        "${viewModel.exchangeAssetFrom?.euro_amount.commaFormatted} ${Constants.EURO}"
                    TODO */
                    tvExchangeFromValue.text =
                        "${viewModel.amount.commaFormatted} ${viewModel.exchangeAssetFrom?.id?.uppercase()}"
                    tvExchangeToValue.text =
                        "${viewModel.assetAmount.commaFormatted} ${viewModel.exchangeAssetTo?.id?.uppercase()}"


                    tvValueTotal.text = "${viewModel.assetAmount.commaFormatted} ${viewModel.exchangeAssetTo?.id?.uppercase()}"

                    btnConfirmInvestment.text = "Confirm exchange"
                    title.text = "Confirm exchange"

                }

            }

        }
    }


    private fun showDialog(logo: String, amount: String, assetSymbol: String) {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            ConfirmationDialogBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(true)
                setContentView(it.root)

                 it.ivAsset.loadCircleCrop(logo)

                it.tvAssetAmount.text = amount.commaFormatted

                it.tvMessage.text =
                    "You now own ${assetSymbol.uppercase()} ${amount.commaFormatted}"

                it.root.setOnClickListener {
                    dismiss()
                }

                setOnDismissListener {
                    requireActivity().clearBackStack()
                }

                show()
            }
        }
    }
}