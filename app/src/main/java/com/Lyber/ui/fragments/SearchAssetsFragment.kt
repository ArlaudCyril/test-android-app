package com.Lyber.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.databinding.AppItemLayoutBinding
import com.Lyber.databinding.FragmentSearchAssetsBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.GetAssetsResponseItem
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.viewmodels.PortfolioViewModel

class SearchAssetsFragment :
    BaseFragment<FragmentSearchAssetsBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapter: SearchAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val keyword: String get() = binding.etSearch.text.trim().toString()

    override fun bind() = FragmentSearchAssetsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SearchAdapter(::itemClicked)
        layoutManager = LinearLayoutManager(requireContext())
        viewModel = getViewModel(requireActivity())

        viewModel.assetsToChoose.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.rvRefresh.isRefreshing = false
                adapter.setList(it)
                binding.rvAssets.startLayoutAnimation()
            }
        }

        binding.etSearch.addTextChangedListener(onTextChange)
        binding.rvAssets.adapter = adapter
        binding.rvAssets.layoutManager = layoutManager

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }
        binding.rvRefresh.setOnRefreshListener {
            viewModel.cancelJob()
            viewModel.assetsToChoose()
        }
        getAssets()
    }


    private fun itemClicked(item: GetAssetsResponseItem) {

        /*val asset = AssetBaseData(
            asset_id = item.asset_id,
            asset_name = item.asset_name.lowercase(),
            euro_amount = 0.0,
            total_balance = 0.0,
            coin_detail = null
        ) TODO*/

        //viewModel.selectedAsset = asset TODO
        viewModel.screenCount = 1
        requireActivity().onBackPressed()

    }

    private fun getAssets(keyword: String = "") {
        checkInternet(binding.root,requireContext()) {
            binding.rvRefresh.isRefreshing = true
            viewModel.assetsToChoose(keyword)
        }
    }

    private val onTextChange = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.cancelJob()
            getAssets(keyword)
        }

    }


    class SearchAdapter(private var clickListener: (GetAssetsResponseItem) -> Unit = { _ -> }) :
        BaseAdapter<GetAssetsResponseItem>() {


        override fun getItemViewType(position: Int): Int {
            return if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ORDINARY_VIEW -> AssetViewHolder(
                    AppItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )

                else -> LoaderViewHolder(
                    LoaderViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                ORDINARY_VIEW -> {
                    itemList[position]?.let {
                        (holder as AssetViewHolder).bind.apply {
                            ivItem.loadCircleCrop(it.image)

                            tvStartTitle.text = it.asset_name
                            tvStartSubTitle.text = it.symbol.uppercase()

                        }
                    }
                }
                else -> {

                }
            }
        }


        inner class AssetViewHolder(val bind: AppItemLayoutBinding) :
            RecyclerView.ViewHolder(bind.root) {
            init {
                bind.root.setOnClickListener {
                    itemList[adapterPosition]?.let(clickListener)
                }
            }
        }

    }
}