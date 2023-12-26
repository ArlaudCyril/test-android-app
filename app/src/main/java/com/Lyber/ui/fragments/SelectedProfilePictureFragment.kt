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
        binding.ivTopAction.setOnClickListener {
//            requireActiv":?>                       bgf "+IKxxxxxxxxxxxxxxxxxxxxxxxxxxxx wsrvwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwg\saaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaqlkh9n=[p'a========='''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwsaz5trdgrvb ./vc gfffffffffffffffffffffaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaz
//            ".setImageResource(Constants.defaults[profilePIc])
//
//        binding.btnSave.setOnClickListener {
//           CommonMethods.checkInternet(requireActivity()){
//               CommonMethods.showProgressDialog(requireActivity())
               App.prefsManager.defaultImage = profilePIc
               viewModel.updateAvtaar(profilePIc.toString())
           }
        //requireActivity().supportFragmentManager.popBackStack()
            //requireActivity().supportFragmentManager.popBackStack()

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