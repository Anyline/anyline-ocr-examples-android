package com.anyline.examples

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import at.nineyards.anyline.BuildConfig.VERSION_CODE_ANYLINE_SDK
import at.nineyards.anyline.BuildConfig.VERSION_NAME_ANYLINE_SDK
import com.anyline.examples.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.anyline2.AnylineSdk
import io.anyline2.core.LicenseException
import timber.log.Timber

class MainActivity : CameraPermissionActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        updateUi()
        binding.requestCameraPermissionButton.setOnClickListener {
            if (!AnylineSdk.isInitialized()) {
                showLicenseKeyAlertDialog()
                return@setOnClickListener
            }
            executeIfCameraPermissionGranted {
                Timber.d("--- Camera permission has been granted!")
                startActivity(SelectConfigActivity.buildIntent(this))
                finish()
            }
        }
        binding.otaUpdateButton.setOnClickListener {
            startActivity(OTAScanActivity.buildIntent(this))
        }
    }

    private fun showLicenseKeyAlertDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.license_key_not_initialized_title))
            .setMessage(resources.getString(R.string.license_key_not_initialized_message))
            .setNegativeButton(resources.getString(R.string.button_cancel)) { dialog, which ->

            }
            .setNeutralButton(resources.getString(R.string.license_key_not_initialized_documentation_button)) { dialog, which ->
                val intent = Intent()
                intent.setAction(Intent.ACTION_VIEW)
                intent.setData(Uri.parse(resources.getString(R.string.license_key_not_initialized_documentation_url)))
                startActivity(Intent.createChooser(intent, null))
            }
            .show()
    }

    private fun updateUi() {
        var expiryDateText = try {
            val expiryDate = io.anyline2.AnylineSdk.getExpiryDate()
            val dateFormat = DateFormat.getDateFormat(this)
            dateFormat.format(expiryDate)
        } catch (e: LicenseException) {
            "(Error getting expiry date: ${e.message})"
        }

        when (checkIfCameraPermissionGranted()) {
            true -> {
                binding.requestCameraPermissionButton.text = getString(R.string.start_scanning)
                binding.otaUpdateButton.isEnabled = true
            }
            false -> {
                binding.requestCameraPermissionButton.text = getString(R.string.request_camera_permission)
                binding.otaUpdateButton.isEnabled = false
            }
        }

        binding.versionTextview.text =
            "Anyline SDK Version: ${VERSION_NAME_ANYLINE_SDK} (${VERSION_CODE_ANYLINE_SDK})\n" +
                    "New SDK App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                    "License expires on: ${expiryDateText}\n"
    }
}