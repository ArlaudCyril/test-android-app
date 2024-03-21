package com.Lyber.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentCreatePinBinding
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.replace
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.Lyber.viewmodels.SignUpViewModel

class CreatePinFragment : BaseFragment<FragmentCreatePinBinding>() {
    override fun bind() = FragmentCreatePinBinding.inflate(layoutInflater)

    private val pin get() = binding.etCreatePin.text.trim().toString()

    private lateinit var viewModel: SignUpViewModel

    override fun onResume() {
        super.onResume()
        binding.etCreatePin.requestKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.prefsManager.savedScreen = javaClass.name
        viewModel = getViewModel(requireParentFragment())
        viewModel.forLogin = requireArguments().getBoolean(Constants.FOR_LOGIN, false)
        binding.etCreatePin.addTextChangedListener(onTextChange)
        if (requireArguments().containsKey(Constants.IS_CHANGE_PIN)
            && requireArguments().getBoolean(Constants.IS_CHANGE_PIN)
        ) {
            binding.ivTopAction.visible()
            binding.llIndicators.gone()
            binding.tvTopAction.gone()
        }
      else  if (arguments != null && requireArguments().containsKey(Constants.FOR_LOGIN) && !requireArguments().getBoolean(
                Constants.FOR_LOGIN
            )){
            binding.llIndicators.visible()
            binding.ivTopClose.visible()
            binding.tvTopAction.gone()
            binding.ivTopAction.gone()
            requireActivity().onBackPressedDispatcher.addCallback(this) {
               stopRegistrationDialog()
            }
        }
        else {
            binding.llIndicators.visible()
            binding.tvTopAction.gone()
        }
        binding.ivTopClose.setOnClickListener {
            stopRegistrationDialog()
        }

        binding.tvTopAction.setOnClickListener {
            showLogoutDialog()
        }
        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

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
//                        viewModel.logout(CommonMethods.getDeviceId(requireActivity().contentResolver))
//                    }
//                }
//
//                show()
//            }
//        }
//    }
    private fun showLogoutDialog() {

        Dialog(requireActivity(), R.style.DialogTheme).apply {

            CustomDialogLayoutBinding.inflate(layoutInflater).let {

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)

                it.tvTitle.text = getString(R.string.log_out)
                it.tvMessage.text = getString(R.string.logout_message)
                it.tvNegativeButton.text = getString(R.string.no_t)
                it.tvPositiveButton.text = getString(R.string.yes_t)

                it.tvNegativeButton.setOnClickListener { dismiss() }

                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    App.prefsManager.logout()
                    requireActivity().finishAffinity()

                    startActivity(
                        Intent(requireActivity(), com.Lyber.ui.activities.SplashActivity::class.java)
                            .putExtra(Constants.FOR_LOGOUT, Constants.FOR_LOGOUT)
                    )
                }

                show()

            }
        }
    }

    /* On Text Change */
    private val onTextChange = object : OnTextChange {
        override fun onTextChange() {
            changeDots(pin.count())
        }
    }

    private fun clearFields() {
        binding.etCreatePin.setText("")
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
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, viewModel.forLogin)
                    if (requireArguments().containsKey(Constants.IS_CHANGE_PIN))
                        putBoolean(
                            Constants.IS_CHANGE_PIN,
                            requireArguments().getBoolean(Constants.IS_CHANGE_PIN)
                        )
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.createPin = pin
                    findNavController().navigate(R.id.confirmPinFragment, bundle)

                    /*App.prefsManager.user?.let {
                    if (it.login_pin_set) {
                        checkInternet(requireContext()) {
                            showProgressDialog(requireContext())
                            viewModel.verifyPin(pin)
                        }
                    }
                }*/

                    clearFields()
                }, 300)
            }
        }
    }


}