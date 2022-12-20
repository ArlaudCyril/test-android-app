package com.au.lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.view.View
import android.widget.MediaController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSplashBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.Constants

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private lateinit var mediaController: MediaController

    override fun bind() = FragmentSplashBinding.inflate(layoutInflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
        exitTransition = Fade()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({

            when (App.prefsManager.savedScreen) {

                "" -> requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flSplashActivity, DiscoveryFragment()).commit()

                CreatePinFragment::class.java.name -> {

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flSplashActivity, DiscoveryFragment())
                        .commit()

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flSplashActivity, SignUpFragment())
                        .addToBackStack(null)
                        .commit()

                }

                else -> requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flSplashActivity, UnlockAppFragment()).commit()

            }

        }, 1500)

    }
}