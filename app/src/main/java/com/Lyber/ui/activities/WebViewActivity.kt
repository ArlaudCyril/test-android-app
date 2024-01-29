package com.Lyber.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.Lyber.R
import com.Lyber.databinding.ActivityWebViewBinding
import com.Lyber.utils.Constants

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {
    override fun bind() = ActivityWebViewBinding.inflate(layoutInflater)
    private val TAG = "WebViewActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.webView.webViewClient = WebClient()
        binding.webView.webChromeClient = ChromeClient(this)
        checkAndRequest()
        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            builtInZoomControls = true
            allowFileAccess = true
            setSupportZoom(true)
            allowContentAccess = true
            displayZoomControls = false
        }
        binding.btnReviewMyInformations.setOnClickListener {
            setResult(RESULT_FIRST_USER)
            finish()
        }
        binding.webView.loadUrl(intent.getStringExtra(Constants.URL)!!)
        //dummyUrl
//        binding.webView.loadUrl("https://marcusbelcher.github.io/wasm-asm-camera-webgl-test/index.html")
    }

    inner class WebClient : WebViewClient() {
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.e("WebView", "Error: ${error?.description}")
            super.onReceivedError(view, request, error)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            Log.d(TAG, "onPageStarted: $url")
//            if (url!!.startsWith("https://preprod.id360docaposte.com/static/process_ui/index.html#/enrollment")){
//                   startActivity( Intent(this@WebViewActivity, WebViewTwoActivity::class.java)
//                       .putExtra(Constants.URL,url))
//                binding.btnReviewMyInformations.gone()
//            }
        }


        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
//            url?.let {
//                if (url.contains("https://www.lyber.com/kyc-finished?action=OK&mode=SIGN&userAction=OK")){
//                    setResult(Activity.RESULT_OK)
//                    finish()
//                }
//            }
            Log.d(TAG, "onPageFinished: $url")
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }


    }

    inner class ChromeClient(private val fragmentActivity: Activity) : WebChromeClient() {

        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.let {
                it.grant(request.resources)
                Log.d("test420", request.resources.toString())

            }
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest?) {
            Log.d("test420", request!!.resources.toString())
            super.onPermissionRequestCanceled(request)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.d("WebView", "${consoleMessage?.message()}")
            return true
        }

    }

    private fun checkAndRequest() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED

                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED

            ) {

                requestMultiplePermissions.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                )

            } else {
                Log.d("TAG2", "Permission Already Granted")
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED

                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED

                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestMultiplePermissions.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )

            } else {
                Log.d("TAG2", "Permission Already Granted")
            }
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            permissions.entries.forEach {
                Log.d("TAG", "${it.key} = ${it.value}")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (permissions[android.Manifest.permission.CAMERA] == true
                    && permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true
                    && permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == true
                ) {
                    Log.d("requestMultiplePermissions", "Permission granted")
                    // isPermissionGranted=true
                } else {

                    if (permissions[android.Manifest.permission.CAMERA] == false) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.CAMERA
                            )
                        ) {
                        } else permissionsDenied()

                    } else if (permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == false) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_MEDIA_VIDEO
                            )
                        ) {

                        } else permissionsDenied()
                    } else if (permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == false) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_MEDIA_IMAGES
                            )
                        ) {
                        } else permissionsDenied()

                    }

                    Log.d("requestMultiplePermissions", "Permission not granted")

                }

            } else {

                if (permissions[android.Manifest.permission.CAMERA] == true && permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                    Log.d("requestMultiplePermissions", "Permission granted")
                    // isPermissionGranted=true

                } else {
                    if (permissions[android.Manifest.permission.CAMERA] == false) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.CAMERA
                            )
                        ) {

                        } else {
                            permissionsDenied()
                        }

                    } else if (permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == false
                        && permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == false
                    ) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            && ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        ) {

                        } else {

                            permissionsDenied()
                        }
                    }

                    Log.d("requestMultiplePermissions", "Permission not granted")

                }

            }

        }

    private var alertDialogRational: AlertDialog? = null
    private fun permissionsDenied() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogRational?.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(this, R.color.black))

        alertDialogRational = alertDialogBuilder.setTitle("Permissions Required")
            .setMessage(
                "Please open settings, go to permissions and allow them."
            )
            .setPositiveButton(
                "Settings"
            ) { _, _ ->

                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts(
                        "package", packageName,
                        null
                    )
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                activityResult.launch(intent)
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ -> }
            .setCancelable(false)
            .create()
        alertDialogRational!!.apply {
            show()
            getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this@WebViewActivity, R.color.black))
            getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this@WebViewActivity, R.color.black))
        }
    }


}