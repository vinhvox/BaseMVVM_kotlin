package com.vapps.navigation.ext

import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.vapps.navigation.NavigateTo
import com.vapps.navigation.Navigation
import com.vapps.navigation.R


fun Fragment.launch(navigation: Navigation) {
    val commonNavOptionsBuilder = NavOptions.Builder()

    val navResId = when (navigation.to) {
        NavigateTo.CHAT_MAIN -> R.id.actionNavToChatMain
        NavigateTo.PROFILE_SETTINGS -> R.id.actionNavToProfileSettings
        else -> 0
    }

    if (navResId > 0)
        findNavController()
            .navigate(
                navResId,
                navigation.extras,
                commonNavOptionsBuilder.build()
            )
    else
        when (navigation.to) {
            NavigateTo.BACK ->
                requireActivity().onBackPressed()

            else -> {

            }
        }

}