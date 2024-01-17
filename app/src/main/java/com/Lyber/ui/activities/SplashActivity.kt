package com.Lyber.ui.activities

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.Lyber.R
import com.Lyber.databinding.ActivitySplashBinding
import com.Lyber.utils.ActivityCallbacks
import com.Lyber.utils.App
import com.Lyber.utils.Constants
import java.util.Locale

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
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navHostFragment.childFragmentManager.backStackEntryCount > 2) {
                        navController.popBackStack()
                    } else {

                        if (navHostFragment.childFragmentManager.backStackEntryCount > 2) {
                            navController.popBackStack()
                        } else {
                            finishAffinity()
                        }
                    }
                }
            })
        if (App.prefsManager.getLanguage().isNotEmpty()) {
            var code = App.prefsManager.getLanguage()
            val locale = Locale(code)
            Locale.setDefault(locale)
            val resources: Resources = resources
            val config: Configuration = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)

        }
        if (intent.data != null && App.prefsManager.userPin.isEmpty()) {
            val uriString = intent.data?.toString()
            if (uriString != null && uriString.contains("reset?token")) {
                Log.d("URI Data", "$uriString")
                val urit = Uri.parse(uriString)
                val token = urit.getQueryParameter("token")
                if (token != null) {
                    Log.d("Token: ", "$token")
//                    navController.navigate(R.id.resetPasswordFragment)
                    val arguments = Bundle().apply {
                        putString("resetToken", token)
                    }
                    navController.navigate(R.id.splashFragment, arguments)
                } else {
                    Log.d("Token not found in the URI", "")
                }
            }
        } else if ((intent?.extras?.getString(Constants.FOR_LOGOUT, "") ?: "").isNotEmpty())
            navController.navigate(R.id.discoveryFragment)
        else navController.navigate(R.id.splashFragment)
    }


    companion object {
        private var _activityCallbacks: ActivityCallbacks? = null
        var activityCallbacks: ActivityCallbacks?
            get() = com.Lyber.ui.activities.SplashActivity.Companion._activityCallbacks
            set(value) {
                com.Lyber.ui.activities.SplashActivity.Companion._activityCallbacks = value
            }
    }

}















