package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import com.Lyber.R
import com.Lyber.databinding.FragmentUnlockAppBinding
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.is1DayOld
import com.Lyber.utils.CommonMethods.Companion.logOut
import com.Lyber.utils.CommonMethods.Companion.setBiometricPromptInfo
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.OnTextChange
import com.Lyber.viewmodels.NetworkViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import org.json.JSONObject
import java.util.Locale

class UnlockAppFragment : BaseFragment<FragmentUnlockAppBinding>(), View.OnClickListener {

    private val pin get() = binding.etPin.text.trim().toString()
    private lateinit var viewModel: NetworkViewModel
    override fun bind() = FragmentUnlockAppBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Fade()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(this)
        viewModel.listener = this
        viewModel.getUser()

        viewModel.userLoginResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                App.prefsManager.accessToken = it.data.access_token
                App.prefsManager.refreshToken = it.data.refresh_token

                findNavController().navigate(R.id.portfolioHomeFragment)

            }
        }
        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                logOut(requireContext())
            }
        }
        viewModel.getUserResponse.observe(viewLifecycleOwner) {
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            try {
                App.prefsManager.user = it.data
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

        binding.etPin.addTextChangedListener(onTextChange)
        binding.tvOne.setOnClickListener(this)
        binding.tvTwo.setOnClickListener(this)
        binding.tvThree.setOnClickListener(this)
        binding.tvFour.setOnClickListener(this)
        binding.tvFive.setOnClickListener(this)
        binding.tvSix.setOnClickListener(this)
        binding.tvSeven.setOnClickListener(this)
        binding.tvEight.setOnClickListener(this)
        binding.tvNine.setOnClickListener(this)
        binding.tvZero.setOnClickListener(this)
        binding.tvBioMetric.setOnClickListener(this)
        binding.tvBackArrow.setOnClickListener(this)
        binding.tvLogOut.setOnClickListener(this)
  }

    override fun onResume() {
        super.onResume()
//        if (CommonMethods.isFaceIdAvail(requireContext()) && CommonMethods.isBiometricReady(requireActivity())) {
        if (App.prefsManager.faceIdEnabled && CommonMethods.isBiometricReady(requireActivity())) {
            binding.tvOr.visible()
            binding.tvBioMetric.visible()
            val auth=getString(R.string.authenticate)
            val enterPin=getString(R.string.enter_your_pin)
            try {

                initBiometricPrompt(requireActivity() as AppCompatActivity).authenticate(
                    setBiometricPromptInfo(auth, enterPin,"", "", false)
                )
            }catch (_:Exception){

            }
        } else {
        binding.tvOr.gone()
        binding.tvBioMetric.gone()
        }
    }

    private val onTextChange = object : OnTextChange {
        override fun onTextChange() {
            changeDots(pin.length)
        }
    }

    private fun verified() {

        when {

            App.prefsManager.refreshToken.isEmpty() -> {

                when (App.prefsManager.savedScreen) {

                    EnableNotificationFragment::class.java.name -> {
                        findNavController().navigate(R.id.enableNotificationFragment)

                    }

                    CompletePortfolioFragment::class.java.name -> {
                        findNavController().navigate(R.id.completePortfolioFragment)
                    }

                    else -> findNavController().navigate(R.id.enableNotificationFragment)

                }
            }

            App.prefsManager.refreshTokenSavedAt.is1DayOld() -> {
                checkInternet(binding.root,requireContext()) {
                   val jsonObject = JSONObject()
                    jsonObject.put("method","refresh_token")
                    jsonObject.put("refresh_token", App.prefsManager.refreshToken)
                    val jsonString = CommonMethods.sortAndFormatJson(jsonObject)
                    // Generate the request hash
                    val requestHash = CommonMethods.generateRequestHash(jsonString)

                    val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                        SplashActivity.integrityTokenProvider?.request(
                            StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                .setRequestHash(requestHash)
                                .build()
                        )
                    integrityTokenResponse?.addOnSuccessListener { response ->
                        Log.d("token", "${response.token()}")
                        CommonMethods.showProgressDialog(requireActivity())
                        viewModel.refreshToken(response.token())

                    }?.addOnFailureListener { exception ->
                        Log.d("token", "${exception}")

                    }
                }
            }

            else -> {
//                findNavController().navigate(R.id.addressFragment)
                findNavController().navigate(R.id.portfolioHomeFragment)
            }
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                tvOne -> binding.etPin.append("1")
                tvTwo -> binding.etPin.append("2")
                tvThree -> binding.etPin.append("3")
                tvFour -> binding.etPin.append("4")
                tvFive -> binding.etPin.append("5")
                tvSix -> binding.etPin.append("6")
                tvSeven -> binding.etPin.append("7")
                tvEight -> binding.etPin.append("8")
                tvNine -> binding.etPin.append("9")
                tvZero -> binding.etPin.append("0")
                tvBackArrow -> {
                    if (pin.isNotEmpty())
                        binding.etPin.setText(pin.substring(0 until pin.length - 1))
                }

                tvBioMetric -> {
                    val auth=getString(R.string.authenticate)
                    val enterPin=getString(R.string.enter_your_pin)
                    initBiometricPrompt(requireActivity() as AppCompatActivity).authenticate(
                        setBiometricPromptInfo(auth, enterPin,"", "", false)
                    )
                }

                tvLogOut -> {
                    CommonMethods.checkInternet(binding.root,requireContext()) {
                          CommonMethods.showProgressDialog(requireActivity())
                            viewModel.logout()
                    }
                }

            }
        }
    }

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
                    if (i <= pinCount - 1) {
                        val drawable = getDrawable(requireContext(), R.drawable.circle_dot_selected)
                        drawable?.setTint(getColor(requireContext(), R.color.purple_500))
                        imageView.background = drawable
                    } else
                        imageView.setBackgroundResource(R.drawable.circle_dot_unselected)
                }

            if (pinCount == 4) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (pin == App.prefsManager.userPin)
                        verified()
                    else {
                        getString(R.string.incorrect_pin).showToast(binding.root,requireContext())
                        binding.etPin.setText("")
                    }
                }, 200)

            }
        }
    }

    /* biometric */
    private val TAG = "UnlockAppFragment"

    private fun initBiometricPrompt(
        activity: AppCompatActivity
    ): BiometricPrompt {

        // 1
        val executor = ContextCompat.getMainExecutor(activity)

        // 2
        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "onAuthenticationError: ")
//                "onAuthenticationError: $errorCode".showToast(requireContext())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(TAG, "Authentication failed for an unknown reason")
//                "Authentication failed for an unknown reason".showToast(requireContext())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                verified()
                Log.d(TAG, "onAuthenticationSucceeded: ${result.cryptoObject}")
            }
        }

        // 3
        return BiometricPrompt(activity, executor, callback)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onRetrofitError(errorCode: Int, msg: String) {
        super.onRetrofitError(errorCode, msg)
        binding.etPin.setText("")
    }

}