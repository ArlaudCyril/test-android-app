package com.Lyber.ui.fragments.bottomsheetfragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.Lyber.databinding.BottomSheetInvestBinding
import com.Lyber.ui.portfolio.fragment.PortfolioHomeFragment

class InvestBottomSheet(val clickListener: (Boolean) -> Unit) :
    BaseBottomSheet<BottomSheetInvestBinding>(), View.OnClickListener {

    override fun bind() = BottomSheetInvestBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clInvestInStrategy.setOnClickListener(this)
        binding.clInvestInAsset.setOnClickListener(this)

        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> dismiss()
                clInvestInStrategy -> {
                    clickListener(true)
                    dismiss()
                }

                clInvestInAsset -> {
                    clickListener(false)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val view = PortfolioHomeFragment.fragmentPortfolio.binding.screenContent
        val viewToDelete = view.getChildAt(view.childCount - 1)
        view.removeView(viewToDelete)
    }


}