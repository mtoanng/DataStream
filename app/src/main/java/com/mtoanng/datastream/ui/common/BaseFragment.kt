package com.mtoanng.datastream.ui.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mtoanng.datastream.DataStreamApp

abstract class BaseFragment : Fragment() {

    protected val app: DataStreamApp
        get() = requireActivity().application as DataStreamApp

    protected val viewModelFactory: ViewModelProvider.Factory
        get() = ViewModelFactory(app)
}
