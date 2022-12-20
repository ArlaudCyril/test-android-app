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
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
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

    private var searching: Boolean = false

    private var assets = mutableListOf<AssetData>()

    private var trendings = mutableListOf<Data>()
    private var topGainers = mutableListOf<Data>()
    private var topLosers = mutableListOf<Data>()
    private var stables = mutableListOf<Data>()

    private var recursiveItems = false

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

        viewModel.trendingCoinResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                /* remove refresh loader */
                binding.rvRefresh.isRefreshing = false

                /* remove bottom progress */
//                if (page > 1 && !searching)
//                    adapter.removeProgress()


                it.data?.let { data ->

//                    if (searching) {
//                        adapter.setList(data)
//                    } else

                    when (binding.tabLayout.selectedTabPosition) {

                        0 -> {
                            if (page == 1)
                                trendings.clear()
                            trendings.addAll(data)
                        }

                        1 -> {
                            if (page == 1)
                                topGainers.clear()
                            topGainers.addAll(data)
                        }

                        2 -> {
                            if (page == 1)
                                topLosers.clear()
                            topLosers.addAll(data)
                        }

                        else -> {
                            if (page == 1)
                                stables.clear()
                            stables.addAll(data)
                        }

                    }

                    shouldLoad = data.count() > limit - 1

//                    if (page == 1) {
//                        binding.rvAddAsset.startLayoutAnimation()
                    /*if (recursiveItems) {
                        if (adapter.itemCount == data.count()) {
                            for (position in 0 until data.count()) {
                                adapter.getItem(position)?.let {
                                    if (it.current_price != data[position].current_price) {
                                        adapter.changeItemAt(position, data[position])
                                    }
                                }
                            }
                        } else adapter.setList(data)
                    } else adapter.setList(data)*/
//                    } else adapter.addList(data)

                    /*Handler(Looper.getMainLooper()).postDelayed({
                        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                            recursiveItems = true
                            getCoins()
                        }
                    }, 3000)*/

                    binding.rvRefresh.isRefreshing = false

                }
            }
        }

        viewModel.assetsResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                assets.addAll(it.data)
                adapter.addList(it.data)
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

            binding.rvRefresh.setOnRefreshListener {
                viewModel.cancelJob()
                page = 1
            }

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
                    viewModel.cancelJob()
                }
            })

        }

        viewModel.getAssetList()

    }


    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab?) {
            page = 1
            adapter.clearList()
            viewModel.cancelJob()
            when (tab?.position) {
                0 -> {}
                1 -> {}
                2 -> {}
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