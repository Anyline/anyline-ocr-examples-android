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
import io.anyline.examples.ocr.result.RedBullResultView;

public class ScanRedBullCodeActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanRedBullCodeActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;
    private RedBullResultView redBullResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        addRedBullResultView();

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // see ScanScrabbleActivity for a more detailed description
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setLanguages("tessdata/rbf_jan2015_v2.traineddata");
        anylineOcrConfig.setCharWhitelist("2346789ABCDEFGHKLMNPQRTUVWXYZ");
        anylineOcrConfig.setMinCharHeight(15);
        anylineOcrConfig.setMaxCharHeight(30);
        anylineOcrConfig.setMinConfidence(75);
        anylineOcrConfig.setValidationRegex("^[0-9A-Z]{4}\n[0-9A-Z]{4}");
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
        anylineOcrConfig.setCharCountX(4);
        anylineOcrConfig.setCharCountY(2);
        anylineOcrConfig.setCharPaddingXFactor(0.3);
        anylineOcrConfig.setCharPaddingYFactor(0.5);
        anylineOcrConfig.setIsBrightTextOnDark(true);
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setDebugListener(this);

        scanView.setConfig(new AnylineViewConfig(this, "rb_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {

                String result = anylineOcrResult.getResult();

                setFeedbackViewActive(false);

                redBullResultView.setResult(result);
                redBullResultView.setVisibility(View.VISIBLE);

                setupScanProcessView(ScanRedBullCodeActivity.this, anylineOcrResult, getScanModule());
            }
        });

        redBullResultView.setOnClickListener(new View.OnClickListener() {
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
        return ScanModuleEnum.ScanModule.RED_BULL_CODE;
    }

    private void addRedBullResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        redBullResultView = new RedBullResultView(this);
        redBullResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(redBullResultView, params);
    }

    private void restartScanningAfterResult() {
        redBullResultView.setVisibility(View.INVISIBLE);
        setFeedbackViewActive(true);
        resetTime();
        if (!scanView.isRunning()) {
            scanView.startScanning();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        redBullResultView.setVisibility(View.INVISIBLE);

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
        if (redBullResultView.getVisibility() == View.VISIBLE) {
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
