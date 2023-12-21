package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentEnableNotificationsBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.viewmodels.SignUpViewModel

class EnableNotificationFragment : BaseFragment<FragmentEnableNotificationsBinding>() {

    private lateinit var navController : NavController
    private var enableNotification: Boolean = false
    private lateinit var onBoardingViewModel: SignUpViewModel

    override fun bind() = FragmentEnableNotificationsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
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

        }

        binding.btnEnableNotifications.setOnClickListener {


            navController.navigate(R.id.completePortfolioFragment)

            /*checkInternet(requireContext()) {
                enableNotification = true
                showProgressDialog(requireContext())
                onBoardingViewModel.enableNotification(enableNotification)
            }*/
        }

        binding.tvNotNow.setOnClickListener {
            navController.navigate(R.id.completePortfolioFragment)
        }
        /*checkInternet(requireContext()) {
            enableNotification = false
            showProgressDialog(requireContext())
            onBoardingViewModel.enableNotification(enableNotification)
        }*/


    }
}