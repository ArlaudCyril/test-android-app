package com.au.lyber.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentCodeScannerBinding
import com.au.lyber.utils.Constants.SCANNED_ADDRESS
import com.au.lyber.utils.Constants.SCAN_COMPLETE
import com.budiyev.android.codescanner.*

class CodeScannerFragment : BaseFragment<FragmentCodeScannerBinding>() {

    private lateinit var codeScanner: CodeScanner

    override fun bind(): FragmentCodeScannerBinding {
        return FragmentCodeScannerBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

        codeScanner = CodeScanner(requireContext(), binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {

            requireActivity().runOnUiThread {

                requireActivity().sendBroadcast(Intent(SCAN_COMPLETE).apply {
                    putExtra(SCANNED_ADDRESS, it.text.toString())
                })
                requireActivity().onBackPressedDispatcher.onBackPressed()

            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
//            requireActivity().runOnUiThread {
//                Toast.makeText(
//                    requireContext(), "Camera initialization error: ${it.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//               BottomSheetFragment().show(requireActivity().supportFragmentManager,null)
//            }
        }

    }


    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun checkPermissions() {
        if (requireContext()?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED || requireContext()?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED || requireContext()?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Request Permissions")
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        } else {

            Log.d("TAG2", "Permission Already Granted")
        }

    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("TAG", "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                Log.d("TAG", "Permission granted")
            } else {
                if (permissions[Manifest.permission.CAMERA] == false) {
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.CAMERA
                        )
                    ) {

                    } else {
                        permissionDeniedDialog()
                    }

                }
//                else if(permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == false
//                    && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == false ){
//                    if (shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        && shouldShowRequestPermissionRationale( Manifest.permission.READ_EXTERNAL_STORAGE)
//                    ) {
//
//                    } else {
//
//                        permissionsDenied()
//                    }
//                }

                Log.d("TAG", "Permission not granted")

            }
        }

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

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
//                CommonMethods.showToastMessage(this,"Call")
                Log.e("RESULT", "$result")

            }
        }


}