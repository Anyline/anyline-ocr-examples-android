/*
 * Anyline
 * ScanMrzActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.mrz;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.mrz.Identification;
import at.nineyards.anyline.modules.mrz.MrzResult;
import at.nineyards.anyline.modules.mrz.MrzResultListener;
import at.nineyards.anyline.modules.mrz.MrzScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;

/**
 * Example Activity for the Anyline-MRZ-Module.
 */
public class ScanMrzActivity extends ScanActivity implements CameraOpenListener {

    private static final String TAG = ScanMrzActivity.class.getSimpleName();
    private MrzScanView mrzScanView;
    private MrzResultView mrzResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_scan_mrz, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        mrzScanView = (MrzScanView) findViewById(R.id.mrz_view);
        mrzResultView = (MrzResultView) findViewById(R.id.mrz_result);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        mrzScanView.setCameraOpenListener(this);
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        mrzScanView.setConfigFromAsset("mrz_view_config.json");

        // initialize Anyline with the license key and a Listener that is called if a result is found
        mrzScanView.initAnyline(getString(R.string.anyline_license_key), new MrzResultListener() {

            @Override
            public void onResult(MrzResult mrzResult) {
                // This is called when a result is found.
                // The Identification includes all the data read from the MRZ
                // as scanned and the given image shows the scanned ID/Passport

                Identification identification = mrzResult.getResult();

                mrzResultView.setIdentification(identification);
                mrzResultView.setVisibility(View.VISIBLE);
                
                setupScanProcessView(ScanMrzActivity.this, mrzResult, getScanModule());
            }
        });

        mrzResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mrzResultView.setVisibility(View.INVISIBLE);
                resetTime();
                if (!mrzScanView.isRunning()) {
                    mrzScanView.startScanning();
                }
            }
        });

    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return mrzScanView;
    }

    @Override
    public Rect getCutoutRect() {
        return mrzScanView.getCutoutRect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mrzResultView.setVisibility(View.INVISIBLE);

        //start the actual scanning
        mrzScanView.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        mrzScanView.cancelScanning();
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        mrzScanView.releaseCameraInBackground();
    }

    @Override
    public void onBackPressed() {
        //close the result view on back press if it is open
        if (mrzResultView.getVisibility() == View.VISIBLE) {
            mrzResultView.setVisibility(View.INVISIBLE);
            if (!mrzScanView.isRunning()) {
                mrzScanView.startScanning();
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
        return ScanModuleEnum.ScanModule.MRZ;
    }

}
