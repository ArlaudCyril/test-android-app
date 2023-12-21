package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentDiscoveryBinding
import com.Lyber.utils.App
import com.Lyber.utils.Constants

class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>() {
    override fun bind() = FragmentDiscoveryBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean(Constants.FOR_LOGIN, false)
            }
            if (App.prefsManager.userPin.isNotEmpty() && App.prefsManager.refreshToken.isEmpty()) {
                findNavController().navigate(R.id.completePortfolioFragment)
            } else {
                findNavController().navigate(R.id.createAccountFragment, bundle)
            }
        }

        binding.tvLogin.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean(Constants.FOR_LOGIN,true)
            }
            findNavController().navigate(R.id.createAccountFragment,bundle)
        }
    }

}