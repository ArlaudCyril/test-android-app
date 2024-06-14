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
    }

    private fun handleExtras() {
        Log.d("Language","${App.prefsManager.getLanguage()}")
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
                    navController.navigate(R.id.splashFragment, arguments) //todo

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
//    private fun checkAndRequest() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(
//                    applicationContext, android.Manifest.permission.CAMERA
//                )
//                != PackageManager.PERMISSION_GRANTED
//
//                ||
//                ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.READ_MEDIA_IMAGES
//                ) != PackageManager.PERMISSION_GRANTED
//                ||
//                ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.READ_MEDIA_VIDEO
//                ) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS
//                ) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//
//            ) {
//
//                requestMultiplePermissions.launch(
//                    arrayOf(
//                        android.Manifest.permission.CAMERA,
//                        android.Manifest.permission.READ_MEDIA_IMAGES,
//                        android.Manifest.permission.READ_MEDIA_VIDEO,
//                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
//                        android.Manifest.permission.RECORD_AUDIO
//                    )
//                )
//
//            } else {
//                Log.d("TAG2", "Permission Already Granted")
//            }
//        }
//        else {
//            if (ActivityCompat.checkSelfPermission(
//                    applicationContext, android.Manifest.permission.CAMERA
//                )
//                != PackageManager.PERMISSION_GRANTED
//
//                ||
//                ActivityCompat.checkSelfPermission(
//                    applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//                != PackageManager.PERMISSION_GRANTED
//
//                ||
//                ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//                ||
//                ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS
//                ) != PackageManager.PERMISSION_GRANTED
//                ||
//                ActivityCompat.checkSelfPermission(
//                    applicationContext,
//                    android.Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//
//                requestMultiplePermissions.launch(
//                    arrayOf(
//                        android.Manifest.permission.CAMERA,
//                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
//                        android.Manifest.permission.RECORD_AUDIO
//                    )
//                )
//
//            } else {
//                Log.d("TAG2", "Permission Already Granted")
//            }
//        }
//    }
//
//    private val requestMultiplePermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//
//            permissions.entries.forEach {
//                Log.d("TAG", "${it.key} = ${it.value}")
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                if (permissions[android.Manifest.permission.CAMERA] == true
//                    && permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true
//                    && permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == true
//                    && permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == true
//                    && permissions[android.Manifest.permission.RECORD_AUDIO] == true
//                ) {
//                    Log.d("requestMultiplePermissions", "Permission granted")
//                    // isPermissionGranted=true
//                } else {
//
//                    if (permissions[android.Manifest.permission.CAMERA] == false) {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.CAMERA
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else permissionsDenied()
//
//                    } else if (permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == false) {
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.READ_MEDIA_VIDEO
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else permissionsDenied()
//                    } else if (permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == false) {
//
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.READ_MEDIA_IMAGES
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else permissionsDenied()
//
//                    } else if (permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == false) {
//
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else permissionsDenied()
//
//                    } else if (permissions[android.Manifest.permission.RECORD_AUDIO] == false) {
//
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.RECORD_AUDIO
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else permissionsDenied()
//
//                    }
//
//                    Log.d("requestMultiplePermissions", "Permission not granted")
//
//                }
//
//            } else {
//
//                if (permissions[android.Manifest.permission.CAMERA] == true && permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
//                    && permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
//                    && permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == true
//                    && permissions[android.Manifest.permission.RECORD_AUDIO] == true
//                ) {
//                    Log.d("requestMultiplePermissions", "Permission granted")
//                    // isPermissionGranted=true
//
//                } else {
//                    if (permissions[android.Manifest.permission.CAMERA] == false) {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.CAMERA
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else {
//                            permissionsDenied()
//                        }
//
//                    } else if (permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == false
//                        && permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == false
//                    ) {
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                            && ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.READ_EXTERNAL_STORAGE
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else {
//
//                            permissionsDenied()
//                        }
//                    } else if (permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == false
//                        && permissions[android.Manifest.permission.RECORD_AUDIO] == false
//                    ) {
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
//                            )
//                            && ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                android.Manifest.permission.RECORD_AUDIO
//                            )
//                        ) {
//                            checkAndRequest()
//                        } else {
//
//                            permissionsDenied()
//                        }
//                    }
//
//                    Log.d("requestMultiplePermissions", "Permission not granted")
//
//                }
//
//            }
//
//        }
//
//    private var alertDialogRational: AlertDialog? = null
//    private fun permissionsDenied() {
//        val alertDialogBuilder = AlertDialog.Builder(this)
//        alertDialogRational?.getButton(AlertDialog.BUTTON_NEGATIVE)
//            ?.setTextColor(
//                ContextCompat.getColor(
//                    this,
//                    R.color.black
//                )
//            )
//
//        alertDialogRational = alertDialogBuilder.setTitle("Permissions Required")
//            .setMessage(
//                "Please open settings, go to permissions and allow them."
//            )
//            .setPositiveButton(
//                "Settings"
//            ) { _, _ ->
//                checkPermission=true
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                val uri: Uri = Uri.fromParts("package", "com.Lyber.dev", null)
//                intent.data = uri
//                startActivity(intent)
//            }
//            .setNegativeButton(
//                "Cancel"
//            ) { _, _ ->
//                finish()
//            }
//            .setCancelable(false)
//            .create()
//        alertDialogRational!!.apply {
//            show()
//            getButton(AlertDialog.BUTTON_NEGATIVE)
//                .setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.black))
//            getButton(AlertDialog.BUTTON_POSITIVE)
//                .setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.black))
//        }
//    }
}


//private fun handleExtras(){
//    if (intent!!.extras != null && intent.hasExtra("fragment_to_show") &&
//        intent.getStringExtra("fragment_to_show").equals(PortfolioHomeFragment::class.java.name)
//    ) {
//        navController.popBackStack(navController.graph.startDestinationId, false)
//        navController.navigate(R.id.portfolioHomeFragment)
//        intent.removeExtra("fragment_to_show")
//    } else if (intent!!.extras != null && intent.hasExtra(Constants.FOR_LOGOUT) && (intent?.extras?.getString(Constants.FOR_LOGOUT, "")
//            ?: "").isNotEmpty()
//    ) {
//        navController.popBackStack(navController.graph.startDestinationId, false)
//        navController.navigate(R.id.discoveryFragment)
//        intent.removeExtra(Constants.FOR_LOGOUT)
//    }
//    else if (intent.data != null && App.prefsManager.userPin.isEmpty()) {
//        val uriString = intent.data?.toString()
//        if (uriString != null && uriString.contains("reset?token")) {
//            Log.d("URI Data", "$uriString")
//            val urit = Uri.parse(uriString)
//            val token = urit.getQueryParameter("token")
//            intent.data=null
//            if (token != null) {
//                Log.d("Token: ", "$token")
////                    navController.navigate(R.id.resetPasswordFragment)
//                val arguments = Bundle().apply {
//                    putString("resetToken", token)
//                }
//                navController.popBackStack(navController.graph.startDestinationId, false)
//                navController.navigate(R.id.splashFragment, arguments)
//            } else {
//                Log.d("Token not found in the URI", "")
//            }
//        }
//    }
////        else if ((intent?.extras?.getString(Constants.FOR_LOGOUT, "") ?: "").isNotEmpty())
////            navController.navigate(R.id.discoveryFragment)
//    else navController.navigate(R.id.splashFragment)
//}















