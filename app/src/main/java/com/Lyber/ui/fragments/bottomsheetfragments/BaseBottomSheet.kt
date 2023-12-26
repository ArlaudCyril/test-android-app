package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.Lyber.R
import com.Lyber.network.RestClient
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.ResponseBody

abstract class BaseBottomSheet<viewBinding : ViewBinding> : BottomSheetDialogFragment(),
    RestClient.OnRetrofitError {
    private var _binding: viewBinding? = null
    val binding get() = _binding!!

    abstract fun bind(): viewBinding
    val behavior get() = BottomSheetBehavior.from(requireView().parent as View)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top)
        setStyle(STYLE_NORMAL, R.style.CustomDialogBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bind()
        return binding.root
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        CommonMethods.dismissProgressDialog()
        CommonMethods.showErrorMessage(requireContext(), responseBody)
    }

    override fun onError() {
        CommonMethods.dismissProgressDialog()
        "Error occurred!".showToast(requireContext())
    }

}