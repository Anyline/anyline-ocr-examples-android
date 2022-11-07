/*
 * Anyline
 * ScanBarcodeActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */
package io.anyline.examples.barcode

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.anyline.camera.CameraController
import io.anyline.camera.CameraOpenListener
import io.anyline.examples.R
import io.anyline.examples.ScanActivity
import io.anyline.examples.ScanModuleEnum.ScanModule
import io.anyline.examples.databinding.ActivityAnylineMultiBarcodeScanViewBinding
import io.anyline.examples.dependencyinjection.IoDispatcher
import io.anyline.plugin.barcode.*
import io.anyline.view.ScanView
import kotlinx.coroutines.*
import javax.inject.Inject


/**
 * Shows Multiple Continuous Barcode Scanning with Overlays. Exists as hidden functionality.
 * Details in the following Ticket and (especially!) PR:
 * Ticket: https://anyline.atlassian.net/browse/SDKY-190
 * PR: https://bitbucket.org/9yardsgmbh/anylineexamples-android/pull-requests/96
 */
@AndroidEntryPoint
class ScanBarcodeWithOverlaysActivity : ScanActivity(), CameraOpenListener {

    private lateinit var binding: ActivityAnylineMultiBarcodeScanViewBinding

    private lateinit var scanView: ScanView
    private lateinit var scanViewPlugin: BarcodeScanViewPlugin
    private lateinit var scanPlugin: BarcodeScanPlugin

    private lateinit var barcodePreferences: BarcodePreferences
    private lateinit var resultText: TextView
    private lateinit var preselectedItems: ArrayList<String>
    private lateinit var scanButton: Button
    private var resultScreenShown = false

    @IoDispatcher
    @Inject
    lateinit var ioDispatcher: CoroutineDispatcher

    private val currentlyVisibleBarcodeViewMap = mutableMapOf<String, BarcodeView>()

    private var lastResultTimeMillis = 0L

    companion object {
        const val ACTIVITYRESULT_CODE_BARCODE_LIST = 2
    }

    /**
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnylineMultiBarcodeScanViewBinding.inflate(
            layoutInflater,
            findViewById<View>(R.id.scan_view_placeholder) as ViewGroup,
            true
        )
        barcodePreferences = BarcodePreferences.getInstance(this)
        preselectedItems = barcodePreferences.arrayString
        resultText = binding.textResult

        scanButton = binding.stopScanningButton

        scanView = binding.scanView
        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        scanView.setCameraOpenListener(this)
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        scanView.setScanConfig("barcode_overlay_view_config.json")

        scanPlugin = BarcodeScanPlugin(applicationContext, "barcode")
        scanPlugin.setCancelOnResult(false)
        if (preselectedItems.size == 0) {
            barcodePreferences.setDefault()
            preselectedItems = barcodePreferences.arrayString
        }
        scanPlugin.enablePDF417Parsing()

        scanViewPlugin = BarcodeScanViewPlugin(
            applicationContext,
            scanPlugin,
            scanView.scanViewPluginConfig
        )
        scanViewPlugin.setMultiBarcode(true) // We keep this true for this mode

        setBarcodeTypes(preselectedItems)
        val flashView = scanView.flashView
        (flashView.parent as ViewGroup).removeView(flashView)

        //build a linear layout for making it possible to arrange the flash as we want to
        val mainLayout = binding.mainLayoutLinear
        mainLayout.visibility = View.VISIBLE
        //set all parameters for the flashview
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.gravity = Gravity.CENTER_VERTICAL
        params.weight = 1.0f
        params.rightMargin = 100
        params.topMargin = 50
        flashView.layoutParams = params

        //add the flash view to the main layout of the buttons
        mainLayout.addView(flashView)
        scanView.scanViewPlugin = scanViewPlugin

        /* Launch a coroutine that checks in periodic time intervals if new results came in,
         * and if there were no new results for a specified time frame, it clears the overlays. */
        lifecycleScope.launch {
            while(true) {
                delay(500)
                if (System.currentTimeMillis() - lastResultTimeMillis > 1000L) {
                    if (binding.viewCanvasSwitch.isChecked) {
                        resetViewOverlay()
                    } else {
                        resetCanvasOverlay()
                    }
                }
            }
        }

        scanViewPlugin.addScannedBarcodesListener { scanResult: BarcodeScanResult ->
            lastResultTimeMillis = System.currentTimeMillis()

            lifecycleScope.launch {
                handleVisibleBarcodes(scanResult)
            }

            showScanButton(true)
            if (scanViewPlugin.isMultiBarcodeEnabled) { // Continue scanning
                scanButton.visibility = View.VISIBLE
                scanButton.setOnClickListener { view: View? ->
                    val path = setupImagePath(scanResult.cutoutImage)
                    scanView.stop()
                    startScanResultIntent(
                        resources.getString(R.string.category_barcodes),
                        getBarcodeResult(scanResult.result),
                        path
                    )
                    setupScanProcessView(this@ScanBarcodeWithOverlaysActivity, scanResult, scanModule)
                    finish()
                }
            } else { // Stop after getting one result
                if (scanResult.result.size != 1) return@addScannedBarcodesListener
                if (!resultScreenShown) {
                    val path = setupImagePath(scanResult.cutoutImage)
                    scanView.stop()
                    resultScreenShown = true
                    startScanResultIntent(
                        resources.getString(R.string.category_barcodes),
                        getBarcodeResult(scanResult.result),
                        path
                    )
                    setupScanProcessView(this@ScanBarcodeWithOverlaysActivity, scanResult, scanModule)
                    finish()
                }
            }
        }

        binding.barcodeScannerSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->
            scanView.stop()
            showScanButton(false)
            scanViewPlugin.setMultiBarcode(isChecked)
            scanView.start()
        }
    }

    private fun showScanButton(show: Boolean) {
        scanButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Takes in a [BarcodeScanResult] and handles logic that decides what overlays to draw on screen.
     *
     * Launch with CoroutineScope to allow server call mocking. This changes color of the
     * background when Views (as opposed to Canvas) are being used:
     * - [MockServerCallStatus.WAITING] -> View background is Blue
     * - [MockServerCallStatus.SUCCESS] -> View background is Green
     * - [MockServerCallStatus.ERROR]   -> View background is Red
     */
    private suspend fun handleVisibleBarcodes(scanResult: BarcodeScanResult) {
        val scanResultVisibleBarcodes = getVisibleBarcodes(scanResult)

        if (binding.viewCanvasSwitch.isChecked) {
            // Draw Barcode Overlays using Views
            resetCanvasOverlay()

            /**
             * scanResultVisibleBarcodes: all Barcodes found with this scanResult
             * currentlyVisibleBarcodeViewMap: (from previous result) newlyVisibleBV + alreadyVisibleBV
             * previouslyVisibleBarcodeViewMap: currentlyVisibleBV - scanResultVisibleB
             * newlyVisibleBarcodeViewMap: scanResultVisibleB - currentlyVisibleBV
             * alreadyVisibleBarcodeViewMap: scanResultVisibleB ∩ currentlyVisibleBV
             */

            val previouslyVisibleBarcodeViewMap = filterToPreviouslyVisibleBarcodeViewMap(
                currentlyVisibleBarcodeViewMap,
                scanResultVisibleBarcodes
            )

            val newlyVisibleBarcodeViewMap = filterToNewlyVisibleBarcodeViewMap(
                scanResultVisibleBarcodes,
                currentlyVisibleBarcodeViewMap
            )

            val alreadyVisibleBarcodeViewMap = filterToAlreadyVisibleBarcodeViewMap(
                currentlyVisibleBarcodeViewMap,
                scanResultVisibleBarcodes
            )

            checkAndRemoveViewsFromBarcodeViewMaps(
                previouslyVisibleBarcodeViewMap,
                currentlyVisibleBarcodeViewMap
            )

            currentlyVisibleBarcodeViewMap.putAll(newlyVisibleBarcodeViewMap)
            currentlyVisibleBarcodeViewMap.putAll(alreadyVisibleBarcodeViewMap)

            // Draw all Barcodes that we want visible at this point
            drawBarcodeViewOverlay(currentlyVisibleBarcodeViewMap.values.toList())
        } else {
            // Draw Barcode Overlays using a Canvas
            resetViewOverlay()
            drawCanvasOverlay(scanResultVisibleBarcodes)
        }
    }

    /**
     * Take a Map<String, BarcodeView> and return those values from it, that are not present in
     * a given List<VisibleBarcode>.
     *
     * previouslyVisibleBarcodeViewMap = currentlyVisibleBarcodeViewMap - scanResultVisibleBarcodes
     */
    private fun filterToPreviouslyVisibleBarcodeViewMap(
        currentlyVisibleBarcodeViewMap: Map<String, BarcodeView>,
        scanResultVisibleBarcodes: List<VisibleBarcode>
    ): MutableMap<String, BarcodeView> {
        return currentlyVisibleBarcodeViewMap.filterNot { currentlyVisible ->
            // Filter all from currentlyVisibleBVM that are NOT in scanResultVisibleBarcodes
            scanResultVisibleBarcodes.any { it.value == currentlyVisible.value.visibleBarcode.value }
        }.toMutableMap()
    }

    /**
     * Take a List<VisibleBarcode> and from the values that are not present in a given
     * Map<String, BarcodeView> create a Map<String, BarcodeView>. Creates Views for the
     * VisibleBarcodes in the process.
     *
     * newlyVisibleBarcodeViewMap = scanResultVisibleBarcodes - currentlyVisibleBarcodeViewMap
     */
    private suspend fun filterToNewlyVisibleBarcodeViewMap(
        scanResultVisibleBarcodes: List<VisibleBarcode>,
        currentlyVisibleBarcodeViewMap: Map<String, BarcodeView>
    ): Map<String, BarcodeView> {
        return scanResultVisibleBarcodes.filterNot { visibleBarcode ->
            // Filter all from scanResultVisibleBarcodes that are NOT in currentlyVisibleBVM
            currentlyVisibleBarcodeViewMap.any { it.key == visibleBarcode.value }
        }.map { newBarcode ->
            // Create new Views for every VisibleBarcode that's new
            val barcodeView = BarcodeView(
                newBarcode,
                createViewOverlay(newBarcode)
            )

            // Start the mock server call
            mockServerCall(barcodeView)

            barcodeView
        }.associateBy { barcodeView ->
            // Create Map <String, VisibleBarcode>
            barcodeView.visibleBarcode.value
        }
    }

    /**
     * Mock a server call. Generate a random Long between 0-mockServerCallPeriodMillis to simulate
     * the time (in milliseconds) how long the server call takes. A call under
     * mockTimeoutDefinitionMillis is considered successful, anything above is considered a
     * Failure/Error. Delays the Coroutine for the random time, then updates the given
     * barcodeView.mockServerCallStatus with the result.
     */
    private suspend fun mockServerCall(
        barcodeView: BarcodeView,
        mockServerCallPeriodMillis: Long = 7000,
        mockTimeoutDefinitionMillis: Long = 5000
    ) {
        CoroutineScope(ioDispatcher).launch {
            val delayTimeMillis = (0..mockServerCallPeriodMillis).random().toLong()
            delay(delayTimeMillis)
            if (delayTimeMillis < mockTimeoutDefinitionMillis) {
                barcodeView.mockServerCallStatus = BarcodeView.MockServerCallStatus.SUCCESS
            } else {
                barcodeView.mockServerCallStatus = BarcodeView.MockServerCallStatus.ERROR
            }
        }
    }

    /**
     * Take a Map<String, BarcodeView> and return those values from it that are present in a given
     * List<VisibleBarcode>. Update the VisibleBarcode with the value from the List in the process.
     *
     * alreadyVisibleBarcodeViewMap = scanResultVisibleB ∩ currentlyVisibleBV
     */
    private fun filterToAlreadyVisibleBarcodeViewMap(
        currentlyVisibleBarcodeViewMap: Map<String, BarcodeView>,
        scanResultVisibleBarcodes: List<VisibleBarcode>
    ): Map<String, BarcodeView> {
        return currentlyVisibleBarcodeViewMap.filter { currentlyVisibleBarcodeView ->
            // Filter all from currentlyVisibleBVM that are in scanResultVisibleBarcodes
            scanResultVisibleBarcodes.any { it.value == currentlyVisibleBarcodeView.key }
        }.map { oldEntry ->
            // Get the matching existing View and update the VisibleBarcode with the new one
            oldEntry.value.visibleBarcode = scanResultVisibleBarcodes.find {
                it.value == oldEntry.key
            } ?: oldEntry.value.visibleBarcode

            oldEntry.value
        }.associateBy { barcodeView ->
            // Create Map <String, VisibleBarcode>
            barcodeView.visibleBarcode.value
        }
    }

    /**
     * Checks if Views need to be removed from previouslyVisibleBarcodeViewMap and
     * currentlyVisibleBarcodeViewMap and removes the entries and the View if necessary.
     */
    private fun checkAndRemoveViewsFromBarcodeViewMaps(
        previouslyVisibleBarcodeViewMap: MutableMap<String, BarcodeView>,
        currentlyVisibleBarcodeViewMap: MutableMap<String, BarcodeView>
    ) {
        // Check & remove Views from previouslyVisibleBVM & currentlyVisibleBVM
        previouslyVisibleBarcodeViewMap.entries.removeIf { entry ->
            val remove = entry.value.shouldBeRemovedOrDecreasesCounter()
            if (remove) {
                // Remove Entry from currentlyVisibleBarcodeViewMap
                currentlyVisibleBarcodeViewMap.remove(entry.key)
                // Remove ViewOverlay
                removeViewOverlay(mutableMapOf(entry.toPair()))
            }
            // Remove Entry from previouslyVisibleBarcodeViewMap
            remove
        }
    }

    /**
     * Build the list of [VisibleBarcode] from a [BarcodeScanResult]
     */
    private fun getVisibleBarcodes(scanResult: BarcodeScanResult): List<VisibleBarcode> {
        val visibleBarcodesList = mutableListOf<VisibleBarcode>()
        scanResult.result.forEach { barcode ->
            val visibleBarcode = VisibleBarcode.fromOriginalBarcode(
                barcode,
                scanResult
            )
            visibleBarcodesList.add(visibleBarcode)
        }
        return visibleBarcodesList
    }

    /**
     * Overlays using Views: Remove the given Views from Layout
     */
    private fun removeViewOverlay(viewsToRemove: MutableMap<String, BarcodeView>) {
        viewsToRemove.forEach {
            binding.overlayFramelayout.removeView(it.value.textView)
        }
    }

    /**
     * Overlays using Views: Clear everything related to Overlays using Views
     */
    private fun resetViewOverlay() {
        currentlyVisibleBarcodeViewMap.clear()
        binding.overlayFramelayout.removeAllViews()
    }

    /**
     * Overlays using Views: Create necessary Views for Overlays using Views
     */
    private fun createViewOverlay(visibleBarcode: VisibleBarcode): TextView {
        val barcodeOverlayView = TextView(this)
        binding.overlayFramelayout.addView(barcodeOverlayView)
        return barcodeOverlayView
    }

    /**
     * Overlays using Views: Update Views (Position, etc) when showing Overlays using Views
     */
    private fun drawBarcodeViewOverlay(barcodeViewsList: List<BarcodeView>) {
        barcodeViewsList.forEach { barcodeView ->
            var normalizedTop: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].y
            var normalizedBottom: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].y
            var normalizedLeft: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].x
            var normalizedRight: Double = barcodeView.visibleBarcode.normalizedCoordinates[0].x
            barcodeView.visibleBarcode.normalizedCoordinates.forEach { point ->
                normalizedTop = point.y.coerceAtMost(normalizedTop)
                normalizedBottom = point.y.coerceAtLeast(normalizedBottom)
                normalizedLeft = point.x.coerceAtMost(normalizedLeft)
                normalizedRight = point.x.coerceAtLeast(normalizedRight)
            }

            val normalizedWidth = normalizedRight - normalizedLeft
            val normalizedHeight = normalizedBottom - normalizedTop

            val width = (normalizedWidth * binding.drawOverlayImageview.width).toInt()
            val height = (normalizedHeight * binding.drawOverlayImageview.height).toInt()

            val left = (normalizedLeft * binding.drawOverlayImageview.width).toInt()
            val top = (normalizedTop * binding.drawOverlayImageview.height).toInt()

            val barcodeOverlayView = barcodeView.textView

            // Color the background with the appropriate color based on the Murelli Flavor (Testing only!)
//            val backgroundColor = BarcodeView.getMurelliColor(barcodeView.visibleBarcode.value)
//            val colorInt = Color.parseColor(backgroundColor)
//            barcodeOverlayView.setBackgroundColor(colorInt)

            // Color the background with the appropriate color based on the mockServerCallStatus
            barcodeOverlayView.setBackgroundColor(barcodeView.getMockServerCallBackgroundColor())

            barcodeOverlayView.text = BarcodeView.getMurelliFlavor(barcodeView.visibleBarcode.value) //barcodeView.visibleBarcode.value
            barcodeOverlayView.setTextColor(Color.WHITE)
            barcodeOverlayView.layoutParams = FrameLayout.LayoutParams(width, height).apply {
                leftMargin = left
                topMargin = top
            }
        }
    }

    /**
     * Overlays using Canvas: Clear Canvas
     */
    private fun resetCanvasOverlay() {
        val drawImageView = binding.drawOverlayImageview
        val bitmap = Bitmap.createBitmap(drawImageView.width, drawImageView.height, Bitmap.Config.ARGB_8888)
        drawImageView.setImageDrawable(BitmapDrawable(resources, bitmap))
    }

    /**
     * Overlays using Canvas: Draw Overlays as Paths (Polygons) on Canvas
     */
    private fun drawCanvasOverlay(visibleBarcodesList: List<VisibleBarcode>) {
        val drawImageView = binding.drawOverlayImageview
        val bitmap = Bitmap.createBitmap(drawImageView.width, drawImageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bluePaint = Paint().apply { color = Color.BLUE }
        val whitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        visibleBarcodesList.forEachIndexed { barcodeIndex, visibleBarcode ->
            val scaledCoordinates = visibleBarcode.normalizedCoordinates.map { point ->
                io.anyline.opencv.core.Point(
                    point.x * drawImageView.width,
                    point.y * drawImageView.height
                )
            }
            val drawPath = drawPolygon(scaledCoordinates)
            canvas.drawPath(drawPath, whitePaint)
        }

        // Draw Circles in the Edges (so we can see the Frame Edges on Screen and confirm it's correct))
        val edgeRadius = 50F
        canvas.drawCircle(0F, 0F, edgeRadius, bluePaint)
        canvas.drawCircle(0F, drawImageView.height.toFloat(), edgeRadius, bluePaint)
        canvas.drawCircle(drawImageView.width.toFloat(), 0F, edgeRadius, bluePaint)
        canvas.drawCircle(drawImageView.width.toFloat(), drawImageView.height.toFloat(), edgeRadius, bluePaint)

        drawImageView.setImageDrawable(BitmapDrawable(resources, bitmap))
    }

    /**
     * Takes a list of [io.anyline.opencv.core.Point] and returns a [Path] based on these Points.
     */
    private fun drawPolygon(points: List<io.anyline.opencv.core.Point>): Path {
        val drawPath = Path()
        points.forEachIndexed { pointIndex, point ->
            when (pointIndex) {
                0 -> drawPath.moveTo(point.x.toFloat(), point.y.toFloat())
                else -> drawPath.lineTo(point.x.toFloat(), point.y.toFloat())
            }
        }
        val lastPoint = points[0]
        drawPath.lineTo(lastPoint.x.toFloat(), lastPoint.y.toFloat())
        return drawPath
    }

    override fun onResume() {
        super.onResume()
        val config = scanViewPlugin.scanViewPluginConfig.cutoutConfig
        scanView.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scanView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val ratio = scanView.width.toFloat() / scanView.height
                config.width = scanView.width
                config.ratio = ratio
                scanView.updateCutoutView()
            }
        })
        resultText.text = ""
        scanView.start()
    }

    override fun onPause() {
        super.onPause()
        //stop the scanning
        scanView.stop()
        //scan button should not be present when the device is on pause
        scanButton.visibility = View.INVISIBLE

        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        scanView.releaseCameraInBackground()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the request code is same as what is passed  here it is ACTIVITYRESULT_CODE_BARCODE_LIST
        if (requestCode == ACTIVITYRESULT_CODE_BARCODE_LIST) {
            scanView.stop()
            preselectedItems = barcodePreferences.arrayString
            if (preselectedItems.size > 0 && !preselectedItems.contains("ALL")) {
                setBarcodeTypes(preselectedItems)
            }
            scanView.start()
        }
    }

    override fun getScanView(): ScanView? {
        return null
    }

    override fun getScanModule(): ScanModule {
        return ScanModule.BARCODE
    }

    override fun onCameraOpened(cameraController: CameraController, width: Int, height: Int) {
        //the camera is opened async and this is called when the opening is finished
    }

    override fun onCameraError(e: Exception) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw RuntimeException(e)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val edit_item = menu.add(0, 0, 0, "")
        edit_item.setIcon(R.drawable.ic_settings)
        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == 0) {
            val intent = Intent(this@ScanBarcodeWithOverlaysActivity, BarcodeListViewActivity::class.java)
            startActivityForResult(intent, ACTIVITYRESULT_CODE_BARCODE_LIST) // Activity is started with requestCode ACTIVITYRESULT_CODE_BARCODE_LIST
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBarcodeTypes(preselectedItems: ArrayList<String>) {
        val barcodeFormatEAN8 =
            if (preselectedItems.contains("UPC/EAN")) BarcodeFormat.EAN_8 else BarcodeFormat.UNKNOWN
        val barcodeFormatEAN13 =
            if (preselectedItems.contains("UPC/EAN")) BarcodeFormat.EAN_13 else BarcodeFormat.UNKNOWN
        val barcodeFormatUPCA =
            if (preselectedItems.contains("UPC/EAN")) BarcodeFormat.UPC_A else BarcodeFormat.UNKNOWN
        val barcodeFormatUPCE =
            if (preselectedItems.contains("UPC/EAN")) BarcodeFormat.UPC_E else BarcodeFormat.UNKNOWN
        val barcodeFormatGS1_Databar =
            if (preselectedItems.contains("GS1 Databar & Composite Codes")) BarcodeFormat.RSS_14 else BarcodeFormat.UNKNOWN
        val barcodeFormatComposite =
            if (preselectedItems.contains("GS1 Databar & Composite Codes")) BarcodeFormat.RSS_EXPANDED else BarcodeFormat.UNKNOWN
        val barcodeFormatCode128 =
            if (preselectedItems.contains("Code 128")) BarcodeFormat.CODE_128 else BarcodeFormat.UNKNOWN
        val barcodeFormatGS1_128 =
            if (preselectedItems.contains("GS1-128")) BarcodeFormat.GS1_128 else BarcodeFormat.UNKNOWN
        val barcodeFormatISTB_128 =
            if (preselectedItems.contains("ISBT 128")) BarcodeFormat.ISBT_128 else BarcodeFormat.UNKNOWN
        val barcodeFormatCode39 =
            if (preselectedItems.contains("Code 39")) BarcodeFormat.CODE_39 else BarcodeFormat.UNKNOWN
        val barcodeFormatTRIOPTIC =
            if (preselectedItems.contains("Trioptic Code 39")) BarcodeFormat.TRIOPTIC else BarcodeFormat.UNKNOWN
        val barcodeFormatCode32 =
            if (preselectedItems.contains("Code 32")) BarcodeFormat.CODE_32 else BarcodeFormat.UNKNOWN
        val barcodeFormatCode93 =
            if (preselectedItems.contains("Code 93")) BarcodeFormat.CODE_93 else BarcodeFormat.UNKNOWN
        val barcodeFormatITF =
            if (preselectedItems.contains("Interleaved 2 of 5")) BarcodeFormat.ITF else BarcodeFormat.UNKNOWN
        val barcodeFormatMatrix =
            if (preselectedItems.contains("Matrix 2 of 5")) BarcodeFormat.MATRIX_2_5 else BarcodeFormat.UNKNOWN
        val barcodeFormatDiscrete =
            if (preselectedItems.contains("Code 25")) BarcodeFormat.DISCRETE_2_5 else BarcodeFormat.UNKNOWN
        val barcodeFormatCodabar =
            if (preselectedItems.contains("Codabar")) BarcodeFormat.CODABAR else BarcodeFormat.UNKNOWN
        val barcodeFormatMSI =
            if (preselectedItems.contains("MSI")) BarcodeFormat.MSI else BarcodeFormat.UNKNOWN
        val barcodeFormatCode11 =
            if (preselectedItems.contains("Code 11")) BarcodeFormat.CODE_11 else BarcodeFormat.UNKNOWN
        val barcodeFormatUSPostnet =
            if (preselectedItems.contains("US Postnet")) BarcodeFormat.US_POSTNET else BarcodeFormat.UNKNOWN
        val barcodeFormatUSPlanet =
            if (preselectedItems.contains("US Planet")) BarcodeFormat.US_PLANET else BarcodeFormat.UNKNOWN
        val barcodeFormatUKPostal =
            if (preselectedItems.contains("UK Postal")) BarcodeFormat.POST_UK else BarcodeFormat.UNKNOWN
        val barcodeFormatUSPS =
            if (preselectedItems.contains("USPS 4CB / OneCode / Intelligent Mail")) BarcodeFormat.USPS_4CB else BarcodeFormat.UNKNOWN
        val barcodeFormatPDF =
            if (preselectedItems.contains("PDF417")) BarcodeFormat.PDF_417 else BarcodeFormat.UNKNOWN
        val barcodeFormatMicroPDF417 =
            if (preselectedItems.contains("MicroPDF417")) BarcodeFormat.MICRO_PDF else BarcodeFormat.UNKNOWN
        val barcodeFormatDataMatrix =
            if (preselectedItems.contains("Data Matrix")) BarcodeFormat.DATA_MATRIX else BarcodeFormat.UNKNOWN
        val barcodeFormatQR =
            if (preselectedItems.contains("QR Code")) BarcodeFormat.QR_CODE else BarcodeFormat.UNKNOWN
        val barcodeFormatMicroQR =
            if (preselectedItems.contains("MicroQR")) BarcodeFormat.MICRO_QR else BarcodeFormat.UNKNOWN
        val barcodeFormatGS1 =
            if (preselectedItems.contains("GS1 QR Code")) BarcodeFormat.GS1_QR_CODE else BarcodeFormat.UNKNOWN
        val barcodeFormatAZTEC =
            if (preselectedItems.contains("Aztec")) BarcodeFormat.AZTEC else BarcodeFormat.UNKNOWN
        val barcodeFormatMaxiCode =
            if (preselectedItems.contains("MaxiCode")) BarcodeFormat.MAXICODE else BarcodeFormat.UNKNOWN
        val barcodeFormatOneDInversed =
            if (preselectedItems.contains("One D Inverse")) BarcodeFormat.ONE_D_INVERSE else BarcodeFormat.UNKNOWN
        scanPlugin.setBarcodeFormats(
            barcodeFormatDiscrete,
            barcodeFormatAZTEC,
            barcodeFormatPDF,
            barcodeFormatCodabar,
            barcodeFormatCode39,
            barcodeFormatCode93,
            barcodeFormatCode128,
            barcodeFormatDataMatrix,
            barcodeFormatEAN8,
            barcodeFormatEAN13,
            barcodeFormatQR,
            barcodeFormatUPCA,
            barcodeFormatUPCE,
            barcodeFormatGS1_128,
            barcodeFormatISTB_128,
            barcodeFormatTRIOPTIC,
            barcodeFormatCode32,
            barcodeFormatITF,
            barcodeFormatMatrix,
            barcodeFormatMSI,
            barcodeFormatCode11,
            barcodeFormatUSPostnet,
            barcodeFormatUSPlanet,
            barcodeFormatUKPostal,
            barcodeFormatUSPS,
            barcodeFormatMicroPDF417,
            barcodeFormatMicroQR,
            barcodeFormatGS1,
            barcodeFormatGS1_Databar,
            barcodeFormatComposite,
            barcodeFormatMaxiCode,
            barcodeFormatOneDInversed
        )
    }

    protected fun getBarcodeResult(result: List<Barcode>): HashMap<String, String> {
        val barcodeResult = LinkedHashMap<String, String>()
        for (i in result.indices) {
            barcodeResult["HEADER" + (i + 1)] =
                getString(R.string.category_barcodes) + " " + (i + 1)
            val barcode = result[i]
            barcodeResult[getString(R.string.barcode_result) + i] =
                barcode.value.ifEmpty { resources.getString(R.string.not_available) }
            barcodeResult[getString(R.string.barcode_format) + i] =
                barcode.barcodeFormat.toString()
            barcodeResult[getString(R.string.barcode_result_pdf417) + i] =
                if (barcode.parsedPDF417 == null || barcode.parsedPDF417.body == null) resources.getString(
                    R.string.not_available
                ) else result[i].parsedPDF417.body
        }
        return barcodeResult
    }
}