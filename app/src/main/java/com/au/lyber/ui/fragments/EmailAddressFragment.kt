package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentEmailAddressBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PersonalDataViewModel

class EmailAddressFragment : BaseFragment<FragmentEmailAddressBinding>() {

    /* input fields */
    private val email: String get() = binding.etEmail.text.trim().toString()
    private val password: String get() = binding.etPassword.text.trim().toString()

    private lateinit var viewModel: PersonalDataViewModel

    override fun bind() = FragmentEmailAddressBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 1
        (requireParentFragment() as FillDetailFragment).setUpViews(1)

//        if (App.prefsManager.user?.personal_info_step == Constants.PERSONAL_DATA)
            (requireParentFragment() as FillDetailFragment).binding.ivTopAction.setBackgroundResource(
                R.drawable.ic_close
            )

        viewModel = CommonMethods.getViewModel(requireParentFragment())

        binding.etEmail.text.clear()
        binding.etPassword.text.clear()

        binding.etEmail.requestKeyboard()

    }


    fun checkData(): Boolean {
        when {
            email.isEmpty() -> {
                getString(R.string.please_enter_your_email).showToast(requireContext())
                binding.etEmail.requestKeyboard()
            }
            !CommonMethods.isValidEmail(email) -> {
                getString(R.string.please_enter_a_valid_email_address).showToast(requireContext())
                binding.etEmail.requestKeyboard()
            }
            password.isEmpty() -> {
                getString(R.string.please_enter_password).showToast(requireContext())
                binding.etPassword.requestKeyboard()
            }
            password.length < 8 -> {
                getString(R.string.password_should_be_of_minimum_8_characters).showToast(requireContext())
                binding.etPassword.requestKeyboard()
            }
            else -> {
                viewModel.email = email
                viewModel.password = password
                return true
            }
        }
        return false
    }


}