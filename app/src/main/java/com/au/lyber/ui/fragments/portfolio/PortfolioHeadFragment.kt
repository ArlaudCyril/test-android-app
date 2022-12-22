package com.au.lyber.ui.fragments.portfolio

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTestPortfolioHeadBinding
import com.au.lyber.ui.fragments.BaseFragment
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.addTypeface
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants

class PortfolioHeadFragment : BaseFragment<FragmentTestPortfolioHeadBinding>() {

    private val viewModel get() = (requireParentFragment() as PortfolioTestFragment).viewModel

    override fun bind() = FragmentTestPortfolioHeadBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAssetResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

//                App.prefsManager.setBalance(it.total_euros_available.toString().toFloat())

                if (it.assets.isNotEmpty()) {

                    viewModel.totalPortfolio = 0.0

                    it.assets.forEach {
                        viewModel.totalPortfolio += (it.coin_detail?.current_price
                            ?: 1.0) * it.total_balance
                    }

                    binding.tvValuePortfolioAndAssetPrice.text =
                        "${viewModel.totalPortfolio.commaFormatted} ${Constants.EURO}"

                }

                CommonMethods.dismissProgressDialog()
            }
        }
    }
}