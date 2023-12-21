package com.Lyber.ui.fragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.AppItemLayoutBinding
import com.Lyber.databinding.FragmentCryptoAddressBookBinding
import com.Lyber.databinding.ItemAddressesBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.Whitelistings
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.ui.fragments.bottomsheetfragments.AddAddressInfoBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.px
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.ProfileViewModel


class AddAddressBookFragment : BaseFragment<FragmentCryptoAddressBookBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: AddressesAdapter

    private var nestedViewHeight: Int = 0
    private val keyword get() = binding.etSearch.text.trim().toString()

    override fun bind() = FragmentCryptoAddressBookBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(false)
        }else{
            requireActivity().window.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        adapter = AddressesAdapter(::showInfo)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.getWhiteListing.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                adapter.removeProgress()
                adapter.setList(it.addresses)
                binding.rvAddresses.startLayoutAnimation()
            }
        }

        viewModel.deleteWhiteList.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                checkInternet(requireContext()) {
                    viewModel.getWhiteListings()
                }
            }
        }

        viewModel.searchWhitelisting.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                adapter.removeProgress()
                adapter.setList(it.addresses)
                binding.rvAddresses.startLayoutAnimation()
            }
        }

        binding.rvAddresses.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
            adapter.addProgress()
        }

        binding.ivTopAction.setOnClickListener(this)
        binding.llAddAddress.setOnClickListener(this)

        binding.switchWhitelisting.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isPressed) {
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    EnableWhiteListingFragment()
                )
            }
        }

        binding.etSearch.addTextChangedListener(onTextChange)

        checkInternet(requireContext()) {
            viewModel.getWhiteListings()
        }


    }


//    private val onFocusChange = View.OnFocusChangeListener { v, hasFocus ->
//        if (hasFocus)
//            binding.appBar.setExpanded(false,true)
//        else binding.appBar.setExpanded(true,true)
//    }

    private fun showInfo(data: Whitelistings) {
        AddAddressInfoBottomSheet(true,requireActivity()) {
            if (it == 1) {
                // delete
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.deleteWhiteList(hashMapOf(Constants.ADDRESS_ID to data._id))
                }
            } else {
                //edit
                viewModel.whitelistAddress = data
                requireActivity().replaceFragment(R.id.flSplashActivity, AddCryptoAddress().apply {
                    arguments = Bundle().apply {
                        putBoolean(Constants.TO_EDIT, true)
                    }
                })
            }
        }
            .setWhiteListing(data)
            .show(childFragmentManager, "")
    }

    private val onTextChange = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            checkInternet(requireContext()) {
                adapter.setList(emptyList())
                adapter.addProgress()
                if (keyword.isNotEmpty())
                    viewModel.searchWhitelist(keyword)
                else
                    viewModel.getWhiteListings()


            }
        }

    }

    override fun onResume() {
//        with(App.prefsManager.isWhitelisting()) {
//            binding.switchWhitelisting.isChecked = this
//            if (this) {
//                binding.llDurationInfo.visible()
//                binding.tvDuration.text = when (App.prefsManager.getExtraSecurity()) {
//                    Constants.HOURS_72 -> "72H"
//                    Constants.HOURS_24 -> "24H"
//                    else -> "No Security"
//                }
//            } else {
//                binding.llDurationInfo.gone()
//            }
//        }

        super.onResume()
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressed()
                llAddAddress -> {
//                    if (App.prefsManager.user?.is_address_whitelisting_enabled == true)
//                        requireActivity().replaceFragment(
//                            R.id.flSplashActivity,
//                            AddCryptoAddress()
//                        ) else "Please enable whitelisting to add addresses".showToast(
//                        requireContext()
//                    )
                }

            }
        }
    }

    class AddressesAdapter(private val clickListener: (Whitelistings) -> Unit = { _ -> }) :
        BaseAdapter<Whitelistings>() {

        inner class AddressViewHolder(val binding: ItemAddressesBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    itemList[adapterPosition]?.let(clickListener)
                }

            }
        }

        override fun getItemViewType(position: Int) =
            if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == LOADER_VIEW) LoaderViewHolder(
                LoaderViewBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else AddressViewHolder(
                ItemAddressesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (itemList[position] != null) {

                (holder as AddressViewHolder).binding.apply {
                    itemList[position]?.let {

                        ivItem.loadCircleCrop(it.logo)

                        tvStartTitle.text = it.name
                        tvStartSubTitle.text = it.address

                    }
                }
            } else {

            }
        }

    }

    override fun onDestroyView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(true)
        }else{
            requireActivity().window.addFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        super.onDestroyView()
    }
}