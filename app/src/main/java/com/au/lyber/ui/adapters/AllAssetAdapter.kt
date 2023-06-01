package com.au.lyber.ui.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAssetBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.PriceServiceResume
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.loadImage
import com.au.lyber.utils.CommonMethods.Companion.roundFloat

class AllAssetAdapter(private val clickListener: (PriceServiceResume) -> Unit = { _ -> }) :
    BaseAdapter<PriceServiceResume>() {


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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (itemList[position] != null) {

            (holder as AssetViewHolder).binding.apply {
                itemList[position]?.let {
                    if (it.priceServiceResumeData.change.roundFloat().toFloat() > 0) {
                        tvAssetVariation.text = "+${it.priceServiceResumeData.change.roundFloat().commaFormatted}%"
                        tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.green_500))
                    } else {
                        tvAssetVariation.text = "${it.priceServiceResumeData.change.roundFloat().commaFormatted}%"
                        tvAssetVariation.setTextColor(tvAssetVariation.context.getColor(R.color.red_500))

                    }
                    val urlLineChart = it.priceServiceResumeData.squiggleURL
                    lineChart.loadImage(urlLineChart)
                    val id = it.id
                    BaseActivity.assets.firstNotNullOfOrNull{ item -> item.takeIf {item.id == id}}
                        ?.let { it1 -> ivAsset.loadCircleCrop(it1.image); tvAssetName.text = it1.fullName }

                    val context = ivAsset.context
                    tvAssetValue.typeface = context.resources.getFont(R.font.mabry_pro_medium)
                    tvAssetValue.setTextColor(context.getColor(R.color.purple_gray_700))
                    ivAsset.context.resources.getFont(R.font.mabry_pro_medium)
                    tvAssetNameCode.text = it.id.uppercase()
                    tvAssetValue.text = it.priceServiceResumeData.lastPrice.currencyFormatted

                }
            }
        }
    }
}