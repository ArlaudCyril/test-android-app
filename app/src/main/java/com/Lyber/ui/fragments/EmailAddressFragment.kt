package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.FragmentEmailAddressBinding
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.viewmodels.PersonalDataViewModel

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