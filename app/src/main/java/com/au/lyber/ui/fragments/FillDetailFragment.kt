package com.au.lyber.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTestFillDetailBinding
import com.au.lyber.models.JWTPayload
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.ui.fragments.bottomsheetfragments.EmailVerificationBottomSheet
import com.au.lyber.utils.ActivityCallbacks
import com.au.lyber.utils.App.Companion.prefsManager
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.add
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PersonalDataViewModel
import com.google.gson.Gson
import com.nimbusds.jwt.JWTParser
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import java.math.BigInteger

class FillDetailFragment : BaseFragment<FragmentTestFillDetailBinding>(), View.OnClickListener,
    ActivityCallbacks {

    var position = 0

    private val fragmentList
        get() = listOf(
            PersonalDataFragment(),
            EmailAddressFragment(),
            VerificationEmailAddressFragment(),
            AddressFragment(),
            InvestmentExperienceFragment()
        )

    private var toEdit = ""

    private lateinit var config: SRP6CryptoParams
    private lateinit var generator: SRP6VerifierGenerator

    lateinit var viewModel: PersonalDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toEdit = arguments?.getString("toEdit", "") ?: ""
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()

    }

    override fun bind() = FragmentTestFillDetailBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)

        SplashActivity.activityCallbacks = this

        viewModel.listener = this
        viewModel.password = ""
        viewModel.email = ""

        /* new Apis */

        viewModel.setUserInfoResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                prefsManager.personalDataSteps = Constants.PERSONAL_DATA
                if (arguments != null && requireArguments().containsKey(Constants.IS_REVIEW)) {
                    position = 2
                    moveToNext()
                } else {
                    replace(R.id.flFillPersonalData, fragmentList[1], true)
                }
            }
        }

        viewModel.setUpEmailResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                prefsManager.personalDataSteps = Constants.EMAIL_ADDRESS
                moveToNext()
            }
        }

        viewModel.getUserResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
            }
        }

        viewModel.setUserAddressResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                prefsManager.personalDataSteps = Constants.ADDRESS
                moveToNext()
            }
        }

        viewModel.setInvestmentExpResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                prefsManager.portfolioCompletionStep = Constants.PERSONAL_DATA_FILLED

                    requireActivity().onBackPressedDispatcher.onBackPressed()
                //viewModel.finishRegistration()
            }
        }

        viewModel.finishRegistrationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()

                prefsManager.accessToken = it.data.access_token
                prefsManager.refreshToken = it.data.refresh_token

                prefsManager.personalDataSteps = Constants.INVESTMENT_EXP
                prefsManager.portfolioCompletionStep = Constants.PERSONAL_DATA_FILLED
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                childFragmentManager.popBackStackImmediate()
                childFragmentManager.popBackStackImmediate()
                requireActivity().onBackPressed()
            }
        }

        viewModel.verifyEmailResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                prefsManager.personalDataSteps = Constants.EMAIL_VERIFIED
                moveToNext()
            }
        }




        binding.ivTopAction.setOnClickListener(this)
        binding.btnCommon.setOnClickListener(this)

        if (arguments!=null && requireArguments().containsKey(Constants.IS_REVIEW)){
            position = 0
            viewModel.isReview = true
        }else {/*
            position = 0
            viewModel.isReview = true*/
            position =
                when (prefsManager.personalDataSteps) {
                    Constants.ACCOUNT_INITIALIZATION -> 0
                    Constants.PERSONAL_DATA -> 1
                    Constants.EMAIL_ADDRESS -> 1
                    Constants.EMAIL_VERIFIED -> 3
                    Constants.ADDRESS -> 4
                    Constants.INVESTMENT_EXP -> 4
                    else -> 0
                }
        }
        add(R.id.flFillPersonalData, fragmentList[position])



    }

    fun setUpViews(position: Int) {

        /* indicators */
        binding.llIndicators.let {
            for (i in 0 until it.childCount) {
                val child: ImageView = it.getChildAt(i) as ImageView
                if (position == i) child.setImageResource(R.drawable.page_selected_indicator)
                else child.setImageResource(R.drawable.indicator_unselected)
            }
        }

        /* top action image */

        when (position) {
            0 -> binding.ivTopAction.setImageResource(R.drawable.ic_close)
            else -> {
                if (childFragmentManager.backStackEntryCount > 0)
                    binding.ivTopAction.setImageResource(R.drawable.ic_back)
                else binding.ivTopAction.setImageResource(R.drawable.ic_close)
            }
        }

        /* button text view */
        binding.btnCommon.text = when (position) {
            4 -> getString(R.string.send_to_lyber)
            2 -> getString(R.string.email_verified)
            else -> getString(R.string.next)
        }

    }

    private fun moveToNext() {

        if (toEdit.isNotEmpty()) {

            when (position) {
                2 -> {
                    position++
                    if (childFragmentManager.backStackEntryCount == 2) {
                        childFragmentManager.popBackStack()
                    }
                    replace(R.id.flFillPersonalData, fragmentList[position])
                }
                in 0..3 -> {
                    position++
                    replace(R.id.flFillPersonalData, fragmentList[position])
                }
            }

        } else when (position) {

            0 -> {

                position++
//                prefsManager.setProfileInfoSteps(Constants.PERSONAL_DATA)
                replace(R.id.flFillPersonalData, fragmentList[position], false)

            }

            1 -> {

                position++
//                prefsManager.setProfileInfoSteps(Constants.EMAIL_ADDRESS)
               // replace(R.id.flFillPersonalData, fragmentList[position])
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
                vc.mainView = getView()?.rootView as ViewGroup
                vc.viewModel = viewModel
                vc.show(childFragmentManager, "")

                // Add the transparent view to the RelativeLayout
                val mainView = getView()?.rootView as ViewGroup
                mainView.addView(transparentView, viewParams)
            }

            2 -> {

                position++
                childFragmentManager.popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
//                prefsManager.setProfileInfoSteps(Constants.EMAIL_VERIFIED)
                replace(R.id.flFillPersonalData, fragmentList[position], false)

            }

            3 -> {

                position++
//                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//                prefsManager.setProfileInfoSteps(Constants.ADDRESS)
                replace(R.id.flFillPersonalData, fragmentList[position], true)

            }

            4 -> {
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.finishRegistration()
                }
            }

        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnCommon -> buttonClicked(position)
                ivTopAction -> {
                    if (position ==0){
                        requireActivity().finishAffinity()
                        startActivity(Intent(requireActivity(),SplashActivity::class.java)
                            .putExtra(Constants.FOR_LOGOUT,Constants.FOR_LOGOUT))
                    }else{
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
    }

    private fun buttonClicked(position: Int) {

        val fragment = childFragmentManager.findFragmentById(R.id.flFillPersonalData)

        when (position) {

            0 -> (fragment as PersonalDataFragment).let {
                if (it.checkData()) {
                    if (toEdit.isEmpty()) {
                        checkInternet(requireContext()) {
//                                showProgressDialog(requireContext())
//                                viewModel.addPersonalInfo(Constants.PERSONAL_DATA)

                            showProgressDialog(requireContext())
                            viewModel.setUserInfo(
                                viewModel.firstName,
                                viewModel.lastName,
                                viewModel.birthPlace,
                                viewModel.birthDate,
                                viewModel.birthCountry,
                                viewModel.nationality,
                                viewModel.specifiedUsPerson == 1
                            )

                        }
                    } else {
                        checkInternet(requireContext()) {
                            showProgressDialog(requireContext())
                            viewModel.setUserInfo(
                                viewModel.firstName,
                                viewModel.lastName,
                                viewModel.birthPlace,
                                viewModel.birthDate,
                                viewModel.birthCountry,
                                viewModel.nationality,
                                viewModel.specifiedUsPerson == 1
                            )
                        }
                    }
                }
            }

            1 -> (fragment as EmailAddressFragment).let {
                if (it.checkData()) {
                    if (toEdit.isNotEmpty() && viewModel.personalData?.email == viewModel.email) {
                        this.position = 2
                        moveToNext()
                    } else checkInternet(requireContext()) {

                        showProgressDialog(requireContext())

                        val emailSalt = BigInteger(1, generator.generateRandomSalt())
                        val emailVerifier = generator.generateVerifier(
                            emailSalt, viewModel.email, viewModel.password
                        )

                        val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                        val phoneVerifier = generator.generateVerifier(
                            phoneSalt, prefsManager.getPhone(), viewModel.password
                        )

                        viewModel.setEmail(
                            viewModel.email,
                            emailSalt.toString(),
                            emailVerifier.toString(),
                            phoneSalt.toString(),
                            phoneVerifier.toString()
                        )
                    }
                }
            }

            2 -> (fragment as EmailVerificationBottomSheet).let {
                showProgressDialog(requireContext())

                val jwtParser = JWTParser.parse(prefsManager.accessToken)
                val jwtPayload = Gson().fromJson(
                    jwtParser.jwtClaimsSet.toPayload().toString(),
                    JWTPayload::class.java
                )

//                JWTParser.parse(prefsManager.getAccessToken()).jwtClaimsSet.jwtid
//                val jwt = JWT.decodeT<JWSES256Algorithm>(prefsManager.getAccessToken(), JWSES256Algorithm)
                viewModel.verifyEmail(/*jwtPayload.uuid,*/ "1234")
            }

            3 -> (fragment as AddressFragment).let {

                if (it.checkData()) {

                    checkInternet(requireContext()) {
                        showProgressDialog(requireContext())
                        viewModel.setUserAddress(
                            viewModel.streetNumber,
                            viewModel.buildingFloorName,
                            viewModel.city,
                            viewModel.state,
                            viewModel.zipCode,
                            viewModel.country
                        )
                    }

                }
            }

            4 -> (fragment as InvestmentExperienceFragment).let {
                if (it.checkData()) {
                    checkInternet(requireContext()) {
                        showProgressDialog(requireContext())
                        viewModel.setInvestmentExp(
                            viewModel.cryptoExp,
                            viewModel.sourceOfIncome,
                            viewModel.workIndustry,
                            viewModel.annualIncome,
                            viewModel.personalAssets
                        )
//                        viewModel.finishRegistration()
//                        if (toEdit.isNotEmpty()) viewModel.updatePersonalInfo()
//                        else viewModel.addPersonalInfo(Constants.INVESTMENT_EXP)
                    }
                }
            }

        }
    }

    override fun onBackPressed(): Boolean {
        return if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
            position--
            false
        } else true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SplashActivity.activityCallbacks = null
    }

}

