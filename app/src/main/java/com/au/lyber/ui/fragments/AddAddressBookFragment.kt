package com.au.lyber.ui.fragments

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
import com.au.lyber.R
import com.au.lyber.databinding.AppItemLayoutBinding
import com.au.lyber.databinding.FragmentCryptoAddressBookBinding
import com.au.lyber.databinding.ItemAddressesBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.Whitelistings
import com.au.lyber.models.WithdrawAddress
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.ui.fragments.bottomsheetfragments.AddAddressInfoBottomSheet
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.ProfileViewModel


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

        viewModel.withdrawalAddresses.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                adapter.removeProgress()
                adapter.setList(it.data)
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

      /*  viewModel.searchWhitelisting.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                adapter.removeProgress()
                adapter.setList(it.addresses)
                binding.rvAddresses.startLayoutAnimation()
            }
        }*/

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
            viewModel.getWithdrawalAddresses()
        }


    }


//    private val onFocusChange = View.OnFocusChangeListener { v, hasFocus ->
//        if (hasFocus)
//            binding.appBar.setExpanded(false,true)
//        else binding.appBar.setExpanded(true,true)
//    }

    private fun showInfo(data: WithdrawAddress) {
        AddAddressInfoBottomSheet(true,requireActivity()) {
            if (it == 1) {
                // delete
                checkInternet(requireContext()) {
                  //  showProgressDialog(requireContext())
                    viewModel.deleteWhiteList(hashMapOf(Constants.ADDRESS_ID to data.network))
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
                    //if (App.prefsManager.user?.is_address_whitelisting_enabled == true)
                        requireActivity().replaceFragment(
                            R.id.flSplashActivity,
                            AddCryptoAddress()
                        )/* else "Please enable whitelisting to add addresses".showToast(
                        requireContext() )
                */}

            }
        }
    }

    class AddressesAdapter(private val clickListener: (WithdrawAddress) -> Unit = { _ -> }) :
        BaseAdapter<WithdrawAddress>() {

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

                        //ivItem.loadCircleCrop(it.logo)

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