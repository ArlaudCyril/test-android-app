package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import com.Lyber.databinding.BottomSheetEnterCodeBinding
import com.Lyber.ui.portfolio.fragment.PortfolioHomeFragment

class EnterCodeBottomSheet(val clickListener: (Boolean) -> Unit) :
    BaseBottomSheet<BottomSheetEnterCodeBinding>(), View.OnClickListener {

    override fun bind() = BottomSheetEnterCodeBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val view = PortfolioHomeFragment.fragmentPortfolio.binding.screenContent
        val viewToDelete = view.getChildAt(view.childCount-1)
        view.removeView(viewToDelete)
    }
}