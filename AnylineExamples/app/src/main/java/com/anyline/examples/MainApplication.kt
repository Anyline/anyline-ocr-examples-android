package com.anyline.examples

import android.app.Application
import io.anyline2.AnylineSdk
import io.anyline2.init.SdkInitializationConfig
import io.anyline2.init.SdkInitializationListener
import io.anyline2.init.SdkInitializationParameters
import io.anyline2.init.SdkInitializationState
import io.anyline2.init.SdkInitializationStrategy

import timber.log.Timber

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AnylineSdk.init(this, SdkInitializationConfig(
            sdkInitializationParameters = SdkInitializationParameters(
                licenseKey = getString(R.string.anyline_license_key)),
            sdkInitializationStrategy = SdkInitializationStrategy.AsyncAuto(object: SdkInitializationListener {
                override fun onInitializationStarted() {
                    Timber
                        .tag("AnylineSdkInit")
                        .d("Anyline SDK initialization in progress...")
                }

                override fun onInitializationFailed(state: SdkInitializationState.NotInitialized) {
                    Timber
                        .tag("AnylineSdkInit")
                        .e("Anyline SDK not initialized: ${state.lastError?.exception?.message}")
                }

                override fun onInitializationSucceeded(state: SdkInitializationState.Initialized) {
                    Timber
                        .tag("AnylineSdkInit")
                        .d("Anyline SDK initialized!")
                }
            }))
        )
    }
}