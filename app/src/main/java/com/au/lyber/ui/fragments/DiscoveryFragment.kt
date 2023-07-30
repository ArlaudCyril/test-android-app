package com.au.lyber.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentDiscoveryBinding

class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>() {
    override fun bind() = FragmentDiscoveryBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("forLogin",false)
            }
            findNavController().navigate(R.id.createAccountFragment,bundle)
        }

        binding.tvLogin.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("forLogin",true)
            }
            findNavController().navigate(R.id.createAccountFragment,bundle)
        }
    }

}