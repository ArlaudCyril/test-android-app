package com.au.lyber.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.au.lyber.R
import com.au.lyber.databinding.FragmentEducationStrategyHolderBinding
import com.au.lyber.models.MessageResponse
import com.au.lyber.ui.activities.SplashActivity
import com.au.lyber.utils.ActivityCallbacks
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.NetworkViewModel

class EducationStrategyHolderFragment : BaseFragment<FragmentEducationStrategyHolderBinding>(),
    ActivityCallbacks {

    private val TAG = "EducationStrategyHolder"
    private var mPosition: Int = 0
    private lateinit var viewModel: NetworkViewModel

    override fun bind() = FragmentEducationStrategyHolderBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onDestroyView: onViewCreated")
        viewModel = getViewModel(this)
        viewModel.listener = this


        binding.viewPager.adapter = Adapter()
        binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback)
        binding.viewPager.isUserInputEnabled = false

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.educationStrategyResponse.observe(viewLifecycleOwner, observer)

        setUpIndicators(mPosition)
    }

    override fun onResume() {
        super.onResume()
        SplashActivity.activityCallbacks = this
    }

    override fun onStop() {
        super.onStop()
        SplashActivity.activityCallbacks = null
    }

    private val observer = Observer<MessageResponse> {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            dismissProgressDialog()
            App.prefsManager.portfolioCompletionStep = Constants.DONE_EDUCATION
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                PickYourStrategyFragment(),
                isBackStack = true,
                topBottom = true
            )


        }
    }

    inner class Adapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 3
        override fun createFragment(position: Int) = EducationStratFragment.new(position)
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(TAG, "onDestroyView: onPageSelected")
            mPosition = position
            setUpIndicators(mPosition)
        }
    }

    private fun setUpIndicators(position: Int) {
        binding.llIndicators.let {
            for (i in 0 until it.childCount) {
                val imageView = it.getChildAt(i)
                if (position == i)
                    imageView.setBackgroundResource(R.drawable.page_selected_indicator)
                else imageView.setBackgroundResource(R.drawable.indicator_unselected)
            }
        }
        when (position) {
            0 -> binding.ivTopAction.setImageResource(R.drawable.ic_close)
            else -> binding.ivTopAction.setImageResource(R.drawable.ic_back)
        }
    }

    fun moveNext() {
        if (mPosition == 2) {
            checkInternet(requireContext()) {
                showProgressDialog(requireContext())
                viewModel.educationStrategy()
            }
        } else {
            mPosition++
            binding.viewPager.setCurrentItem(mPosition, true)
        }
    }

    override fun onBackPressed(): Boolean {
        return if (mPosition > 0) {
            mPosition--
            binding.viewPager.setCurrentItem(mPosition, true)
            false
        } else true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ${requireActivity().supportFragmentManager.backStackEntryCount}")
        viewModel.educationStrategyResponse.removeObserver(observer)
    }

}