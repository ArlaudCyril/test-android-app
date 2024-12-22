package com.Lyber.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Lyber.R
import com.Lyber.databinding.FragmentQRCodeBinding
import com.Lyber.utils.App
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter


class QRCodeFragment : BaseFragment<FragmentQRCodeBinding>() {
    override fun bind()= FragmentQRCodeBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var qrText="tel:${App.prefsManager.user?.phoneNo!!.replace("+","")}"
        Log.d("TEL","$qrText")
        binding.ivQrCode.setImageBitmap(
            getQrCodeBitmap(
                qrText,
                768
            )
        )
        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun getQrCodeBitmap(link: String, size: Int): Bitmap {
        val size = size //pixels
        val qrCodeContent = "$link"
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.TRANSPARENT)
                }
            }
        }
    }
}