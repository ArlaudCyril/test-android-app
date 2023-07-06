package com.au.lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.transition.Fade
import android.view.View
import android.widget.MediaController
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSplashBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.is30DaysOld

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    private lateinit var navController : NavController
    private lateinit var mediaController: MediaController

    override fun bind() = FragmentSplashBinding.inflate(layoutInflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
        exitTransition = Fade()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController= navHostFragment.navController
        Handler(Looper.getMainLooper()).postDelayed({

            // 1- 30 days check if greater then logout if not normal flow
            // check for 24 hours if greater hit api for refresh token if not normal flow
            // if not check for user pin


            if (App.prefsManager.tokenSavedAt.is30DaysOld()) {

                App.prefsManager.logout()
                navController.navigate(R.id.discoveryFragment)
//                requireActivity().supportFragmentManager.beginTransaction()
//                    .replace(R.id.flSplashActivity, DiscoveryFragment()).commit()

            } else {

                if (App.prefsManager.userPin.isNotEmpty())
                {

                    navController.navigate(R.id.unlockAppFragment)
                }
                else {
                    navController.navigate(R.id.discoveryFragment)

                }

            }



        }, 1500)

    }
}