package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ListPopupWindow
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.AppItemLayoutBinding
import com.Lyber.dev.databinding.FragmentChooseAssetDepositBinding
import com.Lyber.dev.databinding.LoaderViewBinding
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.NetworkDeposit
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.fadeIn
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.dev.utils.CommonMethods.Companion.returnErrorCode
import com.Lyber.dev.utils.CommonMethods.Companion.showErrorMessage
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PortfolioViewModel
import okhttp3.ResponseBody
import java.util.*

class ChooseAssetForDepositFragment : BaseFragment<FragmentChooseAssetDepositBinding>(),
    OnClickListener {


    private var network: AssetBaseData? = null
    private lateinit var assetAdapter: AssetPopupAdapter
    private lateinit var assetPopup: ListPopupWindow
    private lateinit var assetAdapterNetwork: AssetPopupAdapterNetwork
    private lateinit var assetPopupNetwork: ListPopupWindow
    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentChooseAssetDepositBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        assetAdapter = AssetPopupAdapter()
        assetAdapterNetwork = AssetPopupAdapterNetwork()
        assetPopup = ListPopupWindow(requireContext())
        assetPopupNetwork = ListPopupWindow(requireContext())
        assetPopup.anchorView = binding.llNetwork
        assetPopupNetwork.anchorView = binding.etNetwork
        assetPopup.setAdapter(assetAdapter)
        assetPopupNetwork.setAdapter(assetAdapterNetwork)
        if (App.prefsManager.assetBaseDataResponse != null)
            App.prefsManager.assetBaseDataResponse.let {
                if (assetPopup.isShowing) {
                    assetAdapter.removeProgress()
                    assetAdapter.setData(it!!.data)
                    assetPopup.show()
                } else {
                    assetAdapter.setData(it!!.data)
                }
                for (sa in it.data) {
                    if (sa.id == requireArguments().getString(Constants.DATA_SELECTED)) {
                        network = sa
                        binding.tvTitle.fadeIn()
                        binding.ivNetwork.visible()
                        binding.etAssets.updatePadding(0)
                        binding.etAssets.setText(
                            "${
                                sa.fullName.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                }
                            } (${sa.id.uppercase()})"
                        )
                        if (sa.id.equals(Constants.MAIN_ASSET, ignoreCase = true)) {
                            binding.btnBuyTether.visible()
                            binding.tvOr.visible()
                        } else {
                            binding.btnBuyTether.gone()
                            binding.tvOr.gone()
                        }
                        binding.ivNetwork.loadCircleCrop(sa.imageUrl)
                        CommonMethods.showProgressDialog(requireActivity())
                        viewModel.getAssetDetailIncludeNetworks(sa.id)
                        break
                    }
                }
            }
        viewModel.getAddress.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                isAddress = false
                CommonMethods.dismissProgressDialog()
                binding.etAddress.text = it.data.address
            }
        }
        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                viewModel.selectedAssetDetail = it.data
                assetAdapterNetwork.setData(it.data.networks)
                for (networkq in it.data.networks) {
                    if (networkq.isDepositActive) {
                        binding.etNetwork.text = networkq.fullName
                        CommonMethods.showProgressDialog(requireActivity())
                        isAddress = true
                        viewModel.getAddress(networkq.id, network!!.id)
                        binding.tvNote.text = getString(
                            R.string.send_only_to_this_address_using_the_protocol,
                            network!!.fullName,
                            network!!.id.uppercase(),
                            networkq.fullName
                        )
                        break
                    } else {
                        binding.etNetwork.text = ""
                        binding.etAddress.text = ""
                        binding.tvNote.text =
                            getString(R.string.all_networks_for_this_asset_are_currently_deactivated_for_deposit)

                    }
                }
            }
        }

        assetPopup.setOnItemClickListener { _, _, position, _ ->
            assetAdapter.getItemAt(position)?.let {
                // change ui
                network = it
                binding.tvTitle.fadeIn()
                binding.ivNetwork.visible()
                binding.etAssets.updatePadding(0)
                binding.etAssets.setText("${it.fullName.capitalize(Locale.ROOT)} (${it.id.uppercase()})")
                if (it.id.equals(Constants.MAIN_ASSET, ignoreCase = true)) {
                    binding.btnBuyTether.visible()
                    binding.tvOr.visible()
                } else {
                    binding.btnBuyTether.gone()
                    binding.tvOr.gone()
                }
                binding.ivNetwork.loadCircleCrop(it.imageUrl)
                viewModel.getAssetDetailIncludeNetworks(it.id)
                CommonMethods.showProgressDialog(requireActivity())
                assetPopup.dismiss()
            }
        }
        assetPopupNetwork.setOnItemClickListener { _, _, position, _ ->
            assetAdapterNetwork.getItemAt(position)?.let {
                binding.etNetwork.text = it.fullName
                CommonMethods.showProgressDialog(requireActivity())
                isAddress = true
                viewModel.getAddress(it.id, network!!.id)
                binding.tvNote.text = getString(
                    R.string.send_only_to_this_address_using_the_protocol,
                    network!!.fullName,
                    network!!.id.uppercase(),
                    it.fullName
                )
                assetPopupNetwork.dismiss()
            }
        }
        binding.btnBuyTether.setOnClickListener(this)
        binding.ivCopy.setOnClickListener(this)
        binding.etNetwork.setOnClickListener(this)
        binding.etAssets.setOnClickListener(this)
        binding.ivScan.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)

    }

    class AssetPopupAdapterNetwork : android.widget.BaseAdapter() {

        private val list = mutableListOf<NetworkDeposit?>()

        fun getItemAt(position: Int): NetworkDeposit? {
            return list[position]
        }

        fun setData(items: List<NetworkDeposit?>) {
            list.clear()
            for (itm in items) {
                if (itm!!.isDepositActive) {
                    list.add(itm)
                }
            }
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
                                "${data.fullName.capitalize()} (${data.id.uppercase()})"

                            return it.root
                        }

                        return it.root
                    }
        }

    }

    class AssetPopupAdapter : android.widget.BaseAdapter() {

        private val list = mutableListOf<AssetBaseData?>()

        fun getItemAt(position: Int): AssetBaseData? {
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

        fun setData(items: List<AssetBaseData?>) {
            list.clear()
            for (itm in items) {
                if (itm!!.isDepositActive) {
                    list.add(itm)
                }
            }
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
                                "${data.fullName.capitalize()} (${data.id.uppercase()})"
                            return it.root
                        }
                        return it.root
                    }
        }
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                btnBuyTether -> {
                    val arguments = Bundle().apply {
                        putString(Constants.FROM, ChooseAssetForDepositFragment::class.java.name)
                    }
                    findNavController().navigate(R.id.buyUsdt, arguments)
                }

                ivCopy -> {
                    if (binding.etAddress.text.toString().isNotEmpty()) {
                        val clipboard =
                            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip =
                            ClipData.newPlainText(
                                getString(R.string.deposit_adress),
                                binding.etAddress.text.toString()
                            )
                        clipboard.setPrimaryClip(clip)
                        getString(R.string.adress_copied).showToast(binding.root, requireContext())
                    }
                }

                etNetwork -> {
                    assetPopupNetwork.show()
                }

                etAssets -> {
                    if (assetAdapter.hasNoData()) {
                        assetAdapter.addProgress()
                    }
                    assetPopup.show()
                }

                ivScan -> {
                    if (binding.etAddress.text.toString().isNotEmpty())
                        startActivity(
                            Intent(
                                requireActivity(),
                                com.Lyber.dev.ui.activities.BarCodeActivity::class.java
                            )
                                .putExtra(
                                    Constants.DATA_SELECTED,
                                    binding.etAddress.text.toString()
                                )
                        )
                }

                ivTopAction -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        when (errorCode) {
            -1 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.error_code_1_negative)
            )

            10012 -> {
                val asset=binding.etAssets.text.toString()
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10012,asset)
                )
            }

            10013 -> {
                val transactionType=getString(R.string.deposit)
                val network=binding.etNetwork.text.toString()
                val asset=binding.etAssets.text.toString()

                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10013,transactionType,network,asset)
                )
            }

            10030 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.error_code_10030)
            )

            15002 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.error_code_15002)
            )

            18000 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.error_code_18000)
            )

            26 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_26)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            3000 -> {
                val transactionType=getString(R.string.deposit)
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_3000,transactionType)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            3006 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_3006)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            10023 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10023)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            10034 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10034)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            10035 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10035)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            10036 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10036)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            10037 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10037)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }

            10042 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_10042)
                )
                findNavController().navigate(R.id.action_choose_asset_for_deposit_to_home_fragment)
            }
            else->  super.onRetrofitError(errorCode, msg)

        }
    }
}