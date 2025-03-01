package com.library.icon_loader_lib.icons.cache;

import android.os.Handler;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A runnable that can be posted to a {@link Handler} which can be canceled.
 */
public class HandlerRunnable<T> implements Runnable {

    private final Handler mWorkerHandler;
    private final Supplier<T> mTask;

    private final Executor mCallbackExecutor;
    private final Consumer<T> mCallback;
    private final Runnable mEndRunnable;

    private boolean mEnded = false;
    private boolean mCanceled = false;

    public HandlerRunnable(Handler workerHandler, Supplier<T> task, Executor callbackExecutor, Consumer<T> callback) {
        this(workerHandler, task, callbackExecutor, callback, () -> {
        });
    }

    public HandlerRunnable(Handler workerHandler, Supplier<T> task, Executor callbackExecutor, Consumer<T> callback, Runnable endRunnable) {
        mWorkerHandler = workerHandler;
        mTask = task;
        mCallbackExecutor = callbackExecutor;
        mCallback = callback;
        mEndRunnable = endRunnable;
    }

    /**
     * Cancels this runnable from being run, only if it has not already run.
     */
    public void cancel() {
        mWorkerHandler.removeCallbacks(this);
        mCanceled = true;
        mCallbackExecutor.execute(this::onEnd);
    }

    @Override
    public void run() {
        T value = mTask.get();
        mCallbackExecutor.execute(() -> {
            if (!mCanceled) {
                mCallback.accept(value);
            }
            onEnd();
        });
    }

    private void onEnd() {
        if (!mEnded) {
            mEnded = true;
            mEndRunnable.run();
        }
    }
}
