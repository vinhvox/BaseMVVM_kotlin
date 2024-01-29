package com.vapps.basemvvm.helper

import androidx.databinding.InverseMethod

object TypeConverter {

    @InverseMethod("toInt")
    fun toString(value: Int): String = value.toString()

    fun toInt(value: String?): Int {
        return if (!value.isNullOrEmpty()) value.toInt() else 0
    }
}
