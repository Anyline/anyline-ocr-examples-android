package com.anyline.examples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.anyline.examples.databinding.ActivityScanBinding
import com.anyline.examples.viewConfigEditor.ViewConfigEditorDefinition
import com.anyline.examples.viewConfigEditor.ViewConfigEditorFragment
import io.anyline2.Event
import io.anyline2.ScanResult
import io.anyline2.sdk.ScanViewConfigHolder
import io.anyline2.sdk.extension.toJsonObject
import io.anyline2.view.ScanView
import io.anyline2.view.ScanViewLoadResult
import io.anyline2.viewplugin.ar.uiFeedback.UIFeedbackOverlayInfoEntry
import io.anyline2.viewplugin.ar.uiFeedback.UIFeedbackOverlayViewElementEventContent
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
     * Receives UIFeedback messages
    {"messages":[
    {"level":"Error","message":"Preset tin_custom_v11 not found."}
    ]}
     */
    private val onUIFeedbackInfo: (JSONObject) -> Unit = {
        it.optJSONArray("messages")?.let { msgArray ->
            for (i in 0 until msgArray.length()) {
                UIFeedbackOverlayInfoEntry.fromJson(msgArray[i] as JSONObject).also { msgEntry ->
                    when (msgEntry.level) {
                        UIFeedbackOverlayInfoEntry.Level.Info -> Timber.tag(TAG).i("onUIFeedbackInfo: ${msgEntry.message}")
                        UIFeedbackOverlayInfoEntry.Level.Warning -> Timber.tag(TAG).w("onUIFeedbackInfo: ${msgEntry.message}")
                        UIFeedbackOverlayInfoEntry.Level.Error -> Timber.tag(TAG).e("onUIFeedbackInfo: ${msgEntry.message}")
                    }
                }
            }
        }
    }

    /**
     * Receives scan results
    {"barcodeResult":{
    "barcodes":[{
    "coordinates":[383,289,851,283,842,419,383,417],
    "format":"TRIOPTIC",
    "value":"NjU0OTg3"}]},
    "confidence":-1,
    "cropRect":{"height":692,"width":864,"x":108,"y":616},
    "pluginID":"Barcode|Barcodes"}
     */
    open val onResult: (ScanResult) -> Unit = {
        evalResults(
            listOf(it),
            scanView.scanViewPlugin.activeScanViewPlugin.first().scanPlugin.scanPluginConfig.cancelOnResult
        )
    }

    private val onResults: (List<ScanResult>) -> Unit = { scanResults ->
        evalResults(scanResults, !scanView.scanViewPlugin?.isStarted!!)
    }

    private fun evalResults(scanResults: List<ScanResult>, showScanAgain: Boolean) {
        for (it: ScanResult in scanResults) {
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
        }

        hideViewConfigEditFragment()
        if (showScanAgain) {
            binding.scanAgainButton.visibility = View.VISIBLE
        }
        binding.bottomScrollView.visibility = View.VISIBLE
        binding.lastresultImageview.setImageBitmap(scanResults.last().cutoutImage.bitmap)
        binding.lastresultImageview.visibility = View.VISIBLE
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
        scanView.setOnScanViewLoaded { result -> onScanViewLoaded(result) }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.fragmentContainerViewConfigEdit.visibility == View.VISIBLE) {
                    hideViewConfigEditFragment()
                    return
                }
                finish()
            }
        })

        binding.scanAgainButton.setOnClickListener {
            /*
             This button is used to get a new scan for ScanPluginConfigs
             set with attr "cancelOnResult" set to true since the ScanProcess
             is canceled after first scan result
             */
            scanView.start()
            lastScanResult = null

            binding.scanAgainButton.visibility = View.GONE
            binding.bottomScrollView.visibility = View.GONE
            binding.lastresultImageview.setImageBitmap(null)
            binding.lastresultImageview.visibility = View.GONE
            binding.textLastscannedResultValue.text = ""
        }
    }

    private fun onScanViewLoaded(result: ScanViewLoadResult) {
        when (result) {
            is ScanViewLoadResult.Succeeded -> {
                if (intent.hasExtra(INTENT_EXTRA_VIEW_CONFIG_FILE)) {
                    //initialize ScanView with asset file config
                    initScanView(ScanViewInitOption.InitWithAssetFile(
                        intent.getStringExtra(INTENT_EXTRA_VIEW_CONFIG_FILE)!!),
                        true)
                }
                else {
                    //initialize ScanView with JSON object
                    initScanView(ScanViewInitOption.InitWithJsonObject(
                        JSONObject(intent.getStringExtra(INTENT_EXTRA_VIEW_CONFIG_JSON)!!)),
                        true)
                }
            }
            is ScanViewLoadResult.Failed -> {
                result.getErrorMessage()?.let { errorString ->
                    showAlertDialog(
                        "Error",
                        resources.getString(R.string.scanview_load_error) + ": " + errorString,
                        { finish() }
                    )
                }
            }
        }

    }

    sealed class ScanViewInitOption {
        data class InitWithAssetFile(val assetFileName: String): ScanViewInitOption()
        data class InitWithJsonObject(val jsonObject: JSONObject): ScanViewInitOption()
    }

    private fun initScanView(scanViewInitOption: ScanViewInitOption,
                             autoStart: Boolean = false): Boolean {
        try {
            when (scanViewInitOption) {
                is ScanViewInitOption.InitWithAssetFile -> {
                    scanView.init(scanViewInitOption.assetFileName)
                }
                is ScanViewInitOption.InitWithJsonObject -> {
                    scanView.init(scanViewInitOption.jsonObject)
                }
            }
            title = scanView.scanViewPlugin.id()
            setupScanViewListeners()
            if (autoStart) {
                scanView.start()
            }
            return true
        } catch (e: Exception) {
            showAlertDialog(
                "Error",
                resources.getString(R.string.scanview_init_error) + ": " + e
            )
        }
        return false
    }

    private fun setupScanViewListeners() {
        //set ScanViewPlugin listeners
        scanView.scanViewPlugin.apply {
            scanInfoReceived = Event { data -> onScanInfo.invoke(data) }
            runSkippedReceived = Event { data -> onRunSkipped.invoke(data) }
            errorReceived = Event { data -> onError.invoke(data) }
            visualFeedbackReceived = Event { data -> onVisualFeedback.invoke(data) }
            uiFeedbackInfoReceived = Event { data -> onUIFeedbackInfo.invoke(data) }
            resultReceived = Event { data -> onResult.invoke(data) }
            resultsReceived = Event { data -> onResults.invoke(data) }
        }

        scanView.onUIFeedbackOverlayViewClickedEvent = Event { data ->
            val elementEventContent: UIFeedbackOverlayViewElementEventContent = data.second
            if (elementEventContent.element.tag.isNotEmpty()) {
                Toast.makeText(this,
                    elementEventContent.element.tag,
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let { rootMenu ->
            rootMenu.add(Menu.NONE, EDIT_CONFIG_MENU_ID, Menu.NONE, "Edit Config").apply {
                setIcon(R.drawable.ic_action_pencil)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
            }
            EDIT_CONFIG_MENU_ID -> {
                scanView.scanViewConfigHolder?.let { scanViewConfigHolder ->
                    scanViewConfigHolder.modifyViewConfig { currentScanViewConfig ->
                        showViewConfigEditFragment(ViewConfigEditorDefinition.ScanViewConfig,
                            currentScanViewConfig.toJsonObject()
                        ) { json ->
                            initScanView(ScanViewInitOption.InitWithJsonObject(json), true)
                        }
                        ScanViewConfigHolder.ModifyViewConfigResult.Discard
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var webContentFragment: ViewConfigEditorFragment? = null

    private fun showViewConfigEditFragment(viewConfigEditorDefinition: ViewConfigEditorDefinition,
                                   jsonContent: JSONObject,
                                   onJsonApply: ((JSONObject) -> Unit)) {
        if (webContentFragment == null) {
            webContentFragment = ViewConfigEditorFragment(
                viewConfigEditorDefinition,
                jsonContent,
                onJsonApply).also { fragment ->
                binding.fragmentContainerViewConfigEdit.apply {
                    supportFragmentManager.beginTransaction()
                        .add(this.id, fragment)
                        .commit()
                }
            }
        }
        binding.fragmentContainerViewConfigEdit.visibility = View.VISIBLE
    }

    private fun hideViewConfigEditFragment() {
        binding.fragmentContainerViewConfigEdit.visibility = View.GONE
    }

    private fun disposeViewConfigEditFragment() {
        binding.fragmentContainerViewConfigEdit.apply {
            webContentFragment?.let {
                supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (scanView.isInitialized) {
            //Starts scanning on Activity resume
            scanView.start()
        }
    }

    override fun onPause() {
        if (scanView.isInitialized) {
            //Stop scanning on Activity pause
            scanView.stop()
        }
        super.onPause()
    }

    override fun onStop() {
        disposeViewConfigEditFragment()
        super.onStop()
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
        const val INTENT_EXTRA_VIEW_CONFIG_FILE = "INTENT_EXTRA_VIEW_CONFIG_FILE"
        const val INTENT_EXTRA_VIEW_CONFIG_JSON = "INTENT_EXTRA_VIEW_CONFIG_JSON"

        private const val EDIT_CONFIG_MENU_ID = 1

        fun buildIntent(context: Context, viewConfigFile: String): Intent {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_VIEW_CONFIG_FILE, viewConfigFile)
            return intent
        }

        fun buildIntent(context: Context, viewConfigJson: JSONObject): Intent {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_VIEW_CONFIG_JSON, viewConfigJson.toString())
            return intent
        }
    }
}