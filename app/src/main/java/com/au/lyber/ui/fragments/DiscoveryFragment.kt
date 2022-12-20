package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.R
import com.au.lyber.databinding.FragmentDiscoveryBinding
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment

class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>() {

    override fun bind() = FragmentDiscoveryBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                SignUpFragment(),
                topBottom = true
            )
        }

        binding.tvLogin.setOnClickListener {
            val bundle = Bundle().apply {
                putString("forLogin", "forLogin")
            }
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                SignUpFragment().apply {
                    arguments = bundle
                },
                topBottom = true
            )
        }
    }

}