package com.au.lyber.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentTestSignUpBinding
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.utils.ActivityCallbacks
import com.au.lyber.utils.App
import com.au.lyber.utils.App.Companion.prefsManager
import com.au.lyber.utils.CommonMethods.Companion.addFragment
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.SignUpViewModel
import com.chrynan.krypt.srp.Client
import com.chrynan.krypt.srp.Group
import com.chrynan.krypt.srp.N2048
import com.chrynan.krypt.srp.SrpHashFunction
import com.nimbusds.srp6.SRP6ClientSession
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity

class SignUpFragment : BaseFragment<FragmentTestSignUpBinding>(), ActivityCallbacks {


    var mPosition: Int = 0
    private lateinit var viewModel: SignUpViewModel

    private lateinit var config: SRP6CryptoParams
    lateinit var generator: SRP6VerifierGenerator
    lateinit var client: SRP6ClientSession

    val fragments: MutableList<Fragment> = mutableListOf(
        CreateAccountFragment(),
        EnterOtpFragment(),
        CreatePinFragment(),
        ConfirmPinFragment(),
        EnableNotificationFragment()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
//        config = SRP6CryptoParams.getInstance(512,"SHA-1")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()

        client = SRP6ClientSession()
        client.xRoutine = XRoutineWithUserIdentity()
    }

    override fun bind() = FragmentTestSignUpBinding.inflate(layoutInflater, null, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)
        SplashActivity.activityCallbacks = this

        mPosition = when (prefsManager.savedScreen) {
            CreatePinFragment::class.java.name -> 2
            EnableNotificationFragment::class.java.name -> 4
            else -> 0
        }

        replace(R.id.frameLayoutSignUp, fragments[mPosition], false)

        viewModel.forLogin = arguments?.getString("forLogin")?.isNotEmpty() == true


        /* new Apis */

        viewModel.setPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                prefsManager.accessToken = it.data.token
                App.accessToken = it.data.token
                dismissProgressDialog()
                mPosition = 1
                replace(R.id.frameLayoutSignUp, fragments[mPosition])
            }
        }

        viewModel.verifyPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                prefsManager.setPhone(viewModel.mobileNumber)
                dismissProgressDialog()
                mPosition = 2
                replace(R.id.frameLayoutSignUp, fragments[mPosition])
            }
        }

        viewModel.userChallengeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                prefsManager.accessToken = it.data.token
                App.accessToken = it.data.token

                val creds =
                    client.step2(config, it.data.salt.toBigInteger(), it.data.B.toBigInteger())


                checkInternet(requireContext()) {
                    viewModel.authenticateUser(creds.A.toString(), creds.M1.toString())
                }
//                }

            }
        }

        viewModel.userLoginResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                prefsManager.accessToken = it.data.access_token
                App.accessToken = it.data.access_token
                prefsManager.refreshToken = it.data.refresh_token

                childFragmentManager.popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )

                mPosition = 2
                replace(R.id.frameLayoutSignUp, fragments[mPosition], false)

            }

        }


        /* old Apis */

        viewModel.enterPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                prefsManager.accessToken = it.token
                App.accessToken = it.token
                dismissProgressDialog()

                if (viewModel.forLogin) {
                    if (viewModel.email.isEmpty()) {
                        mPosition = 1
                        replace(R.id.frameLayoutSignUp, fragments[mPosition])
                    } else replace(R.id.frameLayoutSignUp, VerifyEmailLoginFragment())
                } else {
                    mPosition = 1
                    replace(R.id.frameLayoutSignUp, fragments[mPosition])
                }
            }
        }

        viewModel.emailVerificationResponse.observe(viewLifecycleOwner) {

            dismissProgressDialog()
            viewModel.enterPhoneResponse.value?.let {
                App.prefsManager.user = it.user
            }
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mPosition = 2
            replace(R.id.frameLayoutSignUp, fragments[mPosition])

        }

        viewModel.verifyPinResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()

            viewModel.enterPhoneResponse.value?.let {

                App.prefsManager.user = it.user

                if (viewModel.forLogin) {
                    when {

                        it.user.step == Constants.PROFILE_COMPLETED -> {
                            requireActivity().clearBackStack()
                            requireActivity().addFragment(
                                R.id.flSplashActivity,
                                PortfolioFragment()
                            )
                        }

                        // haven't set login pin yet
                        !it.user.login_pin_set -> {
                            childFragmentManager.popBackStack(
                                null,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            mPosition = 2
                            replace(R.id.frameLayoutSignUp, fragments[mPosition], false)
                        }

                        // haven't enabled push notification
                        it.user.is_push_enabled == 0 -> {
                            childFragmentManager.popBackStack(
                                null,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            mPosition = 4
                            replace(R.id.frameLayoutSignUp, fragments[mPosition], false)
                        }

                        // haven't filled personal info yet
                        it.user.first_name.isNullOrEmpty() ||
                                it.user.profile_verification_status == "Pending" -> {
                            requireActivity().clearBackStack()
                            requireActivity().addFragment(
                                R.id.flSplashActivity,
                                CompletePortfolioFragment()
                            )
                        }
                    }
                } else {

                    childFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    mPosition = 2
                    replace(R.id.frameLayoutSignUp, fragments[mPosition], false)
                }

            }
        }

        viewModel.enterOtpResponse.observe(viewLifecycleOwner) {

            dismissProgressDialog()
            viewModel.enterPhoneResponse.value?.let {
                App.prefsManager.user = it.user
            }
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mPosition = 2
            replace(R.id.frameLayoutSignUp, fragments[mPosition])

        }

        viewModel.setPinResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            App.prefsManager.loginPinSet()
            showDialog()
        }

        viewModel.activateFaceId.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mPosition = 4
            replace(R.id.frameLayoutSignUp, fragments[mPosition], false)
        }

        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            mPosition--
            prefsManager.logout()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }
        binding.tvTopAction.setOnClickListener { showLogoutDialog() }

        /*prefsManager.user?.let {
            if (!it.login_pin_set) {
                mPosition = 2
                replace(R.id.frameLayoutSignUp, fragments[mPosition], false)
            } else {
                mPosition = 4
                replace(R.id.frameLayoutSignUp, fragments[mPosition], false)
            }
            setIndicators(mPosition)
        }*/

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
                    prefsManager.logout()
                    requireActivity().supportFragmentManager.popBackStack()

/*                    CommonMethods.checkInternet(requireContext()) {
                        dismiss()
                        CommonMethods.showProgressDialog(requireContext())
                        viewModel.logout(CommonMethods.getDeviceId(requireActivity().contentResolver))
                    }*/

                }

                show()

            }
        }
    }

    fun showDialog() {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.activate_face_id)
                it.tvMessage.text = getString(R.string.activate_face_message)
                it.tvNegativeButton.text = getString(R.string.decline)
                it.tvPositiveButton.text = getString(R.string.activate)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                    replace(R.id.frameLayoutSignUp, fragments[4])
//                    CommonMethods.showProgressDialog(requireContext())
//                    viewModel.setFaceId(
//                        CommonMethods.getDeviceId(requireActivity().contentResolver),
//                        false
//                    )
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    replace(R.id.frameLayoutSignUp, fragments[4])
//                    CommonMethods.showProgressDialog(requireContext())
//                    viewModel.setFaceId(
//                        CommonMethods.getDeviceId(requireActivity().contentResolver),
//                        true
//                    )
                }
                show()
            }
        }
    }

    /* top Left Action Image icon handling */
    private fun setImage(position: Int) {
        val res = when (position) {
            0 -> {
                binding.ivTopAction.visible()
                binding.tvTopAction.gone()
                R.drawable.ic_close
            }
            2 -> {
                binding.ivTopAction.gone()
                binding.tvTopAction.visible()
                R.drawable.ic_back
            }
            4 -> {
                binding.ivTopAction.gone()
                binding.tvTopAction.visible()
                R.drawable.ic_back
            }
            else -> {
                binding.ivTopAction.visible()
                binding.tvTopAction.gone()
                R.drawable.ic_back
            }
        }
        binding.ivTopAction.setImageResource(res)
    }

    internal fun setIndicators(position: Int) {
        binding.llIndicators.let {
            for (i in 0 until it.childCount) {
                val imageView = it.getChildAt(i) as ImageView
                if (i == position)
                    imageView.setBackgroundResource(R.drawable.page_selected_indicator)
                else imageView.setBackgroundResource(R.drawable.indicator_unselected)
            }

        }
        mPosition = position
        setImage(position)
    }

    /* Host Activity's onBackPressed */
    override fun onBackPressed(): Boolean {
        return if (mPosition == 2 || mPosition == 4) {
            showLogoutDialog()
            false
        } else if (childFragmentManager.backStackEntryCount != 0) {
            mPosition--
            childFragmentManager.popBackStack()
            false
        } else true

    }

    override fun onDestroyView() {
        super.onDestroyView()
        SplashActivity.activityCallbacks = null
    }


}