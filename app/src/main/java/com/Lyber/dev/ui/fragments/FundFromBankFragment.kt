package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.dev.databinding.FragmentFundFromAccountBinding

class FundFromBankFragment : BaseFragment<FragmentFundFromAccountBinding>() {

    override fun bind() = FragmentFundFromAccountBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

    }
}