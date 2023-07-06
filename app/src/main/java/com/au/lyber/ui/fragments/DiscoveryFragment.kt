package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentDiscoveryBinding

class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>() {
    private lateinit var navController : NavController
    override fun bind() = FragmentDiscoveryBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.btnSignUp.setOnClickListener {
            navController.navigate(R.id.signUpFragment)
        }

        binding.tvLogin.setOnClickListener {
            val bundle = Bundle().apply {
                putString("forLogin", "forLogin")
            }
            navController.navigate(R.id.signUpFragment,bundle)
        }
    }

}