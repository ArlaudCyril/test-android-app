package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
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
import com.Lyber.R
import com.Lyber.databinding.ItemAssetBinding
import com.Lyber.databinding.LayoutAddAnAssetBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.Data
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.roundFloat
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout

class AddAssetBottomSheet(private val clickListener: (Data) -> Unit = { _ -> }) :
    BaseBottomSheet<LayoutAddAnAssetBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapter: AddAssetAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var page: Int = 1
    private var limit: Int = 100
    private var shouldLoad: Boolean = true

    private var searching: Boolean = false

    private var trendings = mutableListOf<Data>()
    private var topGainers = mutableListOf<Data>()
    private var topLosers = mutableListOf<Data>()
    private var stables = mutableListOf<Data>()


    private var recursiveItems = false
    override fun bind() = LayoutAddAnAssetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* viewModel */
        viewModel = getViewModel(this)

        /* observers */
        viewModel.trendingCoinResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {


                binding.rvAddAsset.visible()
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

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                            recursiveItems = true
                            getCoins()
                        }
                    }, 3000)

                    binding.rvRefresh.isRefreshing = false

                }

            }
        }

        /* ui */
        binding.apply {

            tabLayout.let {
                for (i in 0 until 4) {
                    val tab = when (i) {
                        0 -> it.newTab().apply { text = getString(R.string.trending) }
                        1 -> it.newTab().apply { text = getString(R.string.top_gainers) }
                        2 -> it.newTab().apply { text = getString(R.string.top_losers) }
                        else -> it.newTab().apply { text = getString(R.string.stable) }
                    }
                    it.addTab(tab)
                }
                it.addOnTabSelectedListener(tabChangeListener)
            }

            rvAddAsset.let {

                adapter = AddAssetAdapter(clickListener, this@AddAssetBottomSheet)
                layoutManager = LinearLayoutManager(requireContext())

                it.adapter = adapter
                it.layoutManager = layoutManager
                it.isNestedScrollingEnabled = false


            }

            ivTopAction.setOnClickListener { dismiss() }
        }

        binding.rvRefresh.setOnRefreshListener {
            viewModel.cancelJob()
            page = 1
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
                    searching = false
                    binding.rvRefresh.isRefreshing = true
                    getCoins()
                } else
                    checkInternet(requireContext()) {
                        searching = true
                        getCoins()
                    }
            }
        })


        /* api call */
        binding.rvRefresh.isRefreshing = true
        getCoins()

    }


    override fun onDismiss(dialog: DialogInterface) {
        viewModel.cancelJob()
        super.onDismiss(dialog)
    }

    private val tabChangeListener = object : TabLayout.OnTabSelectedListener {

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

    private fun getCoins(category: String = Constants.VOLUME_DESC) {
        checkInternet(requireContext()) {

            /*if (page == 1)
                binding.rvRefresh.isRefreshing = true*/
            if (binding.etSearch.text.trim().isEmpty()) {
                when (binding.tabLayout.selectedTabPosition) {
                    0 -> viewModel.getCoins(order = Constants.VOLUME_DESC)
                    1 -> viewModel.getCoins(order = Constants.HOURS_24_DESC)
                    2 -> viewModel.getCoins(order = Constants.HOURS_24_ASC)
                    else -> viewModel.getCoins(order = Constants.STABLE_COINS)
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
//            viewModel.getCoins(page, limit, category)

//            viewModel.getCoins(page, limit, category)
        }
    }


    class AddAssetAdapter(
        private val clickListener: (Data) -> Unit = { _ -> },
        private val bottomSheet: BottomSheetDialogFragment
    ) : BaseAdapter<Data>() {


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

                        if (it.price_change_percentage_24h > 0) {
                            tvAssetVariation.text =
                                "${
                                    it.price_change_percentage_24h.toString()
                                        .roundFloat().commaFormatted
                                }%"
                            tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))

//                            lineChart.setLineData(
//                                it.sparkline_in_7d.price.formLineData(),
//                                R.color.green_500,
//                                R.drawable.drawable_green_fill_line_chart
//                            )
                        } else {
                            tvAssetVariation.text =
                                "${
                                    it.price_change_percentage_24h.toString()
                                        .roundFloat().commaFormatted
                                }%"
                            tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))
//                            lineChart.setLineData(
//                                it.sparkline_in_7d.price.formLineData(),
//                                R.color.red_500,
//                                R.drawable.drawable_red_fill_line_chart
//                            )
                        }
                            ivAsset.loadCircleCrop(it.image)

                        tvAssetName.text = it.name
                        tvAssetNameCode.text = it.symbol
                        tvAssetValue.text =
                            "${it.current_price.toString().roundFloat().commaFormatted} €"

                    }
                }
            }
        }

        inner class ViewHolder(val bind: ItemAssetBinding) : RecyclerView.ViewHolder(bind.root) {
            init {
                bind.root.setOnClickListener {
                    itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
                    bottomSheet.dismiss()
                }
                bind.lineChart.setOnClickListener {
                    itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
                    bottomSheet.dismiss()
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