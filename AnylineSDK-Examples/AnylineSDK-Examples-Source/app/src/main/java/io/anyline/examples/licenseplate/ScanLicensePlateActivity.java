/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-10-27
 */
package io.anyline.examples.licenseplate;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.licenseplate.LicensePlateScanResult;
import io.anyline.plugin.licenseplate.LicensePlateScanViewPlugin;
import io.anyline.view.AnylineViewConfig;
import io.anyline.view.BaseScanViewConfig;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;


public class ScanLicensePlateActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanLicensePlateActivity.class.getSimpleName();
    private ScanView scanView;

    @Override
    protected ScanView getScanView() {
        return null;
    }
@Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.LICENSE_PLATE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_zoom_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        init();

    }

    void init() {
        scanView = (ScanView) findViewById(R.id.scan_view);
        scanView.setScanConfig("license_plate_view_config.json");


        //init the scan view
        final LicensePlateScanViewPlugin scanViewPlugin = new LicensePlateScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), scanView.getScanViewPluginConfig(), "OCR");

        scanView.getCameraView().getCameraController().setZoomGestureEnabled(true);

        scanViewPlugin.addScanResultListener(new ScanResultListener<LicensePlateScanResult>() {
            @Override
            public void onResult(LicensePlateScanResult result) {

                setFeedbackViewActive(false);

                String path = setupImagePath(result.getCutoutImage());

                startScanResultIntent(getResources().getString(R.string.title_license_plate), getLicensePlateResult(result), path);

                setupScanProcessView(ScanLicensePlateActivity.this, result, getScanModule());
            }

        });
        scanViewPlugin.setDebugListener(this);
        scanView.setScanViewPlugin(scanViewPlugin);


    }


    @Override
    protected void onResume() {
        super.onResume();
        scanView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanView.stop();
        scanView.releaseCameraInBackground();
    }

    public HashMap<String, String> getLicensePlateResult(LicensePlateScanResult licensePlateResult) {

        HashMap<String, String> licensePlateResultData = new HashMap<>();

        licensePlateResultData.put(getResources().getString(R.string.license_plate_country), (licensePlateResult.getCountry() == null || licensePlateResult.getCountry().isEmpty()) ? getResources().getString(R.string.not_available) : licensePlateResult.getCountry());
        licensePlateResultData.put(getResources().getString(R.string.license_plate_result), (licensePlateResult.getResult() == null || licensePlateResult.getResult().isEmpty()) ? getResources().getString(R.string.not_available) : licensePlateResult.getResult());

        return licensePlateResultData;
    }

    @Override
    public void onDebug(String s, Object o) {

    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }

}