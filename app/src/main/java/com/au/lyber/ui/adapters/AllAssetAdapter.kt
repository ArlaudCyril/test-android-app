package com.au.lyber.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.au.lyber.R
import com.au.lyber.databinding.ItemAssetBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.priceServiceResume
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.loadImage
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.Constants

class AllAssetAdapter(private val clickListener: (priceServiceResume) -> Unit = { _ -> }) :
    BaseAdapter<priceServiceResume>() {


    private var oldPosition: Int = 0

    inner class AssetViewHolder(val binding: ItemAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
            }
            binding.lineChart.setOnClickListener {
                itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
            }
        }
    }

    override fun getItemViewType(position: Int) =
        if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == LOADER_VIEW) LoaderViewHolder(
            LoaderViewBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
        else AssetViewHolder(
            ItemAssetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (itemList[position] != null) {

            (holder as AssetViewHolder).binding.apply {
                itemList[position]?.let {
                    if (it.change.roundFloat().toFloat() > 0) {
                        tvAssetVariation.text = "+${it.change.roundFloat().commaFormatted}%"
                        tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))
                    } else {
                        tvAssetVariation.text = "${it.change.roundFloat().commaFormatted}%"
                        tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))

                    }
                    val urlLineChart = it.squiggleURL
                    lineChart.loadImage(urlLineChart)
                    val id = it.id
                    BaseActivity.currencies.firstNotNullOfOrNull{ item -> item.takeIf {item.id == id}}
                        ?.let { it1 -> ivAsset.loadCircleCrop(it1.image); tvAssetName.text = it1.fullName }

                    tvAssetNameCode.text = it.id.uppercase()
                    tvAssetValue.text = it.lastPrice.roundFloat().commaFormatted + Constants.EURO
                }
            }
        }
    }

    /*@SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (!payloads.isNullOrEmpty())
            (payloads[position] as MutableList<Data>)[position].let {
                val item = itemList[position]
                if (item != null) {

                    if (item.current_price != it.current_price) {

                        (holder as AssetViewHolder).binding.apply {

                            tvAssetValue.text =
                                it.current_price.toString()
                                    .roundFloat().commaFormatted + Constants.EURO

                            if (it.price_change_percentage_24h > 0) {

                                tvAssetVariation.text =
                                    "+${
                                        it.price_change_percentage_24h.toString()
                                            .roundFloat().commaFormatted
                                    }%"
                                tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))
                                lineChart.setLineData(
                                    it.sparkline_in_7d.price.formLineData(),
                                    R.color.green_500,
                                    R.drawable.drawable_green_fill_line_chart
                                )
                            } else {
                                tvAssetVariation.text =
                                    "${
                                        it.price_change_percentage_24h.toString()
                                            .roundFloat().commaFormatted
                                    }%"
                                tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))
                                lineChart.setLineData(
                                    it.sparkline_in_7d.price.formLineData(),
                                    R.color.red_500,
                                    R.drawable.drawable_red_fill_line_chart
                                )
                            }

                        }

                    } else super.onBindViewHolder(holder, position, payloads)

                } else super.onBindViewHolder(holder, position, payloads)
            }
        else super.onBindViewHolder(holder, position, payloads)
    }*/


    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder.adapterPosition != NO_POSITION) {
            itemList[holder.adapterPosition]?.let {
                (holder as AssetViewHolder).binding.root.animation =
                    AnimationUtils.loadAnimation(
                        holder.binding.root.context,
                        R.anim.zoom_in
                    )
            }
        }

    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder.adapterPosition != NO_POSITION)
            itemList[holder.adapterPosition]?.let {
                (holder as AssetViewHolder).binding.root.clearAnimation()
            }
    }
}