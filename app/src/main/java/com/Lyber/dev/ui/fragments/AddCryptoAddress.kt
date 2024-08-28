package com.Lyber.dev.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ListPopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import com.Lyber.dev.R
import com.Lyber.dev.databinding.AppItemLayoutBinding
import com.Lyber.dev.databinding.CustomDialogLayoutBinding
import com.Lyber.dev.databinding.FragmentAddBitcoinAddressBinding
import com.Lyber.dev.databinding.LoaderViewBinding
import com.Lyber.dev.models.Network
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.addressMatched
import com.Lyber.dev.utils.CommonMethods.Companion.checkFormat
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.fadeIn
import com.Lyber.dev.utils.CommonMethods.Companion.fadeOut
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.ProfileViewModel
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig

class AddCryptoAddress : BaseFragment<FragmentAddBitcoinAddressBinding>(), View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel
    private var toEdit: Boolean = false
    private var originSelectedPosition = 0
    private var networkId: String = ""
    private var isFromWithdraw = false

    /* network popup */
    private var network: Network? = null
    private lateinit var networkAdapter: NetworkPopupAdapter
    private lateinit var networkPopup: ListPopupWindow

    private var selectedBarcodeFormat = BarcodeFormat.FORMAT_QR_CODE

    val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::handleResult)

    private val origin get() = if (originSelectedPosition == 0) "EXCHANGE" else "WALLET"
    private val addressName: String get() = binding.etAddressName.text.trim().toString()
    private val address: String
        get() = binding.etAddress.text?.trim().toString()

    override fun bind() = FragmentAddBitcoinAddressBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireActivity().registerReceiver(
                broadcastReceiver,
                IntentFilter(Constants.SCAN_COMPLETE),
                RECEIVER_NOT_EXPORTED
            )
        else
            requireActivity().registerReceiver(
                broadcastReceiver,
                IntentFilter(Constants.SCAN_COMPLETE)
            )

        arguments?.let {
            toEdit = it.getBoolean(TO_EDIT, false)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                Handler(Looper.getMainLooper()).postDelayed({
                    val address: String = intent?.getStringExtra(Constants.SCANNED_ADDRESS) ?: ""
                    binding.etAddress.setText(address)
                }, 50)
            } catch (ex: Exception) {

            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        if (arguments != null) {
            arguments.let {
                if (requireArguments().containsKey(Constants.ID)) {
                    networkId = requireArguments().getString(Constants.ID)!!
                    isFromWithdraw = requireArguments().getBoolean(Constants.ACTION_WITHDRAW)
                }
            }
        }

        /*  changing ui according to view cases */

        binding.tvTitle.text = getString(R.string.add_address)
        binding.ivNetwork.gone()
        binding.etNetwork.hint = getString(R.string.choose_network)

        if (isFromWithdraw || toEdit) {
            binding.llNetwork.background =
                getDrawable(requireContext(), R.drawable.rec_solid_gray_100)
        }
        if (isFromWithdraw) {
            binding.btnAddUseAddress.text = getString(R.string.add_and_use_this_address)
            binding.tvTitle.text = getString(R.string.add_a_address, networkId.uppercase())
        }
        if (toEdit) {
            binding.tvTitle.text = getString(R.string.edit_address)
            binding.btnAddUseAddress.text = getString(R.string.edit_address)
            binding.etAddress.background =
                getDrawable(requireContext(), R.drawable.rec_solid_gray_100)
        }

        /* observers */
        viewModel.singleNetworkResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val format = address.addressMatched(it.data.addressRegex)
                if (format) {
//                    Log.d("network","$network")
                    viewModel.addAddress(
                        addressName,
                        network?.id ?: "",
                        address,
                        origin,
                        binding.etExchange.text.toString() ?: "",
                        network?.imageUrl ?: ""
                    )
                } else {
                    CommonMethods.dismissProgressDialog()
                    binding.etAddress.requestKeyboard()
                    binding.ttlAddress.helperText = getString(R.string.please_enter_valid_address)
                    getString(R.string.please_enter_valid_address).showToast(binding.root,requireContext())

                }
            }
        }

        viewModel.networkResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if (networkPopup.isShowing) {
                    networkAdapter.removeProgress()
                    networkAdapter.setData(it.data)
                    networkPopup.show()
                } else networkAdapter.setData(it.data)


                if (network != null) {
                    for (data in it.data) {
                        if (data.id == network!!.id) {
                            network = data
                            binding.tvTitle.fadeIn()
                            binding.ivNetwork.visible()
                            binding.etNetwork.updatePadding(0)
                            binding.etNetwork.setText(data.fullName + " (" + data.id.uppercase() + ")")

                            binding.ivNetwork.loadCircleCrop(data.imageUrl)
                            break
                        }
                    }
                }
                if (isFromWithdraw) {
                    for (data in it.data) {
                        if (data.id == networkId) {
                            network = data
                            binding.tvTitle.fadeIn()
                            binding.ivNetwork.visible()
                            binding.etNetwork.updatePadding(0)
                            binding.etNetwork.setText(data.fullName + " (" + data.id.uppercase() + ")")

                            binding.ivNetwork.loadCircleCrop(data.imageUrl)
                            break
                        }
                    }
                }
            }
        }



        viewModel.addWhitelistResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if (isFromWithdraw)
                    WithdrawAmountFragment.newAddress = true

                requireActivity().onBackPressedDispatcher.onBackPressed()
                //infoBottomSheet.dismiss()
            }
        }

        viewModel.updateWhiteList.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        /* click listeners */

        binding.ivTopAction.setOnClickListener(this)
        binding.btnAddUseAddress.setOnClickListener(this)
        binding.llOriginExchange.setOnClickListener(this)
        binding.llOriginWallet.setOnClickListener(this)
        binding.etNetwork.setOnClickListener(this)
        binding.etExchange.setOnClickListener(this)
        binding.etAddress.setOnTouchListener(onDrawableClickListener)

        /* network pop up */
        networkAdapter = NetworkPopupAdapter()
        networkPopup = ListPopupWindow(requireContext())
        networkPopup.anchorView = binding.llNetwork
        networkPopup.setAdapter(networkAdapter)

        networkPopup.setOnItemClickListener { _, _, position, _ ->
            networkAdapter.getItemAt(position)?.let {

                // change ui
                network = it
                Log.d("NetworkC", "$it")
                binding.tvTitle.fadeIn()
                binding.ivNetwork.visible()
                binding.etNetwork.updatePadding(0)
                binding.etNetwork.setText(it.fullName + " (" + it.id.uppercase() + ")")

                binding.ivNetwork.loadCircleCrop(it.imageUrl)

                networkPopup.dismiss()
            }
        }


        /* exchange pop up */


        setOrigin(originSelectedPosition)

        if (toEdit) {
            viewModel.whitelistAddress?.let {
                binding.etAddress.isEnabled = false
                binding.etAddress.setText("${it.address}")
                binding.etAddressName.setText(it.name)
                binding.etNetwork.setText(it.network)
                if (it.exchange != null)
                    binding.etExchange.setText(it.exchange)
                val id = it.network
                network = Network(id = it.network!!)
                originSelectedPosition = if (it.origin == "exchange") 0 else 1
                com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == id } }
                    ?.let { it1 ->
                        binding.ivNetwork.loadCircleCrop(it1.imageUrl)

                    }


                binding.ivNetwork.visible()
                setOrigin(originSelectedPosition)
            }
        }

        checkInternet(binding.root,requireContext()) {
            if (toEdit) {
                showProgressDialog(requireContext())
            }
            viewModel.getNetworks()
        }

    }


    private fun setOrigin(position: Int) {
        when (position) {
            0 -> {

                binding.llOriginExchange.background =
                    getDrawable(requireContext(), R.drawable.round_stroke_purple_500)
                binding.ivRadioExchange.setImageResource(R.drawable.radio_select)

                binding.llOriginWallet.background =
                    getDrawable(requireContext(), R.drawable.round_stroke_gray_100)
                binding.ivRadioWallet.setImageResource(R.drawable.radio_unselect)
                binding.tvWallet.setTextColor(getColor(requireContext(), R.color.purple_gray_800))

                binding.etExchange.fadeIn()
                binding.tvTitleSelectExchange.fadeIn()
            }

            else -> {

                binding.llOriginWallet.background =
                    getDrawable(requireContext(), R.drawable.round_stroke_purple_500)
                binding.ivRadioWallet.setImageResource(R.drawable.radio_select)
                //binding.tvWallet.setTextColor(getColor(requireContext(), R.color.purple_500))

                binding.llOriginExchange.background =
                    getDrawable(requireContext(), R.drawable.round_stroke_gray_100)
                binding.ivRadioExchange.setImageResource(R.drawable.radio_unselect)
                binding.tvExchange.setTextColor(getColor(requireContext(), R.color.purple_gray_800))

                binding.etExchange.fadeOut()
                binding.tvTitleSelectExchange.fadeOut()

            }
        }
    }

    class NetworkPopupAdapter : android.widget.BaseAdapter() {

        private val list = mutableListOf<Network?>()

        fun getItemAt(position: Int): Network? {
            return list[position]
        }

        fun hasNoData(): Boolean {
            return list.isEmpty()
        }

        fun addProgress() {
            list.add(null)
            notifyDataSetChanged()
        }

        fun removeProgress() {
            list.last()?.let {
                list.remove(it)
                notifyDataSetChanged()
            }
        }

        fun setData(items: List<Network?>) {
            list.clear()
            list.addAll(items)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return list.count()
        }

        override fun getItem(position: Int): Any? {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }


        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (list[position] == null)

                LoaderViewBinding.inflate(LayoutInflater.from(parent?.context), parent, false).let {
                    it.ivLoader.animation =
                        AnimationUtils.loadAnimation(it.ivLoader.context, R.anim.rotate_drawable)
                    return it.root
                }
            else
                AppItemLayoutBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
                    .let {

                        list[position]?.let { data ->


                            it.ivItem.loadCircleCrop(data.imageUrl)

                            it.tvStartTitleCenter.text =
                                "${data.fullName} (${data.id.uppercase()})"

                            return it.root
                        }

                        return it.root
                    }
        }


    }


    override fun onDestroyView() {
        if (networkPopup.isShowing)
            networkPopup.dismiss()
        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnAddUseAddress -> {
                    when {


                        addressName.isEmpty() -> {
                            binding.etAddressName.requestKeyboard()
                            getString(R.string.please_enter_address_name).showToast(binding.root,requireContext())
                        }

                        network == null -> {
                            binding.etNetwork.requestFocus()
                            getString(R.string.please_select_a_network).showToast(binding.root,requireContext())
                        }

                        address.isEmpty() -> {
                            binding.etAddress.requestKeyboard()
                            binding.ttlAddress.helperText =
                                getString(R.string.please_enter_address, network?.fullName ?: "")
                            getString(R.string.please_enter_address_).showToast(binding.root,requireContext())
                        }


                        binding.etExchange.text.toString().isEmpty() -> {

                            if (originSelectedPosition == 0) {
                                getString(R.string.please_select_a_exchange).showToast(
                                    binding.root, requireContext()
                                )
                            } else {
                                addAddress()
                            }
                        }

                        else -> {
                            addAddress()
                        }
                    }
                }

                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()

                llOriginExchange -> {
                    originSelectedPosition = 0
                    setOrigin(originSelectedPosition)
                }

                llOriginWallet -> {
                    originSelectedPosition = 1
                    setOrigin(originSelectedPosition)
                }

                etNetwork -> {
                    if (!toEdit && !isFromWithdraw) {
                        if (networkAdapter.hasNoData()) {
                            networkAdapter.addProgress()
                        }
                        networkPopup.show()
                    }
                }


            }
        }
    }

    private fun addAddress() {
        checkInternet(binding.root,requireActivity()) {
            showProgressDialog(requireActivity())
            viewModel.getNetwork(network!!.id)
        }
    }

    private val onTextChange = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (toEdit) {
                viewModel.whitelistAddress?.let {
                    when {
                        address.isEmpty() ->
                            binding.ttlAddress.helperText =
                                getString(R.string.please_enter_address, it.network!!.uppercase())

                        !address.checkFormat(it.network!!.uppercase()) ->
                            binding.ttlAddress.helperText =
                                getString(R.string.please_enter_valid_address)

                        else -> binding.ttlAddress.helperText = ""
                    }
                }

            } else
                network?.let {
                    when {
                        address.isEmpty() ->
                            binding.ttlAddress.helperText = getString(
                                R.string.please_enter_address,
                                it.fullName
                            )

                        !address.checkFormat(it.id.uppercase()) ->
                            binding.ttlAddress.helperText =
                                getString(R.string.please_enter_valid_address)

                        else -> binding.ttlAddress.helperText = ""
                    }
                }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {

        }
    }

    private val onDrawableClickListener = object : View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event?.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.etAddress.right - binding.etAddress.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    checkPermissions()
//                    findNavController().navigate(R.id.codeScannerFragment)
                    return true
                }
            }
            return false
        }


    }
    var firstTimeGallery = false
    private fun checkPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
//                findNavController().navigate(R.id.codeScannerFragment)
            scanCustomCode.launch(
                ScannerConfig.build {
                    setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE)) // set interested barcode formats
                    setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
                    setShowTorchToggle(false) // show or hide (default) torch/flashlight toggle button
                    setShowCloseButton(true) // show or hide (default) close button
                    setHorizontalFrameRatio(1f) // set the horizontal overlay ratio (default is 1 / square frame)
                    setUseFrontCamera(false) // use the front camera
                    setOverlayStringRes(R.string.empty) // string resource used for the scanner overlay
                    setOverlayDrawableRes(null) // drawable resource used for the scanner overlay

                }
            )
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
        } else {
            if (firstTimeGallery)
                permissionDeniedDialog()
            // Directly ask for the permission
            if (!firstTimeGallery)
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA
                    )
                )
            firstTimeGallery = true
        }
//        }
//        else {
//            if (ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                ) ==
//                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) ==
//                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.CAMERA
//                ) ==
//                PackageManager.PERMISSION_GRANTED
//            ) {
//                scanCustomCode.launch(
//                    ScannerConfig.build {
//                        setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE)) // set interested barcode formats
//                        setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
//                        setShowTorchToggle(false) // show or hide (default) torch/flashlight toggle button
//                        setShowCloseButton(true) // show or hide (default) close button
//                        setHorizontalFrameRatio(1f) // set the horizontal overlay ratio (default is 1 / square frame)
//                        setUseFrontCamera(false) // use the front camera
//                        setOverlayStringRes(R.string.empty) // string resource used for the scanner overlay
//                        setOverlayDrawableRes(null) // drawable resource used for the scanner overlay
//
//                    }
//                )
//            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                requestMultiplePermissions.launch(
//                    arrayOf(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    )
//                )
//            } else {
//                if (firstTimeGallery)
//                    permissionDeniedDialog()
//                // Directly ask for the permission
//                if (!firstTimeGallery)
//                    requestMultiplePermissions.launch(
//                        arrayOf(
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        )
//                    )
//                firstTimeGallery = true
//            }
//        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            permissions.entries.forEach {
                Log.d("TAG", "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.CAMERA] == true
            ) {
                Log.d("requestMultiplePermissions", "Permission granted")
                // isPermissionGranted=true
//                        binding.ivOpenGallery.performClick()
//                findNavController().navigate(R.id.codeScannerFragment)
                scanCustomCode.launch(
                    ScannerConfig.build {
                        setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE)) // set interested barcode formats
                        setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
                        setShowTorchToggle(false) // show or hide (default) torch/flashlight toggle button
                        setShowCloseButton(true) // show or hide (default) close button
                        setHorizontalFrameRatio(1f) // set the horizontal overlay ratio (default is 1 / square frame)
                        setUseFrontCamera(false) // use the front camera
                        setOverlayStringRes(R.string.empty) // string resource used for the scanner overlay
                        setOverlayDrawableRes(null) // drawable resource used for the scanner overlay

                    }
                )

            } else {
                Log.d("requestMultiplePermissions", "Permission not granted")

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

    private fun handleResult(result: QRResult) {
        try {
            Log.d("result", "$result")
            val pattern = Regex("rawValue=([^,\\]\\)]+)")

            // Find rawValue using regex
            val matchResult = pattern.find(result.toString())
            val rawValue = matchResult?.groups?.get(1)?.value

            println("Raw value: $rawValue")

            var address = rawValue
            val index = rawValue!!.indexOf(":")
            if (index != -1) {
                val intIndex = index + 1
                address = rawValue.substring(intIndex)
            }
            binding.etAddress.setText(address)
        } catch (_: Exception) {

        }
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    companion object {

        private const val DRAWABLE_RIGHT = 2
        private const val TO_EDIT = "toEdit"


    }

}