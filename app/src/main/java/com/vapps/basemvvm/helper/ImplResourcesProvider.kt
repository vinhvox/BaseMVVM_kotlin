package com.vapps.basemvvm.helper

import android.accounts.NetworkErrorException
import android.content.res.Resources
import com.vapps.basemvvm.R

class ImplResourcesProvider(
    private val resources: Resources
) : ResourcesProvider {

    override fun getErrorMessage(throwable: Throwable?): String =
        when (throwable) {

            is NetworkErrorException ->
                resources.getString(R.string.message_trouble_internet_connection)

            else -> throwable?.localizedMessage ?: ""
        }


    override fun getStringMessage(resId: Int): String =
        resources.getString(resId)

    override fun getFormattedString(resId: Int, vararg args: Any): String =
        resources.getString(resId, *args)
}