package com.au.lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.transition.Fade
import android.view.View
import android.widget.MediaController
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSplashBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.is30DaysOld

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    private lateinit var handler : Handler
    private lateinit var runnable : Runnable
    private var isScreen = false

    override fun bind() = FragmentSplashBinding.inflate(layoutInflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
        exitTransition = Fade()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler =  Handler(Looper.getMainLooper())
           runnable= Runnable {

            // 1- 30 days check if greater then logout if not normal flow
            // check for 24 hours if greater hit api for refresh token if not normal flow
            // if not check for user pin

            if (!isScreen) {
                isScreen = true
                if (App.prefsManager.tokenSavedAt.is30DaysOld()) {
                    App.prefsManager.logout()
                    findNavController().navigate(R.id.discoveryFragment)
                } else {

                    if (App.prefsManager.userPin.isNotEmpty()) {

                        findNavController().navigate(R.id.unlockAppFragment)
                    } else {
                        findNavController().navigate(R.id.discoveryFragment)

                    }

                }
            }


        }
        handler.postDelayed(runnable,1500)


    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}