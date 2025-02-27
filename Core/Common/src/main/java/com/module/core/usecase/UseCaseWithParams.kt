package com.module.core.usecase

import com.module.core.di.ApplicationScope
import com.module.core.di.IODispatcher
import com.module.core.failure.Failure
import com.module.core.functional.Either
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Abstract class for a Use Case (Interactor in terms of
 * Clean Architecture naming convention).
 *
 * This abstraction represents an execution unit for
 * different use cases (this means that any use case
 * in the application should implement this contract).
 *
 * By convention each [UseCaseWithParams] implementation will
 * execute its job in a pool of threads using
 * [Dispatchers.IO].
 *
 * The result of the computation will be posted on the
 * same thread used by the @param 'scope' [CoroutineScope].
 */
abstract class UseCaseWithParams<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Either<Failure, Type>

    operator fun invoke(
        @ApplicationScope scope: CoroutineScope,
        @IODispatcher dispatcher: CoroutineDispatcher,
        params: Params,
        onResult: (Either<Failure, Type>) -> Unit = {}
    ) {
        scope.launch {
            val deferredJob = async(dispatcher) {
                run(params)
            }
            onResult(deferredJob.await())
        }
    }

    /**
     * Helper class to represent Empty
     * Params when a use case does not
     * need them.
     *
     * @see UseCaseWithParams
     */
    class None
}
