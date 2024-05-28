//package com.Lyber.dev.ui.fragments
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Dialog
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import android.view.View
//import android.view.Window
//import android.webkit.PermissionRequest
//import android.webkit.WebChromeClient
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.fragment.app.FragmentActivity
//import com.Lyber.dev.R
//import com.Lyber.dev.databinding.CustomDialogLayoutBinding
//import com.Lyber.dev.databinding.FragmentWebViewBinding
//import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
//import com.Lyber.dev.utils.CommonMethods.Companion.postDelay
//import com.Lyber.dev.viewmodels.VerifyIdentityViewModel
//
//class WebViewFragment : BaseFragment<FragmentWebViewBinding>() {
//
//    private var url: String = ""
//    private lateinit var viewModel: VerifyIdentityViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            url = it.getString("url", "")
//        }
//    }
//
//    override fun bind() = FragmentWebViewBinding.inflate(layoutInflater)
//
//    @SuppressLint("SetJavaScriptEnabled")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel = getViewModel(this)
//        viewModel.listener = this
//        binding.webView.webViewClient = WebClient()
//        binding.webView.webChromeClient = ChromeClient(requireActivity())
//
//        with(binding.webView.settings) {
//            javaScriptEnabled = true
//            javaScriptCanOpenWindowsAutomatically = true
//            mediaPlaybackRequiresUserGesture = false
//            domStorageEnabled = true
//            builtInZoomControls = false
//            allowFileAccess = false
//            setSupportZoom(false)
//        }
//        binding.webView.loadUrl(url)
//
//    }
//
//
//    inner class ChromeClient(private val fragmentActivity: FragmentActivity) : WebChromeClient() {
//
////        override fun onPermissionRequest(request: PermissionRequest?) {
////            request?.let {
////                if (!fragmentActivity.checkPermission(Manifest.permission.CAMERA) || !fragmentActivity.checkPermission(
////                        Manifest.permission.CAMERA
////                    ) || !fragmentActivity.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
////                ) {
////                    permissionLauncher.launch(
////                        arrayOf(
////                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
////                            Manifest.permission.READ_EXTERNAL_STORAGE
////                        )
////                    )
////                } else
////                    for (i in it.resources) {
////                        it.grant(it.resources)
////                    }
////            }
//////            super.onPermissionRequest(request)
////        }
//    }
//
//    inner class WebClient : WebViewClient() {
//
//        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//            super.onPageStarted(view, url, favicon)
//             Log.d(TAG, "onPageStarted: $url")
//        }
//
//        override fun onPageFinished(view: WebView?, url: String?) {
//            super.onPageFinished(view, url)
//            url?.let {
//                if (it.contains("https://lyber.com/kyc")) {
//                    postDelay(1000) {
//                        requireActivity().supportFragmentManager.popBackStack()
//                    }
//                }
//
//                /*
//                *
//                * else if (it.contains("https://id.ubble.ai")) {
//                    postDelay(1000) {
//                        requireActivity().supportFragmentManager.popBackStack()
//                    }
//                }
//                * */
//            }
//            Log.d(TAG, "onPageFinished: $url")
//        }
//
//        override fun onLoadResource(view: WebView?, url: String?) {
//            super.onLoadResource(view, url)
////            Log.d(TAG, "onLoadResource: $url")
//        }
//
//    }
//
//    companion object {
//        private const val TAG = "WebViewFragment"
//        fun get(url: String): WebViewFragment {
//            return WebViewFragment().apply {
//                arguments = Bundle().apply {
//                    putString(
//                        "url", url
//                    )
//                }
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//    }
//
//}