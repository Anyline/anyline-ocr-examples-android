package com.anyline.examples.barcodeOverlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.anyline.examples.CameraStateObserver
import com.anyline.examples.R
import com.anyline.examples.databinding.ActivityScanBinding
import io.anyline2.Event
import io.anyline2.ScanResult
import io.anyline2.sdk.extension.preferredPreviewHeight
import io.anyline2.sdk.extension.preferredPreviewWidth
import io.anyline2.view.ScanView
import io.anyline2.viewplugin.ar.BarcodeOverlayListener
import io.anyline2.viewplugin.ar.BarcodeOverlayView
import io.anyline2.viewplugin.ar.OverlayViewHolder
import io.anyline2.view.ScanViewLoadResult
import io.anyline2.viewplugin.ScanViewPlugin

class BarcodeOverlayScanActivity: AppCompatActivity(), BarcodeOverlayListener {
    private lateinit var binding: ActivityScanBinding
    private lateinit var scanView: ScanView
    private lateinit var cameraStateObserver: CameraStateObserver

    private var scanCount: Long = 0
    private var lastScanResult: ScanResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)

        setContentView(binding.root)
        scanView = binding.scanView
        scanView.setOnScanViewLoaded { result -> onScanViewLoaded(result) }

        cameraStateObserver = CameraStateObserver(this, scanView)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.scanAgainButton.setOnClickListener {
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
                scanView.init(intent.getStringExtra(INTENT_EXTRA_VIEW_CONFIG_FILE)!!)
                scanView.start()
                title = scanView.scanViewPlugin.id()
                setupScanViewListeners()

                cameraStateObserver.apply {
                    /* Reset CameraStateObserver and set a new expected value for the resolution
                     * as defined in the ScanViewConfig. Use this to e.g. notify the end user
                     * if the device doesn't support a requested 4K resolution and needs to switch
                     * to using 1080p.
                     */
                    reset()
                    expectedViewConfigCameraSize =
                        scanView.scanViewConfigHolder.getCameraConfig()?.let { scanViewCameraConfig ->
                            Size(
                                scanViewCameraConfig.preferredPreviewWidth(),
                                scanViewCameraConfig.preferredPreviewHeight())
                        } ?: run { null }
                }
            }
            is ScanViewLoadResult.Failed -> {
                result.getErrorMessage()?.let { errorString ->
                    showAlertDialog(
                        "Error",
                        resources.getString(R.string.scanview_load_error) + ": " + errorString
                    ) { finish() }
                }
            }
        }
    }

    private fun setupScanViewListeners() {
        scanView.scanViewPlugin.apply {
            //overwrites resultReceived method
            resultReceived = Event { data ->
                scanCount += data.pluginResult.barcodeResult.barcodes.size
                binding.textTotalscannedCountValue.text = scanCount.toString()
            }

            enableBarcodeOverlays(activeScanViewPlugin.first())
        }

        scanView.start()
    }

    private fun enableBarcodeOverlays(scanViewPlugin: ScanViewPlugin) {
        //create an instance of BarcodeOverlays which calls evalResults method
        //when user clicks on an overlay view
        scanViewPlugin.enableBarcodeOverlays(this@BarcodeOverlayScanActivity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setOnDismissListener { onDismiss?.invoke() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (scanView.isInitialized) {
            //Starts scanning on Activity resume
            scanView.start()
            enableBarcodeOverlays(scanView.scanViewPlugin.activeScanViewPlugin.first())
        }
    }

    override fun onPause() {
        if (scanView.isInitialized) {
            scanView.scanViewPlugin.activeScanViewPlugin.first().disableBarcodeOverlays()
            //Stop scanning on Activity pause
            scanView.stop()
        }
        super.onPause()
    }

    override fun onCreate(barcodeOverlayView: BarcodeOverlayView): List<OverlayViewHolder> {
        /*
         * onCreate() is called every time a barcode that is not contained
         * on current overlays is discovered. Must return a list of Views
         * that will be placed near the barcode.
         */
        val overlayViewHolderList = BarcodeOverlayViewHolderList(this@BarcodeOverlayScanActivity, barcodeOverlayView)
        overlayViewHolderList.forEach { overlayViewHolder ->
            overlayViewHolder.view?.setOnLongClickListener {
                binding.scanAgainButton.visibility = View.VISIBLE
                binding.bottomScrollView.visibility = View.VISIBLE
                barcodeOverlayView.visibleBarcode.getBarcodeImage()?.let { bmp ->
                    binding.lastresultImageview.setImageBitmap(bmp)
                    binding.lastresultImageview.visibility = View.VISIBLE
                }
                binding.textLastscannedResultValue.text = barcodeOverlayView.visibleBarcode.barcode.value
                scanView.stop()
                true
            }
        }
        return overlayViewHolderList
    }

    override fun onUpdate(viewHolders: List<OverlayViewHolder>, barcodeOverlayView: BarcodeOverlayView) {
        /*
         * onUpdate() is called every time a previously detected barcode needs to be repositioned.
         */
        val oldInfo: BarcodeOverlayViewHolderList = (viewHolders as BarcodeOverlayViewHolderList)
        if (oldInfo.overlayType != BarcodeOverlayViewHolderList.getOverlayTypeFromBarcodeOverlayView(barcodeOverlayView)) {
            // calling invalidate() makes the overlay to be disposed
            // and a new onCreate() will be called for a new view instance
            barcodeOverlayView.invalidate()
        }
    }

    override fun onDestroy() {
        BarcodeOverlayViewHolderList.resetSelectedBarcodes()
        super.onDestroy()
    }

    companion object {
        private const val INTENT_EXTRA_VIEW_CONFIG_FILE = "INTENT_EXTRA_VIEW_CONFIG_FILE"

        fun buildIntent(context: Context, viewConfigFile: String): Intent {
            val intent = Intent(context, BarcodeOverlayScanActivity::class.java)
            intent.putExtra(INTENT_EXTRA_VIEW_CONFIG_FILE, viewConfigFile)
            return intent
        }
    }

}