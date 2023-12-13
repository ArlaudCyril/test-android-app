package com.au.lyber.ui.fragments

import android.os.Bundle
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
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.Constants
import com.au.lyber.utils.ItemOffsetDecoration

class PickYourStrategyFragment : BaseFragment<FragmentPickYourStrategyBinding>(),
    View.OnClickListener {

    private lateinit var adapter: PickStrategyFragmentAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: PortfolioViewModel

    /* previous item clicked */

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

        viewModel.pauseStrategyResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED){
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.getStrategies()
                }
            }
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

    private fun itemClicked(position: Int) {
        viewModel.selectedStrategy = adapter.getItem(position)
        adapter.markSelected(position)
        binding.recyclerViewStrategies.smoothScrollToPosition(position)
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
        vc.mainView = view?.rootView as ViewGroup
        vc.viewModel = viewModel
        vc.show(childFragmentManager, "")

        // Add the transparent view to the RelativeLayout
        val mainView = view?.rootView as ViewGroup
        mainView.addView(transparentView, viewParams)


    }

    private fun clicked(type: Int) {
        when(type){
            -1->{
                adapter.markSelected(-1)
            }
            0->{findNavController().navigate(R.id.investAddMoneyFragment)}
            1->{
                checkInternet(requireActivity()){
                    showProgressDialog(requireActivity())
                    viewModel.pauseStrategy(viewModel.selectedStrategy!!.ownerUuid,viewModel.selectedStrategy!!.name)
                }
            }
            2->{
                checkInternet(requireActivity()){
                    showProgressDialog(requireActivity())
                    viewModel.deleteStrategy(viewModel.selectedStrategy!!.name)
                }
            }
            3->{
                val bundle = Bundle()
                bundle.putBoolean(Constants.ID,true)
                findNavController().navigate(R.id.buildStrategyFragment,bundle)
            }
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



    override fun onDestroyView() {
        super.onDestroyView()
        viewModelStore.clear()
        viewModel.getStrategiesResponse.removeObserver(getStrategies)

    }


}