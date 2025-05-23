
package com.Lyber.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentCryptoAddressBookBinding
import com.Lyber.databinding.ItemAddressesBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.WithdrawAddress
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.ui.fragments.bottomsheetfragments.AddAddressInfoBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.ProfileViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import okhttp3.ResponseBody
import org.json.JSONObject


class AddAddressBookFragment : BaseFragment<FragmentCryptoAddressBookBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: AddressesAdapter
    private val completeList: MutableList<WithdrawAddress> = mutableListOf()
    private val keyword get() = binding.etSearch.text.trim().toString()

    override fun bind() = FragmentCryptoAddressBookBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        adapter = AddressesAdapter(::showInfo)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.withdrawalAddresses.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                adapter.removeProgress()
                adapter.setList(it.data!!)
                completeList.clear()
                completeList.addAll(it.data!!)
                binding.rvAddresses.startLayoutAnimation()
            }
        }

        viewModel.deleteWhiteList.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                checkInternet(binding.root, requireContext()) {
                    viewModel.getWithdrawalAddresses()
                }
            }
        }


        binding.rvAddresses.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
            adapter.addProgress()
        }

        binding.ivTopAction.setOnClickListener(this)
        binding.llAddAddress.setOnClickListener(this)
        binding.llWhitelisting.setOnClickListener {
            findNavController().navigate(R.id.enableWhiteListingFragment)
        }

        checkInternet(binding.root, requireContext()) {
            viewModel.getWithdrawalAddresses()
        }

        setSearchLogic()
    }

    private fun setSearchLogic() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                searchingInList(s.toString())
            }

        })

    }

    private fun searchingInList(newText: String) {
        val dummyList: MutableList<WithdrawAddress> = mutableListOf()
        for (ina in completeList) {
            if (ina.name!!.contains(newText, true) || ina.address!!.contains(newText, true)) {
                dummyList.add(ina)
            }
        }
        adapter.setList(dummyList)
    }


    private fun showInfo(data: WithdrawAddress) {
        AddAddressInfoBottomSheet(true, requireActivity()) {
            if (it == 1) {
                // delete
                checkInternet(binding.root, requireContext()) {
                    viewModel.deleteWhiteList(
                        data.address!!,
                        data.network!!
                    )
                }
            } else {
                //edit
                viewModel.whitelistAddress = data
                val bundle = Bundle().apply {
                    putBoolean(Constants.TO_EDIT, true)
                }
                findNavController().navigate(R.id.addCryptoAddress, bundle)
            }
        }
            .setWhiteListing(data)
            .show(childFragmentManager, "")
    }

    private val onTextChange = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            checkInternet(binding.root, requireContext()) {
                adapter.setList(emptyList())
                adapter.addProgress()
                if (keyword.isNotEmpty()) {
                    viewModel.searchWhitelist(keyword)
                }

                else {
                    viewModel.getWithdrawalAddresses()
                }


            }
        }

    }

    override fun onResume() {
        binding.llDurationInfo.visible()
        binding.tvDuration.text = when (App.prefsManager.withdrawalLockSecurity) {
            Constants.HOURS_72 -> "72H"
            Constants.HOURS_24 -> "24H"
            else -> ""
        }
        binding.tvDurationText.text = when (App.prefsManager.withdrawalLockSecurity) {
            Constants.HOURS_72 -> getString(R.string.active_during)
            Constants.HOURS_24 -> getString(R.string.active_during)
            else -> getString(R.string.no_security)
        }

        super.onResume()
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                llAddAddress -> {
                    findNavController().navigate(R.id.addCryptoAddress)
                }

            }
        }
    }

    class AddressesAdapter(private val clickListener: (WithdrawAddress) -> Unit = { _ -> }) :
        BaseAdapter<WithdrawAddress>() {

        inner class AddressViewHolder(val binding: ItemAddressesBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    itemList[absoluteAdapterPosition]?.let(clickListener)
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
                        val id = it.network
                        com.Lyber.ui.activities.BaseActivity.networkAddress.firstNotNullOfOrNull { item -> item.takeIf { item.id == id } }
                            ?.let { it1 ->
                                ivItem.loadCircleCrop(it1.imageUrl);

                            }
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
        } else {
            requireActivity().window.addFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        super.onDestroyView()
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        dismissProgressDialog()
        when (errorCode) {
            10025 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.error_code_10025)
            )
            else->  super.onRetrofitError(errorCode, msg)

        }
    }
}
