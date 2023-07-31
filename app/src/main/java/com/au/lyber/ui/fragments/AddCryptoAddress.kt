package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.AppItemLayoutBinding
import com.au.lyber.databinding.FragmentAddBitcoinAddressBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.ExchangeAsset
import com.au.lyber.models.Network
import com.au.lyber.models.Whitelistings
import com.au.lyber.ui.fragments.bottomsheetfragments.AddAddressInfoBottomSheet
import com.au.lyber.utils.CommonMethods.Companion.checkFormat
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.fadeOut
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.postDelay
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.ProfileViewModel

class AddCryptoAddress : BaseFragment<FragmentAddBitcoinAddressBinding>(), View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel

    private var fromWithdraw: Boolean = false
    private var toEdit: Boolean = false

    private var originSelectedPosition = 0


    /* network popup */
    private var network: Network? = null
    private lateinit var networkAdapter: NetworkPopupAdapter
    private lateinit var networkPopup: ListPopupWindow

    /* coinbase popup */
    private var exchange: ExchangeAsset? = null
    private lateinit var coinbaseAdapter: CoinbasePopupAdapter
    private lateinit var coinbasePopup: ListPopupWindow

    private val origin get() = if (originSelectedPosition == 0) "EXCHANGE" else "WALLET"

    private val addressName: String get() = binding.etAddressName.text.trim().toString()
    private val address: String
        get() = binding.etAddress.text?.trim().toString()

    private lateinit var infoBottomSheet: AddAddressInfoBottomSheet

    override fun bind() = FragmentAddBitcoinAddressBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(Constants.SCAN_COMPLETE))
        arguments?.let {
            fromWithdraw = it.getBoolean(FROM_WITHDRAW, false)
            toEdit = it.getBoolean(TO_EDIT, false)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val address: String = intent?.getStringExtra(Constants.SCANNED_ADDRESS) ?: ""
            binding.etAddress.setText(address)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoBottomSheet = AddAddressInfoBottomSheet(false,requireActivity(), ::infoBottomSheetCallback)
        viewModel = getViewModel(requireActivity())
        viewModel.listener = this


        /*  changing ui according to view cases */
        if (fromWithdraw) {
            binding.btnAddUseAddress.text = getString(R.string.add_and_use_this_address)
            binding.etNetwork.updatePadding(0)
        } else {
            binding.tvTitle.text = getString(R.string.add_a_crypto_address)
            binding.ivNetwork.gone()
            binding.etNetwork.hint = getString(R.string.choose_network)
        }


        /* observers */

        viewModel.networkResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if (networkPopup.isShowing) {
                    networkAdapter.removeProgress()
                    networkAdapter.setData(it.networks)
                    networkPopup.show()
                } else networkAdapter.setData(it.networks)
            }
        }

        viewModel.exchangeListingResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if (coinbasePopup.isShowing) {
                    coinbaseAdapter.removeProgress()
                    coinbaseAdapter.setData(it.assets)
                    coinbasePopup.show()
                } else coinbaseAdapter.setData(it.assets)
            }
        }

        viewModel.addWhitelistResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                requireActivity().onBackPressed()
                infoBottomSheet.dismiss()
            }
        }

        viewModel.updateWhiteList.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
                requireActivity().onBackPressed()
            }
        }

        /* click listeners */

        binding.ivTopAction.setOnClickListener(this)
        binding.btnAddUseAddress.setOnClickListener(this)
        binding.llOriginExchange.setOnClickListener(this)
        binding.llOriginWallet.setOnClickListener(this)
        binding.etNetwork.setOnClickListener(this)
        binding.etExchange.setOnClickListener(this)
        binding.etAddress.addTextChangedListener(onTextChange)
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

                binding.tvTitle.fadeIn()
                binding.ivNetwork.visible()
                binding.etNetwork.updatePadding(0)
                binding.tvTitle.text = getString(R.string.add_a_address, it.name.uppercase())
                binding.etNetwork.setText("${it.name} (${it.asset_id.uppercase()})")

                binding.ivNetwork.loadCircleCrop(it.logo)

                networkPopup.dismiss()
            }
        }


        /* exchange pop up */
        coinbaseAdapter = CoinbasePopupAdapter()
        coinbasePopup = ListPopupWindow(requireContext())
        coinbasePopup.anchorView = binding.etExchange
        coinbasePopup.setAdapter(coinbaseAdapter)
        coinbasePopup.setOnItemClickListener { parent, view, position, id ->
            coinbaseAdapter.getItemAt(position)?.let {
                exchange = it
                binding.etExchange.setText(it.name)
                coinbasePopup.dismiss()
            }
        }

        setOrigin(originSelectedPosition)

        if (toEdit) {
            viewModel.whitelistAddress?.let {

                binding.etAddress.setText("${it.address}")
                binding.etAddressName.setText(it.name)
                binding.etNetwork.setText(it.network)


                binding.ivNetwork.visible()
                binding.ivNetwork.loadCircleCrop(it.logo)

                if (it.exchange.isNullOrEmpty()) {
                    originSelectedPosition = 1
                } else {
                    binding.etExchange.setText("${it.exchange}")
                    originSelectedPosition = 0
                }

                setOrigin(originSelectedPosition)
            }
        }

        checkInternet(requireContext()) {
            if (toEdit)
                showProgressDialog(requireContext())
            viewModel.getNetworks()
            viewModel.getExchangeListing()
        }

    }

    private fun infoBottomSheetCallback(isLeftButton: Int) {

        if (isLeftButton == 1)
            checkInternet(requireContext()) {
                showProgressDialog(requireContext())
                viewModel.addAddress(
                    addressName,
                    network?.asset_id ?: "",
                    address,
                    origin,
                    exchange?.name ?: "",
                    network?.logo ?: ""
                )
            }
        else {
            infoBottomSheet.dismiss()
        }
    }

    private fun setOrigin(position: Int) {
        when (position) {
            0 -> {

                binding.llOriginExchange.background =
                    getDrawable(requireContext(), R.drawable.round_stroke_purple_500)
                binding.ivRadioExchange.setImageResource(R.drawable.radio_select)
                binding.tvExchange.setTextColor(getColor(requireContext(), R.color.purple_500))

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
                binding.tvWallet.setTextColor(getColor(requireContext(), R.color.purple_500))

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
            return list[position]?.createdAt ?: 0
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


                                it.ivItem.loadCircleCrop(data.logo)

                            it.tvStartTitleCenter.text =
                                "${data.name} (${data.asset_id.uppercase()})"

                            return it.root
                        }

                        return it.root
                    }
        }


    }

    class CoinbasePopupAdapter : android.widget.BaseAdapter() {

        private val list = mutableListOf<ExchangeAsset?>()

        fun getItemAt(position: Int): ExchangeAsset? {
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

        fun setData(items: List<ExchangeAsset?>) {
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
            return System.currentTimeMillis()
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
                        it.ivEndIcon.gone()
                        list[position]?.let { data ->
                            it.tvStartTitleCenter.text = data.name
                        }
                        return it.root
                    }
        }

    }

    override fun onDestroyView() {
        if (networkPopup.isShowing)
            networkPopup.dismiss()
        if (coinbasePopup.isShowing)
            coinbasePopup.dismiss()
        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnAddUseAddress -> {
                    when {

                        toEdit -> {

                            val hashMap = hashMapOf<String, Any>()
                            hashMap[Constants.NAME] = addressName
                            hashMap[Constants.NETWORK] = viewModel.whitelistAddress?.network?: ""
                            hashMap[Constants.ADDRESS_STR] = address
                            hashMap[Constants.ORIGIN] = if (originSelectedPosition == 0) {
                                hashMap[Constants.Exchange] = viewModel.whitelistAddress?.exchange ?: ""
                                getString(R.string.exchange)
                            } else {
                                getString(R.string.wallet)
                            }
                            hashMap[Constants.LOGO] = viewModel.whitelistAddress?.logo ?: ""

                            hashMap[Constants.ADDRESS_ID] = viewModel.whitelistAddress?._id ?: ""

                            network?.let {
                                hashMap[Constants.LOGO] = network?.logo ?: ""
                                hashMap[Constants.NETWORK] = network?.name ?: ""
                            }

                            checkInternet(requireContext()) {
                                showProgressDialog(requireContext())
                                viewModel.updateWhiteList(hashMap)
                            }
                        }

                        addressName.isEmpty() -> {
                            binding.etAddressName.requestKeyboard()
                            getString(R.string.please_enter_address_name).showToast(requireContext())
                        }

                        network == null -> {
                            binding.etNetwork.requestFocus()
                            getString(R.string.please_select_a_network).showToast(requireContext())
                        }

                        address.isEmpty() -> {
                            binding.etAddress.requestKeyboard()
                            binding.ttlAddress.helperText = getString(R.string.please_enter_address, network?.name ?: "")
                            getString(R.string.please_enter_address_).showToast(requireContext())
                        }

                        !address.checkFormat(network?.asset_id?.uppercase() ?: "") -> {
                            binding.etAddress.requestKeyboard()
                            binding.ttlAddress.helperText = getString(R.string.please_enter_valid_address)
                            getString(R.string.please_enter_valid_address).showToast(requireContext())
                        }

                        exchange == null -> {

                            if (originSelectedPosition == 0) {
                                getString(R.string.please_select_a_exchange).showToast(requireContext())
                            } else {
                                val whilelist = Whitelistings(
                                    address = address,
                                    name = addressName,
                                    network = network?.name ?: "",
                                    origin = if (originSelectedPosition == 0) getString(R.string.exchange) else getString(R.string.wallet),
                                    exchange = exchange?.name ?: "",
                                    logo = network?.logo ?: ""
                                )
                                infoBottomSheet.setWhiteListing(whilelist)
                                infoBottomSheet.show(childFragmentManager, "")
                            }
                        }

                        else -> {
                            val whilelist = Whitelistings(
                                address = address,
                                name = addressName,
                                network = network?.name ?: "",
                                origin = if (originSelectedPosition == 0) getString(R.string.exchange) else getString(R.string.wallet),
                                exchange = exchange?.name ?: "",
                                logo = network?.logo ?: ""
                            )
                            infoBottomSheet.setWhiteListing(whilelist)
                            infoBottomSheet.show(childFragmentManager, "")
                        }
                    }
                }
                ivTopAction -> requireActivity().onBackPressed()

                llOriginExchange -> {
                    originSelectedPosition = 0
                    setOrigin(originSelectedPosition)
                }

                llOriginWallet -> {
                    originSelectedPosition = 1
                    setOrigin(originSelectedPosition)
                }

                etNetwork -> {
                    if (networkAdapter.hasNoData()) {
                        networkAdapter.addProgress()
                    }
                    networkPopup.show()
                }

                etExchange -> {
                    if (coinbaseAdapter.hasNoData()) {
                        coinbaseAdapter.addProgress()
                    }
                    coinbasePopup.show()
                }

            }
        }
    }

    private val onTextChange = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (toEdit) {
                viewModel.whitelistAddress?.let {
                    when {
                        address.isEmpty() ->
                            binding.ttlAddress.helperText =
                                getString(R.string.please_enter_address, it.network.uppercase() ?: "")
                        !address.checkFormat(it.network.uppercase()) ->
                            binding.ttlAddress.helperText =getString(R.string.please_enter_valid_address)
                        else -> binding.ttlAddress.helperText = ""
                    }
                }

            } else
                network?.let {
                    when {
                        address.isEmpty() ->
                            binding.ttlAddress.helperText =  getString(R.string.please_enter_address, it.name ?: "")
                        !address.checkFormat(it.asset_id.uppercase()) ->
                            binding.ttlAddress.helperText =getString(R.string.please_enter_valid_address)
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
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        CodeScannerFragment(),
                        topBottom = true
                    )
                    return true
                }
            }
            return false
        }


    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    companion object {

        private const val DRAWABLE_LEFT = 0
        private const val DRAWABLE_TOP = 1
        private const val DRAWABLE_RIGHT = 2
        private const val DRAWABLE_BOTTOM = 3

        private const val FROM_WITHDRAW = "fromWithdraw"
        private const val TO_EDIT = "toEdit"

        fun fromWithdraw(): AddCryptoAddress {
            return AddCryptoAddress().apply {
                arguments = Bundle().apply {
                    putBoolean(FROM_WITHDRAW, true)
                }
            }
        }

        fun toEdit(): AddCryptoAddress {
            return AddCryptoAddress().apply {
                arguments = Bundle().apply {
                    putBoolean(TO_EDIT, true)
                }
            }
        }

    }

}