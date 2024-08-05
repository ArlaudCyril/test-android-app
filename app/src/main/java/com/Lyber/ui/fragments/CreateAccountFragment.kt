package com.Lyber.ui.fragments

//import java.util.Base64
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.databinding.FragmentCreateAccountBinding
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.fadeIn
import com.Lyber.utils.CommonMethods.Companion.fadeOut
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.isValidEmail
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel
import com.au.countrycodepicker.CountryPicker
import com.nimbusds.srp6.SRP6ClientSession
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import com.Lyber.R
import com.Lyber.utils.EncryptionHelper
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class CreateAccountFragment : BaseFragment<FragmentCreateAccountBinding>(), View.OnClickListener {

    private lateinit var viewModel: SignUpViewModel

    private val mobile get() = binding.etPhone.text.trim().toString()
    private val countryCode get() = binding.tvCountryCode.text.trim().toString()
    private val email get() = binding.etEmail.text.trim().toString()
    private val password get() = binding.etPassword.text!!.trim().toString()
    private lateinit var config: SRP6CryptoParams
    lateinit var generator: SRP6VerifierGenerator
    lateinit var client: SRP6ClientSession
    private var resendCode = -1
    private var fromResend = false

    // Define the secret key

    override fun bind() = FragmentCreateAccountBinding.inflate(layoutInflater)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()

        client = SRP6ClientSession()
        client.xRoutine = XRoutineWithUserIdentity()
    }

    @SuppressLint("FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        App.prefsManager.accessToken = ""
        viewModel = getViewModel(this)
        viewModel.forLogin = requireArguments().getBoolean(Constants.FOR_LOGIN, false)
        viewModel.listener = this

        binding.tvCountryCode.text = viewModel.countryCode
        Log.d("clickSignupFinalQ1", viewModel.forLogin.toString())
        binding.ivTopAction.setOnClickListener {
            if (viewModel.forLogin)
                requireActivity().onBackPressed()
            else
                stopRegistrationDialog()
        }
        if (viewModel.forLogin) {

            binding.tvTitle.text = getString(R.string.happy_to_see_you_back)
            binding.tvSubTitle.text =
                getString(R.string.phone_login_helper_text)
            binding.tvLoginViaEmail.fadeIn()
            binding.tvLoginViaEmail.visible()
            binding.tvForgotPassword.fadeIn()
            binding.tvForgotPassword.visible()
            binding.tilPassword.visible()
        } else
            binding.ivBack.visible()
        binding.ivBack.setOnClickListener(this)
        binding.tvLoginViaEmail.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
        binding.tvLoginViaPhone.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)

        binding.tvCountryCode.setOnClickListener(this)
        binding.root.viewTreeObserver?.addOnGlobalLayoutListener(
            keyboardLayoutListener
        )
        setObservers()

    }

    private fun setObservers() {
        viewModel.userChallengeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                App.prefsManager.accessToken = it.data.token
                App.accessToken = it.data.token

                val creds =
                    client.step2(config, it.data.salt.toBigInteger(), it.data.B.toBigInteger())


                checkInternet(requireContext()) {
                    viewModel.authenticateUser(creds.A.toString(), creds.M1.toString())
                }

            }
        }
        viewModel.userLoginResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
                if (it.data.access_token != null) {
                    binding.etEmail.setText("")
                    binding.etPhone.setText("")
                    binding.etPassword.setText("")

                    App.prefsManager.accessToken = it.data.access_token
                    App.accessToken = it.data.access_token
                    App.prefsManager.refreshToken = it.data.refresh_token

                    childFragmentManager.popBackStack(
                        null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    val bundle = Bundle().apply {
                        putBoolean(Constants.FOR_LOGIN, viewModel.forLogin)
                    }
                    findNavController().navigate(R.id.createPinFragment, bundle)
                } else if (!fromResend) {
                    // Create a transparent color view
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

                    val vc = VerificationBottomSheet(::handle)
                    vc.typeVerification = it.data.type2FA
                    vc.viewToDelete = transparentView
                    vc.mainView = getView()?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.show(childFragmentManager, "")

                    // Add the transparent view to the RelativeLayout
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)

                }
                fromResend = false
            }
        }
        viewModel.setPhoneResponse.observe(viewLifecycleOwner) {
            App.prefsManager.userSmsTimestamps=System.currentTimeMillis()
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                App.prefsManager.accessToken = it.data.token
                App.accessToken = it.data.token
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
                if (!fromResend) {
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

                    val vc = VerificationBottomSheet(::handle)

                    vc.viewToDelete = transparentView
                    vc.mainView = getView()?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.fromSignUp = true
                    vc.show(childFragmentManager, "")

                    // Add the transparent view to the RelativeLayout
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)
                }
            }
            fromResend = false
        }
        viewModel.verifyPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                App.prefsManager.setPhone(viewModel.countryCode.substring(1) + viewModel.mobileNumber)
                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_PHONE
                App.prefsManager.portfolioCompletionStep = Constants.ACCOUNT_CREATING
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, false)
                }
                findNavController().navigate(R.id.emailAddressFragment, bundle)
            }
        }
    }


    override fun onClick(v: View?) {

        binding.apply {

            when (v!!) {

                tvCountryCode ->
                    CountryPicker.Builder().with(requireContext())
                        .listener {
                            tvCountryCode.text = it.dialCode
                            viewModel.countryCode = it.dialCode
                        }
                        .style(R.style.CountryPickerStyle)
                        .sortBy(CountryPicker.SORT_BY_NAME)
                        .build()
                        .showDialog(
                            requireActivity() as AppCompatActivity,
                            R.style.CountryPickerStyle,
                            false
                        )

                tvLoginViaPhone -> {

                    tvTitle.text = getString(R.string.happy_to_see_you_back)
                    tvSubTitle.text = getString(R.string.phone_login_helper_text)

                    tvCountryCode.fadeIn()
                    etPhone.fadeIn()
                    tvCountryCode.visible()
                    etPhone.visible()
                    etEmail.gone()
                    etEmail.fadeOut()
                    tvLoginViaEmail.visible()
                    tvLoginViaPhone.gone()
                    etPhone.setText("")
                    etPassword.setText("")
                    etPhone.requestKeyboard()
                }

                tvLoginViaEmail -> {

                    tvTitle.text = getString(R.string.nice_to_see_you_again)
                    tvSubTitle.text = getString(R.string.email_login_helper_text)

                    tvCountryCode.fadeOut()
                    etPhone.fadeOut()
                    tvCountryCode.gone()
                    etPhone.gone()
                    etEmail.visible()
                    etEmail.fadeIn()
                    tvLoginViaEmail.gone()
                    tvLoginViaPhone.visible()
                    etEmail.setText("")
                    etPassword.setText("")
                    etEmail.requestKeyboard()
                }

                btnNext -> {

                    // login case
                    if (viewModel.forLogin) {
                        App.prefsManager.accessToken = ""
                        client =
                            SRP6ClientSession()
                        client.xRoutine =
                            XRoutineWithUserIdentity()


                        when {

                            // phone case
                            etPhone.isShown -> when {

                                verifyMobile() && verifyPassword() ->
                                    checkInternet(requireContext()) {
                                        showProgressDialog(requireContext())
                                        resendCode = 1
                                        val modifiedMobile =
                                            if (mobile.startsWith("0")) mobile.removeRange(
                                                0,
                                                1
                                            ) else mobile

                                        viewModel.mobileNumber = modifiedMobile
                                        viewModel.countryCode = countryCode
                                        viewModel.password = password

                                        client.step1(
                                            countryCode.substring(1) + modifiedMobile,
                                            password
                                        )
                                        viewModel.userChallenge(phone = "${countryCode}$modifiedMobile")
//                                        viewModel.userChallenge(phone = mobile)

                                    }

                            }

                            // email case
                            else -> when {
                                verifyEmail() && verifyPassword() ->
                                    checkInternet(requireContext()) {
                                        showProgressDialog(requireContext())
                                        resendCode = 2
                                        viewModel.email = email.lowercase()
                                        viewModel.password = password
                                        client.step1(
                                            email.lowercase(),
                                            password
                                        )
                                        viewModel.userChallenge(email = viewModel.email)
                                    }
                            }

                        }

                    }
                    // sign up case
                    else
                        when {
                            verifyMobile() ->
                                checkInternet(requireContext()) {
                                    resendCode = 3
                                    val modifiedMobile =
                                        if (mobile.startsWith("0")) mobile.removeRange(
                                            0,
                                            1
                                        ) else mobile

                                    viewModel.mobileNumber = modifiedMobile
                                    viewModel.countryCode = countryCode
                                    if (canSendSms()) {
                                        showProgressDialog(requireContext())
                                        makeApiRequest()
                                    } else {
                                        val currentTime = System.currentTimeMillis()
                                        val lastSentTime = App.prefsManager.userSmsTimestamps
                                        val timeDifference = currentTime - lastSentTime
                                        val cooldownPeriod = 60000 // 60 seconds in milliseconds
                                        val time = cooldownPeriod - timeDifference
                                        var seconds = 0L
                                        if (time > 0L)
                                            seconds = (time / 1000) % 60
                                        getString(
                                            R.string.you_have_to_wait,
                                            seconds.toString()
                                        ).showToast(requireContext())

                                    }
//                                    viewModel.setPhone(
//                                        viewModel.countryCode.substring(1),
//                                        viewModel.mobileNumber
//                                    )

                                }
                        }

                }

                tvForgotPassword -> {
                    App.prefsManager.accessToken = ""
                    findNavController().navigate(R.id.forgotPasswordFragment)
                }

                ivBack -> {
                    findNavController().popBackStack()
                }

            }

        }
    }

    fun canSendSms(): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastSentTime = App.prefsManager.userSmsTimestamps
            ?: return true // First SMS request
        if (lastSentTime == 0L)
            return true
        val timeDifference = currentTime - lastSentTime
        return timeDifference >= 60000 // 60 seconds in milliseconds
    }

    private fun verifyMobile(): Boolean {
        return when {
            mobile.isEmpty() -> {
                binding.etPhone.requestKeyboard()
                getString(R.string.please_enter_phone_number).showToast(requireContext())
                false
            }

            mobile.length !in 7..15 -> {
                getString(R.string.please_enter_valid_phone_number).showToast(requireContext())
                false
            }

            else -> true
        }
    }

    private fun verifyEmail(): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.requestKeyboard()
                getString(R.string.please_enter_email_address).showToast(requireContext())
                false
            }

            !isValidEmail(email) -> {
                getString(R.string.please_enter_valid_email).showToast(requireContext())
                binding.etEmail.requestKeyboard()
                false
            }

            else -> true
        }
    }

    private fun verifyPassword(): Boolean {
        return when {
            password.isEmpty() -> {
                getString(R.string.please_enter_password).showToast(requireContext())
                binding.etPassword.requestFocus()
                false
            }

            else -> true
        }
    }


    override fun onDestroyView() {
        binding.root.viewTreeObserver?.removeOnGlobalLayoutListener(
            keyboardLayoutListener
        )
        super.onDestroyView()
    }


    private var mLastContentHeight = 0

    private val keyboardLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener {

            try {

                val currentContentHeight = binding.view.height

                Log.d(
                    "ViewTreeObserver",
                    "current = $currentContentHeight, previous = $mLastContentHeight"
                )

                if (currentContentHeight > mLastContentHeight) {
                    Log.d("ViewTreeObserver", "closed")
                } else {
                    Log.d("ViewTreeObserver", "opened")
                    /*(requireParentFragment() as SignUpFragment).binding.apply {
                        scrollView.smoothScrollTo(0, scrollView.height)
                    }*/
                }

                mLastContentHeight = currentContentHeight
            } catch (e: Exception) {
                print(e.message)
            }
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.hardKeyboardHidden) {
            Configuration.HARDKEYBOARDHIDDEN_NO -> "closed".showToast(requireContext())

            Configuration.HARDKEYBOARDHIDDEN_YES -> {
                "opened".showToast(requireContext())
                /* (requireParentFragment() as SignUpFragment).view?.findViewById<ScrollView>(R.id.scrollView)
                     ?.let {
                         it.smoothScrollTo(0, it.height)
                     }*/
            }

            else -> {

            }
        }
    }

    private fun handle(txt: String) {
        fromResend = true
        client =
            SRP6ClientSession()
        client.xRoutine =
            XRoutineWithUserIdentity()
        when (resendCode) {
            1 -> {
                val modifiedMobile =
                    if (mobile.startsWith("0")) mobile.removeRange(0, 1) else mobile

                viewModel.mobileNumber = modifiedMobile
                viewModel.countryCode = countryCode
                viewModel.password = password

                client.step1(
                    countryCode.substring(1) + modifiedMobile,
                    password
                )
                viewModel.userChallenge(phone = "${countryCode}$modifiedMobile")
//
            }

            2 -> {
                viewModel.email = email.lowercase()
                viewModel.password = password
                client.step1(
                    email.lowercase(),
                    password
                )
                viewModel.userChallenge(email = viewModel.email)
            }

            3 -> {
                val modifiedMobile =
                    if (mobile.startsWith("0")) mobile.removeRange(0, 1) else mobile

                viewModel.mobileNumber = modifiedMobile
                viewModel.countryCode = countryCode
//                val payload = """{"countryCode":${viewModel.countryCode.substring(1)},"phoneNo":${viewModel.mobileNumber}}"""
//
//// Generate the signature
//                val signature = createSignature(secretKey, payload, timestamp)
                if (canSendSms())
                    makeApiRequest()
                else {
                    CommonMethods.dismissProgressDialog()
                    CommonMethods.dismissAlertDialog()
                    val currentTime = System.currentTimeMillis()
                    val lastSentTime = App.prefsManager.userSmsTimestamps
                    val timeDifference = currentTime - lastSentTime
                    val cooldownPeriod = 60000 // 60 seconds in milliseconds
                    val time = cooldownPeriod - timeDifference
                    var seconds = 0L
                    if (time > 0L)
                        seconds = (time / 1000) % 60
                    getString(R.string.you_have_to_wait, seconds.toString()).showToast(
                        requireContext()
                    )
                }
            }
        }
    }

    private fun makeApiRequest() {
        val timestamp = (System.currentTimeMillis() / 1000).toString()
//        val timestamp = Instant.now().epochSecond.toString()
        val payload =
            """{"countryCode":${viewModel.countryCode.substring(1)},"phoneNo":"${viewModel.mobileNumber}"}"""

        val decryptedText = EncryptionHelper.decrypt(App.secretKey, App.encryptedKey)

        val signature = createSignature(decryptedText, payload, timestamp)

        viewModel.setPhone(
            viewModel.countryCode.substring(1),
            viewModel.mobileNumber, signature, timestamp
        )
//        val okHttpClient = RestClient.getOkHttpClient2(
//            signature,
//            timestamp
//        )
//        val requestBody = RequestBody.create(
//            "application/json; charset=utf-8".toMediaTypeOrNull(),
//            payload
//        )
//
//        val request = Request.Builder()
//            .url(Constants.BASE_URL + "user-service/set-phone")
//            .post(requestBody)
//            .build()
//        okHttpClient.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle failure
//                activity?.runOnUiThread {
//                    CommonMethods.dismissProgressDialog()
//                    CommonMethods.dismissAlertDialog()
//                    getString(R.string.unable_to_connect_to_the_server).showToast(requireContext())
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                // Handle response
//                if (!response.isSuccessful) {
//                    activity?.runOnUiThread {
//                        // Handle unsuccessful response
//                        CommonMethods.dismissProgressDialog()
//                        CommonMethods.dismissAlertDialog()
////                    val errorBody = response.errorBody()?.string() ?: "Unknown error occurred"
////                    CommonMethods.showErrorMessage(requireContext(), response.errorBody(), binding.root)
//                    }
//                    return
//                }
//                activity?.runOnUiThread {
//                    App.prefsManager.userSmsTimestamps=System.currentTimeMillis()
//
//                    // Parse the response body
//                    response.body?.let { responseBody ->
//                        val responseData = responseBody.string()
//                        val gson = Gson()
//                        val it = gson.fromJson(responseData, SetPhoneResponse::class.java)
//
//                        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                            App.prefsManager.accessToken = it.data.token
//                            App.accessToken = it.data.token
//                            CommonMethods.dismissProgressDialog()
//                            CommonMethods.dismissAlertDialog()
//                            if (!fromResend) {
//                                val transparentView = View(context)
//                                transparentView.setBackgroundColor(
//                                    ContextCompat.getColor(
//                                        requireContext(),
//                                        R.color.semi_transparent_dark
//                                    )
//                                )
//
//                                // Set layout parameters for the transparent view
//                                val viewParams = RelativeLayout.LayoutParams(
//                                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                                    RelativeLayout.LayoutParams.MATCH_PARENT
//                                )
//
//                                val vc = VerificationBottomSheet(::handle)
//
//                                vc.viewToDelete = transparentView
//                                vc.mainView = getView()?.rootView as ViewGroup
//                                vc.viewModel = viewModel
//                                vc.fromSignUp = true
//                                vc.show(childFragmentManager, "")
//
//                                // Add the transparent view to the RelativeLayout
//                                val mainView = getView()?.rootView as ViewGroup
//                                mainView.addView(transparentView, viewParams)
//                            } else
//                                resendCodeParam!!.onResend()
//
//                        }
//                        fromResend = false
//                    }
//                }
//            }
//        })

    }


    private fun createSignature(secretKey: String, payload: String, timestamp: String): String {
        val message = "$timestamp.$payload"
        val hmacSHA256 = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), hmacSHA256)
        val mac = Mac.getInstance(hmacSHA256)
        mac.init(secretKeySpec)
        val hash = mac.doFinal(message.toByteArray())
        return hash.joinToString("") { String.format("%02x", it) }
    }

}