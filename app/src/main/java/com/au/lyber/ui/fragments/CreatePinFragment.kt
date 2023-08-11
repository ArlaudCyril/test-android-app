package com.au.lyber.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentCreatePinBinding
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.SignUpViewModel

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
        viewModel.forLogin = requireArguments().getBoolean(Constants.FOR_LOGIN,false)
        binding.etCreatePin.addTextChangedListener(onTextChange)
        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.tvTopAction.setOnClickListener {
            showLogoutDialog()
        }

    }
    private fun showLogoutDialog() {

        Dialog(requireActivity(), R.style.DialogTheme).apply {

            CustomDialogLayoutBinding.inflate(layoutInflater).let {

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)

                it.tvTitle.text = getString(R.string.log_out)
                it.tvMessage.text = getString(R.string.logout_message)
                it.tvNegativeButton.text = getString(R.string.no)
                it.tvPositiveButton.text = getString(R.string.yes)

                it.tvNegativeButton.setOnClickListener { dismiss() }

                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    App.prefsManager.logout()
                    requireActivity().finishAffinity()
                    startActivity(Intent(requireActivity(),SplashActivity::class.java)
                        .putExtra(Constants.FOR_LOGOUT,Constants.FOR_LOGOUT))
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
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.createPin = pin
                    findNavController().navigate(R.id.confirmPinFragment,bundle)

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