package io.anyline.examples.ocr;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrResultListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.examples.ocr.result.BottlecapResultView;

public class ScanBottlecapActivity extends ScanActivity implements AnylineDebugListener {

    private AnylineOcrScanView scanView;
    private BottlecapResultView bottlecapResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        addBottlecapResultView();

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // see ScanScrabbleActivity for a more detailed description
        final AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setLanguages("tessdata/bottlecap.traineddata");
        anylineOcrConfig.setCharWhitelist("123456789ABCDEFGHJKLMNPRSTUVWXYZ");
        anylineOcrConfig.setMinCharHeight(14);
        anylineOcrConfig.setMaxCharHeight(65);
        anylineOcrConfig.setMinConfidence(75);
        anylineOcrConfig.setValidationRegex("^[0-9A-Z]{3}\n[0-9A-Z]{3}\n[0-9A-Z]{3}");
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
        anylineOcrConfig.setCharCountX(3);
        anylineOcrConfig.setCharCountY(3);
        anylineOcrConfig.setCharPaddingXFactor(0.3);
        anylineOcrConfig.setCharPaddingYFactor(0.5);
        anylineOcrConfig.setIsBrightTextOnDark(true);
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setConfig(new AnylineViewConfig(this, "bottlecap_view_config.json"));

        scanView.setDebugListener(this);

        scanView.initAnyline(lic, new AnylineOcrResultListener() {
            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {

                setFeedbackViewActive(false);

                String result = anylineOcrResult.getResult();

                bottlecapResultView.setResult(result);
                bottlecapResultView.setVisibility(View.VISIBLE);

                setupScanProcessView(ScanBottlecapActivity.this, anylineOcrResult, getScanModule());
            }
        });


        bottlecapResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartScanningAfterResult();
            }
        });

        createFeedbackView(scanView);

    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return scanView;
    }


    @Override
    public Rect getCutoutRect() {
        return scanView.getCutoutRect();
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.BOTTLECAP;
    }

    private void addBottlecapResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        bottlecapResultView = new BottlecapResultView(this);
        bottlecapResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(bottlecapResultView, params);
    }


    private void restartScanningAfterResult() {
        bottlecapResultView.setVisibility(View.INVISIBLE);
        setFeedbackViewActive(true);
        resetTime();
        if (!scanView.isRunning()) {
            scanView.startScanning();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bottlecapResultView.setVisibility(View.INVISIBLE);

        scanView.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanView.cancelScanning();
        scanView.releaseCameraInBackground();
    }

    @Override
    public void onBackPressed() {
        //close the result view on back press if it is open
        if (bottlecapResultView.getVisibility() == View.VISIBLE) {
            restartScanningAfterResult();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onDebug(String name, Object value) {

        if (AnylineDebugListener.BRIGHTNESS_VARIABLE_NAME.equals(name) &&
                (AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.equals(value.getClass()) ||
                        AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.isAssignableFrom(value.getClass()))) {
            switch (scanView.getBrightnessFeedback()) {
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
        } else if(AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)){
            handleFeedback(FeedbackType.SHAKY);
        }
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }
}
