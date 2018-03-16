/*
 * Anyline
 * ScanMrzActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.drivinglicense;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.mrz.Identification;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrResultListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;

/**
 * Example Activity for the Anyline-DrivingLicense-Module.
 */
public class ScanDrivingLicenseActivity extends ScanActivity implements CameraOpenListener {

    private static final String TAG = ScanDrivingLicenseActivity.class.getSimpleName();
    private AnylineOcrScanView anylineOcrScanView;
    private DrivingLicenseResultView drivingLicenseResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_scan_driver_license, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        anylineOcrScanView = (AnylineOcrScanView) findViewById(R.id.ocr_view);
        drivingLicenseResultView = (DrivingLicenseResultView) findViewById(R.id.driver_license_result);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        anylineOcrScanView.setCameraOpenListener(this);
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        anylineOcrScanView.setConfigFromAsset("driving_license_view_config.json");

        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setCustomCmdFile("anyline_austrian_driver_license.ale");
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");
        anylineOcrScanView.setAnylineOcrConfig(anylineOcrConfig);

        // initialize Anyline with the license key and a Listener that is called if a result is found
        anylineOcrScanView.initAnyline(getString(R.string.anyline_license_key), new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult result) {
                // This is called when a result is found.
                // The Identification includes all the data read from the driving license
                // as scanned and the given image shows the scanned driving license
                String resultString = result.getResult();
                String[] results = resultString.split("\\|");

                String[] birthdateDocNumber = results[1].split(" ");

                Identification identification = new Identification();
                drivingLicenseResultView.setDocumentNumber(birthdateDocNumber[1]);
                drivingLicenseResultView.setDayOfBirth(birthdateDocNumber[0]);
                drivingLicenseResultView.setName(results[0]);
                drivingLicenseResultView.setVisibility(View.VISIBLE);


                setupScanProcessView(ScanDrivingLicenseActivity.this, result, getScanModule());
            }
        });

        drivingLicenseResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drivingLicenseResultView.setVisibility(View.INVISIBLE);
                resetTime();
                if (!anylineOcrScanView.isRunning()) {
                    anylineOcrScanView.startScanning();
                }
            }
        });

    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return anylineOcrScanView;
    }

    @Override
    public Rect getCutoutRect() {
        return anylineOcrScanView.getCutoutRect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        drivingLicenseResultView.setVisibility(View.INVISIBLE);

        //start the actual scanning
        anylineOcrScanView.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        anylineOcrScanView.cancelScanning();
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        anylineOcrScanView.releaseCameraInBackground();
    }

    @Override
    public void onBackPressed() {
        //close the result view on back press if it is open
        if (drivingLicenseResultView.getVisibility() == View.VISIBLE) {
            drivingLicenseResultView.setVisibility(View.INVISIBLE);
            if (!anylineOcrScanView.isRunning()) {
                anylineOcrScanView.startScanning();
            }
        } else {
            super.onBackPressed();
        }
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

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.DRIVER_LICENSE;
    }
}
