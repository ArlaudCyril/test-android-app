package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.R
import com.au.lyber.databinding.FragmentEnableNotificationsBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.viewmodels.SignUpViewModel

class EnableNotificationFragment : BaseFragment<FragmentEnableNotificationsBinding>() {


    private var enableNotification: Boolean = false
    private lateinit var onBoardingViewModel: SignUpViewModel

    override fun bind() = FragmentEnableNotificationsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.prefsManager.savedScreen = javaClass.name
        (requireParentFragment() as SignUpFragment).setIndicators(4)

        onBoardingViewModel = getViewModel(requireParentFragment())
        onBoardingViewModel.listener = this


        onBoardingViewModel.enableNotificationResponse.observe(viewLifecycleOwner) {
            App.prefsManager.enableNotification(enableNotification)
            dismissProgressDialog()
            requireActivity().clearBackStack()
            requireActivity().supportFragmentManager.beginTransaction()
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                CompletePortfolioFragment(),
                false,

                )
        }

        binding.btnEnableNotifications.setOnClickListener {


            requireActivity().clearBackStack()
            requireActivity().supportFragmentManager.beginTransaction()
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                CompletePortfolioFragment(),
                false
            )

            /*checkInternet(requireContext()) {
                enableNotification = true
                showProgressDialog(requireContext())
                onBoardingViewModel.enableNotification(enableNotification)
            }*/
        }

        binding.tvNotNow.setOnClickListener {
            requireActivity().clearBackStack()
            requireActivity().supportFragmentManager.beginTransaction()
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                CompletePortfolioFragment(),
                false,

                )
        }
        /*checkInternet(requireContext()) {
            enableNotification = false
            showProgressDialog(requireContext())
            onBoardingViewModel.enableNotification(enableNotification)
        }*/


    }
}