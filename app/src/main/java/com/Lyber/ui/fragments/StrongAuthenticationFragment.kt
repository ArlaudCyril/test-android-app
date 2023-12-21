package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentStrongAuthenticationBinding
import com.Lyber.ui.fragments.bottomsheetfragments.AuthenticationCodeBottomSheet
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.viewmodels.ProfileViewModel

class StrongAuthenticationFragment : BaseFragment<FragmentStrongAuthenticationBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel

    private lateinit var bottomSheet: AuthenticationCodeBottomSheet

    override fun bind() = FragmentStrongAuthenticationBinding.inflate(layoutInflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheet = AuthenticationCodeBottomSheet()
        viewModel = getViewModel(this)
        viewModel.listener = this

//        if (App.prefsManager.isStrongAuth()) {
//            binding.switchStrongAuthentication.isChecked = App.prefsManager.isStrongAuth()
//            binding.rlCases.visible()
//            binding.rlCases.fadeIn()
//        } else {
//            binding.rlCases.gone()
//            binding.rlCases.fadeOut()
//        }

//        binding.tvNumber.text = "To: ${App.prefsManager.user?.phone_no ?: 1122334455}"

        viewModel.enableStrongAuthentication.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
                bottomSheet.show(childFragmentManager, "")
            }
        }

        viewModel.verifyStrongAuthentication.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
                bottomSheet.dismiss()
                bottomSheet.clearFields()
//                App.prefsManager.setStrongAuth(!App.prefsManager.isStrongAuth())
//                binding.switchStrongAuthentication.isChecked = App.prefsManager.isStrongAuth()
//                if (App.prefsManager.isStrongAuth()) {
//                    binding.rlCases.visible()
//                    binding.rlCases.fadeIn()
//                } else {
//                    binding.rlCases.gone()
//                    binding.rlCases.fadeOut()
//                }
            }
        }

        binding.switchWhitelisting.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                button.isChecked = !isChecked
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    EnableWhiteListingFragment()
                )
            }
        }

        binding.switchStrongAuthentication.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.enableStrongAuthentication(isChecked)
                }
            }
        }

        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onResume() {
//        binding.switchWhitelisting.isChecked = App.prefsManager.isWhitelisting()
        super.onResume()
    }

    fun dismiss(){
//        binding.switchStrongAuthentication.isChecked = App.prefsManager.isStrongAuth()
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressed()
            }
        }
    }


}