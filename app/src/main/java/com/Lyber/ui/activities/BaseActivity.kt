
package com.Lyber.ui.activities

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.Lyber.models.AssetBaseData
import com.Lyber.models.Balance
import com.Lyber.models.Network
import com.Lyber.models.PriceServiceResume
import com.Lyber.models.RIBData
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.viewmodels.PortfolioViewModel
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
        viewModel1.getUser()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}
