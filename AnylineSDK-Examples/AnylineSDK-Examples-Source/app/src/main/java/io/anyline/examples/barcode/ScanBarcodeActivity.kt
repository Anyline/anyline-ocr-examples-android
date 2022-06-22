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
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import io.anyline.camera.CameraController
import io.anyline.camera.CameraOpenListener
import io.anyline.examples.R
import io.anyline.examples.ScanActivity
import io.anyline.examples.ScanModuleEnum.ScanModule
import io.anyline.examples.databinding.ActivityAnylineBarcodeScanViewBinding
import io.anyline.plugin.barcode.*
import io.anyline.view.ScanView


/**
 * Example activity for the Anyline-Barcode-Module
 */
class ScanBarcodeActivity : ScanActivity(), CameraOpenListener {

    private lateinit var binding: ActivityAnylineBarcodeScanViewBinding

    private lateinit var scanView: ScanView
    private lateinit var scanViewPlugin: BarcodeScanViewPlugin
    private lateinit var scanPlugin: BarcodeScanPlugin

    private lateinit var barcodePreferences: BarcodePreferences
    private lateinit var resultText: TextView
    private lateinit var preselectedItems: ArrayList<String>
    private lateinit var scanButton: Button
    private var resultScreenShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnylineBarcodeScanViewBinding.inflate(
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
        scanView.setScanConfig("barcode_view_config.json")

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
        scanViewPlugin.setMultiBarcode(false)

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

        scanViewPlugin.addScannedBarcodesListener { scanResult: BarcodeScanResult ->
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
                    setupScanProcessView(this@ScanBarcodeActivity, scanResult, scanModule)
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
                    setupScanProcessView(this@ScanBarcodeActivity, scanResult, scanModule)
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
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
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
            val intent = Intent(this@ScanBarcodeActivity, BarcodeListViewActivity::class.java)
            startActivityForResult(intent, 2) // Activity is started with requestCode 2
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
            barcodeResult[getString(R.string.barcode_result_base64) + i] =
                if (barcode.base64 == null || barcode.base64.isEmpty()) resources.getString(R.string.not_available) else barcode.base64
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