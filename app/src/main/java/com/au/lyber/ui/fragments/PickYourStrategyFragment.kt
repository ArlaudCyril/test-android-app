package com.au.lyber.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentPickYourStrategyBinding
import com.au.lyber.models.MessageResponse
import com.au.lyber.models.StrategiesResponse
import com.au.lyber.ui.adapters.PickStrategyFragmentAdapter
import com.au.lyber.ui.fragments.bottomsheetfragments.InvestWithStrategyBottomSheet
import com.au.lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.ItemOffsetDecoration

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
            btnChooseStrategy.setOnClickListener(this@PickYourStrategyFragment)
        }

        checkInternet(requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getStrategies()
        }

    }

    private val getStrategies = Observer<StrategiesResponse> {
        dismissProgressDialog()
        adapter.setList(it.data)
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

    private fun itemClicked(position: Int,currentView:StrategyView) {

        when (previousPosition) {
            -1 -> { // no one is selected yet
                currentView.isStrategySelected = true
                currentView.background = getDrawable(
                    requireContext(),
                    R.drawable.round_stroke_purple_500
                )
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
                    previousPosition = position
                    viewModel.strategyPositionSelected.postValue(previousPosition)
                }
            }
            else -> {
                try {
                    val previousView = (layoutManager.getChildAt(previousPosition) as StrategyView)
                    if (previousView!=null) {
                        previousView.isStrategySelected = false
                        currentView.isStrategySelected = true
                        currentView.background = getDrawable(
                            requireContext(),
                            R.drawable.round_stroke_purple_500
                        )
                        previousView.background = getDrawable(
                            requireContext(),
                            R.drawable.round_stroke_gray_100
                        )
                        previousView.radioButton.setImageResource(R.drawable.radio_unselect)

                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                previousPosition = position
                viewModel.strategyPositionSelected.postValue(previousPosition)
            }
        }
        viewModel.selectedStrategy = adapter.getItem(position)
        val transparentView = View(context)
        transparentView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.semi_transparent_dark
            )
        )

        // Set layout parameters for the transparent view
        val viewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )

        val vc  = InvestWithStrategyBottomSheet(::clicked)
        vc.viewToDelete = transparentView
        vc.mainView = getView()?.rootView as ViewGroup
        vc.show(childFragmentManager, "")

        // Add the transparent view to the RelativeLayout
        val mainView = getView()?.rootView as ViewGroup
        mainView.addView(transparentView, viewParams)

        Log.d(TAG, "itemClicked: $previousPosition $position ")

    }

    private fun clicked(type: Int) {
        when(type){
            0->{findNavController().navigate(R.id.investAddMoneyFragment)}
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnChooseStrategy -> {
                    findNavController().navigate(R.id.buildStrategyFragment)
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


}