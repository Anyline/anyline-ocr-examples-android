package com.anyline.examples

import android.app.Application
import io.anyline2.core.LicenseException

import timber.log.Timber

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // This must be called before doing anything Anyline-related!
        // Try/Catch this to check whether or not your license key is valid!
        try {
            io.anyline2.AnylineSdk.init(getString(R.string.anyline_license_key), this)
        }
        catch (e: LicenseException) {
            // handle exception
            Timber.tag("LicenseException").e(e)
        }
    }
}