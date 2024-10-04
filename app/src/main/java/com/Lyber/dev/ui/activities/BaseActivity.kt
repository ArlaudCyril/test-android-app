package com.Lyber.dev.ui.activities

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.Network
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.models.RIBData
import com.Lyber.dev.models.RIBResponse
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import java.util.Locale

abstract class BaseActivity<viewBinding : ViewBinding> : AppCompatActivity() {

    //Global variables
    companion object {
        var assets = ArrayList<AssetBaseData>()
        var networkAddress = ArrayList<Network>()
        var balances = ArrayList<Balance>()
        var balanceResume = ArrayList<PriceServiceResume>()
        var ribWalletList = ArrayList<RIBData>()
        var selectedLanguage = ""

    }

    private var _binding: viewBinding? = null
    val binding get() = _binding!!
    private lateinit var viewModel1: PortfolioViewModel

    abstract fun bind(): viewBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        _binding = bind()
        setContentView(binding.root)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bind()
        setContentView(binding.root)
        viewModel1 = CommonMethods.getViewModel(this)
        viewModel1.getUserResponse.observe(this) {
            try {
                App.prefsManager.user = it.data


                if (it.data.language.isNotEmpty()) {
                    App.prefsManager.setLanguage(it.data.language)
                    val locale = Locale(it.data.language)
                    Locale.setDefault(locale)
                    val resources: Resources = resources
                    val config: Configuration = resources.configuration
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)
                }
            } catch (_: Exception) {

            }

        }
    }

    fun getUser1() {
        val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
            SplashActivity.integrityTokenProvider?.request(
                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                    .build()
            )
        integrityTokenResponse?.addOnSuccessListener { response ->
            viewModel1.getUser(response.token())
        }?.addOnFailureListener { exception ->
            Log.d("token", "${exception}")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}