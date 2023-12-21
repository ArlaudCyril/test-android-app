package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.Lyber.R
import com.Lyber.network.RestClient
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import okhttp3.ResponseBody

abstract class BaseFragment<viewBinding : ViewBinding> : Fragment(), RestClient.OnRetrofitError {

    private var _binding: viewBinding? = null
    val binding get() = _binding!!

    abstract fun bind(): viewBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bind()
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onRetrofitError(responseBody: ResponseBody?) {
        dismissProgressDialog()
        CommonMethods.showErrorMessage(requireContext(), responseBody)
    }

    override fun onError() {
        dismissProgressDialog()
        getString(R.string.unable_to_connect_to_the_server).showToast(requireContext())
    }

}