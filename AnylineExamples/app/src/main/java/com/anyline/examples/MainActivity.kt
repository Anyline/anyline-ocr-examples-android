package com.anyline.examples

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import at.nineyards.anyline.BuildConfig.VERSION_CODE_ANYLINE_SDK
import at.nineyards.anyline.BuildConfig.VERSION_NAME_ANYLINE_SDK
import com.anyline.examples.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.anyline2.AnylineSdk
import io.anyline2.init.SdkInitializationState

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        AnylineSdk.initializationState.observe(this) { sdkInitializationState ->
            updateUi(sdkInitializationState)
        }

        binding.startScanningButton.setOnClickListener {
            when (val initializationState = AnylineSdk.initializationState.value) {
                is SdkInitializationState.NotInitialized -> {
                    showLicenseKeyAlertDialog(initializationState)
                    return@setOnClickListener
                }
                else -> {
                    startSelectConfigActivity()
                }
            }
        }
    }

    private fun startSelectConfigActivity() {
        startActivity(SelectConfigActivity.buildIntent(this))
        finish()
    }

    private fun showLicenseKeyAlertDialog(sdkNotInitializedState: SdkInitializationState.NotInitialized) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.license_key_not_initialized_title))
            .setMessage(resources.getString(R.string.license_key_not_initialized_message))
            .setNegativeButton(resources.getString(R.string.button_cancel)) { _, _ ->

            }
            .setNeutralButton(resources.getString(R.string.license_key_not_initialized_documentation_button)) { _, _ ->
                val intent = Intent()
                intent.setAction(Intent.ACTION_VIEW)
                intent.setData(Uri.parse(resources.getString(R.string.license_key_not_initialized_documentation_url)))
                startActivity(Intent.createChooser(intent, null))
            }
        sdkNotInitializedState.lastError?.let { lastError ->
            if (lastError.isNetworkRelated()) {
                alertDialogBuilder.setPositiveButton(resources.getString(R.string.button_ignore)) { _, _ ->
                    startSelectConfigActivity()
                }
            }
        }
        alertDialogBuilder.show()
    }

    private fun updateUi(sdkInitializationState: SdkInitializationState) {
        val sdkInitializationStatusText = when (sdkInitializationState) {
            is SdkInitializationState.NotInitialized -> {
                binding.sdkInitProgressBar.visibility = View.GONE

                sdkInitializationState.lastError?.let {
                    "Error initializing SDK: " + (it.exception.message ?: "")
                } ?: "SDK not yet initialized"
            }
            is SdkInitializationState.InProgress -> {
                binding.sdkInitProgressBar.visibility = View.VISIBLE

                "SDK initialization in progress..."
            }
            is SdkInitializationState.Initialized -> {
                binding.sdkInitProgressBar.visibility = View.GONE

                val dateFormat = DateFormat.getDateFormat(this)
                "SDK initialization succeeded. License expires on: " +
                        dateFormat.format(sdkInitializationState.getExpiryDate())
            }
        }

        binding.versionTextview.text =
            "Anyline SDK Version: ${VERSION_NAME_ANYLINE_SDK} (${VERSION_CODE_ANYLINE_SDK})\n" +
                "New SDK App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                "$sdkInitializationStatusText\n"
    }
}