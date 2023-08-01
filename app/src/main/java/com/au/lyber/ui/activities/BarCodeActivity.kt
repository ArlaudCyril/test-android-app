package com.au.lyber.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.au.lyber.R
import com.au.lyber.databinding.ActivityBarcodingBinding
import com.au.lyber.utils.Constants
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class BarCodeActivity : BaseActivity<ActivityBarcodingBinding>() {
    override fun bind() = ActivityBarcodingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TranslucentStatusBar)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding.ivTopAction.setOnClickListener {
            onBackPressed()
        }
        setBarcode()
    }

    private fun setBarcode() {
        try {
            val barcodeEncoder =  BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(intent.getStringExtra(Constants.DATA_SELECTED), BarcodeFormat.QR_CODE, 500, 500)
            binding.ivBarcode.setImageBitmap(bitmap);
        } catch(e:Exception) {
            e.printStackTrace()
        }
    }
}