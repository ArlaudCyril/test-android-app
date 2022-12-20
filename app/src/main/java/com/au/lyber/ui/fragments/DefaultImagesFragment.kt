package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentDefaultPicBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.Constants
import com.au.lyber.utils.ItemOffsetDecoration

class DefaultImagesFragment : BaseFragment<FragmentDefaultPicBinding>() {

    private lateinit var adapter: ImageAdapter
    override fun bind() = FragmentDefaultPicBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ImageAdapter(::itemClicked)

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.rvImages.let {
            it.adapter = adapter
            it.layoutManager = GridLayoutManager(requireContext(), 4)
            it.addItemDecoration(ItemOffsetDecoration(12))
            adapter.addList(Constants.defaults)
            AnimationUtils.loadLayoutAnimation(
                requireContext(),
                R.anim.recycler_view_item_animation
            )
                .let { anim ->
                    anim.animation.duration = 200
                    it.layoutAnimation = anim
                }
            it.startLayoutAnimation()
        }
    }

    private fun itemClicked(imageRes: Int) {
        requireActivity().replaceFragment(
            R.id.flSplashActivity,
            SelectedProfilePictureFragment.get(imageRes)
        )
    }

    private class ImageAdapter(private val itemClicked: (Int) -> Unit = { _ -> }) :
        BaseAdapter<Int>() {

        override fun getItemViewType(position: Int): Int {
            return if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW
        }

        inner class ImageViewHolder(val imageView: ImageView) :
            RecyclerView.ViewHolder(imageView) {
            init {
                imageView.setOnClickListener {
                    itemClicked(adapterPosition)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == LOADER_VIEW) {
                LoaderViewHolder(
                    LoaderViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            } else {
                ImageViewHolder(ImageView(parent.context))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                LOADER_VIEW -> {}
                else -> {
                    itemList[position]?.let {
                        (holder as ImageViewHolder).imageView.setImageResource(it)
                    }
                }
            }

        }

    }
}