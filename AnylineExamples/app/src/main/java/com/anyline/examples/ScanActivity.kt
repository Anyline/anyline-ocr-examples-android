package com.anyline.examples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.anyline.examples.databinding.ActivityScanBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.anyline2.view.ScanView
import io.anyline2.Event
import io.anyline2.ScanResult
import io.anyline2.legacy.trainer.AssetContext
import io.anyline2.legacy.trainer.ProjectContext
import org.json.JSONObject
import timber.log.Timber
import java.util.*

open class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    protected lateinit var scanView: ScanView

    private var scanCount: Long = 0
    private var lastScanResult: ScanResult? = null

    /**
    * Receives intrinsic run information
    {"name":
        "$resizeWidth",
        "type":"Int",
        "value":1080}"
    */
    private val onScanInfo: (JSONObject) -> Unit = {
        Timber.tag(TAG).e("onScanInfo: $it")
    }

    /**
    * Receives errors during runs
    */
    private val onError: (JSONObject) -> Unit = {
        Timber.tag(TAG).e("onError: $it")
    }

    /**
    * Receives info for unsuccessful runs
    {"code":
        5016,
        "line":0,
        "message":"NoLinesFound"}
    */
    private val onRunSkipped: (JSONObject) -> Unit = {
        Timber.tag(TAG).e("onRunSkipped: $it")
    }

    /**
    * Receives visual feedback
    {"squares":[
        {"downLeft":{"x":414,"y":847},
        "downRight":{"x":788,"y":842},
        "upLeft":{"x":414,"y":949},
        "upRight":{ x":781,"y":951}
        }
    ]}
    */
    private val onVisualFeedback: (JSONObject) -> Unit = {
        Timber.tag(TAG).e("onVisualFeedback: $it")
    }

    /**
     * Receives scan results
    {"barcodeResult":{
        "barcodes":[{
            "coordinates":[383,289,851,283,842,419,383,417],
            "format":"TRIOPTIC",
            "isBase64":true,
            "value":"NjU0OTg3"}]},
    "confidence":-1,
    "cropRect":{"height":692,"width":864,"x":108,"y":616},
    "pluginID":"Barcode|Barcodes"}
    */
    open val onResult: (ScanResult) -> Unit = {
        evalResults(
            listOf(it),
            scanView.scanViewPlugin.activeScanViewPlugin.first().scanPlugin.scanPluginConfig.shouldCancelOnResult()
        )
    }

    private val onResults: (List<ScanResult>) -> Unit = { scanResults ->
        evalResults(scanResults, !scanView.scanViewPlugin?.isStarted!!)
    }

    private fun evalResults(scanResults: List<ScanResult>, showScanAgain: Boolean) {
        for(it: ScanResult in scanResults) {
            Timber.tag(TAG).e("onResult: ${it.result}")

            lastScanResult = it

            /*
             Filter for an specific PluginId case where the aim is to
             count the number of barcodes scanned on a single ScanResult
             */
            if (it.pluginResult.pluginID.lowercase(Locale.getDefault()).contains("barcode")) {
                scanCount += it.pluginResult.barcodeResult.barcodes.size
            }
            else if (it.pluginResult.pluginID.lowercase(Locale.getDefault()).contains("mrz")) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.mrz_info_title))
                    .setMessage(resources.getString(R.string.mrz_info_message_read_nfc))
                    .setPositiveButton(resources.getString(R.string.button_yes)) { dialog, which ->
                        if (NFCMrzActivity.isNfcEnabled(this)) {
                            val intent = NFCMrzActivity.buildIntent(this, it.pluginResult.mrzResult)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                        }
                    }
                    .setNegativeButton(resources.getString(R.string.button_not_now)) { dialog, which ->

                    }
                    .show()
                break
            }
            else {
                scanCount++
            }
        }

        if (showScanAgain) {
            binding.scanAgainButton.visibility = View.VISIBLE
        }
        binding.lastresultImageview.setImageBitmap(scanResults.last().cutoutImage.bitmap)
        binding.textTotalscannedCountValue.text = scanCount.toString()
        binding.textLastscannedResultValue.text = scanResults
            .map {
                it.result.toString(2)
            }
            .joinToString { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scanView = binding.scanView

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.scanAgainButton.setOnClickListener {
            /*
             This button is used to get a new scan for ScanPluginConfigs
             set with attr "cancelOnResult" set to true since the ScanProcess
             is canceled after first scan result
             */
            scanView.start()
            lastScanResult = null

            binding.scanAgainButton.visibility = View.GONE
            binding.lastresultImageview.setImageBitmap(null)
            binding.textLastscannedResultValue.text = ""
        }

        val assetContextInfo = intent.getStringExtra(INTENT_EXTRA_ASSETCONTEXT_INFO_CONTENT)
        assetContextInfo?.let { assetInfo ->
            val assetContextJSON = JSONObject(assetInfo)
            val assetContext = ProjectContext(this, assetContextJSON)

            try {
                //initialize ScanView with AssetContext Info
                scanView.init(assetContext, 30)
                setupScanViewListeners()
            }
            catch (e: Exception) {
                showAlertDialog("Error",
                    resources.getString(R.string.ota_initAsset_error) + ": " + e
                ) { finish() }
            }

        } ?: run {
            //initialize ScanView with JSON asset file config
            scanView.init(intent.getStringExtra(INTENT_EXTRA_VIEW_CONFIG)!!)
            setupScanViewListeners()
        }
    }

    private fun setupScanViewListeners() {
        //set ScanViewPlugin listeners
        scanView.scanViewPlugin.apply {
            scanInfoReceived = Event { data -> onScanInfo.invoke(data)}
            runSkippedReceived = Event { data -> onRunSkipped.invoke(data)}
            errorReceived = Event { data -> onError.invoke(data)}
            visualFeedbackReceived = Event { data -> onVisualFeedback.invoke(data)}
            resultReceived = Event { data -> onResult.invoke(data)}
            resultsReceived = Event { data -> onResults.invoke(data) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->  {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        //Starts scanning on Activity resume
        scanView.start()
        title = scanView.scanViewPlugin.id()
    }

    override fun onPause() {
        //Stop scanning on Activity pause
        scanView.stop()
        super.onPause()
    }

    /**
     * Display the last recognized result as prettified JSON in an AlertDialog Window
     */
    private fun showDialogWithlastResult() {
        lastScanResult?.result?.let {
            showAlertDialog(getString(R.string.last_result), it.toString(2))
        }
    }


    /**
     * Show an AlertDialog with [title] and [message]
     */
    protected fun showAlertDialog(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setOnDismissListener { onDismiss?.invoke() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    companion object {
        private const val TAG = "AnylineScanActivity"
        const val INTENT_EXTRA_VIEW_CONFIG = "INTENT_EXTRA_VIEW_CONFIG"
        const val INTENT_EXTRA_ASSETCONTEXT_INFO_CONTENT = "INTENT_EXTRA_ASSET_INFO_CONTENT"

        fun buildIntent(context: Context, viewConfig: String): Intent {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_VIEW_CONFIG, viewConfig)
            return intent
        }

        fun buildIntent(context: Context, assetContext: AssetContext): Intent {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_ASSETCONTEXT_INFO_CONTENT, assetContext.toJSONObject().toString())
            return intent
        }
    }
}