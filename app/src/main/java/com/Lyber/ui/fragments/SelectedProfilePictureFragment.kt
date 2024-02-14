package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.Lyber.databinding.FragmentSelectedProfilePictureBinding
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.Constants

class SelectedProfilePictureFragment : BaseFragment<FragmentSelectedProfilePictureBinding>() {

    private var profilePIc: String = ""
    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentSelectedProfilePictureBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profilePIc = it.getString(PROFILE_PIC, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        val matchingAvatar = Constants.defaults.find { it.avatar_name == profilePIc }

// If matchingAvatar is not null, set the drawable
        matchingAvatar?.let {
            // Use the drawable resource ID
            val drawableResId = it.avatar_is

            binding.ivProfile.setImageResource(drawableResId)
            // Now you can set the drawable wherever needed
        }
//        binding.ivProfile.setImageResource(Constants.defaults[profilePIc])
        binding.ivTopAction.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            //requireActivity().supportFragmentManager.popBackStack()
        }
        binding.btnSave.setOnClickListener {
            CommonMethods.checkInternet(requireActivity()) {
                CommonMethods.showProgressDialog(requireActivity())
                App.prefsManager.defaultImage = profilePIc
                viewModel.updateAvtaar(profilePIc.toString())
            }
        }


        viewModel.updateUserInfoResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED){
                CommonMethods.dismissProgressDialog()
                requireActivity().supportFragmentManager.popBackStack()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

    }

    companion object {

        private const val PROFILE_PIC = "profilePic"

        fun get(profilePic: String): SelectedProfilePictureFragment {
            return SelectedProfilePictureFragment().apply {
                arguments = Bundle().apply {
                    putString(PROFILE_PIC, profilePic)
                }
            }
        }
    }
}