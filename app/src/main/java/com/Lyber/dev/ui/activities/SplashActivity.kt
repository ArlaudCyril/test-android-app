package com.Lyber.dev.ui.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.Lyber.dev.R
import com.Lyber.dev.databinding.ActivitySplashBinding
import com.Lyber.dev.ui.fragments.DiscoveryFragment
import com.Lyber.dev.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.dev.utils.ActivityCallbacks
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods.Companion.is30DaysOld
import com.Lyber.dev.utils.Constants
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import java.util.Locale


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val TAG = "CompletePortfolioFragme"
    lateinit var navController: NavController
    override fun bind() = ActivitySplashBinding.inflate(layoutInflater)

    //    private var checkPermission: Boolean = false
//    override fun onResume() {
//        super.onResume()
//        if(checkPermission) {
//            checkPermission=false
//            checkAndRequest()
//        }
//    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleExtras()
    }

    private var keepSplashOnScreen = true
    private val delay = 1250L

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
//        setTheme(R.style.TranslucentStatusBar)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        val splash = installSplashScreen()
//        splash.setKeepOnScreenCondition { false }
        setTheme(R.style.TranslucentStatusBar)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

//        installSplashScreen().setKeepOnScreenCondition { keepSplashOnScreen }
//        Handler(Looper.getMainLooper()).postDelayed({ keepSplashOnScreen = false }, delay)
        super.onCreate(savedInstanceState)
        //        // Create an instance of a manager.
        val standardIntegrityManager: StandardIntegrityManager =
            IntegrityManagerFactory.createStandard(this.applicationContext)

        val cloudProjectNumber: Long = 672116436946 // console id

// Prepare integrity token. Can be called once in a while to keep internal state fresh.
        standardIntegrityManager.prepareIntegrityToken(
            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build()
        ).addOnSuccessListener { tokenProvider ->
            integrityTokenProvider = tokenProvider
            Log.d("Token", "$integrityTokenProvider")
        }.addOnFailureListener { exception ->
//                        handleError(exception)
            Log.d("Exception", "$exception")

        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentFragment =
                        navHostFragment.childFragmentManager.primaryNavigationFragment
                    if (currentFragment is DiscoveryFragment) {
                        finishAffinity()
                    } else if (currentFragment is PortfolioHomeFragment) {
                        finishAffinity()
                    } else {
                        if (navHostFragment.childFragmentManager.backStackEntryCount > 1) {
                            navController.popBackStack()
                        } else {

                            if (navHostFragment.childFragmentManager.backStackEntryCount > 1) {
                                navController.popBackStack()
                            } else {
                                finishAffinity()
                            }
                        }
                    }
                }
            })

//        checkAndRequest()
        if (App.prefsManager != null && App.prefsManager.user != null && !App.prefsManager.user?.language.isNullOrEmpty()) {
            App.prefsManager.setLanguage(App.prefsManager.user?.language!!)
            val code = App.prefsManager.getLanguage()
            val locale = Locale(code)
            Locale.setDefault(locale)
            val resources: Resources = resources
            val config: Configuration = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        } else if (App.prefsManager.getLanguage().isNotEmpty()) {
            val code = App.prefsManager.getLanguage()
            val locale = Locale(code)
            Locale.setDefault(locale)
            val resources: Resources = resources
            val config: Configuration = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        handleExtras()
    }

    companion object {
        private var _activityCallbacks: ActivityCallbacks? = null
        var activityCallbacks: ActivityCallbacks?
            get() = _activityCallbacks
            set(value) {
                _activityCallbacks = value
            }
        var integrityTokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    }

    private fun handleExtras() {
        Log.d("Language", "${App.prefsManager.getLanguage()}")
        if (App.prefsManager.getLanguage().isNotEmpty()) {
            val code = App.prefsManager.getLanguage()
            val locale = Locale(code)
            Locale.setDefault(locale)
            val resources: Resources = resources
            val config: Configuration = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        if (intent!!.extras != null && intent.hasExtra("fragment_to_show") &&
            intent.getStringExtra("fragment_to_show").equals(PortfolioHomeFragment::class.java.name)
        ) {
            val arguments = Bundle().apply {
                putString("showLoader", "showLoader")
            }
            navController.popBackStack(navController.graph.startDestinationId, false)
            navController.navigate(R.id.portfolioHomeFragment, arguments)
            intent.removeExtra("fragment_to_show")
        } else if (intent!!.extras != null && intent.hasExtra(Constants.FOR_LOGOUT) && (intent?.extras?.getString(
                Constants.FOR_LOGOUT,
                ""
            )
                ?: "").isNotEmpty()
        ) {
            navController.popBackStack(navController.graph.startDestinationId, false)
            navController.navigate(R.id.discoveryFragment)
            intent.removeExtra(Constants.FOR_LOGOUT)
        } else if (intent.data != null && App.prefsManager.userPin.isEmpty()) {
            val uriString = intent.data?.toString()
            if (uriString != null && uriString.contains("reset?token")) {
                Log.d("URI Data", "$uriString")
                val urit = Uri.parse(uriString)
                val token = urit.getQueryParameter("token")
                intent.data = null
                if (token != null) {
                    Log.d("Token: ", "$token")
//                    navController.navigate(R.id.resetPasswordFragment)
                    val arguments = Bundle().apply {
                        putString("resetToken", token)
                    }
                    navController.popBackStack(navController.graph.startDestinationId, false)
                    navController.navigate(R.id.splashFragment, arguments)

//                    navController.navigate(R.id.resetPasswordFragment, arguments)
                } else {
                    Log.d("Token not found in the URI", "")
                }
            }
        } else if ((intent?.extras?.getString(Constants.FOR_LOGOUT, "") ?: "").isNotEmpty())
            navController.navigate(R.id.discoveryFragment)
        else navController.navigate(R.id.splashFragment)
//        else splashFragmentFunction()
    }

    fun splashFragmentFunction() {
        if (App.prefsManager.tokenSavedAt.is30DaysOld()) {
            App.prefsManager.logout()
            navController.navigate(R.id.discoveryFragment)
        } else {

            if (App.prefsManager.userPin.isNotEmpty()) {
                if (App.prefsManager.refreshToken.isEmpty()) {
                    navController.navigate(R.id.discoveryFragment)
                } else {
                    navController.navigate(R.id.unlockAppFragment)
                }
            } else {
                navController.navigate(R.id.discoveryFragment)

            }

        }
    }
}









