package com.Lyber.ui.fragments

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentEnableNotificationsBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class EnableNotificationFragment : BaseFragment<FragmentEnableNotificationsBinding>() {

    private lateinit var navController: NavController
    private lateinit var onBoardingViewModel: SignUpViewModel
    private var fcmToken = ""
    private var firstTime = false
    private lateinit var settingDialog: Dialog
    override fun bind() = FragmentEnableNotificationsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments == null) {
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.findNavController()
            App.prefsManager.savedScreen = javaClass.name
            binding.ivTopAction.visible()
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                stopRegistrationDialog()
            }
        } else {
            binding.ivTopAction.gone()
            binding.ivBack.visible()
        }
        // (requireParentFragment() as SignUpFragment).setIndicators(4)

        onBoardingViewModel = getViewModel(requireParentFragment())
        onBoardingViewModel.listener = this

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        "FirebaseMessagingService.TAG",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@OnCompleteListener
                }

                val token = task.result
                fcmToken = token

                Log.d("FirebaseMessagingService.TAG", token)

            })
        onBoardingViewModel.notificationResponse.observe(viewLifecycleOwner) { response ->
            if (response.success == true) {
                dismissProgressDialog()

                if (arguments == null) {
                    App.prefsManager.portfolioCompletionStep = Constants.ACCOUNT_CREATED
//                navController.navigate(R.id.completePortfolioFragment)
                    navController.navigate(R.id.action_enableNotificationFragment_to_completePortfolioFragment)

                } else {
                    findNavController().navigate(R.id.portfolioHomeFragment)
                }
            }

        }
        binding.ivTopAction.setOnClickListener {
            stopRegistrationDialog()
        }
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
//            navController.popBackStack()
        }

        binding.btnEnableNotifications.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                askNotificationPermission()
            else
                checkInternet(binding.root,requireContext()) {
                    showProgressDialog(requireContext())
                    if (fcmToken.isEmpty()) {
                        FirebaseMessaging.getInstance().token
                            .addOnCompleteListener(OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w(
                                        "FirebaseMessagingService.TAG",
                                        "Fetching FCM registration token failed",
                                        task.exception
                                    )
                                    return@OnCompleteListener
                                }

                                val token = task.result
                                fcmToken = token
                                onBoardingViewModel.enableNotification(fcmToken)
                                Log.d("FirebaseMessagingService.TAG", token)

                            })
                    } else
                        onBoardingViewModel.enableNotification(fcmToken)
                }
        }

        binding.tvNotNow.setOnClickListener {
            if (arguments == null) {
                App.prefsManager.portfolioCompletionStep = Constants.ACCOUNT_CREATED
                navController.navigate(R.id.completePortfolioFragment)
            } else
                findNavController().navigate(R.id.portfolioHomeFragment)
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            checkInternet(binding.root,requireContext()) {
                showProgressDialog(requireContext())
                if (fcmToken.isEmpty()) {
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(
                                    "FirebaseMessagingService.TAG",
                                    "Fetching FCM registration token failed",
                                    task.exception
                                )
                                return@OnCompleteListener
                            }

                            val token = task.result
                            fcmToken = token
                            onBoardingViewModel.enableNotification(fcmToken)
                            Log.d("FirebaseMessagingService.TAG", token)

                        })
                } else
                    onBoardingViewModel.enableNotification(fcmToken)
            }
        } else {
            //  Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                checkInternet(binding.root,requireContext()) {
                    showProgressDialog(requireContext())
                    if(fcmToken.isEmpty()){
                        FirebaseMessaging.getInstance().token
                            .addOnCompleteListener(OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w(
                                        "FirebaseMessagingService.TAG",
                                        "Fetching FCM registration token failed",
                                        task.exception
                                    )
                                    return@OnCompleteListener
                                }

                                val token = task.result
                                fcmToken = token
                                onBoardingViewModel.enableNotification(fcmToken)
                                Log.d("FirebaseMessagingService.TAG", token)

                            })
                    }
                    else
                        onBoardingViewModel.enableNotification(fcmToken)
                }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                if (firstTime)
                    showNotificationDialog()
                // Directly ask for the permission
                if (!firstTime)
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                firstTime = true
            }
        }
    }

    private fun showNotificationDialog() {

        settingDialog = Dialog(requireActivity(), R.style.DialogTheme).apply {

            CustomDialogLayoutBinding.inflate(layoutInflater).let {

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(it.root)

                it.tvTitle.text = getString(R.string.enable_notifications)
                it.tvMessage.text = getString(R.string.notification_detail)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.setting)

                it.tvNegativeButton.setOnClickListener { dismiss() }

                it.tvPositiveButton.setOnClickListener {
                    openSetting()
                }

                show()

            }
        }

    }

    fun openSetting() {
        settingDialog.dismiss()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", "com.Lyber", null)
        intent.data = uri
        startActivity(intent)
    }
}