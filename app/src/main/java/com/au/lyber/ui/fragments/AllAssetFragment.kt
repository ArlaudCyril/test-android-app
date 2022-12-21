package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentAllAssetsBinding
import com.au.lyber.models.AssetData
import com.au.lyber.models.Data
import com.au.lyber.ui.adapters.AllAssetAdapter
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.PortfolioViewModel
import com.google.android.material.tabs.TabLayout

class AllAssetFragment : BaseFragment<FragmentAllAssetsBinding>(), View.OnClickListener {

    private lateinit var adapter: AllAssetAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel

    private var type: String = ""

    private var shouldLoad: Boolean = true
    private var page: Int = 1
    private var limit: Int = 10

    private var assets = mutableListOf<AssetData>()
    private var topGainers = mutableListOf<AssetData>()
    private var topLosers = mutableListOf<AssetData>()

    private val searchText get() = binding.etSearch.text.trim().toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString("type", "")
        }
    }

    override fun bind() = FragmentAllAssetsBinding.inflate(layoutInflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* viewModel */
        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.assetsResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                assets.clear()
                topLosers.clear()
                topGainers.clear()

                assets.addAll(it.data)
                topLosers.addAll(assets.topLosers())
                topGainers.addAll(topLosers.reversed())

                when (binding.tabLayout.selectedTabPosition) {
                    0 -> adapter.setList(assets)
                    1 -> adapter.setList(topGainers)
                    2 -> adapter.setList(topLosers)
                    else -> {}
                }

                dismissProgressDialog()
                binding.rvRefresh.isRefreshing = false
            }
        }

        /* ui changes */
        binding.let {

            /* tab layout */
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = "Trending" })
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = "Top gainers" })
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = "Top loosers" })
//            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = "Stable" })
            it.tabLayout.addOnTabSelectedListener(tabSelectedListener)

            adapter = AllAssetAdapter(::assetClicked)
            layoutManager = LinearLayoutManager(requireContext())

            it.rvAddAsset.adapter = adapter
            it.rvAddAsset.layoutManager = layoutManager
            it.rvAddAsset.itemAnimator = null

            it.tvTitle.text = "All assets"
            it.ivTopAction.setImageResource(R.drawable.ic_back)
            it.ivTopAction.setOnClickListener(this)
            it.etSearch.setOnClickListener(this)

            it.etSearch.addTextChangedListener(object : OnTextChange {
                override fun onTextChange() {

                    when (binding.tabLayout.selectedTabPosition) {
                        0 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(assets.filter { it.id.contains(searchText) })
                            else adapter.setList(assets)
                        }
                        1 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(topGainers.filter { it.id.contains(searchText) })
                            else adapter.setList(topGainers)
                        }
                        2 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(topLosers.filter { it.id.contains(searchText) })
                            else adapter.setList(topLosers)
                        }

                        else -> {

                        }
                    }
                }
            })

        }

        checkInternet(requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getAssetList()
        }

        binding.rvRefresh.setOnRefreshListener {
            viewModel.getAssetList()
        }


    }

    private fun List<AssetData>.topLosers(): List<AssetData> {
        return sortedBy { it.change.toFloat() }
    }


    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> adapter.setList(assets)
                1 -> adapter.setList(topGainers)
                2 -> adapter.setList(topLosers)
                else -> {}
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}

    }

    private fun assetClicked(asset: AssetData) {

        getViewModel<PortfolioViewModel>(requireActivity()).let {
//            it.selectedAsset = asset
            it.chosenAssets = asset
            it.screenCount = 1
            viewModel.selectedAsset = it.selectedAsset
            requireActivity().onBackPressed()
        }

        /*if (type.isNotEmpty()) {
            viewModel.exchangeAssetTo = asset.extractAsset()
            requireActivity().onBackPressed()
        } else {
            getViewModel<PortfolioViewModel>(requireActivity()).let {
                it.selectedAsset = asset.extractAsset()
                it.screenCount = 1
                viewModel.selectedAsset = it.selectedAsset
                requireActivity().onBackPressed()
            }
        }*/
    }

    private fun optionSelected(option: String, asset: Data) {
        when (option) {
            "deposit" -> {

            }
            "withdraw" -> {
                viewModel.selectedOption = Constants.USING_WITHDRAW
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AddAmountFragment(),
                    topBottom = true
                )
            }
            "exchange" -> {
                viewModel.selectedOption = Constants.USING_EXCHANGE
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    SwapWithdrawFromFragment()
                )
            }
            "buy" -> {
                viewModel.selectedOption = Constants.USING_SINGULAR_ASSET
//                viewModel.selectedAsset = asset.extractAsset()
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    AddAmountFragment()
                )
            }
            "sell" -> {

            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressed()
//                includedMyAsset.root -> requireActivity().onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        page = 1
        viewModel.cancelJob()
        super.onDestroyView()
    }

}