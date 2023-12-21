package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import com.Lyber.databinding.ProfileBottomSheetBinding

class ProfileBottomSheet(private val clickListener: (Int) -> Unit = { _ -> }) :
    BaseBottomSheet<ProfileBottomSheetBinding>(), View.OnClickListener {

    override fun bind() = ProfileBottomSheetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivTopAction.setOnClickListener(this)
        binding.rlCamera.setOnClickListener(this)
        binding.rlGallery.setOnClickListener(this)
        binding.rlDefaultPictures.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> dismiss()
                rlCamera -> {
                    clickListener(1)
                    dismiss()
                }
                rlGallery -> {
                    clickListener(2)
                    dismiss()
                }
                rlDefaultPictures -> {
                    clickListener(3)
                    dismiss()
                }

            }
        }
    }


}