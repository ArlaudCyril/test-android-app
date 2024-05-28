package com.Lyber.dev.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.ProgressBarBinding

object LoaderObject {
    private var dialog: Dialog? = null

    fun showLoader(context: Context) {
        if (dialog == null || (dialog != null && !dialog!!.isShowing)) {
            dialog = Dialog(context)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog!!.window!!.setDimAmount(0.2F)
            dialog!!.setCancelable(false)
            dialog!!.setContentView(ProgressBarBinding.inflate(LayoutInflater.from(context)).root)

            try {
                dialog?.findViewById<ImageView>(R.id.progressImage)?.animation =
                    AnimationUtils.loadAnimation(context, R.anim.rotate_drawable)
                dialog!!.show()
                AppLifeCycleObserver.progressDialogVisible=true
            } catch (e: Exception) {
                Log.d("Exception", "showProgressDialog: ${e.message}")
                dialog?.dismiss()
                dialog = null
            }
            if (context is Activity && !context.isFinishing) {
                dialog?.show()
            }
        }
        
    }

    fun hideLoader() {
        try {
            dialog?.dismiss()
            dialog = null
        }catch (_:Exception){}

    }
}