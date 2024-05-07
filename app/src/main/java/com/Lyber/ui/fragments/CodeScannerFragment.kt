package com.Lyber.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentCodeScannerBinding
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants.SCANNED_ADDRESS
import com.Lyber.utils.Constants.SCAN_COMPLETE
import com.budiyev.android.codescanner.*
import com.google.android.material.snackbar.Snackbar
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig

class CodeScannerFragment : BaseFragment<FragmentCodeScannerBinding>() {

    private lateinit var codeScanner: CodeScanner

    override fun bind(): FragmentCodeScannerBinding {
        return FragmentCodeScannerBinding.inflate(layoutInflater)
    }

    private var selectedBarcodeFormat = BarcodeFormat.FORMAT_QR_CODE

    val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::handleResult)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()


        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
//        scanCustomCode.launch(
//            ScannerConfig.build {
//                setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE)) // set interested barcode formats
//                setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
//                setShowTorchToggle(false) // show or hide (default) torch/flashlight toggle button
//                setShowCloseButton(true) // show or hide (default) close button
//                setHorizontalFrameRatio(1f) // set the horizontal overlay ratio (default is 1 / square frame)
//                setUseFrontCamera(false) // use the front camera
//                setOverlayStringRes(R.string.empty) // string resource used for the scanner overlay
//                setOverlayDrawableRes(null) // drawable resource used for the scanner overlay
//
//            }
//        )
        codeScanner = CodeScanner(requireContext(), binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

//        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {

            requireActivity().runOnUiThread {
                Log.d("text", "${trimAddress(it.text)}")
                requireActivity().sendBroadcast(Intent(SCAN_COMPLETE).apply {
                    putExtra(SCANNED_ADDRESS, trimAddress(it.text))
                })
                requireActivity().onBackPressedDispatcher.onBackPressed()

            }
        }
//
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            Log.d("textError", "$it")
            it.showToast(requireContext())
//            requireActivity().runOnUiThread {
//                Toast.makeText(
//                    requireContext(), "Camera initialization error: ${it.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//               BottomSheetFragment().show(requireActivity().supportFragmentManager,null)
//            }
        }

    }

    fun handleResult(result: QRResult) {
        Log.d("result", "$result")
    }

    fun trimAddress(code: String): String {
        var address = code
        val index = code.indexOf(":")
        if (index != -1) {
            val intIndex = index + 1
            address = code.substring(intIndex)
        }
        return address
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

//    private fun checkPermissions() {
//        if (requireContext().let {
//                ContextCompat.checkSelfPermission(
//                    it,
//                    Manifest.permission.CAMERA
//                )
//            } != PackageManager.PERMISSION_GRANTED || requireContext().let {
//                ContextCompat.checkSelfPermission(
//                    it,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//            } != PackageManager.PERMISSION_GRANTED || requireContext().let {
//                ContextCompat.checkSelfPermission(
//                    it,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                )
//            } != PackageManager.PERMISSION_GRANTED) {
//            Log.d("TAG", "Request Permissions")
//            requestMultiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                )
//            )
//        } else {
//
//            Log.d("TAG2", "Permission Already Granted")
//        }
//
//    }


    var firstTimeGallery = false
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
            } else {
                if (firstTimeGallery)
                    permissionDeniedDialog()
                // Directly ask for the permission
                if (!firstTimeGallery)
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                    )
                firstTimeGallery = true
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
            } else {
                if (firstTimeGallery)
                    permissionDeniedDialog()
                // Directly ask for the permission
                if (!firstTimeGallery)
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                firstTimeGallery = true
            }
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            permissions.entries.forEach {
                Log.d("TAG", "${it.key} = ${it.value}")
            }
                if (permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
                    && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                ) {
                    Log.d("requestMultiplePermissions", "Permission granted")
                    // isPermissionGranted=true
//                        binding.ivOpenGallery.performClick()

                } else {
                    Log.d("requestMultiplePermissions", "Permission not granted")

                }

        }

//    private val requestMultiplePermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            permissions.entries.forEach {
//                Log.d("TAG", "${it.key} = ${it.value}")
//            }
//            if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
//                Log.d("TAG", "Permission granted")
//            } else {
//                if (permissions[Manifest.permission.CAMERA] == false) {
//                    if (shouldShowRequestPermissionRationale(
//                            Manifest.permission.CAMERA
//                        )
//                    ) {
//
//                    } else {
//                        permissionDeniedDialog()
//                    }
//
//                }
////                else if(permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == false
////                    && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == false ){
////                    if (shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE)
////                        && shouldShowRequestPermissionRationale( Manifest.permission.READ_EXTERNAL_STORAGE)
////                    ) {
////
////                    } else {
////
////                        permissionsDenied()
////                    }
////                }
//
//                Log.d("TAG", "Permission not granted")
//
//            }
//        }

    private fun permissionDeniedDialog() {

        Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.permission_required)
                it.tvMessage.text = getString(R.string.message_permission_required)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.setting)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                show()
            }
        }
    }


}