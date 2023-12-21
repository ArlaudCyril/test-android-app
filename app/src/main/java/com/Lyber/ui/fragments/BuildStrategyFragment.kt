package com.Lyber.ui.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.R
import com.Lyber.databinding.BottomSheetSpinnerBinding
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.FragmentBuildStrategyBinding
import com.Lyber.models.AddedAsset
import com.Lyber.models.Data
import com.Lyber.ui.adapters.BuildStrategyAdapter
import com.Lyber.ui.fragments.bottomsheetfragments.AddAssetBottomSheet
import com.Lyber.ui.fragments.bottomsheetfragments.BaseBottomSheet
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.toPx
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel

class BuildStrategyFragment : BaseFragment<FragmentBuildStrategyBinding>(), View.OnClickListener {

    private lateinit var adapter: BuildStrategyAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel

    private var canBuildStrategy: Boolean = false

    override fun bind() = FragmentBuildStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddAssets.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        binding.btnSaveMyStrategy.setOnClickListener(this)

        viewModel = getViewModel(requireActivity())
        viewModel.buildStrategyResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            requireActivity().onBackPressed()
        }

        adapter = BuildStrategyAdapter(::clickListener)
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssets.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
            it.isNestedScrollingEnabled = false
        }


    }

    private fun clickListen(asset: Data) {
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
                    binding.tvNumberAssets.visible()
                    binding.tvAllocationInfo.visible()
                    binding.tvNumberAssets.setTextColor(redColor)
                    binding.tvAllocationInfo.setTextColor(redColor)
                    binding.tvNumberAssets.text = getString(R.string.assetbasedata, count())
                    binding.tvAllocationInfo.text =
                        getString(
                            R.string.your_allocations_is_greater_than_100_remove,
                            (count - 100).toInt()
                        )
                    binding.btnSaveMyStrategy.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.button_purple_400)
                }
                count < 100 -> {
                    canBuildStrategy = false
                    val redColor = ContextCompat.getColor(requireContext(), R.color.red_500)
                    binding.tvNumberAssets.visible()
                    binding.tvAllocationInfo.visible()
                    binding.tvNumberAssets.setTextColor(redColor)
                    binding.tvAllocationInfo.setTextColor(redColor)
                    binding.tvNumberAssets.text = getString(R.string.assetbasedata, count())
                    binding.tvAllocationInfo.text = getString(R.string.your_allocations_is_less_than_100_add, (100 - count).toInt())
                    binding.btnSaveMyStrategy.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.button_purple_400)
                }
                else -> {
                    val one = ContextCompat.getColor(requireContext(), R.color.purple_gray_800)
                    val two = ContextCompat.getColor(requireContext(), R.color.purple_gray_600)

                    binding.tvNumberAssets.visible()
                    binding.tvAllocationInfo.visible()
                    binding.tvNumberAssets.setTextColor(one)
                    binding.tvAllocationInfo.setTextColor(two)
                    binding.tvNumberAssets.text = getString(R.string.assetbasedata, count())
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
                    AddAssetBottomSheet(::clickListen).show(
                        requireActivity().supportFragmentManager,
                        ""
                    )
                }
                ivTopAction -> requireActivity().onBackPressed()
                btnSaveMyStrategy -> {
                    if (canBuildStrategy) {
                        showDialog()
                    }
                }
            }
        }
    }

    private fun showDialog() {
        Dialog(requireActivity(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let { bind ->
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(bind.root)
                bind.rlStrategy.visible()
                bind.rlCustom.gone()

                bind.tvCancel.setOnClickListener {
                    dismiss()
                }
                bind.tvSave.setOnClickListener {
                    val name: String = bind.etInput.text.trim().toString()
                    when {
                        name.isEmpty() -> {
                            "Please enter name for your strategy.".showToast(requireContext())
                            bind.etInput.requestKeyboard()
                        }
                        else -> {
                            checkInternet(requireContext()) {
                                dismiss()
                                showProgressDialog(requireContext())
                                checkInternet(requireContext()) {
                                    showProgressDialog(requireContext())
                                    viewModel.buildOwnStrategy(name)
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
            val assetsName = it.addAsset.name + " (${it.addAsset.symbol})"
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

}