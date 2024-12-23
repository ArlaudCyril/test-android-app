package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.R
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.databinding.FragmentAllAssetsBinding
import com.Lyber.models.PriceServiceResume
import com.Lyber.ui.adapters.AllAssetAdapter
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.AppLifeCycleObserver
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.google.android.material.bottomsheet.BottomSheetDialog
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
                com.Lyber.ui.activities.BaseActivity.balanceResume.clear()
                com.Lyber.ui.activities.BaseActivity.balanceResume.addAll(it)
                assets.addAll(it)
                if (type == Constants.Exchange) {
                    for (asset in it) {
                        if (asset.id == viewModel.exchangeAssetFrom) {
                            assets.remove(asset)
                        }
                    }
                }
                val usdtAsset = it.find { it.id == "usdt" }
                if(usdtAsset!=null)
                assets.remove(usdtAsset)

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
            it.tvTitle.text = getString(R.string.choose_an_asset)

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
        if (AppLifeCycleObserver.fromBack) {
            AppLifeCycleObserver.fromBack = false
            hitApi()
        }
    }

    private fun hitApi() {
        CommonMethods.checkInternet(binding.root,requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
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
        val balance =
            com.Lyber.ui.activities.BaseActivity.balances.find { it1 -> it1.id == Constants.MAIN_ASSET }
        if (asset.id == Constants.MAIN_ASSET) {

            if (checkKyc()) {
                val arguments = Bundle().apply {
                    putString(Constants.FROM, SelectAssestForBuy::class.java.name)
                }
                findNavController().navigate(R.id.buyUsdt, arguments)
            }
        } else if (balance != null) {
            viewModel.exchangeAssetTo = asset.id
            viewModel.exchangeAssetFrom = Constants.MAIN_ASSET
            findNavController().navigate(R.id.addAmountForExchangeFragment)
        } else {
            if (App.prefsManager.user!!.kycStatus == "OK" && App.prefsManager.user!!.yousignStatus == "SIGNED")
                showDialog()
            else checkKyc()
        }
        //showDialog()*


    }


    private fun showDialog() {
        BottomSheetDialog(requireContext(), R.style.CustomDialogBottomSheet).apply {
            CustomDialogVerticalLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                App.prefsManager.accountCreationSteps = Constants.Account_CREATION_STEP_PHONE
                setContentView(it.root)
                it.tvTitle.text = getString(R.string.buy_usdc)
                it.tvMessage.text =
                    getString(R.string.usdt_error)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.buy_usdc)
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
//                    viewModel.selectedAsset=assetUsdt
//                    findNavController().navigate(R.id.buyUsdt)
                    val arguments = Bundle().apply {
                        putString(Constants.FROM, SelectAssestForBuy::class.java.name)
                    }
                    findNavController().navigate(R.id.buyUsdt, arguments)

                }
                show()
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