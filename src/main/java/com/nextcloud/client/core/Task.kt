package com.nextcloud.client.core

import java.util.concurrent.atomic.AtomicBoolean

/**
 * This is a wrapper for a function run in background.
 *
 * Runs task function and posts result if task is not cancelled.
 */
internal class Task<T>(
    private val postResult: (Runnable) -> Unit,
    private val taskBody: () -> T,
    private val onSuccess: OnResultCallback<T>?,
    private val onError: OnErrorCallback?
) : Runnable, Cancellable {

    private val cancelled = AtomicBoolean(false)

    override fun run() {
        @Suppress("TooGenericExceptionCaught") // this is exactly what we want here
        try {
            val result = taskBody.invoke()
            if (!cancelled.get()) {
                postResult.invoke(Runnable {
                    onSuccess?.invoke(result)
                })
            }
        } catch (t: Throwable) {
            if (!cancelled.get()) {
                postResult(Runnable { onError?.invoke(t) })
            }
        }
    }

    override fun cancel() {
        cancelled.set(true)
    }
}
