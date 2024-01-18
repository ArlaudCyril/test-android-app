package com.Lyber.ui.activities

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.Lyber.databinding.ActivityWebViewBinding
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.Constants

class WebViewActivity : com.Lyber.ui.activities.BaseActivity<ActivityWebViewBinding>() {
    override fun bind() = ActivityWebViewBinding.inflate(layoutInflater)
    private val TAG = "WebViewFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.webView.webViewClient = WebClient()
        binding.webView.webChromeClient = ChromeClient(this)
        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            builtInZoomControls = true
            allowFileAccess = true
            setSupportZoom(true)
        }
        binding.btnReviewMyInformations.setOnClickListener {
            setResult(Activity.RESULT_FIRST_USER)
            finish()
        }
        binding.webView.loadUrl(intent.getStringExtra(Constants.URL)!!)
    }
    inner class WebClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            Log.d(TAG, "onPageStarted: $url")
            if (url!!.startsWith("https://preprod.id360docaposte.com/static/process_ui/index.html#/enrollment")){
                binding.btnReviewMyInformations.gone()
              //  checkAndRequest()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let {
                if (url.contains("https://www.lyber.com/kyc-finished?action=OK&mode=SIGN&userAction=OK")){
                    setResult(Activity.RESULT_OK)
                    finish()
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
                it.grant(request.resources)
                Log.d("test420",request.resources.toString())


            }
     //    super.onPermissionRequest(request)
        }
    }






}