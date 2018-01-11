/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-12-03
 */
package io.anyline.examples.meter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.energy.EnergyResult;
import at.nineyards.anyline.modules.energy.EnergyResultListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ResultDialogBuilder;
import io.anyline.examples.SettingsFragment;

/**
 * Example activity for the Anyline-Energy-Module
 * <p>Note: Heat Meter scanning is in ALPHA stage, API and everything may change!</p>
 */
public class ScanDigitalHeatMeterActivity extends AppCompatActivity implements CameraOpenListener {

    private static final String TAG = ScanDigitalHeatMeterActivity.class.getSimpleName();
    private EnergyScanView energyScanView;
    private Toast toast;
    private AlertDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_heat_meter);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get the view from the layout (check out the xml for the configuration of the view)
        energyScanView = (EnergyScanView) findViewById(R.id.scan_view);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        energyScanView.setCameraOpenListener(this);

        // disable the reporting if set to off in preferences
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                SettingsFragment.KEY_PREF_REPORTING_ON, true)) {
            //The reporting of Analog Meter Results - including the photo of a scanned meter -
            // helps us in improving our product, and the customer experience.
            // However, if you wish to turn off this reporting feature, you can do it like this:
            energyScanView.setReportingEnabled(false);
        }
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

                //display the result in a simple dialog

                resultDialog = new ResultDialogBuilder(ScanDigitalHeatMeterActivity.this)
                        .setResultImage(energyResult.getCutoutImage())
                        .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32)
                        .setTextGravity(Gravity.CENTER)
                        .setText(energyResult.getResult())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!energyScanView.isRunning()) {
                                    energyScanView.startScanning();
                                }
                            }
                        })
                        .setTitle(R.string.heat_meter)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                if (!energyScanView.isRunning()) {
                                    energyScanView.startScanning();
                                }
                            }
                        }).create();
                resultDialog.show();
            }
        });

        //set the scan mode to start with
        energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_4);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        // switch the scan mode depending on user selection
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.radio_heat_4_3 == checkedId) {
                    showToast(getString(R.string.header_config, getString(R.string.heat_meter_4_3_short)));
                    energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_4);
                    if (!energyScanView.isRunning()) {
                        energyScanView.startScanning();
                    }

                } else if (R.id.radio_heat_5_3 == checkedId) {
                    showToast(getString(R.string.header_config, getString(R.string.heat_meter_5_3_short)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_5);
                    if (!energyScanView.isRunning()) {
                        energyScanView.startScanning();
                    }
                } else if (R.id.radio_heat_6_3 == checkedId) {
                    showToast(getString(R.string.header_config, getString(R.string.heat_meter_6_3_short)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_6);
                    if (!energyScanView.isRunning()) {
                        energyScanView.startScanning();
                    }
                }
            }
        });
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
    public void onCameraOpened(CameraController cameraController, int width, int height) {
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


    protected void showToast(String string) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
