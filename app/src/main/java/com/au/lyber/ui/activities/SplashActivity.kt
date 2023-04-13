package com.au.lyber.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        setTheme(R.style.TranslucentStatusBar)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        if ((intent?.extras?.getString("fromLogout", "") ?: "").isNotEmpty())
            addFragment(R.id.flSplashActivity, DiscoveryFragment())
        else addFragment(R.id.flSplashActivity, SplashFragment())
    }



    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activityCallbacks != null) {
            if (activityCallbacks!!.onBackPressed())
                super.onBackPressed()
        } else
            super.onBackPressed()

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















