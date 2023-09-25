package com.au.lyber.ui.fragments.bottomsheetfragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAssetBinding
import com.au.lyber.databinding.LayoutAddAnAssetBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.PriceServiceResume
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.loadImage
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.Constants
import com.au.lyber.utils.OnTextChange
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout

class AddAssetBottomSheet(private val clickListener: (PriceServiceResume) -> Unit = { _ -> }) :
    BaseBottomSheet<LayoutAddAnAssetBinding>(),View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapter: AddAssetAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var page: Int = 1
    private var assets = mutableListOf<PriceServiceResume>()
    private var trendings = mutableListOf<PriceServiceResume>()
    private var topGainers = mutableListOf<PriceServiceResume>()
    private var topLosers = mutableListOf<PriceServiceResume>()
    private var stables = mutableListOf<PriceServiceResume>()
    private val searchText get() = binding.etSearch.text.trim().toString()

    private var recursiveItems = false
    override fun bind() = LayoutAddAnAssetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* viewModel */

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
                BaseActivity.balanceResume.clear()
                BaseActivity.balanceResume.addAll(it)
                assets.addAll(it)
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

            adapter = AddAssetAdapter(clickListener,this)
            layoutManager = LinearLayoutManager(requireContext())

            it.rvAddAsset.adapter = adapter
            it.rvAddAsset.layoutManager = layoutManager
            it.rvAddAsset.itemAnimator = null
            it.tvTitle.text = getString(R.string.all_assets)
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

        checkInternet(requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAllPriceResume()
        }

        binding.rvRefresh.setOnRefreshListener {
            viewModel.getAllPriceResume()
        }


    }
    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun List<PriceServiceResume>.topLosers(): List<PriceServiceResume> {
        return sortedBy { it.priceServiceResumeData.change.toFloat() }
    }

    private fun List<PriceServiceResume>.stables(): List<PriceServiceResume> {
        return filter { CommonMethods.getAsset(it.id).isStablecoin }
    }
    override fun onDismiss(dialog: DialogInterface) {
        viewModel.cancelJob()
        super.onDismiss(dialog)
    }

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
        }
    }


    class AddAssetAdapter(
        private val clickListener: (PriceServiceResume) -> Unit = { _ -> },
        private val bottomSheet: BottomSheetDialogFragment
    ) : BaseAdapter<PriceServiceResume>() {


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
            else ViewHolderAsset(
                ItemAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (itemList[position] != null) {
                (holder as ViewHolderAsset).bind.apply {
                    itemList[position]?.let {
                        if (it.priceServiceResumeData.change.roundFloat().toFloat() > 0) {
                            tvAssetVariation.text = "+${it.priceServiceResumeData.change.roundFloat().commaFormatted}%"
                            tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))
                        } else {
                            tvAssetVariation.text = "${it.priceServiceResumeData.change.roundFloat().commaFormatted}%"
                            tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))

                        }
                        val urlLineChart = it.priceServiceResumeData.squiggleURL
                        lineChart.loadImage(urlLineChart)
                        val id = it.id
                        BaseActivity.assets.firstNotNullOfOrNull{ item -> item.takeIf {item.id == id}}
                            ?.let {
                                    it1 -> ivAsset.loadCircleCrop(it1.imageUrl); tvAssetName.text = it1.fullName
                            }

                        val context = ivAsset.context
                        tvAssetValue.typeface = context.resources.getFont(R.font.mabry_pro_medium)
                        tvAssetValue.setTextColor(context.getColor(R.color.purple_gray_700))
                        ivAsset.context.resources.getFont(R.font.mabry_pro_medium)
                        tvAssetNameCode.text = it.id.uppercase()
                        tvAssetValue.text = it.priceServiceResumeData.lastPrice.currencyFormatted

                    }
                }
            }
        }

        inner class ViewHolderAsset(val bind: ItemAssetBinding) : RecyclerView.ViewHolder(bind.root) {
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
                    (holder as ViewHolderAsset).bind.root.animation =
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
                    (holder as ViewHolderAsset).bind.root.clearAnimation()
                }
        }

    }

}