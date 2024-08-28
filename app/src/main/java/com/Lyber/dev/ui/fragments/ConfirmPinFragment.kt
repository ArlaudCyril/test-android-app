package com.Lyber.dev.ui.fragments

import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.CustomDialogLayoutBinding
import com.Lyber.dev.databinding.FragmentConfirmPinBinding
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.SignUpViewModel
import java.util.Locale

class ConfirmPinFragment : BaseFragment<FragmentConfirmPinBinding>() {

    override fun bind() = FragmentConfirmPinBinding.inflate(layoutInflater)
    private val pinConfirm get() = binding.etConfirmPin.text.trim().toString()

    private lateinit var viewModel: SignUpViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = getViewModel(requireParentFragment())
        viewModel.forLogin = requireArguments().getBoolean(Constants.FOR_LOGIN, false)
        viewModel.listener = this
        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.etConfirmPin.addTextChangedListener(onTextChange)
        binding.etConfirmPin.requestKeyboard()
        if (requireArguments().containsKey(Constants.IS_CHANGE_PIN)
            && requireArguments().getBoolean(Constants.IS_CHANGE_PIN)
        ) {
            binding.llIndicators.visibility = View.GONE
        }
        if (viewModel.forLogin) {
            viewModel.getUser()
        } else if (!requireArguments().containsKey(Constants.IS_CHANGE_PIN))
            binding.ivTopClose.visible()
        binding.ivTopClose.setOnClickListener {
            stopRegistrationDialog()
        }
        if (viewModel.forLogin)
        viewModel.getUserResponse.observe(viewLifecycleOwner) {
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            try {
                App.prefsManager.user = it.data
                if (it.data.kycStatus != "OK" || it.data.yousignStatus != "SIGNED")
                    startJob()
                if (it.data.language.isNotEmpty()) {
                    App.prefsManager.setLanguage(it.data.language)
                    val locale = Locale(it.data.language)
                    Locale.setDefault(locale)
                    val resources: Resources = resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                }
            } catch (_: Exception) {

            }

//            }
        }
    }


    /* On Text Change */
    private val onTextChange = object : OnTextChange {
        override fun onTextChange() {
            changeDots(pinConfirm.count())
        }
    }

    private fun clearField() {
        binding.etConfirmPin.setText("")
    }

    /* Present the dots */
    private fun changeDots(pinCount: Int) {
        binding.llPinIndicators.let {
            if (pinCount == 0)
                for (i in 0 until it.childCount) {
                    val imageView = it.getChildAt(i)
                    imageView.setBackgroundResource(R.drawable.circle_dot_unselected)
                }
            else
                for (i in 0 until it.childCount) {
                    val imageView = it.getChildAt(i)
                    if (i <= pinCount - 1)
                        imageView.setBackgroundResource(R.drawable.circle_dot_selected)
                    else
                        imageView.setBackgroundResource(R.drawable.circle_dot_unselected)
                }

            if (pinCount == 4) {

                if (viewModel.createPin == pinConfirm) {

                    App.prefsManager.userPin = pinConfirm

                    if (viewModel.forLogin) {
                        val bundle = Bundle().apply {
                            putBoolean(Constants.FOR_LOGIN, viewModel.forLogin)
                        }
                        findNavController().navigate(R.id.enableNotificationFragment, bundle)
                        clearField()
                    } else if (requireArguments().containsKey(Constants.IS_CHANGE_PIN)
                        && requireArguments().getBoolean(Constants.IS_CHANGE_PIN)
                    ) {
                        findNavController().navigate(R.id.action_confirmPinFragment_to_profile)
                    } else {
                        App.prefsManager.accountCreationSteps =
                            Constants.Account_CREATION_STEP_CREATE_PIN
                        findNavController().navigate(R.id.enableNotificationFragment)
                    }
//                        showDialog() For now
                    /*checkInternet(binding.root,requireContext()) {
                        viewModel.confirmPin = pinConfirm
                        clearField()
                        showProgressDialog(requireContext())
                        viewModel.setPin()
                    }*/

                } else {
                    clearField()
                    getString(R.string.please_enter_the_correct_pin).showToast(binding.root,requireContext())
                }

            }
        }
    }

    fun showDialog() {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_CREATE_PIN
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.activate_face_id)
                it.tvMessage.text = getString(R.string.activate_face_message)
                it.tvNegativeButton.text = getString(R.string.decline)
                it.tvPositiveButton.text = getString(R.string.activate)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                    findNavController().navigate(R.id.enableNotificationFragment)
//                    CommonMethods.showProgressDialog(requireContext())
//                    viewModel.setFaceId(
//                        CommonMethods.getDeviceId(requireActivity().contentResolver),
//                        false
//                    )
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    findNavController().navigate(R.id.enableNotificationFragment)
//                    CommonMethods.showProgressDialog(requireContext())
//                    viewModel.setFaceId(
//                        CommonMethods.getDeviceId(requireActivity().contentResolver),
//                        true
//                    )
                }
                show()
            }
        }
    }
}