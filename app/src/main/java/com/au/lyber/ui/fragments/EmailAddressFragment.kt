package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentEmailAddressBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.EmailVerificationBottomSheet
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PersonalDataViewModel
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import java.math.BigInteger

class EmailAddressFragment : BaseFragment<FragmentEmailAddressBinding>() {

    /* input fields */
    private lateinit var config: SRP6CryptoParams
    private lateinit var generator: SRP6VerifierGenerator
    private val email: String get() = binding.etEmail.text.trim().toString()
    private val password: String get() = binding.etPassword.text.trim().toString()

    private lateinit var viewModel: PersonalDataViewModel

    override fun bind() = FragmentEmailAddressBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CommonMethods.getViewModel(requireParentFragment())
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()
        binding.etEmail.text.clear()
        binding.etPassword.text.clear()
        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.etEmail.requestKeyboard()
        setObervers()
        binding.btnNext.setOnClickListener {
            if (checkData()){
                checkInternet(requireActivity()){
                    CommonMethods.showProgressDialog(requireContext())

                    val emailSalt = BigInteger(1, generator.generateRandomSalt())
                    val emailVerifier = generator.generateVerifier(
                        emailSalt, binding.etEmail.text.toString(), binding.etPassword.text.toString()
                    )

                    val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                    val phoneVerifier = generator.generateVerifier(
                        phoneSalt, App.prefsManager.getPhone(),binding.etPassword.text.toString()
                    )

                    viewModel.setEmail(
                        binding.etEmail.text.toString(),
                        emailSalt.toString(),
                        emailVerifier.toString(),
                        phoneSalt.toString(),
                        phoneVerifier.toString()
                    )
                }
            }
        }
    }

    private fun setObervers() {
        viewModel.verifyEmailResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, false)
                }
                App.prefsManager.accountCreationSteps= Constants.Account_CREATION_STEP_PHONE
                findNavController().navigate(R.id.createPinFragment,bundle)
            }
        }
        viewModel.setUpEmailResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                App.prefsManager.personalDataSteps = Constants.EMAIL_ADDRESS
                val transparentView = View(context)
                transparentView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.semi_transparent_dark
                    )
                )

                // Set layout parameters for the transparent view
                val viewParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )

                val vc  = EmailVerificationBottomSheet()

                vc.viewToDelete = transparentView
                vc.mainView = view?.rootView as ViewGroup
                vc.viewModel = viewModel
                vc.show(childFragmentManager, "")

                // Add the transparent view to the RelativeLayout
                val mainView = view?.rootView as ViewGroup
                mainView.addView(transparentView, viewParams)

            }
        }
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