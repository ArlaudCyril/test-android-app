package com.Lyber.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentEmailAddressBinding
import com.Lyber.ui.fragments.bottomsheetfragments.EmailVerificationBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.PersonalDataViewModel
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import java.math.BigInteger
import java.util.regex.Pattern

class EmailAddressFragment : BaseFragment<FragmentEmailAddressBinding>() {

    /* input fields */
    private lateinit var config: SRP6CryptoParams
    private lateinit var generator: SRP6VerifierGenerator
    private val email: String get() = binding.etEmail.text.trim().toString()
    private val password: String get() = binding.etPassword.text.trim().toString()

    private lateinit var viewModel: PersonalDataViewModel

    private val passwordRegex = Pattern.compile("(?=.*[a-z])(?=.*[A-Z])(?=.*[^\\w]).{10,}")

    private var isPasswordOk = false
    private var fromResend = false

    override fun bind() = FragmentEmailAddressBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = CommonMethods.getViewModel(requireParentFragment())
        viewModel.listener = this
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()
        binding.etEmail.text.clear()
        binding.etPassword.text.clear()
        binding.ivTopAction.setOnClickListener {
            stopRegistrationDialog()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle back button press
            stopRegistrationDialog()
        }
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (passwordRegex.matcher(s.toString()).matches()) {
                    isPasswordOk = true
                    binding.tvPasswordWarning.text = getString(R.string.you_have_strong_password)
                    binding.tvPasswordWarning.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.green_500
                        )
                    )
                } else {
                    isPasswordOk = false
                    binding.tvPasswordWarning.text = getString(R.string.password_warning)
                    binding.tvPasswordWarning.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.red_500
                        )
                    )
                }
            }

        })
        binding.etEmail.requestKeyboard()
        setObervers()
        binding.btnNext.setOnClickListener {
            binding.etEmail.clearFocus()
            binding.etPassword.clearFocus()
            if (checkData()) {
                checkInternet(requireActivity()) {
                    CommonMethods.showProgressDialog(requireContext())

                    val emailSalt = BigInteger(1, generator.generateRandomSalt())
                    val emailVerifier = generator.generateVerifier(
                        emailSalt,
                        binding.etEmail.text.toString().lowercase(),
                        binding.etPassword.text.toString()
                    )

                    val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                    val phoneVerifier = generator.generateVerifier(
                        phoneSalt, App.prefsManager.getPhone(), binding.etPassword.text.toString()
                    )

                    viewModel.setEmail(
                        binding.etEmail.text.toString().lowercase(),
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
//                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_PHONE
                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_EMAIL
                findNavController().navigate(R.id.createPinFragment, bundle)
            }
        }
        viewModel.setUpEmailResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
                if (!fromResend) {
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

                    val vc = EmailVerificationBottomSheet(::handle)

                    vc.viewToDelete = transparentView
                    vc.mainView = view?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.show(childFragmentManager, "")

                    // Add the transparent view to the RelativeLayout
                    val mainView = view?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)
                }
                fromResend=false


            }
        }
    }

    private fun handle(txt:String) {
        fromResend = true
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()
        binding.etEmail.clearFocus()
        binding.etPassword.clearFocus()
        if (checkData()) {
            checkInternet(requireActivity()) {
                val emailSalt = BigInteger(1, generator.generateRandomSalt())
                val emailVerifier = generator.generateVerifier(
                    emailSalt,
                    binding.etEmail.text.toString().lowercase(),
                    binding.etPassword.text.toString()
                )

                val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                val phoneVerifier = generator.generateVerifier(
                    phoneSalt, App.prefsManager.getPhone(), binding.etPassword.text.toString()
                )

                viewModel.setEmail(
                    binding.etEmail.text.toString().lowercase(),
                    emailSalt.toString(),
                    emailVerifier.toString(),
                    phoneSalt.toString(),
                    phoneVerifier.toString()
                )
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

            !isPasswordOk -> {
                //getString(R.string.please_enter_password).showToast(requireContext())
                binding.etPassword.requestKeyboard()
            }

            else -> {
                viewModel.email = email.lowercase()
                viewModel.password = password
                return true
            }
        }
        return false
    }


}