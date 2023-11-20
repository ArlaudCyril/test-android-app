package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.databinding.FragmentSelectedProfilePictureBinding
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.Constants

class SelectedProfilePictureFragment : BaseFragment<FragmentSelectedProfilePictureBinding>() {

    private var profilePIc: Int = 0
    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentSelectedProfilePictureBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profilePIc = it.getInt(PROFILE_PIC, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivProfile.setImageResource(Constants.defaults[profilePIc])
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

        fun get(profilePic: Int): SelectedProfilePictureFragment {
            return SelectedProfilePictureFragment().apply {
                arguments = Bundle().apply {
                    putInt(PROFILE_PIC, profilePic)
                }
            }
        }
    }
}