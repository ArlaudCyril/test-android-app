/*
package com.au.lyber.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentTestSignUpBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.au.lyber.utils.App
import com.au.lyber.utils.App.Companion.prefsManager
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.viewmodels.SignUpViewModel
import com.nimbusds.srp6.SRP6ClientSession
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity

class SignUpFragment : BaseFragment<FragmentTestSignUpBinding>() {

    var mPosition: Int = 0
    private lateinit var viewModel: SignUpViewModel

    private lateinit var config: SRP6CryptoParams
    lateinit var generator: SRP6VerifierGenerator
    lateinit var client: SRP6ClientSession


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()

        client = SRP6ClientSession()
        client.xRoutine = XRoutineWithUserIdentity()

    }

    override fun bind() = FragmentTestSignUpBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)

        mPosition = when (prefsManager.savedScreen) {
            CreatePinFragment::class.java.name -> 2
            EnableNotificationFragment::class.java.name -> 4
            else -> 0
        }

        viewModel.forLogin = arguments?.getString("forLogin")?.isNotEmpty() == true
        Log.d("clickSignup",viewModel.forLogin.toString())

        changeFragment(mPosition,false)
        */
/* new Apis *//*


        viewModel.setPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                prefsManager.accessToken = it.data.token
                App.accessToken = it.data.token
                dismissProgressDialog()
                mPosition = 1
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

                val vc  = VerificationBottomSheet()

                vc.viewToDelete = transparentView
                vc.mainView = getView()?.rootView as ViewGroup
                vc.viewModel = viewModel
                vc.show(childFragmentManager, "")

                // Add the transparent view to the RelativeLayout
                val mainView = getView()?.rootView as ViewGroup
                mainView.addView(transparentView, viewParams)
            }
        }

        viewModel.verifyPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                prefsManager.setPhone(viewModel.mobileNumber)
                dismissProgressDialog()
                mPosition = 2
                changeFragment(mPosition,true)
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

            }
        }

        viewModel.userLoginResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if(it.data.access_token != null){

                    prefsManager.accessToken = it.data.access_token
                    App.accessToken = it.data.access_token
                    prefsManager.refreshToken = it.data.refresh_token

                    childFragmentManager.popBackStack(
                        null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    mPosition = 2
                    changeFragment(mPosition,false)
                }else{
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

                    val vc  = VerificationBottomSheet()
                    vc.typeVerification = it.data.type2FA
                    vc.viewToDelete = transparentView
                    vc.mainView = getView()?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.show(childFragmentManager, "")

                    // Add the transparent view to the RelativeLayout
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)

                }


            }

        }


        */
/* old Apis *//*


        viewModel.enterPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                prefsManager.accessToken = it.token
                App.accessToken = it.token
                dismissProgressDialog()

                if (viewModel.forLogin) {
                    if (viewModel.email.isEmpty()) {
                        mPosition = 1
                        changeFragment(mPosition,true)
                    } else replace(R.id.frameLayoutSignUp, VerifyEmailLoginFragment())
                } else {
                    mPosition = 1
                    changeFragment(mPosition,true)
                }
            }
        }

        viewModel.emailVerificationResponse.observe(viewLifecycleOwner) {

            dismissProgressDialog()
            viewModel.enterPhoneResponse.value?.let {
//                App.prefsManager.user = it.user
            }
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mPosition = 2
            changeFragment(mPosition,true)

        }

        viewModel.verifyPinResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()

            viewModel.enterPhoneResponse.value?.let {

                if (viewModel.forLogin) {
                    when {

                    }
                } else {

                    childFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    mPosition = 2
                   changeFragment(mPosition,false)
                }

            }
        }

        viewModel.enterOtpResponse.observe(viewLifecycleOwner) {

            dismissProgressDialog()
            viewModel.enterPhoneResponse.value?.let {
//                App.prefsManager.user = it.user
            }
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mPosition = 2
            changeFragment(mPosition,true)

        }

        viewModel.setPinResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
//            App.prefsManager.loginPinSet()
            showDialog()
        }

        viewModel.activateFaceId.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mPosition = 4
            changeFragment(mPosition,false)

        }

        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            mPosition--
            prefsManager.logout()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }
        binding.tvTopAction.setOnClickListener { showLogoutDialog() }


    }

    private fun changeFragment(mPosition: Int, isBackStack: Boolean) {
        */
/*        CreateAccountFragment(),
        EnterOtpFragment(),
        CreatePinFragment(),
        ConfirmPinFragment(),
        EnableNotificationFragment()*//*

        val bundle = Bundle().apply {
            putBoolean("forLogin", viewModel.forLogin)
        }
        when(mPosition){
            0->{
                setIndicators(0)
                findNavController().navigate(R.id.createAccountFragment,bundle)
            }
            2->{
                setIndicators(2)
                findNavController().navigate(R.id.createPinFragment)
            }
            3->{
                setIndicators(3)
                findNavController().navigate(R.id.confirmPinFragment)
            }
            4->{
                setIndicators(4)
                findNavController().navigate(R.id.enableNotificationFragment)
            }
        }
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

*/
/*                    CommonMethods.checkInternet(requireContext()) {
                        dismiss()
                        CommonMethods.showProgressDialog(requireContext())
                        viewModel.logout(CommonMethods.getDeviceId(requireActivity().contentResolver))
                    }*//*


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
                    changeFragment(4,true)
//                    CommonMethods.showProgressDialog(requireContext())
//                    viewModel.setFaceId(
//                        CommonMethods.getDeviceId(requireActivity().contentResolver),
//                        false
//                    )
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    changeFragment(4,true)
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

    */
/* top Left Action Image icon handling *//*

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

     private fun setIndicators(position: Int) {
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

    */
/* Host Activity's onBackPressed *//*

  */
/*  override fun onBackPressed(): Boolean {
        return if (mPosition == 2 || mPosition == 4) {
            showLogoutDialog()
            false
        } else if (requireActivity().supportFragmentManager.backStackEntryCount != 0) {
            mPosition--
            requireActivity().supportFragmentManager.popBackStack()
            false
        } else true

    }*//*





}*/
