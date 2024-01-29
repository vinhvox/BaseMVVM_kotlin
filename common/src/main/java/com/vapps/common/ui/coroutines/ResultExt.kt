package com.vapps.common.ui.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


// Based on: https://proandroiddev.com/resilient-use-cases-with-kotlin-result-coroutines-and-annotations-511df10e2e16

/**
 * Like [runCatching], but with proper coroutines cancellation handling. Also only catches [Exception] instead of [Throwable].
 *
 * Cancellation exceptions need to be rethrown. See https://github.com/Kotlin/kotlinx.coroutines/issues/1814.
 */
/*
inline fun <R> resultOf(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (t: TimeoutCancellationException) {
        Result.failure(t)
    } catch (c: CancellationException) {
        throw c
    } catch (e: Exception) {
        Result.failure(e)
    }
}

 */

/**
 * Like [runCatching], but with proper coroutines cancellation handling. Also only catches [Exception] instead of [Throwable].
 *
 * Cancellation exceptions need to be rethrown. See https://github.com/Kotlin/kotlinx.coroutines/issues/1814.
 */

inline fun <T, R> T.resultOf(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (t: TimeoutCancellationException) {
        Result.failure(t)
    } catch (c: CancellationException) {
        throw c
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Like [mapCatching], but uses [resultOf] instead of [runCatching].
 */

inline fun <R, T> Result<T>.mapResult(transform: (value: T) -> R): Result<R> {
    val successResult = getOrNull()
    return when {
        successResult != null -> resultOf { transform(successResult) }
        else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R, T> Result<T>.andThen(transform: (value: T) -> Result<R>): Result<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }

    val successResult = getOrNull()
    return when {
        successResult != null -> transform(successResult)
        else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
    }
}


@OptIn(ExperimentalContracts::class)
inline fun <R, T> Result<T>.andThenFlow(transform: (value: T) -> Flow<Result<R>>): Flow<Result<R>> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }

    val successResult: T? = getOrNull()

    return when {
        successResult != null -> transform(successResult)
        else -> flowOf(Result.failure(exceptionOrNull() ?: error("Unreachable state")))
    }
}


class None
