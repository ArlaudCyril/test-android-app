package com.Lyber.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import com.Lyber.R
import com.Lyber.databinding.FragmentSplashBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.is30DaysOld

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isScreen = false
    var token = ""
    override fun bind() = FragmentSplashBinding.inflate(layoutInflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
        exitTransition = Fade()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            // 1- 30 days check if greater then logout if not normal flow
            // check for 24 hours if greater hit api for refresh token if not normal flow
            // if not check for user pin

            if (!isScreen) {
                isScreen = true
                if (arguments != null && requireArguments().containsKey("resetToken")) {
                    token = requireArguments().getString("resetToken").toString()
                    val arguments = Bundle().apply {
                        putString("resetToken", token)
                    }
                    findNavController().navigate(R.id.resetPasswordFragment, arguments)
                } else
                    if (App.prefsManager.tokenSavedAt.is30DaysOld()) {
                        App.prefsManager.logout()
                        findNavController().navigate(R.id.discoveryFragment)
                    } else {

                        if (App.prefsManager.userPin.isNotEmpty()) {
                            if (App.prefsManager.refreshToken.isEmpty()) {
                                findNavController().navigate(R.id.discoveryFragment)
                            } else {
                                findNavController().navigate(R.id.unlockAppFragment)
                            }
                        } else {
                            findNavController().navigate(R.id.discoveryFragment)

                        }

                    }
            }


        }
        view.post {
            handler.postDelayed(runnable, 1500)
        }


    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}