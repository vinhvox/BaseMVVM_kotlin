package com.vapps.common.ui.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams


abstract class BaseActivity(
    @LayoutRes
    private val layoutId: Int,
) : AppCompatActivity() {
    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private var actionBar: ActionBar? = null

    abstract fun initViews(layoutView: View)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)
        actionBar = supportActionBar


        //populate view
        val layoutView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        initViews(layoutView)

        applyWindowInsets(findViewById(android.R.id.content))
    }

    /**
     * Use Window Insets to apply system paddings to this activity
     */
    private fun applyWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->

            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }

            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }
}