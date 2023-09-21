package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentVerifyYourIdentityBinding
import com.au.lyber.network.RestClient
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
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
        binding.btnStartKyc.setOnClickListener(this)
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

    }

    companion object {
        const val NONE: Int = 0
    }

    override fun onClick(v: View?) {
        when(v){
            binding.ivTopAction->{
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            binding.btnStartKyc->{
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
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        binding.btnContinue.text = getString(R.string.start_identity_verifications)
    }

    private fun hitAcpi() {
        binding.progress.visible()
        binding.progress.animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
        binding.btnContinue.text = ""
        portfolioViewModel.startKyc()

    }
}