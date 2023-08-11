package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.AppItemLayoutBinding
import com.au.lyber.databinding.FragmentChooseAssetDepositBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.NetworkDeposit
import com.au.lyber.ui.activities.BarCodeActivity
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import java.util.*

class ChooseAssetForDepositFragment : BaseFragment<FragmentChooseAssetDepositBinding>() {


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
        binding.ivScan.setOnClickListener {
            if (binding.etAddress.text.toString().isNotEmpty())
                startActivity(Intent(requireActivity(),BarCodeActivity::class.java)
                    .putExtra(Constants.DATA_SELECTED,binding.etAddress.text.toString())) }
        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.btnAddUseAddress.setOnClickListener {
           // viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
            //requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
        }
        binding.ivCopy.setOnClickListener {
            if (binding.etAddress.text.toString().isNotEmpty()) {
                val clipboard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip =
                    ClipData.newPlainText(getString(R.string.deposit_adress), binding.etAddress.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireActivity(),
                    getString(R.string.adress_copied), Toast.LENGTH_SHORT).show()
            }
        }



        assetAdapter = AssetPopupAdapter()
        assetAdapterNetwork= AssetPopupAdapterNetwork()
        assetPopup = ListPopupWindow(requireContext())
        assetPopupNetwork = ListPopupWindow(requireContext())
        assetPopup.anchorView = binding.llNetwork
        assetPopupNetwork.anchorView = binding.etNetwork
        assetPopup.setAdapter(assetAdapter)
        assetPopupNetwork.setAdapter(assetAdapterNetwork)
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

                    binding.etAssets.setText("${sa.fullName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    }} (${sa.id.uppercase()})")
                    binding.ivNetwork.loadCircleCrop(sa.imageUrl)
                    binding.btnAddUseAddress.text = "Buy ${sa.fullName.capitalize()} on Lyber"
                    CommonMethods.showProgressDialog(requireActivity())
                    viewModel.getAssetDetailIncludeNetworks(sa.id)
                    break
                }
            }
        }
        viewModel.getAddress.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED){
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
                        binding.etNetwork.text =networkq.fullName
                        CommonMethods.showProgressDialog(requireActivity())
                        viewModel.getAddress(networkq.id,network!!.id)
                        binding.tvNote.text = getString(
                            R.string.send_only_to_this_address_using_the_protocol,
                            network!!.fullName,
                            network!!.id,
                            networkq.fullName
                        )
                        break
                    }else{
                        binding.etNetwork.text =""
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

                binding.ivNetwork.loadCircleCrop(it.imageUrl)
                binding.btnAddUseAddress.text = "Buy ${it.fullName.capitalize()} on Lyber"
                viewModel.getAssetDetailIncludeNetworks(it.id)
                CommonMethods.showProgressDialog(requireActivity())
                assetPopup.dismiss()
            }
        }
        assetPopupNetwork.setOnItemClickListener { _, _, position, _ ->
            assetAdapterNetwork.getItemAt(position)?.let {
                binding.etNetwork.text = it.fullName
                CommonMethods.showProgressDialog(requireActivity())
                viewModel.getAddress(it.id,network!!.id)
                binding.tvNote.text = getString(
                    R.string.send_only_to_this_address_using_the_protocol,
                    network!!.fullName,
                    network!!.id,
                    it.fullName
                )

                assetPopupNetwork.dismiss()
            }
        }


        binding.etNetwork.setOnClickListener {
            assetPopupNetwork.show()
        }
        binding.etAssets.setOnClickListener {
            if (assetAdapter.hasNoData()) {
                assetAdapter.addProgress()
            }
            assetPopup.show()
        }


        viewModel.selectedAsset?.let {
            binding.btnAddUseAddress.text = "Buy ${it.fullName.capitalize()} on Lyber"
        }



    }

    class AssetPopupAdapterNetwork : android.widget.BaseAdapter() {

        private val list = mutableListOf<NetworkDeposit?>()

        fun getItemAt(position: Int): NetworkDeposit? {
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


}