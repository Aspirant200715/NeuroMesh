package com.neuromesh.crisis.util

import android.content.Context
import android.util.Log
import com.neuromesh.crisis.BuildConfig

object Logger {
    private const val APP_TAG = "NeuroMesh"
    private var initialized = false

    fun init(context: Context) {
        initialized = true
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d("$APP_TAG/$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$APP_TAG/$tag", message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w("$APP_TAG/$tag", message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$APP_TAG/$tag", message, throwable)
    }
}
