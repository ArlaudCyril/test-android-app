package com.Lyber.ui.fragments

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.AllocationViewBinding
import com.Lyber.databinding.ItemAssetsStrategyBinding
import com.Lyber.models.InvestmentStrategyAsset
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.Constants.colors
import com.Lyber.utils.ItemOffsetDecoration
import kotlin.math.roundToInt

class AllocationView : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attributeSet,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attributeSet, defStyleAttr, defStyleRes) {
        init()
    }

    private var _binding: AllocationViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecyclerAdapter
    private lateinit var layoutManager: GridLayoutManager

    val rvAllocation get() = binding.rv
    private fun init() {
        orientation = VERTICAL
        _binding = AllocationViewBinding.inflate(LayoutInflater.from(context), this)
        adapter = RecyclerAdapter()
        layoutManager = GridLayoutManager(context, 2)
        binding.rv.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
            it.addItemDecoration(ItemOffsetDecoration(12))
        }

    }


    fun setAssetsList(list: List<InvestmentStrategyAsset>) {
        for (pos in 0 until list.count()) {

            val view = View(context)
            view.layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT
            ).apply {
                weight = list[pos].share.toFloat()
            }

            when (pos) {
                0 -> {
                    if (list.count() == 1) {
                        view.background =
                            ContextCompat.getDrawable(context, R.drawable.curved_layout_4_dp)
                        view.setBackgroundTint(colors[pos])
                    } else {
                        view.background =
                            ContextCompat.getDrawable(context, R.drawable.start_drawable_progress)
                        view.setBackgroundTint(colors[pos])
                    }
                }
                list.count() - 1 -> {
                    view.background =
                        ContextCompat.getDrawable(context, R.drawable.end_drawable_progress)
                    view.setBackgroundTint(colors[pos])
                }
                else -> view.setBackgroundColor(context.getColor(colors[pos]))
            }
            binding.llProgressBar.addView(view)
        }
        adapter.setList(list)
        postInvalidate()
    }

    class RecyclerAdapter :
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


        fun setList(list: List<InvestmentStrategyAsset>) {
            val start = this.list.count()
            this.list.clear()
            this.list.addAll(list)
            notifyItemRangeInserted(start, list.count())
        }

        private var list: MutableList<InvestmentStrategyAsset> = mutableListOf()


        override fun getItemCount() = list.count()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemAssetsStrategyBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.apply {
                list[position].let {
                    Log.d("postPositionList",list.count().toString())
                    Log.d("postPosition",position.toString())
                    ivColor.setBackgroundTint(colors[position])
                    tvAssetsName.text = it.asset.uppercase()
                    tvAssetPercent.text = "${it.share.roundToInt()}%"
                }
            }
        }

        inner class ViewHolder(val binding: ItemAssetsStrategyBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
            }
        }
    }

}