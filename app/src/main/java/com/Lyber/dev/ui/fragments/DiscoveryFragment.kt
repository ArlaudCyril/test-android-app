package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentDiscoveryBinding
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import kotlinx.coroutines.launch
import java.util.Locale

class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>(), OnClickListener {
    override fun bind() = FragmentDiscoveryBinding.inflate(layoutInflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("argument", "OnDiscovery")
        if (App.prefsManager.getLanguage().isNotEmpty()) {
            when (App.prefsManager.getLanguage()) {
                Constants.ENGLISH -> binding.ivLanguage.setImageResource(R.drawable.flag_british)
                Constants.FRENCH -> binding.ivLanguage.setImageResource(R.drawable.flag_france)
            }
        } else {
            val configuration =
                resources.configuration.locales[0]
            if (configuration.language.uppercase() == Constants.FRENCH) {
                binding.ivLanguage.setImageResource(R.drawable.flag_france)
                App.prefsManager.setLanguage(Constants.FRENCH)
            } else {
                binding.ivLanguage.setImageResource(R.drawable.flag_british)
                App.prefsManager.setLanguage(Constants.ENGLISH)
            }

        }
        binding.btnSignUp.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.ivLanguage.setOnClickListener(this)
        binding.tvEnglish.setOnClickListener(this)
        binding.tvFrench.setOnClickListener(this)


        binding.root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Hide cvRoot when the root layout is touched
                    binding.cvLanguage.visibility = View.GONE
                    true
                }
                else -> false
            }
        }
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                btnSignUp -> {
//                    val bundle = Bundle().apply {
//                        putBoolean(Constants.FOR_LOGIN, false)
//                    }
//                    if ( App.prefsManager.portfolioCompletionStep !=-1) {
                    findNavController().navigate(R.id.completePortfolioFragment)
//                    } else {
////                findNavController().navigate(R.id.createAccountFragment, bundle)
//                        findNavController().navigate(R.id.completePortfolioFragment)
//                    }
                }

                tvLogin -> {
                    val bundle = Bundle().apply {
                        putBoolean(Constants.FOR_LOGIN, true)
                    }
                    findNavController().navigate(R.id.createAccountFragment, bundle)
                }

                ivLanguage -> {
                    if (binding.cvLanguage.isVisible)
                        binding.cvLanguage.gone()
                    else
                        binding.cvLanguage.visible()
                }

                tvEnglish -> {
                    ivLanguage.setImageResource(R.drawable.flag_british)
                    cvLanguage.gone()
                    App.prefsManager.setLanguage(Constants.ENGLISH)
                    val code = App.prefsManager.getLanguage()
                    val locale = Locale(code)
                    Locale.setDefault(locale)
                    val resources: Resources = resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                    setTexts()

                }

                tvFrench -> {
                    ivLanguage.setImageResource(R.drawable.flag_france)
                    cvLanguage.gone()
                    App.prefsManager.setLanguage(Constants.FRENCH)
                    val code = App.prefsManager.getLanguage()
                    val locale = Locale(code)
                    Locale.setDefault(locale)
                    val resources: Resources = resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                    setTexts()
                }
            }

        }
    }

    private fun setTexts() {
        binding.apply {
            tvLogin.text = getString(R.string.log_in)
            tvTitle.text = getString(R.string.lyber_investments_reinvented)
            tvSubTitle.text = getString(R.string.diversified_regular_simple)
            btnSignUp.text = getString(R.string.sign_up_in_5)
        }
    }
}


