package com.au.lyber.ui.fragments

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.au.lyber.databinding.FragmentChooseLanguageBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.Constants
import java.util.Locale


class ChooseLanguageFragment : BaseFragment<FragmentChooseLanguageBinding>(), OnClickListener {
    override fun bind() = FragmentChooseLanguageBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (App.prefsManager.getLanguage().isNotEmpty()) {
           val ln = App.prefsManager.getLanguage()
                if (ln == Constants.FRENCH) {
                    binding.ivFrench.visibility = View.VISIBLE
                    binding.ivEnglish.visibility = View.GONE
                } else {
                    binding.ivEnglish.visibility = View.VISIBLE
                binding.ivFrench.visibility = View.GONE
            }

        }
        binding.ivTopAction.setOnClickListener(this)
        binding.rlEnglish.setOnClickListener(this)
        binding.rlFrench.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                rlEnglish -> {
                    val locale = Locale(Constants.ENGLISH)
                    Locale.setDefault(locale)
                    val resources: Resources = requireActivity().resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                    binding.ivEnglish.visibility = View.VISIBLE
                    binding.ivFrench.visibility = View.GONE
                    App.prefsManager.setLanguage(Constants.ENGLISH)
                }

                rlFrench -> {
                    val locale = Locale(Constants.FRENCH)
                    Locale.setDefault(locale)
                    val resources: Resources = requireActivity().resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                    binding.ivEnglish.visibility = View.GONE
                    binding.ivFrench.visibility = View.VISIBLE
                    App.prefsManager.setLanguage(Constants.FRENCH)

                }
            }
        }
    }


}