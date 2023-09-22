package com.au.lyber.ui.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import com.au.lyber.R
import com.au.lyber.databinding.ActivityWebViewBinding
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.utils.CommonMethods.Companion.checkPermission
import com.au.lyber.utils.CommonMethods.Companion.shouldShowPermission
import com.au.lyber.utils.Constants

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {
    override fun bind() = ActivityWebViewBinding.inflate(layoutInflater)
    private val TAG = "WebViewFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.webView.webViewClient = WebClient()
        //binding.webView.webChromeClient = ChromeClient(this)
        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            builtInZoomControls = true
            allowFileAccess = true
            setSupportZoom(true)
        }
        binding.webView.loadUrl(intent.getStringExtra(Constants.URL)!!)
    }
    inner class WebClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            //checkAndRequest()
            Log.d(TAG, "onPageStarted: $url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let {
                if (it.contains("https://lyber.com/kyc")) {

                }

                /*
                *
                * else if (it.contains("https://id.ubble.ai")) {
                    postDelay(1000) {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                * */
            }
            Log.d(TAG, "onPageFinished: $url")
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
//            Log.d(TAG, "onLoadResource: $url")
        }


    }
    inner class ChromeClient(private val fragmentActivity: Activity) : WebChromeClient() {

        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.let {
                if (!fragmentActivity.checkPermission(Manifest.permission.CAMERA) || !fragmentActivity.checkPermission(
                        Manifest.permission.CAMERA
                    ) || !fragmentActivity.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                ) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                } else
                    for (i in it.resources) {
                        it.grant(it.resources)
                    }
            }
//            super.onPermissionRequest(request)
        }
    }
    private fun checkAndRequest() {
        if (!checkPermission(Manifest.permission.CAMERA) || !checkPermission(
                Manifest.permission.CAMERA
            ) || !checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            val isCamera = it[Manifest.permission.CAMERA] == true
            val doWrite = it[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            val doRead = it[Manifest.permission.READ_EXTERNAL_STORAGE] == true


            when {

                // open camera all permissions are granted
                isCamera && doWrite && doRead -> {
                }

                !isCamera -> if (!shouldShowPermission(Manifest.permission.CAMERA)) {
                    permissionDeniedDialog(
                        getString(R.string.permission_required),
                        getString(R.string.openSettings)
                    )
                } else requestPermission()

                !doWrite && !doRead -> {
                    if (!shouldShowPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && !shouldShowPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ) {
                        permissionDeniedDialog(
                            getString(R.string.permission_required),
                            getString(R.string.openSettings)
                        )
                    } else requestPermission()
                }


            }


        }
    private fun permissionDeniedDialog(title: String, message: String) {

        Dialog(this, R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitle.text = title
                it.tvMessage.text = message
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.setting)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                    onBackPressedDispatcher.onBackPressed()
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts(
                            "package",
                            packageName,
                            null
                        )
                    )
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activityLauncher.launch(intent)

                }
                show()
            }
        }

    }
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkAndRequest()
        }
}