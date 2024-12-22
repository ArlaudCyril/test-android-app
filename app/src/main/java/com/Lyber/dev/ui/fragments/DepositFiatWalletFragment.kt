package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.FragmentDepositFiatWalletBinding
import com.Lyber.utils.CommonMethods.Companion.replaceFragment

class DepositFiatWalletFragment : BaseFragment<FragmentDepositFiatWalletBinding>() {

    override fun bind() = FragmentDepositFiatWalletBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.rlFundAccount.setOnClickListener {
            requireActivity().replaceFragment(R.id.flSplashActivity, FundFromBankFragment())
        }

        binding.rlAddCard.setOnClickListener {
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                AddCreditCardFragment()
            )
        }
    }


}