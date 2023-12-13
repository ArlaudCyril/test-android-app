package com.au.lyber.ui.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.BottomSheetSpinnerBinding
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentBuildStrategyBinding
import com.au.lyber.models.AddedAsset
import com.au.lyber.models.PriceServiceResume
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.adapters.BuildStrategyAdapter
import com.au.lyber.ui.fragments.bottomsheetfragments.AddAssetBottomSheet
import com.au.lyber.ui.fragments.bottomsheetfragments.BaseBottomSheet
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.toPx
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants


class BuildStrategyFragment : BaseFragment<FragmentBuildStrategyBinding>(), View.OnClickListener {

    private lateinit var adapter: BuildStrategyAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel
//    private val viewModel: PortfolioViewModel by viewModels()


    private var canBuildStrategy: Boolean = false
    private var isEdit = false

    override fun bind() = FragmentBuildStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null && requireArguments().containsKey(Constants.ID)) {
            isEdit = requireArguments().getBoolean(Constants.ID)
        }
        binding.btnAddAssets.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        binding.btnSaveMyStrategy.setOnClickListener(this)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        viewModel.buildStrategyResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        adapter = BuildStrategyAdapter(binding.rvAssets, ::assetClicked)
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssets.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
            it.isNestedScrollingEnabled = false
        }
        setItemTouchHelper(requireContext(), binding.rvAssets, adapter)

        if (isEdit) {
            val strategy = viewModel.selectedStrategy
            for (ada in strategy!!.bundle) {
                val priceServiceResume =
                    BaseActivity.balanceResume.firstNotNullOfOrNull { item -> item.takeIf { item.id == ada.asset } }
                val assest = AddedAsset(priceServiceResume!!, ada.share, false)
                viewModel.addedAsset.apply {
                    add(assest)
                }
                adapter.addItem(assest)

            }
            calculateAllocations()

        }

    }

    private fun clickListen(asset: PriceServiceResume) {
        val item = AddedAsset(asset, 100F)
        viewModel.addedAsset.apply {

            // recycler view
            when {
                // case-1 when there is no item
                isEmpty() -> {
                    add(item)
                    adapter.addItem(item)
                    calculateAllocations()
                }

                // case-2 if the item already exists in list
                item in this -> {}

                // case-3 if we extend the list
                else -> {
                    adapter.clearList()
                    add(item)
                    for (i in 0 until count()) {
                        get(i).allocation = 100F / count()
                        get(i).isChangedManually = false
                        adapter.addItem(get(i))
                    }

                    //scroll to position
                    // use scroll views method
                    layoutManager.scrollToPosition(viewModel.addedAsset.count() - 1)

                    calculateAllocations()
                }

            }

        }

    }

    private fun calculateAllocations() {

        binding.tvInitialInfo.gone()

        var count = 0F
        viewModel.addedAsset.apply {

            for (i in this) count += i.allocation

            when {
                count > 100 -> {
                    canBuildStrategy = false
                    val redColor = ContextCompat.getColor(requireContext(), R.color.red_500)
                    binding.tvAllocationInfo.visible()
                    binding.tvAllocationInfo.setTextColor(redColor)
                    binding.tvAllocationInfo.text = "${
                        getString(
                            R.string.your_allocations_is_greater_than_100_remove
                        )
                    } ${(count - 100).toInt()} %"
                    binding.btnSaveMyStrategy.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.button_purple_400)
                }

                count < 100 -> {
                    canBuildStrategy = false
                    val redColor = ContextCompat.getColor(requireContext(), R.color.red_500)
                    binding.tvAllocationInfo.visible()
                    binding.tvAllocationInfo.setTextColor(redColor)
                    binding.tvAllocationInfo.text =
                        "${getString(R.string.your_allocations_is_less_than_100_add)} ${(100 - count).toInt()}%"
                    binding.btnSaveMyStrategy.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.button_purple_400)
                }

                else -> {
                    val one = ContextCompat.getColor(requireContext(), R.color.purple_gray_800)
                    val two = ContextCompat.getColor(requireContext(), R.color.purple_gray_600)
                    binding.tvAllocationInfo.visible()
                    binding.tvAllocationInfo.setTextColor(two)
                    binding.tvAllocationInfo.text =
                        getString(R.string.your_strategy_is_ready_to_be_saved)
                    binding.btnSaveMyStrategy.setBackgroundResource(R.drawable.button_purple_500)
//                        ContextCompat.getDrawable(requireContext(), R.drawable.button_purple_500)
                    canBuildStrategy = true
                }

            }
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnAddAssets -> {
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

                    val vc = AddAssetBottomSheet(::clickListen, viewModel.addedAsset)

                    vc.viewToDelete = transparentView
                    vc.mainView = view?.rootView as ViewGroup
                    vc.show(childFragmentManager, "")

                    // Add the transparent view to the RelativeLayout
                    val mainView = view?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)

                }

                ivTopAction -> requireActivity().onBackPressed()
                btnSaveMyStrategy -> {
                    if (isEdit) {
                        checkInternet(requireContext()) {
                            showProgressDialog(requireContext())
                            checkInternet(requireContext()) {
                                showProgressDialog(requireContext())
                                viewModel.editOwnStrategy(viewModel.selectedStrategy!!.name)

                            }
                        }
                    } else {
                        if (canBuildStrategy) {
                            showDialog()
                        }
                    }
                }
            }
        }
    }

    private fun showDialog() {
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

        // Add the transparent view to the RelativeLayout
        val mainView = view?.rootView as ViewGroup
        mainView.addView(transparentView, viewParams)
        val dialog = Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let { bind ->
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(bind.root)
                bind.rlStrategy.visible()
                bind.rlCustom.gone()

                bind.tvCancel.setOnClickListener {
                    mainView.removeView(transparentView)
                    dismiss()
                }
                bind.tvSave.setOnClickListener {
                    val name: String = bind.etInput.text.trim().toString()
                    when {
                        name.isEmpty() -> {
                            getString(R.string.please_enter_name_for_your_strategy).showToast(
                                requireContext()
                            )
                            bind.etInput.requestKeyboard()
                        }

                        else -> {
                            checkInternet(requireContext()) {
                                mainView.removeView(transparentView)
                                dismiss()
                                showProgressDialog(requireContext())
                                checkInternet(requireContext()) {
                                    showProgressDialog(requireContext())
                                    if (isEdit) {
                                        viewModel.editOwnStrategy(name)
                                    } else {
                                        viewModel.buildOwnStrategy(name)
                                    }
                                }
                            }
                        }
                    }

                }
                show()
            }
        }
    }


    /* callbacks */

    private fun clickListener(position: Int) {
        viewModel.addedAsset[position].let {
            val allocationValue = it.allocation.toInt()
            val assest =
                BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.addedAsset[position].addAsset.id } }
            val assetsName = assest!!.fullName + " (${assest.id.uppercase()})"
            SpinnerBottomSheet(::manuallySelectedAllocation).apply {
                arguments = Bundle().apply {
                    putString("assetsName", assetsName)
                    putInt("allocationValue", allocationValue)
                    putInt("position", position)
                }
            }.show(parentFragmentManager, "")
        }
    }

    private fun manuallySelectedAllocation(value: Int, position: Int) {
        adapter.getItem(position)?.allocation = value.toFloat()
        adapter.getItem(position)?.isChangedManually = true
        adapter.notifyItemChanged(position)
        calculateAllocations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.addedAsset.clear()
    }

    class SpinnerBottomSheet(private val handle: (Int, Int) -> Unit = { _, _ -> }) :
        BaseBottomSheet<BottomSheetSpinnerBinding>() {

        private var assetName: String = ""
        private var position: Int = 0
        private var allocationValue: Int = 0

        override fun bind() = BottomSheetSpinnerBinding.inflate(layoutInflater)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                assetName = it.getString("assetsName", "")
                position = it.getInt("position", 0)
                allocationValue = it.getInt("allocationValue", 0)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.apply {


                tvAssetsName.text = assetName
                allocationSpinner.maxValue = 19
                allocationSpinner.minValue = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    allocationSpinner.textSize = 22F.toPx(requireContext()).toFloat()
                allocationSpinner.displayedValues = getArray()
                allocationSpinner.wrapSelectorWheel = false

                allocationSpinner.value = getArray().indexOf("$allocationValue%")

                ivTopAction.setOnClickListener { dismiss() }

                btnSetAllocation.setOnClickListener {
                    val string = getArray()[allocationSpinner.value]
                    handle(string.split("%")[0].toInt(), position)
                    dismiss()
                }


            }
        }

        private fun getArray(): Array<String> {
            val list = mutableListOf<String>()
            for (i in (5..100 step 5)) list.add("$i%")
            return list.toTypedArray()
        }

    }

    private fun dipToPx(context: Context): Int {
        return (100f * context.resources.displayMetrics.density).toInt()
    }

    private fun setItemTouchHelper(
        context: Context,
        recyclerView: RecyclerView,
        adapter: BuildStrategyAdapter,
    ) {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            private var limitScrollX = dipToPx(context)
            private var currentScrollX = 0
            private var currentScrollXWhenInActive = 0
            private var initXWhenInActive = 0f
            private var firstInActive = false
            var leftSwipeChecker = false

            private var handler = Handler(Looper.getMainLooper())

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = 0
                val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return Integer.MAX_VALUE.toFloat()
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return Integer.MAX_VALUE.toFloat()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                viewHolder.itemView.findViewById<View>(R.id.llOptionsHere).post {
                    val itemOptions = viewHolder.itemView.findViewById<View>(R.id.llOptions)
                    val itemOptionsHere = viewHolder.itemView.findViewById<View>(R.id.llOptionsHere)
                    limitScrollX = itemOptionsHere.width
                    val swipedDirection =
                        if (dX > 0) ItemTouchHelper.RIGHT else ItemTouchHelper.LEFT

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                        val itemView = viewHolder.itemView.findViewById<View>(R.id.llContent)
                        Log.d("dx", "$dX")
                        if (itemView.scrollX == 0) {
                            leftSwipeChecker = true
                        }

                        leftSwipeChecker = leftSwipeChecker && dX < 0

                        if (leftSwipeChecker) {
                            recoverSwipedItem(viewHolder, recyclerView)
                            if (itemView.scrollX != 0) {
                                leftSwipeChecker = false
                            }
                        }

                        if (dX == 0f) {
                            currentScrollX = itemView.scrollX
                            firstInActive = true
                        }

                        if (isCurrentlyActive) {
                            var scrollOffset = currentScrollX + (-dX).toInt()
                            if (scrollOffset > limitScrollX) {
                                scrollOffset = limitScrollX
                            } else if (scrollOffset < 0) {
                                scrollOffset = 0
                            }
                            Log.d("swipeFunc", "isCurrentlyActive $scrollOffset")
                            itemView.scrollTo(scrollOffset, 0)
                            drawChild(itemOptions, scrollOffset, 0)

                        } else {
                            if (firstInActive) {
                                firstInActive = false
                                currentScrollXWhenInActive = itemView.scrollX
                                initXWhenInActive = dX
                            }
                            if (itemView.scrollX < limitScrollX) {
                                val value =
                                    (currentScrollXWhenInActive * dX / initXWhenInActive).toInt()
                                Log.d("swipeFunc", "$value")
                                drawChild(itemOptions, value, 0)
                                itemView.scrollTo(
                                    value,
                                    0
                                )
                            }
                        }
                    }

                }

            }

            private fun drawChild(itemView: View?, x: Int, y: Int) {
                val param = itemView?.layoutParams
                param?.width = x
                itemView?.layoutParams = param
            }

            private fun recoverSwipedItem(
                viewHolder: RecyclerView.ViewHolder,
                recyclerView: RecyclerView
            ) {

                for (i in adapter.itemCount downTo 0) {
                    val itemView =
                        recyclerView.findViewHolderForAdapterPosition(i)?.itemView?.findViewById<View>(
                            R.id.llContent
                        )
                    val itemOption =
                        recyclerView.findViewHolderForAdapterPosition(i)?.itemView?.findViewById<View>(
                            R.id.llOptions
                        )


                    if (i != viewHolder.adapterPosition) {

                        itemView?.let {
                            if (it.scrollX > 0) {
                                recoverItemAnim(itemView, itemOption)
                            }
                        }
                    }

                }
            }

            private fun recoverItemAnim(itemView: View?, itemOption: View?) {
                itemView?.scrollTo(0, 0)
                drawChild(itemOption, 0, 0)
                handler.postDelayed({
                    drawChild(itemOption, 0, 0)
                    itemView?.scrollTo(0, 0)
                }, 300)
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                val item = viewHolder.itemView.findViewById<View>(R.id.llContent)

                val itemOption = viewHolder.itemView.findViewById<View>(R.id.llOptions)
                if (item.scrollX > limitScrollX) {
                    drawChild(itemOption, 0, 0)
                    item.scrollTo(limitScrollX, 0)
                } else if (item.scrollX < 0) {
                    drawChild(itemOption, 0, 0)
                    item.scrollTo(0, 0)
                }
            }

        }).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    companion object {
        fun isAnyItemSwiped(recyclerView: RecyclerView): Boolean {
            for (i in 0 until recyclerView.childCount) {
                val itemView = recyclerView.getChildAt(i)?.findViewById<View>(R.id.llContent)
                if (itemView != null && itemView.scrollX != 0) {
                    return true
                }
            }
            return false
        }
    }


    private fun assetClicked(position: Int, action: String) {
        adapter.getItem(position)?.let { item ->

            when (action) {
                "allocation" -> {
                    for (i in 0 until binding.rvAssets.childCount) {
                        val itemView =
                            binding.rvAssets.getChildAt(i)?.findViewById<View>(R.id.llContent)

                        val itemOption =
                            binding.rvAssets.getChildAt(i)?.findViewById<View>(R.id.llOptions)
                        itemView?.scrollTo(0, 0)
                        val param = itemOption!!.layoutParams
                        param?.width = 0
                        itemOption.layoutParams = param
                        Handler(Looper.getMainLooper()).postDelayed({
                            val param = itemOption.layoutParams
                            param?.width = 0
                            itemOption.layoutParams = param
                            itemView?.scrollTo(0, 0)
                        }, 300)
                    }
                    viewModel.addedAsset[position].let {
                        val allocationValue = it.allocation.toInt()
                        val assest =
                            BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.addedAsset[position].addAsset.id } }
                        val assetsName = assest!!.fullName + " (${assest.id.uppercase()})"
                        SpinnerBottomSheet(::manuallySelectedAllocation).apply {
                            arguments = Bundle().apply {
                                putString("assetsName", assetsName)
                                putInt("allocationValue", allocationValue)
                                putInt("position", position)
                            }
                        }.show(parentFragmentManager, "")
                    }
                }

                "setView" -> {
                    for (i in 0 until binding.rvAssets.childCount) {
                        val itemView =
                            binding.rvAssets.getChildAt(i)?.findViewById<View>(R.id.llContent)

                        val itemOption =
                            binding.rvAssets.getChildAt(i)?.findViewById<View>(R.id.llOptions)
                        itemView?.scrollTo(0, 0)
                        val param = itemOption!!.layoutParams
                        param?.width = 0
                        itemOption.layoutParams = param
                        Handler(Looper.getMainLooper()).postDelayed({
                            val param = itemOption.layoutParams
                            param?.width = 0
                            itemOption.layoutParams = param
                            itemView?.scrollTo(0, 0)
                        }, 300)
                    }
                }

                "delete" -> {
                    try {
                        for (i in 0 until binding.rvAssets.childCount) {
                            val itemView =
                                binding.rvAssets.getChildAt(i)?.findViewById<View>(R.id.llContent)

                            val itemOption =
                                binding.rvAssets.getChildAt(i)?.findViewById<View>(R.id.llOptions)
                            itemView?.scrollTo(0, 0)
                            val param = itemOption!!.layoutParams
                            param?.width = 0
                            itemOption.layoutParams = param
                            Handler(Looper.getMainLooper()).postDelayed({
                                val param = itemOption.layoutParams
                                param?.width = 0
                                itemOption.layoutParams = param
                                itemView?.scrollTo(0, 0)
                            }, 300)
                        }
                        val id = viewModel.addedAsset[position]
                        viewModel.addedAsset.apply {
                            remove(id)
                        }
                        adapter.removeItem(id)
                    } catch (_: Exception) {

                    }
                }

                else -> {}

            }

        }
    }
//    class SwipeController(private val adapter: BuildStrategyAdapter) : ItemTouchHelper.SimpleCallback(
//        0, ItemTouchHelper.LEFT
//    ) {
//
//        private val SWIPE_THRESHOLD = 0.5f
//
//        override fun onMove(
//            recyclerView: RecyclerView,
//            viewHolder: RecyclerView.ViewHolder,
//            target: RecyclerView.ViewHolder
//        ): Boolean {
//            return false
//        }
//
//        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            val position = viewHolder.adapterPosition
//            val itemView = viewHolder.itemView
//
//            if (direction == ItemTouchHelper.LEFT) {
//                // Calculate the swipe percentage
//                val swipePercentage = (itemView.width - itemView.right) / itemView.width.toFloat()
//
//                // Show button on half swipe
//                if (swipePercentage < SWIPE_THRESHOLD) {
//                    adapter.showButton(position)
//                } else {
//                    // Delete item on full swipe
//                    adapter.removeItem(position)
//                }
//            }
//        }
//
//        override fun onChildDraw(
//            c: Canvas,
//            recyclerView: RecyclerView,
//            viewHolder: RecyclerView.ViewHolder,
//            dX: Float,
//            dY: Float,
//            actionState: Int,
//            isCurrentlyActive: Boolean
//        ) {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//
//            // Customize the drawing of the view during the swipe
//            val itemView = viewHolder.itemView
//            val icon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_delete)!!
//            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
//
//            // Calculate the swipe percentage
//            val swipePercentage = (itemView.width - itemView.right) / itemView.width.toFloat()
//
//            // Show button on half swipe
//            if (swipePercentage < SWIPE_THRESHOLD) {
//                icon.setBounds(
//                    itemView.left + iconMargin,
//                    itemView.top + iconMargin,
//                    itemView.left + iconMargin + icon.intrinsicWidth,
//                    itemView.bottom - iconMargin
//                )
//                icon.draw(c)
//            }
//        }
//    }


}