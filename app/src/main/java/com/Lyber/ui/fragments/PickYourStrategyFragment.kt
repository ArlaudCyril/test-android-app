package com.Lyber.ui.fragments

import android.content.Context
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
import androidx.recyclerview.widget.LinearSmoothScroller
import com.Lyber.R
import com.Lyber.databinding.FragmentPickYourStrategyBinding
import com.Lyber.models.MessageResponse
import com.Lyber.models.Strategy
import com.Lyber.ui.adapters.PickStrategyFragmentAdapter
import com.Lyber.ui.fragments.bottomsheetfragments.InvestWithStrategyBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.AppLifeCycleObserver
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.ItemOffsetDecoration
import com.Lyber.viewmodels.PortfolioViewModel
import com.google.gson.GsonBuilder
import java.lang.Math.abs


class PickYourStrategyFragment : BaseFragment<FragmentPickYourStrategyBinding>(),
    View.OnClickListener {

    private lateinit var adapter: PickStrategyFragmentAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: PortfolioViewModel

    /* previous item clicked */
    private var previousPosition: Int = -1
    lateinit var strategyList: List<Strategy>
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
        viewModel.selectedStrategyResponse.observe(viewLifecycleOwner, chooseStrategy)
        viewModel.getStrategiesResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                strategyList = it.data
                adapter.setList(strategyList)
                binding.recyclerViewStrategies.startLayoutAnimation()
            }
        }
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

        hitApi()

        viewModel.pauseStrategyResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                checkInternet(binding.root,requireContext()) {
                    showProgressDialog(requireContext())
                   viewModel.getStrategies()

                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if(AppLifeCycleObserver.fromBack){
            AppLifeCycleObserver.fromBack=false
            hitApi()
        }
    }
    private fun hitApi(){
        checkInternet(binding.root,requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getStrategies()

        }
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

    private fun itemClicked(position: Int, currentView: StrategyView) {

        viewModel.selectedStrategy = adapter.getItem(position)
        val slowScroller = SlowLinearSmoothScroller(requireContext())
        slowScroller.targetPosition = position
        layoutManager.startSmoothScroll(slowScroller)




        if (currentView.topText.equals("Advanced"))
            binding.viewGap.visible()

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

        val vc = InvestWithStrategyBottomSheet(::clicked)
        vc.viewToDelete = transparentView
        vc.mainView = getView()?.rootView as ViewGroup
        vc.viewModel = viewModel
        vc.show(childFragmentManager, "")

        // Add the transparent view to the RelativeLayout
        val mainView = getView()?.rootView as ViewGroup
        mainView.addView(transparentView, viewParams)

        Log.d(TAG, "itemClicked: $previousPosition $position ")

    }

    private fun clicked(type: Int) {
        binding.viewGap.gone()
        for (i in 0..strategyList.size - 1) {
            strategyList[i].isSelected = false
        }
        adapter.notifyDataSetChanged()
        when (type) {
            0 -> {
                if(checkKyc())
                findNavController().navigate(R.id.investAddMoneyFragment)
            }

            1 -> {
                if(checkKyc())
                checkInternet(binding.root,requireActivity()) {
                    CommonMethods.showProgressDialog(requireActivity())
                     viewModel.pauseStrategy(
                           viewModel.selectedStrategy!!.ownerUuid,
                            viewModel.selectedStrategy!!.name
                        )

                }
            }

            2 -> {
                checkInternet(binding.root,requireActivity()) {
                    CommonMethods.showProgressDialog(requireActivity())
                   viewModel.deleteStrategy(
                            viewModel.selectedStrategy!!.name
                        )

                }
            }

            3 -> {
                    val bundle = Bundle()
                    bundle.putBoolean(Constants.ID, true)
                    findNavController().navigate(R.id.buildStrategyFragment, bundle)
                 }

            4 -> {
                if(checkKyc()) {
                    val bundle = Bundle()
                    bundle.putBoolean(Constants.ONE_TIME, true)
                    findNavController().navigate(R.id.investAddMoneyFragment, bundle)
                } }

            5 -> {
                if(checkKyc()) {
                    val bundle = Bundle()
                    bundle.putBoolean(Constants.EDIT_ACTIVE_STRATEGY, true)
                    val gson = GsonBuilder().create()
                    var data = ""
                    data = gson.toJson(viewModel.selectedStrategy)
                    bundle.putString("data", data)
                    findNavController().navigate(R.id.investAddMoneyFragment, bundle)
                }   }
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

    class SlowLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {

        companion object {
            private const val SCROLL_DURATION =
                300 // Adjust as needed (make it smaller for faster scrolling)
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            val proportion = abs(dx) / targetPosition.toFloat()
            val extraSmoothTime = super.calculateTimeForScrolling(dx)
            val duration = (extraSmoothTime * proportion).toInt()

            // Ensure the scroll duration doesn't exceed the maximum
            return if (duration > SCROLL_DURATION) SCROLL_DURATION else duration
        }
//        override fun calculateTimeForScrolling(dx: Int): Int {
//            val proportion = abs(dx) / targetPosition.toFloat()
//            val extraSmoothTime = super.calculateTimeForScrolling(dx)
//            val duration = (extraSmoothTime * proportion).toInt()
//            return if (duration > MAX_SCROLL_ON_FLING_DURATION) MAX_SCROLL_ON_FLING_DURATION else duration
//
//        }

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START // or SNAP_TO_END or SNAP_TO_ANY
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        CommonMethods.dismissProgressDialog()

        when (errorCode) {
            13003 -> CommonMethods.showSnack(
                binding.root,
                requireContext(),
                getString(R.string.error_code_13003)
            )

            else ->  super.onRetrofitError(errorCode, msg)

        }
    }

}