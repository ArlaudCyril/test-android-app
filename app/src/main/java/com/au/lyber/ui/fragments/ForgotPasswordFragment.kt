package com.au.lyber.ui.fragments

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentForgotPasswordBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.SignUpViewModel


class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>(), OnClickListener {
    private lateinit var viewModel: SignUpViewModel
    override fun bind() = FragmentForgotPasswordBinding.inflate(layoutInflater)
var buttonClicked=false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(this)
        binding.etEmail.addTextChangedListener(onTextChange)
        binding.btnSendResetLink.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                ConfirmationBottomSheet().show(childFragmentManager, Constants.EMAIL_SENT)
            }
        }
    }

    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (binding.etEmail.text.trim().toString().isNotEmpty()) {
                if (CommonMethods.isValidEmail(binding.etEmail.text.trim().toString())) {
                    val colorStateList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_500
                            )
                        )
                    binding.btnSendResetLink.backgroundTintList = colorStateList

                    // Set background tint mode to SRC_ATOP
                    binding.btnSendResetLink.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                    buttonClicked = true
                } else {
                    val colorStateList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_gray_600
                            )
                        )
                    binding.btnSendResetLink.backgroundTintList = colorStateList

                    // Set background tint mode to SRC_ATOP
                    binding.btnSendResetLink.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                    buttonClicked = false
                }
            }
            else
                buttonClicked=false

        }


    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnSendResetLink -> {
                    if(buttonClicked) {
                        val email = binding.etEmail.text.trim().toString()
                        CommonMethods.showProgressDialog(requireContext())
                        viewModel.forgotPass(email)
                    }

                }

            }
        }
    }
}