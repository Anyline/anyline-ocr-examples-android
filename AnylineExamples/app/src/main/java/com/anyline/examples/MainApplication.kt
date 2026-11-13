package com.anyline.examples

import android.app.Application
import io.anyline2.AnylineSdk
import io.anyline2.init.SdkInitializationConfig
import io.anyline2.init.SdkInitializationListener
import io.anyline2.init.SdkInitializationParameters
import io.anyline2.init.SdkInitializationState
import io.anyline2.init.SdkInitializationStrategy

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        AnylineSdk.init(this, SdkInitializationConfig(
            sdkInitializationParameters = SdkInitializationParameters(
                licenseKey = getString(R.string.anyline_license_key)),
            sdkInitializationStrategy = SdkInitializationStrategy.AsyncAuto(object: SdkInitializationListener {
                override fun onInitializationStarted() {
                    DebugLog.d("AnylineSdkInit", "Anyline SDK initialization in progress...")
                }

                override fun onInitializationFailed(state: SdkInitializationState.NotInitialized) {
                    DebugLog.e("AnylineSdkInit", "Anyline SDK not initialized: ${state.lastError?.exception?.message}")
                }

                override fun onInitializationSucceeded(state: SdkInitializationState.Initialized) {
                    DebugLog.d("AnylineSdkInit", "Anyline SDK initialized!")
                }
            }))
        )
    }
}