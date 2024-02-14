package com.Lyber.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentWebViewBinding
import com.Lyber.utils.CommonMethods.Companion.checkPermission
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.postDelay
import com.Lyber.utils.CommonMethods.Companion.shouldShowPermission
import com.Lyber.viewmodels.VerifyIdentityViewModel

class WebViewFragment : BaseFragment<FragmentWebViewBinding>() {

    private var url: String = ""
    private lateinit var viewModel: VerifyIdentityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString("url", "")
        }
    }

    override fun bind() = FragmentWebViewBinding.inflate(layoutInflater)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(this)
        viewModel.listener = this
        binding.webView.webViewClient = WebClient()
        binding.webView.webChromeClient = ChromeClient(requireActivity())

        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            builtInZoomControls = false
            allowFileAccess = false
            setSupportZoom(false)
        }
        binding.webView.loadUrl(url)

    }

    private fun checkAndRequest() {
        if (!requireActivity().checkPermission(Manifest.permission.CAMERA) || !requireActivity().checkPermission(
                Manifest.permission.CAMERA
            ) || !requireActivity().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
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

                !isCamera -> if (!requireActivity().shouldShowPermission(Manifest.permission.CAMERA)) {
                    permissionDeniedDialog(
                        getString(R.string.permission_required),
                        getString(R.string.openSettings)
                    )
                } else requestPermission()

                !doWrite && !doRead -> {
                    if (!requireActivity().shouldShowPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && !requireActivity().shouldShowPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
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

        Dialog(requireContext(), R.style.DialogTheme).apply {
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
                    requireActivity().onBackPressed()
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts(
                            "package",
                            requireActivity().packageName,
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


    inner class ChromeClient(private val fragmentActivity: FragmentActivity) : WebChromeClient() {

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

    inner class WebClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            checkAndRequest()
            Log.d(TAG, "onPageStarted: $url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let {
                if (it.contains("https://lyber.com/kyc")) {
                    postDelay(1000) {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
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


    companion object {
        private const val TAG = "WebViewFragment"
        fun get(url: String): WebViewFragment {
            return WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        "url", url
                    )
                }
            }
        }
    }

//    override fun onBackPressed(): Boolean {
//        return if (binding.webView.canGoBack()) {
//            binding.webView.goBack()
//            false
//        } else true
//    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}