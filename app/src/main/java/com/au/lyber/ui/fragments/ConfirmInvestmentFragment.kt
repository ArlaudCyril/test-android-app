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
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods.Companion.addFragment
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoint
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
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


        viewModel.investStrategyResponse.observe(viewLifecycleOwner) {
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
                        Constants.USING_STRATEGY -> {
                            viewModel.selectedStrategy?.let {
                                checkInternet(requireContext()) {
                                    showProgressDialog(requireContext())
                                    viewModel.investStrategy(
                                        it.ownerUuid,
                                        viewModel.selectedFrequency.uppercase(),
                                        viewModel.amount.toFloat().toInt()
                                    )
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

                    viewModel.selectedStrategy?.bundle?.let {
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
                    getString(R.string.you_now_own, assetSymbol.uppercase(), amount.commaFormatted)

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