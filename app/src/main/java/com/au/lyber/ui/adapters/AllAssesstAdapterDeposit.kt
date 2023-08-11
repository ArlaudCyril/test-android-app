package com.au.lyber.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.ItemAllAssestBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop

class AllAssesstAdapterDeposit (
    private val clickListener: (AssetBaseData) -> Unit = { _ -> },private val context: Context) :
    BaseAdapter<AssetBaseData>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return AvailableAssetViewHolder(
            ItemAllAssestBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemList[position] != null)
            (holder as AvailableAssetViewHolder).binding.apply {
                itemList[position]?.let { it ->
                    val id = it.id
                    val currency : AssetBaseData? = BaseActivity.assets.firstNotNullOfOrNull{ item -> item.takeIf {item.id == id}}
                    if (currency != null) {
                        ivAsset.loadCircleCrop(currency.imageUrl)
                    }

                    tvAssetName.text = it.fullName
                    if (it.isDepositActive){
                        tvAssetNameCode.visibility = View.GONE
                    }else{
                        tvAssetNameCode.visibility = View.VISIBLE
                        tvAssetNameCode.text = context.getString(R.string.deactivated)
                    }
                }
            }
    }

    inner class AvailableAssetViewHolder(val binding: ItemAllAssestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (itemList[adapterPosition]!!.isDepositActive) {
                    itemList[adapterPosition]?.let { it1 -> clickListener(it1) }
                }
            }
        }
    }
}