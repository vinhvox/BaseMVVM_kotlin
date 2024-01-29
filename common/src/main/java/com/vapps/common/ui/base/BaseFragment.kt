package com.vapps.common.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<B : ViewDataBinding>(
    @LayoutRes
    private val layoutId: Int,
) : Fragment() {

    lateinit var viewBinding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Called to Initialize view data binding variables when fragment view is created.
     */
    abstract fun onInitDataBinding()

    abstract fun observeLifecycleEvents()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        viewBinding.lifecycleOwner = viewLifecycleOwner
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onInitDataBinding()
        observeLifecycleEvents()
    }
}
