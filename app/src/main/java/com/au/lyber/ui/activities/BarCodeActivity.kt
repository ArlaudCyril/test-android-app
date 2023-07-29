package com.au.lyber.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.au.lyber.R
import com.au.lyber.databinding.ActivityBarcodingBinding

class BarCodeActivity : BaseActivity<ActivityBarcodingBinding>() {
    override fun bind() = ActivityBarcodingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TranslucentStatusBar)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding.ivTopAction.setOnClickListener {
            onBackPressed()
        }
    }
}