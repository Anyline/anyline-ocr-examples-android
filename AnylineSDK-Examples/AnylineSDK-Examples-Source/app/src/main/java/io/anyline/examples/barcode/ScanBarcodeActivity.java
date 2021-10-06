/*
 * Anyline
 * ScanBarcodeActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.barcode;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.anyline.camera.CameraController;
import io.anyline.camera.CameraOpenListener;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.barcode.Barcode;
import io.anyline.plugin.barcode.BarcodeFormat;
import io.anyline.plugin.barcode.BarcodeScanPlugin;
import io.anyline.plugin.barcode.BarcodeScanViewPlugin;
import io.anyline.view.CutoutConfig;
import io.anyline.view.FlashView;
import io.anyline.view.ScanView;

//import io.anyline.examples.baseactivities.BarcodeListView;

/**
 * Example activity for the Anyline-Barcode-Module
 */
public class ScanBarcodeActivity extends ScanActivity implements CameraOpenListener {

    private static final String TAG = ScanBarcodeActivity.class.getSimpleName();
    private ScanView barcodeScanView;
    private BarcodePrefferences barcodePrefferences;
    private TextView resultText;
    private ArrayList<String> preselectedItems;
    private ArrayList<String> defaultItems;
    private LinearLayout barcodeContiner;
    private Switch barcodeSwitch;
    private BarcodeScanViewPlugin scanViewPlugin;
    private Button scanButton;
    MutableLiveData<Boolean> listen = new MutableLiveData<>();

    BarcodeScanPlugin scanPlugin = null;
    private boolean resultScreenShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_barcode_scan_view,
                (ViewGroup) findViewById(R.id.scan_view_placeholder));

        barcodePrefferences = BarcodePrefferences.getInstance(this);
        preselectedItems = barcodePrefferences.getArrayString();
        resultText = (TextView) findViewById(R.id.text_result);
        barcodeContiner = findViewById(R.id.barcode_switch_container);
        barcodeSwitch = (Switch) findViewById(R.id.barcode_scanner_switch);
        barcodeScanView = (ScanView) findViewById(R.id.scan_view);
        scanButton = findViewById(R.id.stop_scanning_button);
        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        barcodeScanView.setCameraOpenListener(this);
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)

        //	barcodeContiner.setVisibility(View.VISIBLE);

        barcodeScanView.setScanConfig("barcode_view_config.json");

        scanPlugin = new BarcodeScanPlugin(getApplicationContext(), "barcode");
        scanViewPlugin = new BarcodeScanViewPlugin(getApplicationContext(), scanPlugin, barcodeScanView.getScanViewPluginConfig());
        scanPlugin.setCancelOnResult(false);
        if (preselectedItems.size() == 0) {
            barcodePrefferences.setDefault();
            preselectedItems = barcodePrefferences.getArrayString();
        }
        scanViewPlugin.setMultiBarcode(false);
        setBarcodeTypes(preselectedItems);

        FlashView flashView = barcodeScanView.getFlashView();
        ((ViewGroup) flashView.getParent()).removeView(flashView);

        //build a linear layout for making it possible to arrange the flash as we want to
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout_linear);
        mainLayout.setVisibility(View.VISIBLE);
        //set all parameters for the flashview
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.weight = 1.0f;
        params.rightMargin = 100;
        params.topMargin = 50;
        flashView.setLayoutParams(params);

        //add the flash view to the main layout of the buttons
        mainLayout.addView(flashView);

        barcodeScanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScannedBarcodesListener(scanResult -> {
            listen.postValue(true);

            String path = setupImagePath(scanResult.getCutoutImage());
            if (scanViewPlugin != null && scanViewPlugin.isMultiBarcodeEnabled()) {
                scanButton.setVisibility(View.VISIBLE);
                scanButton.setOnClickListener(view -> {
                    //setup the scan process
                    barcodeScanView.stop();
                    startScanResultIntent(getResources().getString(R.string.category_barcodes), getBarcodeResult(scanResult.getResult()), path);
                    setupScanProcessView(ScanBarcodeActivity.this, scanResult, getScanModule());
                    finish();
                });
            } else {
                if (scanResult.getResult().size() != 1) return;
                if (!resultScreenShown) {
                    barcodeScanView.stop();
                    resultScreenShown = true;
                    startScanResultIntent(getResources().getString(R.string.category_barcodes), getBarcodeResult(scanResult.getResult()), path);
                    setupScanProcessView(ScanBarcodeActivity.this, scanResult, getScanModule());
                    finish();
                }
            }
        });

        //create a count down for visibility of the scan button
        CountDownTimer countDown = new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                scanButton.setVisibility(View.INVISIBLE);
            }

        }.start();

        listen.observe(this, changedValue -> {
            if (changedValue) {
                scanButton.setVisibility(View.VISIBLE);
            } else {
                countDown.start();
            }
        });

        barcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                barcodeScanView.stop();
                scanViewPlugin.setMultiBarcode(isChecked);
                barcodeScanView.start();
            }
        });


    }

    @Override
    protected ScanView getScanView() {
        return null;
    }


    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.BARCODE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CutoutConfig config = scanViewPlugin.getScanViewPluginConfig().getCutoutConfig();

        barcodeScanView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                barcodeScanView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float ratio = (float) barcodeScanView.getWidth() / barcodeScanView.getHeight();

                config.setWidth(barcodeScanView.getWidth());
                config.setRatio(ratio);
                barcodeScanView.updateCutoutView();
            }
        });

        resultText.setText("");
        //start the actual scanning
        barcodeScanView.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            barcodeScanView.stop();
            preselectedItems = barcodePrefferences.getArrayString();

            if (preselectedItems.size() > 0 && !preselectedItems.contains("ALL")) {
                setBarcodeTypes(preselectedItems);
            }
            barcodeScanView.start();
        }
    }

    private void setBarcodeTypes(ArrayList<String> preselectedItems) {
        BarcodeFormat barcodeFormatEAN8 = preselectedItems.contains("UPC/EAN") ? BarcodeFormat.EAN_8 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatEAN13 = preselectedItems.contains("UPC/EAN") ? BarcodeFormat.EAN_13 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatUPCA = preselectedItems.contains("UPC/EAN") ? BarcodeFormat.UPC_A : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatUPCE = preselectedItems.contains("UPC/EAN") ? BarcodeFormat.UPC_E : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatGS1_Databar = preselectedItems.contains("GS1 Databar & Composite Codes") ? BarcodeFormat.RSS_14 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatComposite = preselectedItems.contains("GS1 Databar & Composite Codes") ? BarcodeFormat.RSS_EXPANDED : BarcodeFormat.UNKNOWN;

        BarcodeFormat barcodeFormatCode128 = preselectedItems.contains("Code 128") ? BarcodeFormat.CODE_128 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatGS1_128 = preselectedItems.contains("GS1-128") ? BarcodeFormat.GS1_128 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatISTB_128 = preselectedItems.contains("ISBT 128") ? BarcodeFormat.ISBT_128 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatCode39 = preselectedItems.contains("Code 39") ? BarcodeFormat.CODE_39 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatTRIOPTIC = preselectedItems.contains("Trioptic Code 39") ? BarcodeFormat.TRIOPTIC : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatCode32 = preselectedItems.contains("Code 32") ? BarcodeFormat.CODE_32 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatCode93 = preselectedItems.contains("Code 93") ? BarcodeFormat.CODE_93 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatITF = preselectedItems.contains("Interleaved 2 of 5") ? BarcodeFormat.ITF : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatMatrix = preselectedItems.contains("Matrix 2 of 5") ? BarcodeFormat.MATRIX_2_5 : BarcodeFormat.UNKNOWN;

        BarcodeFormat barcodeFormatDiscrete = preselectedItems.contains("Code 25") ? BarcodeFormat.DISCRETE_2_5 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatCodabar = preselectedItems.contains("Codabar") ? BarcodeFormat.CODABAR : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatMSI = preselectedItems.contains("MSI") ? BarcodeFormat.MSI : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatCode11 = preselectedItems.contains("Code 11") ? BarcodeFormat.CODE_11 : BarcodeFormat.UNKNOWN;

        BarcodeFormat barcodeFormatUSPostnet = preselectedItems.contains("US Postnet") ? BarcodeFormat.US_POSTNET : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatUSPlanet = preselectedItems.contains("US Planet") ? BarcodeFormat.US_PLANET : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatUKPostal = preselectedItems.contains("UK Postal") ? BarcodeFormat.POST_UK : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatUSPS = preselectedItems.contains("USPS 4CB / OneCode / Intelligent Mail") ? BarcodeFormat.USPS_4CB : BarcodeFormat.UNKNOWN;

        BarcodeFormat barcodeFormatPDF = preselectedItems.contains("PDF417") ? BarcodeFormat.PDF_417 : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatMicroPDF417 = preselectedItems.contains("MicroPDF417") ? BarcodeFormat.MICRO_PDF : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatDataMatrix = preselectedItems.contains("Data Matrix") ? BarcodeFormat.DATA_MATRIX : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatQR = preselectedItems.contains("QR Code") ? BarcodeFormat.QR_CODE : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatMicroQR = preselectedItems.contains("MicroQR") ? BarcodeFormat.MICRO_QR : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatGS1 = preselectedItems.contains("GS1 QR Code") ? BarcodeFormat.GS1_QR_CODE : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatAZTEC = preselectedItems.contains("Aztec") ? BarcodeFormat.AZTEC : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatMaxiCode = preselectedItems.contains("MaxiCode") ? BarcodeFormat.MAXICODE : BarcodeFormat.UNKNOWN;
        BarcodeFormat barcodeFormatOneDInversed = preselectedItems.contains("One D Inverse") ? BarcodeFormat.ONE_D_INVERSE : BarcodeFormat.UNKNOWN;

        scanPlugin.setBarcodeFormats(barcodeFormatDiscrete, barcodeFormatAZTEC, barcodeFormatPDF, barcodeFormatCodabar, barcodeFormatCode39, barcodeFormatCode93, barcodeFormatCode128, barcodeFormatDataMatrix, barcodeFormatEAN8, barcodeFormatEAN13, barcodeFormatQR, barcodeFormatUPCA, barcodeFormatUPCE, barcodeFormatGS1_128, barcodeFormatISTB_128, barcodeFormatTRIOPTIC, barcodeFormatCode32, barcodeFormatITF, barcodeFormatMatrix, barcodeFormatMSI, barcodeFormatCode11,
                barcodeFormatUSPostnet, barcodeFormatUSPlanet, barcodeFormatUKPostal, barcodeFormatUSPS, barcodeFormatMicroPDF417, barcodeFormatMicroQR, barcodeFormatGS1, barcodeFormatGS1_Databar, barcodeFormatComposite, barcodeFormatMaxiCode,
                barcodeFormatOneDInversed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        barcodeScanView.stop();
        //scan button should not be present when the device is on pause
        scanButton.setVisibility(View.INVISIBLE);

        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        barcodeScanView.releaseCameraInBackground();
    }

    @Override
    public void onCameraOpened(CameraController cameraController, int width, int height) {
        //the camera is opened async and this is called when the opening is finished
    }

    @Override
    public void onCameraError(Exception e) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw new RuntimeException(e);
    }

    protected HashMap<String, String> getBarcodeResult(List<Barcode> result) {
        LinkedHashMap<String, String> barcodeResult = new LinkedHashMap<>();

        for (int i = 0; i < result.size(); i++) {
            barcodeResult.put("HEADER" + (i + 1), getString(R.string.category_barcodes) + " " + (i + 1));

            barcodeResult.put(getString(R.string.barcode_result) + i, (result.get(i).getValue() == null || result.get(i).getValue().isEmpty()) ? getResources().getString(R.string.not_available) : result.get(i).getValue());
            barcodeResult.put(getString(R.string.barcode_result_base64) + i, (result.get(i).getBase64() == null || result.get(i).getBase64().isEmpty()) ? getResources().getString(R.string.not_available) : result.get(i).getBase64());
            barcodeResult.put(getString(R.string.barcode_format) + i, (result.get(i).getBarcodeFormat() == null) ? getResources().getString(R.string.not_available) : result.get(i).getBarcodeFormat().toString());

        }
        return barcodeResult;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem edit_item = menu.add(0, 0, 0, "");
        edit_item.setIcon(R.drawable.ic_settings);
        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == 0) {
            Intent intent = new Intent(ScanBarcodeActivity.this, BarcodeListViewActivity.class);
            startActivityForResult(intent, 2);// Activity is started with requestCode 2
        }
        return super.onOptionsItemSelected(item);
    }
}
