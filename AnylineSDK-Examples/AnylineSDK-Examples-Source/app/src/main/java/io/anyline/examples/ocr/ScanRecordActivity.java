package io.anyline.examples.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.apis.RecordSearchActivity;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;

public class ScanRecordActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanRecordActivity.class.getSimpleName();
    private ScanView scanView;

    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.RECORD;
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
            scanView.init("record_view_config.json", getString(R.string.anyline_license_key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        OcrScanViewPlugin scanViewPlugin = (OcrScanViewPlugin) scanView.getScanViewPlugin();

       // scanViewPlugin.getAnylineOcrConfig().setValidationRegex("^([A-Z]+\\s*-*\\s*)?[0-9A-Z-\\s\\.]{3,}$");
        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult anylineOcrResult) {

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
        scanViewPlugin.setDebugListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        scanView.start();
        createFeedbackView(scanView);
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
        } else if(AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)){
            handleFeedback(FeedbackType.SHAKY);
        }
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }
}
