package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.FragmentEducationOneBinding

class EducationStratFragment : BaseFragment<FragmentEducationOneBinding>() {

    private var position: Int = 0

    private var title: String = ""
    private var subTitle: String = ""
    private var buttonTitle: String = ""

    override fun bind() = FragmentEducationOneBinding.inflate(layoutInflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { position = it.getInt("position", 0) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (position) {
            0 -> {
                binding.imageViewHalf.setBackgroundResource(R.drawable.slider_one)
                title = getString(R.string.titleOne)
                subTitle = getString(R.string.subTitleOne)
                buttonTitle = getString(R.string.next)

            }
            1 -> {
                binding.imageViewHalf.setBackgroundResource(R.drawable.slider_two)
                title = getString(R.string.titleTwo)
                subTitle = getString(R.string.subTitleTwo)
                buttonTitle = getString(R.string.next)
            }
            else -> {
                binding.imageView.setBackgroundResource(R.drawable.slider_three)
                title = getString(R.string.titleThree)
                subTitle = getString(R.string.subTitleThree)
                buttonTitle = getString(R.string.choose_strategy)
            }
        }

        binding.tvTitle.text = title
        binding.tvSubTitle.text = subTitle
        binding.btnNext.text = buttonTitle
        binding.btnNext.setOnClickListener {
            (requireParentFragment() as EducationStrategyHolderFragment).moveNext()
        }
    }

    companion object {
        fun new(position: Int) = EducationStratFragment().apply {
            arguments = Bundle().apply {
                putInt("position", position)
            }
        }
    }
}