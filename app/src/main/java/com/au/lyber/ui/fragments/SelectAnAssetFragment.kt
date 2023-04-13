package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSelectAnAssetBinding
import com.au.lyber.databinding.ItemAssetBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.Data
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.extractAsset
import com.au.lyber.utils.CommonMethods.Companion.formLineData
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.PortfolioViewModel
import com.google.android.material.tabs.TabLayout

class SelectAnAssetFragment : BaseFragment<FragmentSelectAnAssetBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapter: AddAssetAdapter

    private lateinit var layoutManager: LinearLayoutManager

    private var page: Int = 1
    private var limit: Int = 100
    private var shouldLoad: Boolean = true

    private var trendings = mutableListOf<Data>()
    private var topGainers = mutableListOf<Data>()
    private var topLosers = mutableListOf<Data>()
    private var stables = mutableListOf<Data>()

    private var recursiveItems = false

    override fun bind() = FragmentSelectAnAssetBinding.inflate(layoutInflater)

    @SuppressLint("FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        page = 1
        /* viewModel */
        viewModel = getViewModel(requireActivity())
        viewModel.selectedOption = Constants.USING_SINGULAR_ASSET

        /* observer */
        viewModel.trendingCoinResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

//                if (page > 1) adapter.removeProgress()

                it.data?.let { data ->

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

                    if (recursiveItems) {
                        if (adapter.itemCount == data.count()) {
                            for (position in 0 until data.count()) {
                                adapter.getItem(position)?.let {
                                    if (it.current_price != data[position].current_price) {
                                        adapter.changeItemAt(position, data[position])
                                    }
                                }
                            }
                        } else adapter.setList(data)
                    } else adapter.setList(data)
//                    } else adapter.addList(data)

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                            recursiveItems = true
                            getCoins()
                        }
                    }, 3000)


                    binding.rvRefresh.isRefreshing = false

//                    if (page == 1) adapter.setList(data)
//                    else adapter.addList(data)

                }

            }
        }

        /* ui */
        binding.tabLayout.let {
            for (i in 0 until 4) {
                val tab = when (i) {
                    0 -> it.newTab().apply { text = "Trending" }
                    1 -> it.newTab().apply { text = "Top gainers" }
                    2 -> it.newTab().apply { text = "Top loosers" }
                    else -> it.newTab().apply { text = "Stable" }
                }
                it.addTab(tab)
            }
            it.addOnTabSelectedListener(tabSelectedListener)
        }

        binding.rvAddAsset.let {
            it.isNestedScrollingEnabled = false
            adapter = AddAssetAdapter(::onAssetSelected)
            layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
            it.layoutManager = layoutManager
//            it.addOnScrollListener(object : ScrollListener(layoutManager) {
//                override fun shouldLoad() = shouldLoad
//                override fun loadMore() {
//                    if (!binding.rvRefresh.isRefreshing) {
//                        shouldLoad = false
//                        page++
//                        adapter.addProgress()
//                        getCoins()
//                    }
//                }
//            })
        }

        binding.tvTitle.text = "Choose an asset"
        binding.ivTopAction.setImageResource(R.drawable.ic_back)
        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.rvRefresh.setOnRefreshListener {
            viewModel.cancelJob()
            page = 1
//            adapter.removeProgress()
            when (binding.tabLayout.selectedTabPosition) {
                0 -> getCoins()
                1 -> getCoins(Constants.HOURS_24_DESC)
                2 -> getCoins(Constants.HOURS_24_ASC)
                else -> getCoins(Constants.STABLE_COINS)
            }
        }

        binding.etSearch.addTextChangedListener(object : OnTextChange {
            override fun onTextChange() {
                viewModel.cancelJob()
                if (binding.etSearch.text.trim().isEmpty()) {
                    binding.rvRefresh.isRefreshing = true
                    getCoins()
//                        adapter.setResults()
                } else
                    checkInternet(requireContext()) {
                        getCoins()
                    }
            }
        })

        /* api call */
        binding.rvRefresh.isRefreshing = true
        getCoins()
    }


    override fun onDestroy() {
        viewModel.cancelJob()
        super.onDestroy()
    }

    override fun onDestroyView() {
        viewModel.cancelJob()
        super.onDestroyView()
    }

    private fun getCoins(category: String = Constants.VOLUME_DESC) {
        checkInternet(requireContext()) {
//            if (page == 1)
//                binding.rvRefresh.isRefreshing = true
            if (binding.etSearch.text.trim().isEmpty()) {
                when (binding.tabLayout.selectedTabPosition) {
                    0 -> viewModel.getCoins(limit = limit, order = category)
                    1 -> viewModel.getCoins(limit = limit, order = Constants.HOURS_24_DESC)
                    2 -> viewModel.getCoins(limit = limit, order = Constants.HOURS_24_ASC)
                    else -> viewModel.getCoins(limit = limit, order = Constants.STABLE_COINS)
                }
            } else {
                when (binding.tabLayout.selectedTabPosition) {
                    0 -> viewModel.getCoins(
                        order = Constants.VOLUME_DESC,
                        keyword = binding.etSearch.text.trim().toString()
                    )
                    1 -> viewModel.getCoins(
                        order = Constants.HOURS_24_DESC,
                        keyword = binding.etSearch.text.trim().toString()
                    )
                    2 -> viewModel.getCoins(
                        order = Constants.HOURS_24_ASC,
                        keyword = binding.etSearch.text.trim().toString()
                    )
                    else -> viewModel.getCoins(
                        order = Constants.STABLE_COINS,
                        keyword = binding.etSearch.text.trim().toString()
                    )
                }
            }
//            viewModel.getCoins(limit = limit, order = category)
//            viewModel.getCoins(page, limit, category)
        }
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab?) {
            page = 1
            adapter.clearList()
            viewModel.cancelJob()
            recursiveItems = false
            binding.rvRefresh.isRefreshing = true

            when (tab?.position) {
                0 -> {
                    adapter.setList(trendings)
                    getCoins()
                }
                1 -> {
                    adapter.setList(topGainers)
                    getCoins(Constants.HOURS_24_DESC)
                }
                2 -> {
                    adapter.setList(topLosers)
                    getCoins(Constants.HOURS_24_ASC)
                }
                else -> {
                    adapter.setList(stables)
                    getCoins(Constants.STABLE_COINS)
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    private fun onAssetSelected(item: Data) {
        viewModel.selectedAsset = item.extractAsset()
        requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
    }

    class AddAssetAdapter(private val clickListener: (Data) -> Unit = { _ -> }) :
        BaseAdapter<Data>() {

        override fun getItemViewType(position: Int) =
            if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == LOADER_VIEW) LoaderViewHolder(
                LoaderViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else ViewHolder(
                ItemAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        inner class ViewHolder(val bind: ItemAssetBinding) : RecyclerView.ViewHolder(bind.root) {
            init {
                bind.root.setOnClickListener {
                    itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
                }
                bind.lineChart.setOnClickListener {
                    itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (itemList[position] == null) {
                (holder as BaseAdapter<*>.LoaderViewHolder).bind.ivLoader.animation =
                    AnimationUtils.loadAnimation(
                        holder.bind.ivLoader.context,
                        R.anim.rotate_drawable
                    )
            } else {
                (holder as ViewHolder).bind.apply {

                    itemList[position]?.let {

                        ivAsset.loadCircleCrop(it.image)

                        if (it.price_change_percentage_24h > 0) {
                            tvAssetVariation.text =
                                "+${it.price_change_percentage_24h.toString().roundFloat()}%"
                            tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))

//                                lineChart.setLineData(
//                                it.sparkline_in_7d.price.formLineData(),
//                                R.color.green_500,
//                                R.drawable.drawable_green_fill_line_chart
//                            )
                        } else {
                            tvAssetVariation.text =
                                "${it.price_change_percentage_24h.toString().roundFloat()}%"
                            tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))
//                            lineChart.setLineData(
//                                it.sparkline_in_7d.price.formLineData(),
//                                R.color.red_500,
//                                R.drawable.drawable_red_fill_line_chart
//                            )
                        }

                        tvAssetName.text = it.name
                        tvAssetNameCode.text = it.symbol
                        tvAssetValue.text =
                            "${it.current_price.toString().roundFloat()}${Constants.EURO}"
                    }
                }

            }
        }


        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            super.onViewAttachedToWindow(holder)

            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                itemList[holder.adapterPosition]?.let {
                    (holder as ViewHolder).bind.root.animation =
                        AnimationUtils.loadAnimation(
                            holder.bind.root.context,
                            R.anim.zoom_in
                        )
                }
            }

        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            if (holder.adapterPosition != RecyclerView.NO_POSITION)
                itemList[holder.adapterPosition]?.let {
                    (holder as ViewHolder).bind.root.clearAnimation()
                }
        }


    }
}