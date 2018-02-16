package io.anyline.examples.ocr;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewGroup;

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
import io.anyline.examples.ocr.apis.RecordSearchActivity;
import io.anyline.examples.ocr.feedback.FeedbackType;

public class ScanRecordActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanRecordActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));


        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // see ScanIbanActivity for a more detailed description
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-.");
        anylineOcrConfig.setMinCharHeight(15);
        anylineOcrConfig.setMaxCharHeight(70);
        anylineOcrConfig.setMinConfidence(75);
        anylineOcrConfig.setValidationRegex("^([A-Z]+\\s*-*\\s*)?[0-9A-Z-\\s\\.]{3,}$");
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.LINE);
        anylineOcrConfig.setRemoveSmallContours(false);

        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setDebugListener(this);

        scanView.setConfig(new AnylineViewConfig(this, "record_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {

                String result = anylineOcrResult.getResult();

                if (!result.isEmpty()) {
                    setFeedbackViewActive(false);

                    Intent i = new Intent(ScanRecordActivity.this, RecordSearchActivity.class);
                    i.putExtra(RecordSearchActivity.RECORD_INPUT, result.trim());

                    startActivity(i);
                    overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);

                    setupScanProcessView(ScanRecordActivity.this, anylineOcrResult, getScanModule());
                }
            }
        });

        createFeedbackView(scanView);

    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return scanView;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    public Rect getCutoutRect() {
        return scanView.getCutoutRect();
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.RECORD;
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanView.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanView.cancelScanning();
        scanView.releaseCameraInBackground();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setFeedbackViewActive(true);
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
