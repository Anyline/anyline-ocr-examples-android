package com.anyline.examples

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Base64
import io.anyline2.ScanResult
import io.anyline2.core.ScanController
import io.anyline2.legacy.products.AnylineUpdater
import io.anyline2.legacy.products.IAnylineUpdateDelegate
import io.anyline2.legacy.trainer.AssetContext
import io.anyline2.legacy.trainer.ProjectContext
import org.json.JSONException
import org.json.JSONObject

class OTAScanActivity : ScanActivity(), IAnylineUpdateDelegate {

    private var assetContext: AssetContext? = null
    private var progressDialog: ProgressDialog? = null

    override val onResult: (ScanResult) -> Unit = { scanResult ->
        if (scanResult.pluginResult.barcodeResult.barcodes.size > 0) {
            scanResult.pluginResult.barcodeResult.barcodes[0].apply {
                val decodedResult = when (this.isBase64) {
                    true -> Base64.decode(this.value, Base64.DEFAULT).toString(charset("UTF-8"))
                    else -> this.value
                }
                 try {
                     val jsonResult = JSONObject(decodedResult)
                     assetContext = ProjectContext(this@OTAScanActivity, jsonResult).apply {
                         if (jsonResult.has("projectId")) {
                             //json projectId attr differs from TrainerUtil.k_projectID by "d" upper/lower case
                             setParameter("projectID", jsonResult.getString("projectId"))
                         }
                         AnylineUpdater.update(this@OTAScanActivity,
                             this,
                             this@OTAScanActivity,
                             ScanController.PluginType.OCR
                         )
                     }
                     showProgressDialog(resources.getString(R.string.ota_checkForUpdate_label),
                         resources.getString(R.string.ota_checkForUpdate_downloading),
                         false)
                }
                 catch (je: JSONException) {
                     showEndMessageDialog("BarcodeError",
                         resources.getString(R.string.ota_checkForUpdate_error_barcode) + ": " + je
                     ) { finish() }
                 }
                 catch (e: Exception) {
                     showEndMessageDialog("Error",
                         resources.getString(R.string.ota_checkForUpdate_error) + ": " + e
                     ) { finish() }
                 }
            }
        }

    }

    private fun scanWithAssetConfig() {
        assetContext?.let { asset ->
            val intent = buildIntent(this, asset)
            finish()
            startActivity(intent)
        }
    }

    private fun showEndMessageDialog(title: String, message: String, onDismiss: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            progressDialog?.let {
                it.dismiss()
                progressDialog = null
            }
            showAlertDialog(title, message, onDismiss)
        }
    }

    private fun showProgressDialog(title: String, message: String, indeterminate: Boolean) {
        Handler(Looper.getMainLooper()).post {
            progressDialog?.let {
                it.dismiss()
                progressDialog = null
            }
            progressDialog = ProgressDialog(this).apply {
                setTitle(title)
                setMessage(message)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                isIndeterminate = indeterminate
                if (!indeterminate) {
                    setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                    max = 100
                }
                show()
            }
        }
    }

    private fun updateDialogProgress(value: Int) {
        progressDialog?.let {
            it.progress = value
        }
    }

    companion object {
        private const val TAG = "AnylineOTAScanActivity"

        fun buildIntent(context: Context): Intent {
            val intent = Intent(context, OTAScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_VIEW_CONFIG, "barcode_config_ota.json")
            return intent
        }
    }

    override fun onUpdateProgress(fileName: String?, progress: Float) {
        updateDialogProgress((progress * 100).toInt())
    }

    override fun onUpdateError(error: String?) {
        showEndMessageDialog("AssetUpdateError",
            resources.getString(R.string.ota_checkForUpdate_error) + ": " + error
        ) { finish() }
    }

    override fun onUpdateFinished() {
        scanWithAssetConfig()
    }
}