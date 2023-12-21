package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.databinding.FragmentSelectedProfilePictureBinding
import com.Lyber.utils.App
import com.Lyber.utils.Constants

class SelectedProfilePictureFragment : BaseFragment<FragmentSelectedProfilePictureBinding>() {

    private var profilePIc: Int = 0

    override fun bind() = FragmentSelectedProfilePictureBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profilePIc = it.getInt(PROFILE_PIC, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.ivProfile.setImageResource(Constants.defaults[profilePIc])

        binding.btnSave.setOnClickListener {
//            App.prefsManager.defaultImage = profilePIc
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.popBackStack()
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