package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ListPopupWindow
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.AppItemLayoutBinding
import com.au.lyber.databinding.FragmentChooseAssetDepositBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.GetAssetsResponseItem
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.fadeIn
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import java.util.*

class ChooseAssetForDepositFragment : BaseFragment<FragmentChooseAssetDepositBinding>() {


    private var network: AssetBaseData? = null
    private lateinit var assetAdapter: AssetPopupAdapter
    private lateinit var assetPopup: ListPopupWindow

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentChooseAssetDepositBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.btnAddUseAddress.setOnClickListener {
            viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
            requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
        }



        assetAdapter = AssetPopupAdapter()
        assetPopup = ListPopupWindow(requireContext())
        assetPopup.anchorView = binding.llNetwork
        assetPopup.setAdapter(assetAdapter)
        App.prefsManager.assetBaseDataResponse.let {
            if (assetPopup.isShowing) {
                assetAdapter.removeProgress()
                assetAdapter.setData(it!!.data)
                assetPopup.show()
            } else {
                assetAdapter.setData(it!!.data)
            }
            for (sa in it.data) {
                if (sa.id == requireArguments().getString("dataSelected")) {
                    network = sa

                    binding.tvTitle.fadeIn()
                    binding.ivNetwork.visible()
                    binding.etAssets.updatePadding(0)

                    binding.etAssets.setText("${sa.fullName.capitalize(Locale.ROOT)} (${sa.id.uppercase()})")

                    binding.ivNetwork.loadCircleCrop(sa.imageUrl)
                    break
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

                assetPopup.dismiss()
            }
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
                                "${data.fullName.capitalize()} (${data.id.uppercase()})"

                            return it.root
                        }

                        return it.root
                    }
        }


    }


}