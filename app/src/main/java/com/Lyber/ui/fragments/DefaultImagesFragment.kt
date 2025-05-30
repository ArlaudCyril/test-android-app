package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentDefaultPicBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.AvatarData
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.Constants
import com.Lyber.utils.ItemOffsetDecoration

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

    private fun itemClicked(imageRes: String) {
        val bundle = Bundle().apply {
            putString("profilePic",imageRes)
        }
        findNavController().navigate(R.id.selectedProfilePictureFragment,bundle)
        /*requireActivity().replaceFragment(
            R.id.flSplashActivity,
            SelectedProfilePictureFragment.get(imageRes)
        )*/
    }

    private class ImageAdapter(private val itemClicked: (String) -> Unit = { _ -> }) :
        BaseAdapter<AvatarData>() {

        override fun getItemViewType(position: Int): Int {
            return if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW
        }

        inner class ImageViewHolder(val imageView: ImageView) :
            RecyclerView.ViewHolder(imageView) {
            init {
                imageView.setOnClickListener {
                    itemClicked(itemList[absoluteAdapterPosition]!!.avatar_name)
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
                        (holder as ImageViewHolder).imageView.setImageResource(it.avatar_is)
                    }
                }
            }

        }

    }
}