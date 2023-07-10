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
    private lateinit var navController : NavController
    override fun bind() = FragmentDiscoveryBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        val navOptions =  NavOptions.Builder()
            .setPopUpTo(R.id.discoveryFragment, true)
            .build()
        navController.graph.setStartDestination(R.id.signUpFragment)
        binding.btnSignUp.setOnClickListener {
            Log.d("clickSignup","Signup")
            navController.navigate(R.id.signUpFragment)
        }

        binding.tvLogin.setOnClickListener {
            val bundle = Bundle().apply {
                putString("forLogin", "forLogin")
            }
            Log.d("clickSignup","Login ${bundle.toString()}")
            navController.navigate(R.id.signUpFragment,bundle)
        }
    }

}