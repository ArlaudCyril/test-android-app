package com.Lyber.ui.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.Lyber.models.AssetBaseData
import com.Lyber.models.Balance
import com.Lyber.models.PriceServiceResume

abstract class BaseActivity<viewBinding : ViewBinding> : AppCompatActivity() {

    //Global variables
    companion object {
        var assets = ArrayList<AssetBaseData>()
        var balances = ArrayList<Balance>()
        var balanceResume = ArrayList<PriceServiceResume>()
        var selectedLanguage=""

    }

    private var _binding: viewBinding? = null
    val binding get() = _binding!!

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
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}