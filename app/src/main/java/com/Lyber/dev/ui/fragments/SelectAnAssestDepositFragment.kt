package com.Lyber.dev.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentSelectAssestForDepostBinding
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.ui.adapters.AllAssesstAdapterDeposit
import com.Lyber.dev.ui.portfolio.action.AssestFragmentAction
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.AppLifeCycleObserver
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants

class SelectAnAssestDepositFragment : BaseFragment<FragmentSelectAssestForDepostBinding>(),
    AssestFragmentAction {
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var adapterAllAsset: AllAssesstAdapterDeposit
    private lateinit var layoutManager: LinearLayoutManager
    private val completeList: MutableList<AssetBaseData> = mutableListOf()
    override fun bind() = FragmentSelectAssestForDepostBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        layoutManager = LinearLayoutManager(requireActivity())
        binding.rvAddAsset.layoutManager = layoutManager
        adapterAllAsset = AllAssesstAdapterDeposit(::assetClicked, requireActivity())
        binding.rvAddAsset.adapter = adapterAllAsset
        binding.rvRefresh.setOnRefreshListener {
            viewModel.getAllAssets()
            binding.etSearch.setText("")
            binding.tvNoResultFound.gone()
            try {
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view.windowToken, 0)

            }catch (_:Exception){
            }
        }
        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        hitApi()
        viewModel.allAssets.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                binding.rvRefresh.isRefreshing = false
                App.prefsManager.assetBaseDataResponse = it
                adapterAllAsset.setList(it.data as ArrayList<AssetBaseData>)
                completeList.clear()
                completeList.addAll(it.data as ArrayList<AssetBaseData>)
            }
        }

        setSearchLogic()
    }

    override fun onResume() {
        super.onResume()
        if(AppLifeCycleObserver.fromBack){
            AppLifeCycleObserver.fromBack=false
            hitApi()
        }
    }

    private fun hitApi(){
        checkInternet(requireContext()){
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getAllAssets()
        }
    }

    private fun setSearchLogic() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
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
        val dummyList: MutableList<AssetBaseData> = mutableListOf()
        for (ina in completeList) {
            if (ina.id.contains(newText, true) || ina.fullName.contains(newText, true)) {
                dummyList.add(ina)
            }
        }
        adapterAllAsset.setList(dummyList)
        if (dummyList.isEmpty())
            binding.tvNoResultFound.visible()
        else
            binding.tvNoResultFound.gone()
    }

    override fun assetClicked(balance: AssetBaseData) {
        val bundle = Bundle().apply {
            putString(Constants.DATA_SELECTED, balance.id)
        }
        findNavController().navigate(R.id.chooseAssetForDepositFragment, bundle)
    }

}

