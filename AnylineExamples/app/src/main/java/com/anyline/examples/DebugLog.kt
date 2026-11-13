package com.anyline.examples

import android.util.Log

/**
 * Writes to the Android log in debug builds only.
 *
 * The examples log SDK callbacks and scan results, which can contain personal data such as
 * ID, MRZ or license plate values. Release builds must not write them to the system log, so
 * use these functions instead of calling [Log] directly.
 */
object DebugLog {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.e(tag, message)
    }
}
