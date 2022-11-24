/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-10-27
 */
package io.anyline.examples.ocr;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanShippingContainerActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanShippingContainerActivity.class.getSimpleName();
    private ScanView scanView;
    private io.anyline.view.AnylineViewConfig anylineViewConfig;


    @Override
    protected ScanView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.SHIPPING_CONTAINER;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        init();

    }

    void init() {
        scanView = (ScanView) findViewById(R.id.scan_view);


        try {
            scanView.init("shipping_container_view_config.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        OcrScanViewPlugin scanViewPlugin = (OcrScanViewPlugin) scanView.getScanViewPlugin();

        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {
                String path = setupImagePath(result.getCutoutImage());

                startScanResultIntent(getResources().getString(R.string.shipping_reading_result), getShippingcontainerResult(result.getResult().toString()), path);
                setupScanProcessView(ScanShippingContainerActivity.this, result, getScanModule());
            }

        });

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

    protected LinkedHashMap<String, String> getShippingcontainerResult (String result) {

        LinkedHashMap<String, String> shippingContainer = new LinkedHashMap();

        shippingContainer.put(getResources().getString(R.string.reading_result), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );

        return shippingContainer;
    }

    @Override
    public void onDebug(String s, Object o) {

    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }

}