package com.au.lyber.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.databinding.ItemResourcesBinding
import com.au.lyber.models.News
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


class ResourcesAdapter() : BaseAdapter<News>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ResourcesViewHolder(
            ItemResourcesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemList[position] != null)
            (holder as ResourcesViewHolder).binding.apply {
                itemList[position]?.let {
                    Glide.with(ivResource).load(it.image_url)
                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                        .into(ivResource)
                    tvResourceText.text = it.title
                }
            }
    }

    inner class ResourcesViewHolder(val binding: ItemResourcesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemList[adapterPosition]?.let {
                    binding.root.context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(it.url)
                        )
                    )

                }
            }
        }
    }
}