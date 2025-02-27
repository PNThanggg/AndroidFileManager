package com.file.manager

import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class FileManagerApp : MultiDexApplication() {
    companion object {
        private const val TAG = "FileManagerApp"

        private class ReleaseTree : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                    return
                }
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        Timber.tag(TAG).d("onCreate()")
    }
}

