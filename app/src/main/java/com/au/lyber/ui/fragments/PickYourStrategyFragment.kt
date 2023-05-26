package com.au.lyber.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentPickYourStrategyBinding
import com.au.lyber.models.MessageResponse
import com.au.lyber.models.StrategiesResponse
import com.au.lyber.models.Strategy
import com.au.lyber.ui.adapters.PickStrategyFragmentAdapter
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.ItemOffsetDecoration
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel

class PickYourStrategyFragment : BaseFragment<FragmentPickYourStrategyBinding>(),
    View.OnClickListener {

    private lateinit var adapter: PickStrategyFragmentAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: PortfolioViewModel

    /* previous item clicked */
    private var previousPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dismissProgressDialog()
    }

    override fun bind() = FragmentPickYourStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.prefsManager.savedScreen = javaClass.name

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        adapter = PickStrategyFragmentAdapter(::itemClicked)

        layoutManager = LinearLayoutManager(requireContext())

        viewModel.strategyPositionSelected.observe(viewLifecycleOwner, selectedStrategy)
        viewModel.getStrategiesResponse.observe(viewLifecycleOwner, getStrategies)
        viewModel.selectedStrategyResponse.observe(viewLifecycleOwner, chooseStrategy)

        binding.apply {
            recyclerViewStrategies.let {
                it.adapter = adapter
                it.layoutManager = layoutManager
                it.isNestedScrollingEnabled = false
                it.addItemDecoration(ItemOffsetDecoration(12))
            }
            ivTopAction.setOnClickListener(this@PickYourStrategyFragment)
            tvBuildYourStrategy.setOnClickListener(this@PickYourStrategyFragment)
            btnChooseStrategy.setOnClickListener(this@PickYourStrategyFragment)
        }

        checkInternet(requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getStrategies()
        }

    }

    private val getStrategies = Observer<StrategiesResponse> {
        dismissProgressDialog()

        adapter.setList(it.strategies)

        val strategy: Strategy? = it.strategies.find { it.is_chosen == 1 }
        val position = it.strategies.indexOf(strategy)
        previousPosition = position
        viewModel.strategyPositionSelected.postValue(position)

        binding.recyclerViewStrategies.startLayoutAnimation()
    }

    private val selectedStrategy = Observer<Int> {
        if (it == -1) {
            binding.btnChooseStrategy.background =
                getDrawable(requireContext(), R.drawable.button_purple_400)
        } else {
            binding.btnChooseStrategy.background =
                getDrawable(requireContext(), R.drawable.button_purple_500)
        }
    }

    private val chooseStrategy = Observer<MessageResponse> {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            dismissProgressDialog()
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                InvestMoneyFragment(),
                topBottom = true
            )
        }
    }

    private fun itemClicked(position: Int) {

        val currentView = (layoutManager.getChildAt(position) as StrategyView)
        when (previousPosition) {
            -1 -> { // no one is selected yet
                currentView.isStrategySelected = true
                currentView.background = getDrawable(
                    requireContext(),
                    R.drawable.round_stroke_purple_500
                )
                currentView.radioButton.setImageResource(R.drawable.radio_select)
                previousPosition = position
                viewModel.strategyPositionSelected.postValue(previousPosition)
            }
            position -> { // previous
                if (currentView.isStrategySelected) { // unselected the item
                    currentView.isStrategySelected = false
                    currentView.background = getDrawable(
                        requireContext(),
                        R.drawable.round_stroke_gray_100
                    )
                    currentView.radioButton.setImageResource(R.drawable.radio_unselect)
                    previousPosition = -1
                    viewModel.strategyPositionSelected.postValue(previousPosition)
                } else { // select the item
                    currentView.isStrategySelected = true
                    currentView.background = getDrawable(
                        requireContext(),
                        R.drawable.round_stroke_purple_500
                    )
                    currentView.radioButton.setImageResource(R.drawable.radio_select)
                    previousPosition = position
                    viewModel.strategyPositionSelected.postValue(previousPosition)
                }
            }
            else -> {
                val previousView = (layoutManager.getChildAt(previousPosition) as StrategyView)

                previousView.isStrategySelected = false
                currentView.isStrategySelected = true
                currentView.background = getDrawable(
                    requireContext(),
                    R.drawable.round_stroke_purple_500
                )
                currentView.radioButton.setImageResource(R.drawable.radio_select)
                previousView.background = getDrawable(
                    requireContext(),
                    R.drawable.round_stroke_gray_100
                )
                previousView.radioButton.setImageResource(R.drawable.radio_unselect)
                previousPosition = position
                viewModel.strategyPositionSelected.postValue(previousPosition)
            }
        }

        Log.d(TAG, "itemClicked: $previousPosition $position ")

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressed()

                tvBuildYourStrategy -> {
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        BuildStrategyFragment(),
                        topBottom = true
                    )
                }
                btnChooseStrategy -> {
                    if (previousPosition != -1) {
                        viewModel.selectedStrategy = adapter.getItem(previousPosition)
                        checkInternet(requireContext()) {
                            showProgressDialog(requireContext())
                            viewModel.chooseStrategy()
                        }
                    } else "Please selected an strategy".showToast(requireContext())
                }
            }
        }
    }

    companion object {
        private const val TAG = "PickYourStrategyFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelStore.clear()
    }


    /* static data */
//    private fun getStrategies(): List<Strategy> {
//        val list = mutableListOf<Strategy>()
//        list.add(Strategy("Safe", "~5% APY", "Low", assetsOne))
//        list.add(Strategy("Intermediate", "~10% APY", "Medium", assetsTwo))
//        list.add(Strategy("Bold", "~15% APY", "High", assetsThree))
//        return list
//    }

/*    private val assetsOne = mutableListOf<AssetItems>().apply {
        add(AssetItems("USDC", 50, R.color.purple_600))
        add(AssetItems("USDT", 50, R.color.purple_400))
    }

    private val assetsTwo = mutableListOf<AssetItems>().apply {
        add(AssetItems("USDC", 50, R.color.purple_600))
        add(AssetItems("BTC", 25, R.color.purple_400))
        add(AssetItems("ETH", 25, R.color.purple_200))
    }

    private val assetsThree = mutableListOf<AssetItems>().apply {
        add(AssetItems("BTC", 20, R.color.purple_800))
        add(AssetItems("ETH", 20, R.color.purple_700))
        add(AssetItems("SOL", 10, R.color.purple_600))
        add(AssetItems("AVAX", 10, R.color.purple_500))
        add(AssetItems("AVA", 10, R.color.purple_400))
        add(AssetItems("XRP", 10, R.color.purple_300))
        add(AssetItems("FTT", 10, R.color.purple_100))
        add(AssetItems("MATIC", 10, R.color.purple_00))
    }*/

}