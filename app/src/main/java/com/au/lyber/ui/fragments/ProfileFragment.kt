package com.au.lyber.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.BuildConfig
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentProfileBinding
import com.au.lyber.databinding.ItemMyAssetBinding
import com.au.lyber.databinding.ItemTransactionBinding
import com.au.lyber.models.TransactionData
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.ui.fragments.bottomsheetfragments.TransactionDetailsBottomSheetFragment
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.checkPermission
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getDeviceId
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.saveImageToExternalStorage
import com.au.lyber.utils.CommonMethods.Companion.setProfile
import com.au.lyber.utils.CommonMethods.Companion.shouldShowPermission
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.google.gson.GsonBuilder
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : BaseFragment<FragmentProfileBinding>(), View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapter: TransactionAdapter

    private var imageFile: File? = null
    private var option = 1 // 1 -> camera option 2-> gallery option
    val limit = 5 // as on this screen we have to show max 3 enteries
    var offset = 0
    override fun bind() = FragmentProfileBinding.inflate(layoutInflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this


        CommonMethods.checkInternet(requireContext()) {
           binding.progressImage.animation =
            AnimationUtils.loadAnimation(context, R.anim.rotate_drawable)
            viewModel.getTransactions(limit, offset)
        }
        viewModel.getTransactionListingResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                binding.tvNoTransaction.setBackgroundResource(0)
                binding.progressImage.clearAnimation()
                binding.progressImage.visibility=View.GONE
                when {
                    it.data.isEmpty() -> {
                        binding.tvNoTransaction.visible()
                        binding.rvTransactions.gone()
                        binding.tvViewAllTransaction.gone()
                    }
                    it.data.count() in 1..3 -> {
                        adapter.setList(it.data)
                        binding.tvViewAllTransaction.visible()
                        binding.tvNoTransaction.gone()
                    }
                    else -> {
                        adapter.setList(it.data.subList(0, 3))
                        binding.tvViewAllTransaction.visible()
                        binding.tvNoTransaction.gone()
                    }
                }

                binding.rvTransactions.startLayoutAnimation()
            }
        }

        adapter = TransactionAdapter(requireActivity())

        binding.rvTransactions.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
        }

        App.prefsManager.user?.let {
            binding.tvName.text = "${it.firstName} ${it.lastName}"
            binding.tvEmail.text = it.email
//            binding.tvName.text = "${it.first_name} ${it.last_name}"
//            binding.tvEmail.text = "${it.email}"
//            binding.switchFaceId.isChecked = it.is_face_id_enabled == 1
//            if (it.bic != null && it.iban != null) {
//                binding.llBankInfo.visible()
//                binding.tvIban.text = "${it.iban}"
//                binding.tvBic.text = "${it.bic}"
//                binding.tvAddPaymentMethod.gone()
//            }
        }

        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            App.prefsManager.logout()
            startActivity(Intent(requireActivity(), SplashActivity::class.java).apply {
                putExtra("fromLogout", "fromLogout")
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }

        viewModel.otpPinChangeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                requireActivity().replaceFragment(R.id.flSplashActivity, UpdatePinFragment())
            }
        }

        viewModel.faceIdResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
//                App.prefsManager.setFaceIdEnabled(App.prefsManager.user?.is_face_id_enabled == 0)
//                binding.switchFaceId.isChecked = App.prefsManager.user?.is_face_id_enabled == 1
            }
        }

        viewModel.uploadResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.setProfileImage(it.s3Url)
                checkInternet(requireContext()) {
                    viewModel.updateUser(hashMapOf("profile_pic" to it.s3Url))
                }
            }
        }

        viewModel.updateUser.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
//                App.prefsManager.defaultImage = -1
                binding.ivProfile.setProfile
            }
        }


//        binding.tvStatusStrongAuth.text =
//            if (App.prefsManager.isStrongAuth()) "Enabled" else "Disabled"

        binding.tvStatusAddressBook.gone()
        binding.tvStatusAddressBook.text = when (App.prefsManager.withdrawalLockSecurity) {
            Constants.HOURS_72 -> "72H"
            Constants.HOURS_24 -> "24H"
            else -> "No Security"
        }
        binding.ivTopAction.setOnClickListener(this)
        binding.llChangePin.setOnClickListener(this)
        binding.tvViewAllTransaction.setOnClickListener(this)
//        binding.tvAddPaymentMethod.setOnClickListener(this)
        binding.tvLogout.setOnClickListener(this)

        binding.llStrongAuthentication.setOnClickListener(this)
//        binding.rlAddressBook.setOnClickListener(this)

        binding.ivProfile.setOnClickListener(this)
//        binding.llNotification.setOnClickListener(this)

        binding.switchFaceId.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.setFaceId(getDeviceId(requireActivity().contentResolver), isChecked)
                }
            } else {

            }
        }

//        viewModel.transactionResponse.value?.let {
//            dismissProgressDialog()
//
//            when {
//                it.transactions.isEmpty() -> {
//                    binding.tvNoTransaction.visible()
//                    binding.rvTransactions.gone()
//                    binding.tvViewAllTransaction.gone()
//                }
//
//                it.transactions.count() in 1..3 -> {
//                    adapter.setList(it.transactions)
//                    binding.tvViewAllTransaction.visible()
//                    binding.tvNoTransaction.gone()
//                }
//
//                else -> {
//                    adapter.setList(it.transactions.subList(0, 3))
//                    binding.tvViewAllTransaction.visible()
//                    binding.tvNoTransaction.gone()
//                }
//            }
//
//            binding.rvTransactions.startLayoutAnimation()
//        }


    }

    override fun onResume() {
        super.onResume()
        binding.ivProfile.setProfile
    }


    private fun optionSelected(option: Int) {
        when (option) {
            1 -> openCamera()
            2 -> openGallery()
            else ->
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    DefaultImagesFragment(),
                    topBottom = true
                )

        }
    }

    private fun showLogoutDialog() {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.log_out)
                it.tvMessage.text = getString(R.string.logout_message)
                it.tvNegativeButton.text = getString(R.string.no)
                it.tvPositiveButton.text = getString(R.string.yes)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                it.tvPositiveButton.setOnClickListener {

                    App.prefsManager.logout()
                    startActivity(Intent(requireActivity(), SplashActivity::class.java).apply {
                        putExtra("fromLogout", "fromLogout")
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                    /*checkInternet(requireContext()) {
                        dismiss()
                        showProgressDialog(requireContext())
                        viewModel.logout(getDeviceId(requireActivity().contentResolver))
                    }*/
                }
                show()
            }
        }
    }


    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivProfile -> findNavController().navigate(R.id.defaultImagesFragment)/*ProfileBottomSheet(::optionSelected).show(childFragmentManager, "")*/

                rlAddressBook -> findNavController().navigate(R.id.addAddressBookFragment)

                llStrongAuthentication -> findNavController().navigate(R.id.strongAuthentication)
//                    requireActivity().replaceFragment(
//                    R.id.flSplashActivity,
//                    StrongAuthenticationFragment()
//                )

                ivTopAction -> requireActivity().onBackPressed()

                llNotification -> requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    NotificationFragment(),
                    topBottom = true
                )


                tvLogout -> showLogoutDialog()

                tvAddPaymentMethod -> requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AddPaymentMethodFragment()
                )

                tvViewAllTransaction ->
                    findNavController().navigate(R.id.transactionFragment)

                llChangePin -> checkInternet(requireContext()) {
                    val bundle = Bundle().apply {
                        putBoolean(Constants.FOR_LOGIN, false)
                        putBoolean(Constants.IS_CHANGE_PIN, true)
                    }
                    findNavController().navigate(R.id.createPinFragment, bundle)
                }
            }
        }
    }

    inner class TransactionAdapter(private val context: Context) : BaseAdapter<TransactionData>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return TransactionViewHolder(
                ItemTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as TransactionViewHolder).binding.apply {
                itemList[position]?.let {
                    when (it.type) {
                        Constants.ORDER -> {
                            ivItem.setImageResource(R.drawable.ic_exchange)
                            tvStartTitle.text = "Exchange"
                            tvStartSubTitle.text =
                                "${it.fromAsset.uppercase()} to ${it.toAsset.uppercase()}"
                            var roundedNumber=BigDecimal(it.fromAmount)
                            try {
                                val originalNumber = BigDecimal(it.fromAmount)
                                val scale = originalNumber.scale()
                                roundedNumber = if (scale > 8) {
                                    originalNumber.setScale(8, RoundingMode.HALF_UP)
                                } else
                                    originalNumber
                            }catch (_:Exception){

                            }
                            tvEndTitle.text = "-${roundedNumber}${it.fromAsset.uppercase()}"
//                            tvEndTitle.text = "-${it.fromAmount} ${it.fromAsset.uppercase()}"
                            var amount = it.toAmount
                            try {
                                amount = String.format(Locale.US,"%.10f", it.toAmount.toFloat())
                            } catch (ex: java.lang.Exception) {

                            }
                            tvEndSubTitle.text = "+${amount} ${it.toAsset.uppercase()}"


                        }

                        Constants.STRATEGY -> {
                            ivItem.setImageResource(R.drawable.strategy)
                            tvStartTitleCenter.text =
                                "${it.strategyName.replaceFirstChar(Char::uppercase)}"
                            if (it.status == Constants.FAILURE)
                                tvFailed.visibility = View.VISIBLE
                            else {
                                if (it.successfulBundleEntries.isNotEmpty()) {
                                    try {
                                        tvEndTitleCenter.text =
                                            "${it.successfulBundleEntries[0].assetAmount} ${it.successfulBundleEntries[0].asset.uppercase(Locale.US)}"

                                    } catch (ex: java.lang.Exception) {

                                    }
                                }
                            }

                        }

                        Constants.DEPOSIT -> {
                            ivItem.setImageResource(R.drawable.ic_deposit)
                            tvStartTitle.text = "${it.asset.uppercase()} Deposit"
                            tvStartSubTitle.text =
                                it.status.lowercase().replaceFirstChar(Char::uppercase)
                            tvEndTitleCenter.text = "+${it.amount} ${it.asset.uppercase()}"
                        }

                        Constants.WITHDRAW -> { // single asset
                            //TODO
                            ivItem.setImageResource(R.drawable.ic_withdraw)
                            tvStartTitle.text =
                                "${it.asset.uppercase()} ${getString(R.string.withdrawal)}"
                            tvStartSubTitle.text =
                                it.status.lowercase().replaceFirstChar(Char::uppercase)
                            tvEndTitleCenter.text = "-${it.amount} ${it.asset.uppercase()}"
                            tvFailed.visibility = View.GONE
                            tvStartTitleCenter.visibility = View.GONE


//                            tvEndTitle.text = it.type.lowercase().replaceFirstChar(Char::uppercase)
//                            tvEndSubTitle.text = it.type.lowercase().replaceFirstChar(Char::uppercase)
                        }

                        else -> root.gone()
                    }
                }
            }
        }

        inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {

                    val detailBottomSheet = TransactionDetailsBottomSheetFragment()
                    val gson = GsonBuilder().create()
                    var data = ""
                    data = gson.toJson(itemList[adapterPosition])
                    detailBottomSheet.arguments = Bundle().apply {
                        putString("data", data)
                    }
                    detailBottomSheet.show(childFragmentManager, "")
                }
//                binding.ivDropIcon.gone()
//                binding.tvAssetName.visible()
            }
        }

    }


    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                when (option) {
                    1 -> imageFile?.let { image ->
                        checkInternet(requireContext()) {
                            showProgressDialog(requireContext())
                            viewModel.upload(image)
                        }
                    }

                    2 -> {
                        it.data?.data?.let { uri ->


                            try {

                                requireContext().contentResolver.query(
                                    uri, arrayOf(MediaStore.Images.Media.DATA),
                                    null,
                                    null,
                                    null
                                )?.let { cursor ->

                                    cursor.moveToFirst()
                                    val columnIndex =
                                        cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                                    val picturePath = cursor.getString(columnIndex)

                                    val bitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().contentResolver,
                                        uri
                                    )

                                    imageFile = saveImageToExternalStorage(bitmap, requireContext())

                                    checkInternet(requireContext()) {
                                        showProgressDialog(requireContext())
                                        viewModel.upload(imageFile!!)
                                    }
//                                        it.toFile("profile_image_${System.currentTimeMillis()}")
//                                            ?.let { file ->
//                                                checkInternet(requireContext()) {
//                                                    showProgressDialog(requireContext())
//                                                    viewModel.upload(file)
//                                                }
//                                            }

                                    cursor.close()
                                }

                            } catch (e: IllegalArgumentException) {
                                getString(R.string.file_not_found).showToast(requireContext())
                            } catch (e: Exception) {
                                getString(R.string.error_occurred).showToast(requireContext())
                            }
                        }
                    }

                    else -> {

                    }
                }
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            val isCamera = it[Manifest.permission.CAMERA] == true
            val doWrite = it[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            val doRead = it[Manifest.permission.READ_EXTERNAL_STORAGE] == true


            when {

                // open camera all permissions are granted
                isCamera && doWrite && doRead -> {
                    if (option == 1) openCamera() else openGallery()
                }

                !isCamera -> if (!requireActivity().shouldShowPermission(Manifest.permission.CAMERA)) {
                    permissionDeniedDialog()
                }

                !doWrite && !doRead -> {
                    if (!requireActivity().shouldShowPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && !requireActivity().shouldShowPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ) {
                        permissionDeniedDialog()
                    }
                }


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

    private fun openCamera() {

        option = 1
        if (requireActivity().checkPermission(Manifest.permission.CAMERA) &&
            requireActivity().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
            requireActivity().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {

            createImageFile()?.let {

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                intent.putExtra(
//                    "android.intent.extras.CAMERA_FACING",
//                    0
//                )
                /* if (verifyIdentityViewModel.imageCaptured == VerifyYourIdentityFragment.ONE) {
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
             }*/
                activityLauncher.launch(intent)
            }
        } else permissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
    }

    private fun openGallery() {
        option = 2
        if (requireActivity().checkPermission(Manifest.permission.CAMERA) ||
            requireActivity().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            requireActivity().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        )
            Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                activityLauncher.launch(Intent.createChooser(this, "Select Picture"))
            }
        else permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun createImageFile(): File? {
        // Create an image file name

        try {
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "LYBER_IDENTITY_" + timeStamp + "_"
//            Environment.DIRECTORY_PICTURES +
            val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/" + Constants.PICTURE_DIRECTORY
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

}