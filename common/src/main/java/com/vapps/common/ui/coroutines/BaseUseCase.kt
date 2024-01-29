package com.vapps.common.ui.coroutines

import kotlinx.coroutines.CoroutineDispatcher


abstract class BaseUseCase(
    executionDispatcher: CoroutineDispatcher
) {
    protected val dispatcher = executionDispatcher

    abstract fun logException(e: Exception)
}