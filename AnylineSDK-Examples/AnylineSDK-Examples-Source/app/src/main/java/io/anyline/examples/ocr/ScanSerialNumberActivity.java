/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 */
package io.anyline.examples.ocr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.util.AssetUtil;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineOcrConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanSerialNumberActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanSerialNumberActivity.class.getSimpleName();
    private static final int INTENT_SETTINGS = 1;

    SharedPreferences prefs;
    SerialNumberPreferences snPrefs;


    private ScanView scanView;
    private OcrScanViewPlugin scanViewPlugin;
    io.anyline.plugin.ocr.AnylineOcrConfig anylineOcrConfig;
    JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id.scan_view_placeholder));
        prefs = getSharedPreferences("io.anyline.examples.ocr", MODE_PRIVATE);
        snPrefs = SerialNumberPreferences.getInstance(this);

        scanView = findViewById(R.id.scan_view);

        try {
            jsonObject = null;
            jsonObject = AssetUtil.getAnylineAssetsJson(this, "serial_number_view_config.json");
        } catch (RuntimeException e) {
            Log.d(ScanView.class.getName(), "Invalid JSON File" + e.getMessage());
            throw e;
            // can happen if returned from recorder but preview not jet active
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        anylineOcrConfig = new AnylineOcrConfig();

        initViews();
    }


    private void initViews() {
        Log.i (TAG, "height, width initViews: " + scanView.getHeight() + " " + scanView.getWidth());

        jsonObject = snPrefs.updateJsonFromSettings(this, jsonObject);

        // see ScanIbanActivity for a more detailed description
        anylineOcrConfig.setValidationRegex(snPrefs.getRegex());
        String allowedChars = snPrefs.getAllowedChars();
        if (allowedChars.length() == 0) {
            allowedChars = null;
        }
        anylineOcrConfig.setCharWhitelist(allowedChars);

        Log.i(TAG, "Regex, allowed chars: init: " + snPrefs.getRegex() + "   " + snPrefs.getAllowedChars());
        scanView.setScanConfig(jsonObject);

        //init the scanViewPlugin config
        //init the scan view
        scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), anylineOcrConfig,
                                               scanView.getScanViewPluginConfig(), "OCR");

        scanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScanResultListener((ScanResultListener<OcrScanResult>) result -> {
            String path = setupImagePath(result.getCutoutImage());

            startScanResultIntent(getResources().getString(R.string.serial_number), getSerialNumberResult(result.getResult()), path);
            Log.i(TAG, "Regex, allowed chars: result: " + result.getResult());

            setupScanProcessView(ScanSerialNumberActivity.this, result, getScanModule());
        });

        createFeedbackView(scanView);
    }

    @Override
    protected ScanView getScanView() {
        return null;
    }


    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.SERIAL_NUMBER;
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

    @Override
    public void onDebug(String name, Object value) {

        if (AnylineDebugListener.BRIGHTNESS_VARIABLE_NAME.equals(name) &&
            (AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.equals(value.getClass()) ||
             AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.isAssignableFrom(value.getClass()))) {
            switch (scanView.getBrightnessFeedBack()) {
                case TOO_BRIGHT:
                    handleFeedback(FeedbackType.TOO_BRIGHT);
                    break;
                case TOO_DARK:
                    handleFeedback(FeedbackType.TOO_DARK);
                    break;
                case OK:
                    handleFeedback(FeedbackType.PERFECT);
                    break;
            }
        } else if (AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)) {
            handleFeedback(FeedbackType.SHAKY);
        }
    }

    protected HashMap<String, String> getSerialNumberResult(String result) {

        HashMap<String, String> serialNumberResult = new HashMap();

        serialNumberResult.put(getResources().getString(R.string.universal_reading_result),
                               (result.isEmpty() || result == null) ? getResources().getString(R.string.not_available) : result);

        return serialNumberResult;
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
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
            scanView.stop();
            scanView.releaseCameraInBackground();
            Log.i (TAG, "height, width before intent: " + scanView.getHeight() + " " + scanView.getWidth());

            Intent intentSettings = new Intent(ScanSerialNumberActivity.this, SerialNumberSettingsMenuActivity.class);
            ScanSerialNumberActivity.this.startActivityForResult(intentSettings, INTENT_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_SETTINGS) {
            initViews();
            scanView.start();
        }
    }

}
