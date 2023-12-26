package com.Lyber.ui.fragments

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
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.is1DayOld
import com.Lyber.utils.CommonMethods.Companion.isBiometricReady
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.setBiometricPromptInfo
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.OnTextChange
import com.Lyber.viewmodels.NetworkViewModel
import okhttp3.ResponseBody

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
        viewModel.userLoginResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                App.prefsManager.accessToken = it.data.access_token
                App.prefsManager.refreshToken = it.data.refresh_token

                findNavController().navigate(R.id.portfolioHomeFragment)

            }
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
    }

    override fun onResume() {
        super.onResume()
        if (isBiometricReady(requireContext())) {
            binding.tvOr.visible()
            binding.tvBioMetric.visible()
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
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.refreshToken()
                }
            }

            else -> {
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
                    initBiometricPrompt(requireActivity() as AppCompatActivity).authenticate(
                        setBiometricPromptInfo("Authentications", "", "", false)
                    )
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
                        "Incorrect Pin".showToast(requireContext())
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
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(TAG, "Authentication failed for an unknown reason")
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


    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        binding.etPin.setText("")
    }

}