package com.Lyber.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentEnableNotificationsBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel

class EnableNotificationFragment : BaseFragment<FragmentEnableNotificationsBinding>() {

    private lateinit var navController: NavController
    private var enableNotification: Boolean = false
    private lateinit var onBoardingViewModel: SignUpViewModel

    override fun bind() = FragmentEnableNotificationsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        App.prefsManager.savedScreen = javaClass.name
        // (requireParentFragment() as SignUpFragment).setIndicators(4)

        onBoardingViewModel = getViewModel(requireParentFragment())
        onBoardingViewModel.listener = this


        onBoardingViewModel.enableNotificationResponse.observe(viewLifecycleOwner) {
//            App.prefsManager.enableNotification(enableNotification)
            dismissProgressDialog()
            requireActivity().clearBackStack()
            navController.navigate(R.id.completePortfolioFragment)

        }
        binding.ivTopAction.setOnClickListener {
            stopRegistrationDialog()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            stopRegistrationDialog()
        }
        binding.btnEnableNotifications.setOnClickListener {

            App.prefsManager.portfolioCompletionStep = Constants.ACCOUNT_CREATED

            navController.navigate(R.id.completePortfolioFragment)

            /*checkInternet(requireContext()) {
                enableNotification = true
                showProgressDialog(requireContext())
                onBoardingViewModel.enableNotification(enableNotification)
            }*/
        }

        binding.tvNotNow.setOnClickListener {
            App.prefsManager.portfolioCompletionStep = Constants.ACCOUNT_CREATED

            navController.navigate(R.id.completePortfolioFragment)
        }
        /*checkInternet(requireContext()) {
            enableNotification = false
            showProgressDialog(requireContext())
            onBoardingViewModel.enableNotification(enableNotification)
        }*/
    }

//    private fun stopRegistrationDialog() {
//        Dialog(requireActivity(), R.style.DialogTheme).apply {
//
//            CustomDialogLayoutBinding.inflate(layoutInflater).let {
//
//                requestWindowFeature(Window.FEATURE_NO_TITLE)
//                setCancelable(false)
//                setCanceledOnTouchOutside(false)
//                setContentView(it.root)
//
//                it.tvTitle.text = getString(R.string.stop_reg)
//                it.tvMessage.text = getString(R.string.reg_message)
//                it.tvNegativeButton.text = getString(R.string.cancel)
//                it.tvPositiveButton.text = getString(R.string.ok)
//
//                it.tvNegativeButton.setOnClickListener { dismiss() }
//
//                it.tvPositiveButton.setOnClickListener {
//                    dismiss()
//                    App.prefsManager.logout()
//                    findNavController().popBackStack()
//                    findNavController().navigate(R.id.discoveryFragment)
//                    CommonMethods.checkInternet(requireContext()) {
//                        dismiss()
//                        CommonMethods.showProgressDialog(requireContext())
//                        onBoardingViewModel.logout(CommonMethods.getDeviceId(requireActivity().contentResolver))
//                    }
//                }
//
//                show()
//            }
//        }
//    }
}