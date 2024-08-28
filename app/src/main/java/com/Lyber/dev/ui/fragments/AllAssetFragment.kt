package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentAllAssetsBinding
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.ui.adapters.AllAssetAdapter
import com.Lyber.dev.utils.AppLifeCycleObserver
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.material.tabs.TabLayout

class AllAssetFragment : BaseFragment<FragmentAllAssetsBinding>(), View.OnClickListener {

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
    private var fromAddAmount = false

    private val searchText get() = binding.etSearch.text.trim().toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(Constants.TYPE, "")
            fromAddAmount = it.containsKey(Constants.FROM)
        }
    }

    override fun bind() = FragmentAllAssetsBinding.inflate(layoutInflater)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* viewModel */
        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.priceServiceResumes.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                assets.clear()
                trendings.clear()
                topLosers.clear()
                topGainers.clear()
                stables.clear()
                com.Lyber.dev.ui.activities.BaseActivity.balanceResume.clear()
                com.Lyber.dev.ui.activities.BaseActivity.balanceResume.addAll(it)
                assets.addAll(it)
                if (type == Constants.Exchange) {
                    for (asset in it) {
                        if (asset.id == viewModel.exchangeAssetFrom) {
                            assets.remove(asset)
                        }
                        if (asset.id == "usdt") {
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
                    3 -> adapter.setList(stables)
                    else -> {}
                }

                dismissProgressDialog()
                binding.rvRefresh.isRefreshing = false
            }
        }

        /* ui changes */
        binding.let {

            /* tab layout */
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = getString(R.string.trending) })
            it.tabLayout.addTab(
                it.tabLayout.newTab().apply { text = getString(R.string.top_gainers) })
            it.tabLayout.addTab(
                it.tabLayout.newTab().apply { text = getString(R.string.top_losers) })
            it.tabLayout.addTab(it.tabLayout.newTab().apply { text = getString(R.string.stable) })
            it.tabLayout.addOnTabSelectedListener(tabSelectedListener)

            adapter = AllAssetAdapter(::assetClicked, type == Constants.Exchange)
            layoutManager = LinearLayoutManager(requireContext())

            it.rvAddAsset.adapter = adapter
            it.rvAddAsset.layoutManager = layoutManager
            it.rvAddAsset.itemAnimator = null
            it.tvTitle.text = getString(R.string.all_assets)
            if (type == Constants.Exchange) {
                it.tvTitle.text = getString(R.string.exchange_to)
//                it.includedAsset.root.visible()
//                it.includedAsset.ivDropIcon.gone()
//                it.tvTitleSec.visible()
            }
            it.ivTopAction.setImageResource(R.drawable.ic_back)
            it.ivTopAction.setOnClickListener(this)
            it.etSearch.setOnClickListener(this)

            it.etSearch.addTextChangedListener(object : OnTextChange {
                override fun onTextChange() {
                    var list = mutableListOf<PriceServiceResume>()
                    if (searchText.isNotEmpty())
                        list = assets.filter {
                            it.id.startsWith(searchText, true)
                                    || CommonMethods.getAsset(it.id).fullName.startsWith(
                                searchText,
                                true
                            )
                        }.toMutableList()
                    else
                        binding.tvNoResultFound.gone()
                    when (binding.tabLayout.selectedTabPosition) {
                        0 -> {
                            if (searchText.isNotEmpty()) {
                                if (list.isEmpty())
                                    binding.tvNoResultFound.visible()
                                else
                                    binding.tvNoResultFound.gone()
                                adapter.setList(list)
                            } else adapter.setList(trendings)
                        }

                        1 -> {
                            if (searchText.isNotEmpty()) {
                                if (list.isEmpty())
                                    binding.tvNoResultFound.visible()
                                else
                                    binding.tvNoResultFound.gone()
                                adapter.setList(list)
                            } else adapter.setList(topGainers)
                        }

                        2 -> {
                            if (searchText.isNotEmpty()) {
                                if (list.isEmpty())
                                    binding.tvNoResultFound.visible()
                                else
                                    binding.tvNoResultFound.gone()
                                adapter.setList(list)
                            } else adapter.setList(topLosers)
                        }

                        3 -> {
                            if (searchText.isNotEmpty()) {
                                if (list.isEmpty())
                                    binding.tvNoResultFound.visible()
                                else
                                    binding.tvNoResultFound.gone()
                                adapter.setList(list)
                            } else adapter.setList(stables)
                        }

                        else -> {

                        }
                    }
                }
            })

        }

        hitApi()

        binding.rvRefresh.setOnRefreshListener {
            viewModel.getAllPriceResume()
            binding.etSearch.setText("")
            binding.tvNoResultFound.gone()
            try {
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view.windowToken, 0)

            } catch (_: Exception) {
            }
        }


    }

    override fun onResume() {
        super.onResume()
        if(AppLifeCycleObserver.fromBack){
            AppLifeCycleObserver.fromBack=false
            hitApi()
        }
    }
    private fun hitApi() {
        checkInternet(binding.root,requireContext()) {
            showProgressDialog(requireContext())
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
            binding.etSearch.setText("")
            binding.tvNoResultFound.gone()
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
        getViewModel<PortfolioViewModel>(requireActivity()).let {
            it.selectedAssetPriceResume = asset
            it.chosenAssets = asset
            it.screenCount = 1
            viewModel.selectedAsset = it.selectedAsset
            if (fromAddAmount) {
                viewModel.exchangeAssetTo = asset.id
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else if (type == Constants.Exchange) {
//                findNavController().popBackStack()
                viewModel.exchangeAssetTo = asset.id
                findNavController().navigate(R.id.addAmountForExchangeFragment)

            } else if (type == "assets") {
                findNavController().popBackStack()
                viewModel.selectedAsset = CommonMethods.getAsset(asset.id)
                viewModel.selectedBalance = CommonMethods.getBalance(asset.id)
                findNavController().navigate(R.id.portfolioDetailFragment)
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

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