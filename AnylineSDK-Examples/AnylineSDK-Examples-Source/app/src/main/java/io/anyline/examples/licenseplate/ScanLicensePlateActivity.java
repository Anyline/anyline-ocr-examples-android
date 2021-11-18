/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-10-27
 */
package io.anyline.examples.licenseplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

import at.nineyards.anyline.core.RunFailure;
import io.anyline.AnylineDebugListener;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.licenseplate.LicensePlateScanMode;
import io.anyline.plugin.licenseplate.LicensePlateScanPlugin;
import io.anyline.plugin.licenseplate.LicensePlateScanResult;
import io.anyline.plugin.licenseplate.LicensePlateScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanLicensePlateActivity extends ScanActivity implements AnylineDebugListener {

    private static final String EXTRA_REGION = "EXTRA_REGION";

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
        final LicensePlateScanViewPlugin scanViewPlugin = new LicensePlateScanViewPlugin(getApplicationContext(), scanView.getScanViewPluginConfig(), "OCR");
        ((LicensePlateScanPlugin) scanViewPlugin.getScanPlugin()).setScanMode(getLicensePlateScanModeFromIntent());

        scanView.getCameraView().getCameraController().setZoomGestureEnabled(true);

        scanViewPlugin.addScanResultListener((ScanResultListener<LicensePlateScanResult>) result -> {
            setFeedbackViewActive(false);
            String path = setupImagePath(result.getCutoutImage());
            startScanResultIntent(getResources().getString(R.string.title_license_plate), getLicensePlateResult(result), path);
            setupScanProcessView(ScanLicensePlateActivity.this, result, getScanModule());
        });
        scanViewPlugin.setDebugListener(this);
        scanView.setScanViewPlugin(scanViewPlugin);


    }

    private LicensePlateScanMode getLicensePlateScanModeFromIntent() {
        Region region = (Region) getIntent().getSerializableExtra(EXTRA_REGION);

        switch (region) {
            case US:
                return LicensePlateScanMode.UnitedStates;
            case Africa:
                return LicensePlateScanMode.Africa;
            default:
                return LicensePlateScanMode.Auto;
        }
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

        if (getLicensePlateScanModeFromIntent() != LicensePlateScanMode.Africa) {
            licensePlateResultData.put(getResources().getString(R.string.license_plate_country), (licensePlateResult.getCountry() == null || licensePlateResult.getCountry().isEmpty()) ? getResources().getString(R.string.not_available) : licensePlateResult.getCountry());
        }
        if (getLicensePlateScanModeFromIntent() == LicensePlateScanMode.UnitedStates) {
            licensePlateResultData.put(getResources().getString(R.string.license_plate_state), (licensePlateResult.getArea() == null || licensePlateResult.getArea().isEmpty()) ? getResources().getString(R.string.not_available) : licensePlateResult.getArea());
        }
        licensePlateResultData.put(getResources().getString(R.string.license_plate_result), (licensePlateResult.getResult() == null || licensePlateResult.getResult().isEmpty()) ? getResources().getString(R.string.not_available) : licensePlateResult.getResult());

        return licensePlateResultData;
    }

    @Override
    public void onDebug(String s, Object o) {

    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }

    public static Intent newIntent(Activity activity, Region region) {
        Intent intent = new Intent(activity, ScanLicensePlateActivity.class);
        intent.putExtra(EXTRA_REGION, region);
        return intent;
    }
}