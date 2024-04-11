package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentChooseLanguageBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.NetworkViewModel
import java.util.Locale


class ChooseLanguageFragment : BaseFragment<FragmentChooseLanguageBinding>(), OnClickListener {
    override fun bind() = FragmentChooseLanguageBinding.inflate(layoutInflater)
    private lateinit var viewModel: NetworkViewModel
    val hashMap = HashMap<String, Any>()

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(this)
        viewModel.listener = this

        viewModel.updateUserInfoResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (hashMap["language"] == Constants.ENGLISH) {
                    val locale = Locale(Constants.ENGLISH)
                    Locale.setDefault(locale)
                    val resources: Resources = requireActivity().resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                    binding.ivEnglish.visibility = View.VISIBLE
                    binding.ivFrench.visibility = View.INVISIBLE
                    App.prefsManager.setLanguage(Constants.ENGLISH)
                    binding.tvTitle.text=getString(R.string.language)
                } else if (hashMap["language"] == Constants.FRENCH) {
                    val locale = Locale(Constants.FRENCH)
                    Locale.setDefault(locale)
                    val resources: Resources = requireActivity().resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                    binding.ivEnglish.visibility = View.INVISIBLE
                    binding.ivFrench.visibility = View.VISIBLE
                    App.prefsManager.setLanguage(Constants.FRENCH)
                    binding.tvTitle.text=getString(R.string.language)
                }
                App.prefsManager.user?.language = hashMap["language"].toString()

            }
        }

        if (App.prefsManager.getLanguage().isNotEmpty()) {
            val ln = App.prefsManager.getLanguage()
            if (ln == Constants.FRENCH) {
                binding.ivFrench.visibility = View.VISIBLE
                binding.ivEnglish.visibility = View.INVISIBLE
            } else {
                binding.ivEnglish.visibility = View.VISIBLE
                binding.ivFrench.visibility = View.INVISIBLE
            }

        }else{
            val ln = CommonMethods.getDeviceLocale(requireContext())
            if (ln == Constants.FRENCH) {
                binding.ivFrench.visibility = View.VISIBLE
                binding.ivEnglish.visibility = View.INVISIBLE
            } else {
                binding.ivEnglish.visibility = View.VISIBLE
                binding.ivFrench.visibility = View.INVISIBLE
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
                    hashMap.clear()
                    hashMap["language"] = Constants.ENGLISH
                    CommonMethods.checkInternet(requireContext()) {
                        CommonMethods.showProgressDialog(requireContext())
                        viewModel.updateUserInfo(hashMap)
                    }
                }

                rlFrench -> {
                    hashMap.clear()
                    hashMap["language"] = Constants.FRENCH
                    CommonMethods.checkInternet(requireContext()) {
                        CommonMethods.showProgressDialog(requireContext())
                        viewModel.updateUserInfo(hashMap)
                    }

                }
            }
        }
    }


}