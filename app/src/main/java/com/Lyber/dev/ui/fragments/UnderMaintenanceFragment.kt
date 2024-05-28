package com.Lyber.dev.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentUnderMaintenanceBinding


class UnderMaintenanceFragment : BaseFragment<FragmentUnderMaintenanceBinding>() {
    override fun bind()= FragmentUnderMaintenanceBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}