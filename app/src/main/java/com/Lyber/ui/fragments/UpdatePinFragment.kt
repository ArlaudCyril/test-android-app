package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentUpdatePinBinding
import com.Lyber.ui.activities.SplashActivity.Companion.activityCallbacks
import com.Lyber.utils.ActivityCallbacks
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.replace
import com.Lyber.viewmodels.ProfileViewModel
/*
        Not in use
 */
class UpdatePinFragment : BaseFragment<FragmentUpdatePinBinding>(), ActivityCallbacks {

    private lateinit var viewModel: ProfileViewModel

    override fun bind() = FragmentUpdatePinBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)

        activityCallbacks = this

        replace(R.id.flUpdatePin, VerifyPhoneForPinFragment(), false)

        binding.ivTopAction.setImageResource(R.drawable.ic_back)
        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }


        viewModel.verifyPhoneForPinResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                replace(R.id.flUpdatePin, CreateNewPinFragment(), false)
            }
        }

        viewModel.updatePinResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                childFragmentManager.popBackStack()
                childFragmentManager.popBackStackImmediate()
                requireActivity().onBackPressed()
            }
        }

    }

    override fun onDestroyView() {
        activityCallbacks = null
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        return if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
            false
        } else true
    }

}