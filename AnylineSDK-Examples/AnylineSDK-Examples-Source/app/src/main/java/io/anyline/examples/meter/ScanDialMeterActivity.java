/*
 * Anyline
 * ScanDialMeter.java
 *
 * Copyright (c) 2017 Anyline GmbH
 *
 * Created by clemens at 2017-06-29
 */

package io.anyline.examples.meter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.barcode.NativeBarcodeResultListener;
import at.nineyards.anyline.modules.energy.EnergyResult;
import at.nineyards.anyline.modules.energy.EnergyResultListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ResultDialogBuilder;
import io.anyline.examples.SettingsFragment;

/**
 * Example activity for the Anyline-Energy-Module
 */
public class ScanDialMeterActivity extends AppCompatActivity implements CameraOpenListener {

    private static final String TAG = ScanDialMeterActivity.class.getSimpleName();
    protected EnergyScanView energyScanView;
    private String lastDetectedBarcodeValue = "";
    private AlertDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_dial_meter);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get the view from the layout (check out the xml for the configuration of the view)
        energyScanView = (EnergyScanView) findViewById(R.id.energy_scan_view);

        Switch barcodeDetectionSwitch = (Switch) findViewById(R.id.barcode_scanner_switch);

        // set a listener on the switch to enable and disable barcode detection
        barcodeDetectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                lastDetectedBarcodeValue = "";
                if (isChecked) {
                    Log.d(TAG, "barcode detection enabled");

                    // enables the barcode detection for the full image (the preview view, rather than the cutout only)
                    energyScanView.enableBarcodeDetection(true, new NativeBarcodeResultListener() {
                        @Override
                        public void onBarcodesReceived(SparseArray<com.google.android.gms.vision.barcode.Barcode> sparseArray) {

                            // For this demonstration purpose, we only use the latest barcode that has been found.
                            // However, note that you receive a list of barcodes, e.g. it detects multiple barcodes at once.
                            // Also the listener is called every time barcodes are found on a frame,
                            // so it is independent from the energy result
                            if (sparseArray.size() > 0) {
                                lastDetectedBarcodeValue = sparseArray.valueAt(0).displayValue;
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "barcode detection disabled");
                    energyScanView.disableBarcodeDetection();
                }
            }
        });

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        energyScanView.setCameraOpenListener(this);

        // set reporting according to prefs or true on default
        energyScanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));
        // initialize Anyline with the license key and a Listener that is called if a result is found
        energyScanView.initAnyline(getString(R.string.anyline_license_key), new EnergyResultListener() {
            @Override
            public void onResult(EnergyResult energyResult) {
                // This is called when a result is found.
                // The scanMode is the mode the result was found for. The result is the actual result.
                // If the a meter reading was scanned two images are provided as well, one shows the targeted area only
                // the other shows the full image. (Images are null in barcode mode)
                // The result for meter readings is a String with leading zeros (if any) and no decimals.

                resultDialog =  new ResultDialogBuilder(ScanDialMeterActivity.this)
                        .setResultImage(energyResult.getCutoutImage())
                        .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22)
                        .setTextGravity(Gravity.CENTER)
                        .setText(getFormattedResult(energyResult.getResult(), lastDetectedBarcodeValue))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // reset the last detected barcode value, as it has already been displayed
                                lastDetectedBarcodeValue = "";
                                if (!energyScanView.isRunning()) {
                                    energyScanView.startScanning();
                                }
                            }
                        })
                        .setTitle(getString(R.string.title_dial_meter))
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                // reset the last detected barcode value, as it has already been displayed
                                lastDetectedBarcodeValue = "";
                                if (!energyScanView.isRunning()) {
                                    energyScanView.startScanning();
                                }
                            }
                        }).create();
                resultDialog.show();
            }

        });

        // ANALOG_METER will work for all types of analog meters (gas, electric, water) and
        // automatically detects digits before and after the point
        energyScanView.setScanMode(EnergyScanView.ScanMode.DIAL_METER);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(resultDialog == null || !resultDialog.isShowing()) {
            //start the actual scanning
            energyScanView.startScanning();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        energyScanView.cancelScanning();
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        energyScanView.releaseCameraInBackground();
    }

    @Override
    public void onCameraOpened(final CameraController cameraController, int width, int height) {
        //the camera is opened async and this is called when the opening is finished
        Log.d(TAG, "Camera opened successfully. Frame resolution " + width + " x " + height);
    }

    @Override
    public void onCameraError(Exception e) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw new RuntimeException(e);
    }

    /**
     * Format a meter reading to look a bit like a meter.
     *
     * @param result the meter reading
     * @return the formatted string
     */
    private Spanned getFormattedResult(String result, String barcodeValue) {

        SpannableStringBuilder sb = new SpannableStringBuilder();

        for (int i = 0, n = result.length(); i < n; i++) {
            char text = result.charAt(i);
            sb.append(" ");
            sb.append(text);
            sb.append(" ");
            sb.setSpan(new BackgroundColorSpan(Color.BLACK), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Color.WHITE), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(" ");
        }

        if (barcodeValue != null && !barcodeValue.isEmpty()) {
            sb.append("\n\nBarcode: ");
            sb.append(barcodeValue);
        }

        return sb;
    }


}
