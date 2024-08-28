package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentEnterOtpBinding
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.viewmodels.ProfileViewModel
import okhttp3.ResponseBody

class VerifyPhoneForPinFragment : BaseFragment<FragmentEnterOtpBinding>() {

    private lateinit var viewModel: ProfileViewModel

    private val codeOne get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree get() = binding.etCodeThree.text.trim().toString()
    private val codeFour get() = binding.etCodeFour.text.trim().toString()


    override fun bind() = FragmentEnterOtpBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llNewCodeSentIn.gone()
        binding.tvPhoneNumber.gone()

        viewModel = getViewModel(requireParentFragment())
        viewModel.listener = this

        binding.apply {
            etCodeOne.addTextChangedListener(onTextChange)
            etCodeTwo.addTextChangedListener(onTextChange)
            etCodeThree.addTextChangedListener(onTextChange)
            etCodeFour.addTextChangedListener(onTextChange)

            etCodeOne.setOnKeyListener(key)
            etCodeTwo.setOnKeyListener(key)
            etCodeThree.setOnKeyListener(key)
            etCodeFour.setOnKeyListener(key)

        }
        binding.etCodeOne.requestKeyboard()
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
    }

    fun getCode() = codeOne + codeTwo + codeThree + codeFour

    private val key = View.OnKeyListener { v, keyCode, event ->

        val delPressed = event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DEL

        if (delPressed) {
            when {
                codeTwo.isEmpty() -> binding.etCodeOne.requestFocus()
                codeThree.isEmpty() -> binding.etCodeTwo.requestFocus()
                codeFour.isEmpty() -> binding.etCodeThree.requestFocus()
                else -> {}
            }
            return@OnKeyListener false
        }
        false

    }

    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (getCode().length == 4) {
                checkInternet(binding.root,requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.verifyPhoneForPin(getCode())
                }
            } else
                when {
                    codeOne.isEmpty() -> binding.etCodeOne.requestFocus()
                    codeTwo.isEmpty() -> binding.etCodeTwo.requestFocus()
                    codeThree.isEmpty() -> binding.etCodeThree.requestFocus()
                    codeFour.isEmpty() -> binding.etCodeFour.requestFocus()
                }
        }

    }

    fun clearFields() {
        binding.apply {
            etCodeOne.setText("")
            etCodeTwo.setText("")
            etCodeThree.setText("")
            etCodeFour.setText("")
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        clearFields()
    }


}