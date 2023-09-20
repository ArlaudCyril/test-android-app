package com.au.lyber.ui.fragments

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.BuildConfig
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.DialogSettingBinding
import com.au.lyber.databinding.FragmentVerifyYourIdentityBinding
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.shouldShowPermission
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.strikeText
import com.au.lyber.utils.Constants
import com.au.lyber.utils.Constants.PICTURE_DIRECTORY
import com.au.lyber.viewmodels.VerifyIdentityViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class VerifyYourIdentityFragment : BaseFragment<FragmentVerifyYourIdentityBinding>(),
    View.OnClickListener {
    private lateinit var navController : NavController
    private lateinit var verifyIdentityViewModel: VerifyIdentityViewModel
    private lateinit var portfolioViewModel: PortfolioViewModel

    private var imageFile: File? = null
    override fun bind() = FragmentVerifyYourIdentityBinding.inflate(layoutInflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        //optional
        binding.btnOpenSend.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)

        verifyIdentityViewModel = getViewModel(this)
        portfolioViewModel = getViewModel(requireActivity())

        verifyIdentityViewModel.listener = this
        verifyIdentityViewModel.uploadResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
        }
        stateOne()
    }

    private fun stateOne() {

        binding.btnOpenSend.text = "Start verification"

        binding.tvOne.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_drawable)?.apply {
                setTint(getColor(requireContext(), R.color.purple_gray_50))
            }
        binding.tvOne.text = "1"

        binding.tvTwo.text = "2"
        binding.tvTwo.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_drawable)?.apply {
                setTint(getColor(requireContext(), R.color.purple_gray_50))
            }
    }

    private fun stateTwo() {

        binding.btnOpenSend.text = getString(R.string.next)
        binding.tvTitlePapers.strikeText()
        binding.tvSubTitlePapers.strikeText()
        binding.tvTakeSelfie.strikeText()

        binding.tvTitlePapers.setTextColor(getColor(requireContext(), R.color.purple_gray_500))
        binding.tvTakeSelfie.setTextColor(getColor(requireContext(), R.color.purple_gray_500))

        binding.tvOne.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.drawable_circle_checked)
        binding.tvOne.text = ""

        binding.tvTwo.text = ""
        binding.tvTwo.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.drawable_circle_checked)
    }

    private fun setUpUI(type: Int) {
        binding.apply {
            when (type) {

                ONE -> {
                    tvTitlePapers.setTextColor(getColor(requireContext(), R.color.purple_gray_500))
                    tvTitlePapers.strikeText()
                    tvSubTitlePapers.strikeText()
                }

                TWO -> {
                    tvTitlePapers.setTextColor(getColor(requireContext(), R.color.purple_gray_500))
                    tvTakeSelfie.setTextColor(getColor(requireContext(), R.color.purple_gray_500))
                    tvTitlePapers.strikeText()
                    tvSubTitlePapers.strikeText()
                    tvTakeSelfie.strikeText()
                    btnOpenSend.text = getString(R.string.send_to_lyber)
                }

                else -> {}

            }
        }
    }

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (imageFile != null) {

                    verifyIdentityViewModel.let {
                        when (it.imageCaptured) {
                            NONE -> {
                                imageFile?.let { image ->
                                    checkInternet(requireContext()) {
                                        showProgressDialog(requireContext())
                                        it.paperPhoto = image.toString()
                                        verifyIdentityViewModel.upload(image)
                                        it.imageCaptured += 1
                                    }
                                }
                            }
                            ONE -> {
                                imageFile?.let { image ->
                                    checkInternet(requireContext()) {
                                        it.selfiePhoto = image.toString()
                                        showProgressDialog(requireContext())
                                        verifyIdentityViewModel.upload(image)
                                        it.imageCaptured += 1
                                    }
                                }
                            }
                            else -> {}
                        }

                        setUpUI(it.imageCaptured)
                    }

                }
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            val isCamera = it[CAMERA] == true
            val doWrite = it[WRITE_EXTERNAL_STORAGE] == true
            val doRead = it[READ_EXTERNAL_STORAGE] == true


            when {

                // open camera all permissions are granted
                isCamera && doWrite && doRead -> {
                    openCamera()
                }

                !isCamera -> if (!requireActivity().shouldShowPermission(CAMERA)) {
                    permissionDeniedDialog(
                        getString(R.string.permissions_required),
                        getString(R.string.openSettings)
                    )
                }

                !doWrite && !doRead -> {
                    if (!requireActivity().shouldShowPermission(WRITE_EXTERNAL_STORAGE)
                        && !requireActivity().shouldShowPermission(READ_EXTERNAL_STORAGE)
                    ) {
                        permissionDeniedDialog(
                            getString(R.string.permissions_required),
                            getString(R.string.openSettings)
                        )
                    }
                }


            }


        }

    private fun permissionDeniedDialog(title: String, message: String) {
        Dialog(requireContext()).apply {
            DialogSettingBinding.inflate(layoutInflater).let {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitleSettings.text = title
                it.tvDialogMsgSettings.text = message
                it.btnSettings.setOnClickListener {
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
                it.btnCancel.setOnClickListener { dismiss() }
                window?.let { window ->
                    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val layoutParams = WindowManager.LayoutParams()
                    layoutParams.copyFrom(window.attributes)
                    layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                    window.attributes = layoutParams
                }
                show()
            }
        }
    }

    private fun openCamera() {

        val intent = Intent(ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null)

        createImageFile()?.let {

            val uri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                it
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.putExtra(
                "android.intent.extras.CAMERA_FACING",
                0
            )
            if (verifyIdentityViewModel.imageCaptured == ONE) {
                when {
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> {
                        intent.putExtra(
                            "android.intent.extras.CAMERA_FACING",
                            CameraCharacteristics.LENS_FACING_FRONT
                        )  // Tested on API 24 Android version 7.0(Samsung S6)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        intent.putExtra(
                            "android.intent.extras.CAMERA_FACING",
                            1
                        ) // Tested on API 27 Android version 8.0(Nexus 6P)
                        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                    }
                    else -> intent.putExtra(
                        "android.intent.extras.CAMERA_FACING",
                        android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
                    )
                }
            }
            activityLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name

        try {
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "LYBER_IDENTITY_" + timeStamp + "_"
//            Environment.DIRECTORY_PICTURES +
            val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/" + PICTURE_DIRECTORY
            )
            storageDir.mkdirs()
            imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
            )
        } catch (e: Exception) {
            imageFile = null
        }

        return imageFile
    }

    private fun dialogVerify(title: String, message: String) {

        Dialog(requireContext(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitle.text = title
                it.tvMessage.text = message
                it.tvNegativeButton.setTextColor(requireContext().getColor(R.color.red_500))
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.ok)
                it.tvNegativeButton.setOnClickListener { dismiss() }
                it.tvPositiveButton.setOnClickListener {
                    stateTwo()
                    dismiss()
                }
                show()
            }
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnOpenSend -> {
                    if (btnOpenSend.text == "Next") {
                        App.prefsManager.portfolioCompletionStep = Constants.PROFILE_COMPLETED
                        requireActivity().clearBackStack()
                        navController.navigate(R.id.portfolioHomeFragment)
                    } else
                        dialogVerify(
                            getString(R.string.verify_identity),
                            "It will open Unable link to verify your identity."
                        )




//                    if (verifyIdentityViewModel.imageCaptured != TWO) {
//                        if (!requireActivity().checkPermission(CAMERA) || !requireActivity().checkPermission(
//                                CAMERA
//                            ) || !requireActivity().checkPermission(READ_EXTERNAL_STORAGE)
//                        ) {
//                            permissionLauncher.launch(
//                                arrayOf(
//                                    CAMERA, WRITE_EXTERNAL_STORAGE,
//                                    READ_EXTERNAL_STORAGE
//                                )
//                            )
//                        } else openCamera()
//                    } else {
//                        portfolioViewModel.identityDocument = verifyIdentityViewModel.paperPhoto
//                        portfolioViewModel.selfieImage = verifyIdentityViewModel.selfiePhoto
//                        portfolioViewModel.verificationInitiated = true
//                        requireActivity().onBackPressed()
//                    }


                }
                ivTopAction -> requireActivity().onBackPressed()
            }
        }
    }

    companion object {
        private const val TAG = "VerifyYourIdentityFragment"
        const val NONE: Int = 0
        const val ONE: Int = 1
        const val TWO: Int = 2
    }
}