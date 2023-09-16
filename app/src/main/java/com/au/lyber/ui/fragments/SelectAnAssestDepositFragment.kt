package com.au.lyber.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSelectAssestForDepostBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.ui.adapters.AllAssesstAdapterDeposit
import com.au.lyber.ui.portfolio.action.AssestFragmentAction
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.Constants

class SelectAnAssestDepositFragment : BaseFragment<FragmentSelectAssestForDepostBinding>()
,AssestFragmentAction{
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapterAllAsset: AllAssesstAdapterDeposit
    private lateinit var layoutManager: LinearLayoutManager
    private val completeList :MutableList<AssetBaseData> = mutableListOf()
    override fun bind()= FragmentSelectAssestForDepostBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        layoutManager = LinearLayoutManager(requireActivity())
        binding.rvAddAsset.layoutManager = layoutManager
        adapterAllAsset = AllAssesstAdapterDeposit(::assetClicked,requireActivity())
        binding.rvAddAsset.adapter = adapterAllAsset
        binding.rvRefresh.setOnRefreshListener {
            viewModel.getAllAssets()
        }
        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        viewModel.getAllAssets()
        viewModel.allAssets.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.rvRefresh.isRefreshing = false
                App.prefsManager.assetBaseDataResponse = it
               adapterAllAsset.setList(it.data as ArrayList<AssetBaseData>)
                completeList.clear()
                completeList.addAll(it.data as ArrayList<AssetBaseData>)
            }
        }

        setSearchLogic()
    }

    private fun setSearchLogic() {
        binding.etSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                searchingInList(s.toString())
            }

        })

    }

    private fun searchingInList(newText: String) {
        val dummyList  :MutableList<AssetBaseData> = mutableListOf()
        for (ina in completeList){
            if (ina.id.contains(newText,true) || ina.fullName.contains(newText,true)){
                dummyList.add(ina)
            }
        }
        adapterAllAsset.setList(dummyList)
    }

    override fun assetClicked(balance: AssetBaseData) {
        val bundle = Bundle().apply {
            putString(Constants.DATA_SELECTED,balance.id)
        }
        findNavController().navigate(R.id.chooseAssetForDepositFragment,bundle)
    }

}

