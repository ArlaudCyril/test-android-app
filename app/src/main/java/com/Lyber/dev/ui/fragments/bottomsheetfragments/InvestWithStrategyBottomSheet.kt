package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.Lyber.dev.databinding.BottomSheetInvestWithStrategyBinding
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible

class InvestWithStrategyBottomSheet(val clickListener: (Int) -> Unit) :
    BaseBottomSheet<BottomSheetInvestWithStrategyBinding>(), View.OnClickListener {
    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup
    lateinit var viewModel: PortfolioViewModel
    var clickVal = -1
    override fun bind() = BottomSheetInvestWithStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        isCancelable = false
        viewModel.listener = this
        binding.clAdjust.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        //
        binding.clPause.setOnClickListener(this)
        binding.clDelete.setOnClickListener(this)
        binding.clTailor.setOnClickListener(this)
        binding.clOneTimeInvest.setOnClickListener(this)
        //
        binding.clAdjustNew.setOnClickListener(this)
        if (viewModel.selectedStrategy!!.activeStrategy != null) {
            binding.apply {
                clDelete.gone()
                clPause.visible()
                clAdjust.gone()
                clAdjustNew.visible()
            }
        } else {
            binding.apply {
                clDelete.visible()
                clPause.gone()
                clAdjust.visible()
                clAdjustNew.gone()
                if (viewModel.selectedStrategy!!.publicType != null && viewModel.selectedStrategy!!.publicType
                    == "lyber"
                ) {
                    ivDelete.gone()
                    tvTitleDelete.gone()
                    ivRightArrowDelete.gone()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> {
                    clickVal = -1
                    dismiss()
                }

                clAdjust -> {
                    clickVal = 0
                    dismiss()
                }

                clPause -> {
                    clickVal = 1
                    dismiss()
                }

                clDelete -> {
                    clickVal = 2
                    dismiss()
                }

                clTailor -> {
                    clickVal = 3
                    dismiss()
                }

                clOneTimeInvest -> {
                    clickVal = 4
                    dismiss()
                }

                clAdjustNew -> {
                    clickVal = 5
                    dismiss()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        clickListener(clickVal)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.mainView.removeView(this.viewToDelete)
    }


}