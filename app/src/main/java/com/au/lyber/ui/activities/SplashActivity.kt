package com.au.lyber.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.ActivitySplashBinding
import com.au.lyber.ui.fragments.DiscoveryFragment
import com.au.lyber.ui.fragments.SplashFragment
import com.au.lyber.utils.ActivityCallbacks
import com.au.lyber.utils.CommonMethods.Companion.addFragment

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val TAG = "CompletePortfolioFragme"
    override fun bind() = ActivitySplashBinding.inflate(layoutInflater)

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        setTheme(R.style.TranslucentStatusBar)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back is pressed... Finishing the activit
                navController.popBackStack()
            }
        })

        if ((intent?.extras?.getString("fromLogout", "") ?: "").isNotEmpty())
            navController.navigate(R.id.discoveryFragment)            //addFragment(R.id.flSplashActivity, DiscoveryFragment())
        else navController.navigate(R.id.splashFragment)//addFragment(R.id.flSplashActivity, SplashFragment())
    }


    companion object {
        private var _activityCallbacks: ActivityCallbacks? = null
        var activityCallbacks: ActivityCallbacks?
            get() = _activityCallbacks
            set(value) {
                _activityCallbacks = value
            }
    }

}















