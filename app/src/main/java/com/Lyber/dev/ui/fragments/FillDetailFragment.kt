package com.Lyber.dev.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentTestFillDetailBinding
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.utils.ActivityCallbacks
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.App.Companion.prefsManager
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.add
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.replace
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PersonalDataViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import org.json.JSONObject

class FillDetailFragment : BaseFragment<FragmentTestFillDetailBinding>(), View.OnClickListener,
    ActivityCallbacks {

    var position = 0

    private val fragmentList
        get() = listOf(
//            PersonalDataFragment(),
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

        com.Lyber.dev.ui.activities.SplashActivity.activityCallbacks = this

        viewModel.listener = this

        /* new Apis */

        viewModel.setUserInfoResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
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
                dismissProgressDialog()
                prefsManager.portfolioCompletionStep = Constants.PERSONAL_DATA_FILLED
                findNavController().popBackStack()
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




        binding.ivTopActionClear.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        binding.btnCommon.setOnClickListener(this)

        if (arguments != null && requireArguments().containsKey(Constants.IS_REVIEW)) {
            position = 0
            viewModel.isReview = true
        } else {/*
            position = 0
            viewModel.isReview = true*/
            position =
                when (prefsManager.personalDataSteps) {
                    Constants.ACCOUNT_INITIALIZATION -> 0
//                    Constants.PERSONAL_DATA -> 1
                    Constants.ADDRESS -> 1
                    Constants.INVESTMENT_EXP -> 2
                    else -> 0
                }
        }
        add(R.id.flFillPersonalData, fragmentList[position])


    }

    fun setUpViews(pos: Int) {

        /* indicators */
        binding.llIndicators.let {
            for (i in 0 until it.childCount) {
                val child: ImageView = it.getChildAt(i) as ImageView
                if (pos == i) child.setImageResource(R.drawable.page_selected_indicator)
                else child.setImageResource(R.drawable.indicator_unselected)
            }
        }

        /* top action image */

        when (pos) {
            0 -> binding.ivTopAction.gone()
            else -> {
                binding.ivTopAction.visible()
            }
        }

        /* button text view */
        binding.btnCommon.text = when (pos) {
            3 -> getString(R.string.send_to_lyber)
            else -> getString(R.string.next)
        }

    }

    private fun moveToNext() {

        if (toEdit.isNotEmpty()) {

            when (position) {
                1 -> {
                    position++
                    if (childFragmentManager.backStackEntryCount == 1) {
                        childFragmentManager.popBackStack()
                    }
                    replace(R.id.flFillPersonalData, fragmentList[position])
                }

                in 0..2 -> {
                    position++
                    replace(R.id.flFillPersonalData, fragmentList[position])
                }
            }

        } else when (position) {

            0 -> {

                position++
                replace(R.id.flFillPersonalData, fragmentList[position], true)

            }

            1 -> {

                position++
                childFragmentManager.popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                replace(R.id.flFillPersonalData, fragmentList[position], false)
            }

            2 -> {

                position++
                replace(R.id.flFillPersonalData, fragmentList[position], true)

            }

            3 -> {
                //TODO check if in use ornot
//                checkInternet(binding.root,requireContext()) {
//                    showProgressDialog(requireContext())
//                    viewModel.finishRegistration()
//                }
            }

        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnCommon -> buttonClicked(position)
                ivTopAction -> {
                    if (position == 0) {
                        requireActivity().finishAffinity()
                        startActivity(
                            Intent(
                                requireActivity(),
                                com.Lyber.dev.ui.activities.SplashActivity::class.java
                            )
                                .putExtra(Constants.FOR_LOGOUT, Constants.FOR_LOGOUT)
                        )
                    } else {
                        requireActivity().onBackPressed()
                    }
                }

                ivTopActionClear -> {
                    stopRegistrationDialog()

                }
            }
        }
    }

    fun hitSetUser() {
        val hashMap = hashMapOf<String, Any>()
        if (viewModel.streetNumber.isNotEmpty())
            hashMap["streetNumber"] = viewModel.streetNumber
//            if (street.isNotEmpty())
        hashMap["street"] = viewModel.street.toString()
        hashMap["city"] = viewModel.city
        hashMap["zipCode"] = viewModel.zipCode
        hashMap["country"] = viewModel.country
        hashMap["isUSCitizen"] = false
        showProgressDialog(requireActivity())
        viewModel.setUserAddress(
            hashMap
        )

    }

    private fun buttonClicked(position: Int) {

        val fragment = childFragmentManager.findFragmentById(R.id.flFillPersonalData)

        when (position) {
            0 -> (fragment as AddressFragment).let {
                if (it.checkData()) {
                    val hashMap = hashMapOf<String, Any>()
                    if (App.prefsManager.getLanguage().isNotEmpty())
                        hashMap["language"] = App.prefsManager.getLanguage()
                    else {
                        val configuration = requireContext().resources.configuration.locales[0]
                        if (configuration.language.uppercase() == Constants.FRENCH)
                            hashMap["language"] = Constants.FRENCH
                        else
                            hashMap["language"] = Constants.ENGLISH
                    }
                       showProgressDialog(requireActivity())
                        viewModel.setUserInfo(hashMap)
                        hitSetUser()

                }
            }

            1 -> (fragment as InvestmentExperienceFragment).let {
                if (it.checkData()) {
                    checkInternet(binding.root, requireContext()) {
                         showProgressDialog(requireContext())
                            viewModel.setInvestmentExp(
                                viewModel.cryptoExp,
                                viewModel.sourceOfIncome,
                                viewModel.workIndustry,
                                viewModel.annualIncome,
                                viewModel.personalAssets
                            )

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
        com.Lyber.dev.ui.activities.SplashActivity.activityCallbacks = null
    }
}


