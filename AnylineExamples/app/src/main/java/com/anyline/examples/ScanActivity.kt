package com.anyline.examples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.anyline.examples.databinding.ActivityScanBinding
import io.anyline.view.ScanView
import io.anyline2.Event
import io.anyline2.ScanResult
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var scanView: ScanView

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
    private val onResult: (ScanResult) -> Unit = {
        Timber.tag(TAG).e("onResult: ${it.result}")

        lastScanResult = it

        /*
         Filter for an specific PluginId case where the aim is to
         count the number of barcodes scanned on a single ScanResult
         */
        if (it.pluginResult.pluginID.lowercase(Locale.getDefault()).contains("barcode")) {
            scanCount += it.pluginResult.barcodeResult.barcodes.size
        } else {
            scanCount++
        }

        /*
         This is being called from a background thread,
         so you must make sure to switch to a UI thread when updating the UI.
        */
        Handler(Looper.getMainLooper()).post {
            if (scanView.scanViewPlugin.scanPlugin.scanPluginConfig.shouldCancelOnResult()) {
                binding.scanAgainButton.visibility = View.VISIBLE
            }
            binding.lastresultImageview.setImageBitmap(it.image.bitmap)
            binding.textTotalscannedCountValue.text = scanCount.toString()
            binding.textLastscannedResultValue.text = it.result.toString(2)
        }
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
            scanView.scanViewPlugin.start()
            lastScanResult = null

            binding.scanAgainButton.visibility = View.GONE
            binding.lastresultImageview.setImageBitmap(null)
            binding.textLastscannedResultValue.text = ""
        }

        initScanView(intent.getStringExtra(INTENT_EXTRA_VIEW_CONFIG)!!)
    }

    private fun initScanView(viewConfig: String) {
        //initialize ScanView with JSONConfig
        scanView.init(viewConfig)

        //set ScanViewPlugin listeners
        scanView.scanViewPlugin.apply {
            scanInfoReceived = Event { data -> onScanInfo.invoke(data)}
            runSkippedReceived = Event { data -> onRunSkipped.invoke(data)}
            errorReceived = Event { data -> onError.invoke(data)}
            visualFeedbackReceived = Event { data -> onVisualFeedback.invoke(data)}
            resultReceived = Event { data -> onResult.invoke(data)}
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        //Starts scanning on Activity resume
        scanView.start()
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
    private fun showAlertDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title).setMessage(message)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    companion object {
        private const val TAG = "AnylineScanActivity"
        private const val INTENT_EXTRA_VIEW_CONFIG = "INTENT_EXTRA_VIEW_CONFIG"

        fun buildIntent(context: Context, viewConfig: String): Intent {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_VIEW_CONFIG, viewConfig)
            return intent
        }
    }
}