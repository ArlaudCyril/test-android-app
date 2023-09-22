package com.au.lyber.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentVerifyYourIdentityBinding
import com.au.lyber.network.RestClient
import com.au.lyber.ui.activities.WebViewActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.VerifyIdentityViewModel
import okhttp3.ResponseBody


class VerifyYourIdentityFragment : BaseFragment<FragmentVerifyYourIdentityBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError  {
    private lateinit var navController : NavController
    private lateinit var verifyIdentityViewModel: VerifyIdentityViewModel
    private lateinit var portfolioViewModel: PortfolioViewModel
    override fun bind() = FragmentVerifyYourIdentityBinding.inflate(layoutInflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        //optional
        binding.btnContinue.setOnClickListener(this)
        binding.btnReviewMyInformations.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)

        verifyIdentityViewModel = getViewModel(this)
        portfolioViewModel = getViewModel(requireActivity())

        verifyIdentityViewModel.listener = this
        verifyIdentityViewModel.uploadResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
        }
        setObserver()
    }

    private fun setObserver() {
        portfolioViewModel.finishRegistrationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()

                App.prefsManager.accessToken = it.data.access_token
                App.prefsManager.refreshToken = it.data.refresh_token

                App.prefsManager.personalDataSteps = Constants.INVESTMENT_EXP
                App.prefsManager.portfolioCompletionStep = Constants.PERSONAL_DATA_FILLED
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                childFragmentManager.popBackStackImmediate()
                childFragmentManager.popBackStackImmediate()
                requireActivity().onBackPressed()
            }
        }
        portfolioViewModel.kycResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
               dismissAnimation()
                resultLauncher.launch(Intent(requireActivity(),WebViewActivity::class.java)
                    .putExtra(Constants.URL,it.data.url))
            }
        }
    }

    companion object {
        const val NONE: Int = 0
    }

    override fun onClick(v: View?) {
        when(v){
            binding.ivTopAction->{
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            binding.btnContinue->{
                hitAcpi()
            }
            binding.btnReviewMyInformations->{
                val bundle = Bundle().apply {
                    putBoolean(Constants.IS_REVIEW,true)
                }
                findNavController().navigate(R.id.fillDetailFragment,bundle)
            }
        }
    }
    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        dismissAnimation()
    }

    private fun dismissAnimation() {
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        binding.btnContinue.text = getString(R.string.start_identity_verifications)
    }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_FIRST_USER) {
                val bundle = Bundle().apply {
                    putBoolean(Constants.IS_REVIEW,true)
                }
                findNavController().navigate(R.id.fillDetailFragment,bundle)
            }else if (result.resultCode == Activity.RESULT_OK){
                portfolioViewModel.finishRegistration()
            }
        }

    private fun hitAcpi() {
        checkInternet(requireActivity()){
            binding.progress.visible()
            binding.progress.animation =
                AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
            binding.btnContinue.text = ""
            portfolioViewModel.startKyc()
        }

    }
}