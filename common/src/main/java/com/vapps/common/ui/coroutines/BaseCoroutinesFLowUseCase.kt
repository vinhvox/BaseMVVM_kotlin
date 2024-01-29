package com.vapps.common.ui.coroutines

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

abstract class BaseCoroutinesFLowUseCase<Results, in Params>(
    executionDispatcher: CoroutineDispatcher
) : BaseUseCase(executionDispatcher) {

    abstract fun buildUseCaseFlow(params: Params? = null): Flow<Results>

    fun execute(params: Params? = null): Flow<Result<Results>> =
        try {
            if (dispatcher == Dispatchers.Main)
                throw RuntimeException("Use case '${this::class.simpleName}' cannot be executed in $dispatcher")

            this.buildUseCaseFlow(params)
                .flowOn(dispatcher)
                .map {
                    resultOf { it }
                }
        } catch (e: Exception) {
            logException(e)
            flowOf(Result.failure(Throwable(e.localizedMessage)))
        }

    override fun logException(e: Exception) {
        Log.e(this::class.simpleName, "${this::class.simpleName} : ${e.localizedMessage}")
    }
}