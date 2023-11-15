package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentAllAssetsBinding
import com.au.lyber.databinding.FragmentSelectAssestForDepostBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.PriceServiceResume
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.adapters.AllAssesstAdapterDeposit
import com.au.lyber.ui.adapters.AllAssetAdapter
import com.au.lyber.ui.portfolio.action.AssestFragmentAction
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.google.android.material.tabs.TabLayout

class SelectAssestForBuy : BaseFragment<FragmentAllAssetsBinding>(), View.OnClickListener {

    private lateinit var adapter: AllAssetAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: PortfolioViewModel
    private var type: String = ""
    private var page: Int = 1
    private var assets = mutableListOf<PriceServiceResume>()
    private var trendings = mutableListOf<PriceServiceResume>()
    private var topGainers = mutableListOf<PriceServiceResume>()
    private var topLosers = mutableListOf<PriceServiceResume>()
    private var stables = mutableListOf<PriceServiceResume>()

    private val searchText get() = binding.etSearch.text.trim().toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(Constants.TYPE, "")
        }
    }

    override fun bind() = FragmentAllAssetsBinding.inflate(layoutInflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* viewModel */
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.priceServiceResumes.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                assets.clear()
                trendings.clear()
                topLosers.clear()
                topGainers.clear()
                stables.clear()
                BaseActivity.balanceResume.clear()
                BaseActivity.balanceResume.addAll(it)
                assets.addAll(it)
                if (type == Constants.Exchange){
                    for (asset in it){
                        if (asset.id == viewModel.exchangeAssetFrom){
                            assets.remove(asset)
                        }
                    }
                }

                trendings.addAll(assets)
                topLosers.addAll(assets.topLosers())
                topGainers.addAll(topLosers.reversed())
                stables.addAll(assets.stables())

                trendings.removeAll(stables)
                topLosers.removeAll(stables)
                topGainers.removeAll(stables)


                when (binding.tabLayout.selectedTabPosition) {
                    0 -> adapter.setList(trendings)
                    1 -> adapter.setList(topGainers)
                    2 -> adapter.setList(topLosers)
                    3 -> adapter.setList(mutableListOf())
                    else -> {}
                }

                CommonMethods.dismissProgressDialog()
                binding.rvRefresh.isRefreshing = false
            }
        }

        /* ui changes */
        binding.let {

            /* tab layout */
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = getString(R.string.trending) })
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = getString(R.string.top_gainers) })
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = getString(R.string.top_losers) })
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = getString(R.string.stable) })
            it.tabLayout.addOnTabSelectedListener(tabSelectedListener)

            adapter = AllAssetAdapter(::assetClicked,type == Constants.Exchange)
            layoutManager = LinearLayoutManager(requireContext())

            it.rvAddAsset.adapter = adapter
            it.rvAddAsset.layoutManager = layoutManager
            it.rvAddAsset.itemAnimator = null
            it.tvTitle.text = getString(R.string.choose_an_asset)

            it.ivTopAction.setImageResource(R.drawable.ic_back)
            it.ivTopAction.setOnClickListener(this)
            it.etSearch.setOnClickListener(this)

            it.etSearch.addTextChangedListener(object : OnTextChange {
                override fun onTextChange() {

                    when (binding.tabLayout.selectedTabPosition) {
                        0 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(assets.filter { it.id.startsWith(searchText, true)
                                        || CommonMethods.getAsset(it.id).fullName.startsWith(searchText, true)})
                            else adapter.setList(trendings)
                        }
                        1 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(assets.filter { it.id.startsWith(searchText, true)
                                        || CommonMethods.getAsset(it.id).fullName.startsWith(searchText, true)})
                            else adapter.setList(topGainers)
                        }
                        2 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(assets.filter { it.id.startsWith(searchText, true)
                                        || CommonMethods.getAsset(it.id).fullName.startsWith(searchText, true)})
                            else adapter.setList(topLosers)
                        }
                        3 -> {
                            if (searchText.isNotEmpty())
                                adapter.setList(assets.filter { it.id.startsWith(searchText, true)
                                        || CommonMethods.getAsset(it.id).fullName.startsWith(searchText, true)})
                            else adapter.setList(stables)
                        }
                        else -> {

                        }
                    }
                }
            })

        }

        CommonMethods.checkInternet(requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAllPriceResume()
        }

        binding.rvRefresh.setOnRefreshListener {
            viewModel.getAllPriceResume()
        }


    }

    private fun List<PriceServiceResume>.topLosers(): List<PriceServiceResume> {
        return sortedBy { it.priceServiceResumeData.change.toFloat() }
    }

    private fun List<PriceServiceResume>.stables(): List<PriceServiceResume> {
        return filter { CommonMethods.getAsset(it.id).isStablecoin }
    }//get is stable add the parameter and dispatch it from the others list

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> adapter.setList(trendings)
                1 -> adapter.setList(topGainers)
                2 -> adapter.setList(topLosers)
                3 -> adapter.setList(stables)
                else -> {}
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}

    }

    private fun assetClicked(asset: PriceServiceResume) {
        val balance =BaseActivity.balances.find { it1 -> it1.id == "usdt"}
        if (balance!=null){
            viewModel.exchangeAssetTo = asset.id
            viewModel.exchangeAssetFrom = "usdt"
            findNavController().navigate(R.id.addAmountForExchangeFragment)
        }else{

        }

      /*  CommonMethods.getViewModel<PortfolioViewModel>(requireActivity()).let {
            it.selectedAssetPriceResume = asset
            it.chosenAssets = asset
            it.screenCount = 1
            viewModel.selectedAsset = it.selectedAsset
            if (type == Constants.Exchange) {
                viewModel.exchangeAssetTo = asset.id
                findNavController().navigate(R.id.addAmountForExchangeFragment)
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }*/

    }



    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        page = 1
        viewModel.cancelJob()
        super.onDestroyView()
    }

}