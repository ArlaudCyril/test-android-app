package com.Lyber.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentTestFillDetailBinding
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.utils.ActivityCallbacks
import com.Lyber.utils.App.Companion.prefsManager
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.add
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.replace
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.PersonalDataViewModel
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity

class FillDetailFragment : BaseFragment<FragmentTestFillDetailBinding>(), View.OnClickListener,
    ActivityCallbacks {

    var position = 0

    private val fragmentList
        get() = listOf(
            PersonalDataFragment(),
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

        com.Lyber.ui.activities.SplashActivity.activityCallbacks = this

        viewModel.listener = this

        /* new Apis */

        viewModel.setUserInfoResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                prefsManager.personalDataSteps = Constants.PERSONAL_DATA
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
                    Constants.PERSONAL_DATA -> 1
                    Constants.ADDRESS -> 2
                    Constants.INVESTMENT_EXP -> 3
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
            0 -> binding.ivTopAction.gone()
            else -> {
                binding.ivTopAction.visible()
            }
        }

        /* button text view */
        binding.btnCommon.text = when (position) {
            3 -> getString(R.string.send_to_lyber)
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
                replace(R.id.flFillPersonalData, fragmentList[position], false)

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
                childFragmentManager.popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                replace(R.id.flFillPersonalData, fragmentList[position], false)

            }

            3 -> {

                position++
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
                    if (position == 0) {
                        requireActivity().finishAffinity()
                        startActivity(
                            Intent(
                                requireActivity(),
                                com.Lyber.ui.activities.SplashActivity::class.java
                            )
                                .putExtra(Constants.FOR_LOGOUT, Constants.FOR_LOGOUT)
                        )
                    } else {
                        requireActivity().onBackPressed()
                    }
                }

                ivTopActionClear -> {
                    showLogoutDialog()

                }
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

                it.tvTitle.text = getString(R.string.stop_reg)
                it.tvMessage.text = getString(R.string.reg_message)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.ok)

                it.tvNegativeButton.setOnClickListener { dismiss() }

                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    prefsManager.logout()
                    requireActivity().supportFragmentManager.popBackStack()


                    CommonMethods.checkInternet(requireContext()) {
                        dismiss()
                        CommonMethods.showProgressDialog(requireContext())
                        viewModel.logout(CommonMethods.getDeviceId(requireActivity().contentResolver))
                    }


                }

                show()

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


            1 -> (fragment as AddressFragment).let {

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

            2 -> (fragment as InvestmentExperienceFragment).let {
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
        com.Lyber.ui.activities.SplashActivity.activityCallbacks = null
    }
}


