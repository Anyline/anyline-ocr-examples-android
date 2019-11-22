
package io.anyline.examples.ocr;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.apis.IsbnActivity;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineOcrConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.BaseScanViewConfig;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;


public class ScanIsbnActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanIsbnActivity.class.getSimpleName();
    private ScanView scanView;
    private io.anyline.view.AnylineViewConfig anylineViewConfig;

    @Override
    protected ScanView getScanView() {
        return null;
    }
@Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ISBN;
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

        //anyline Ocr Config setup
        final AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setLanguages("eng_no_dict.traineddata", "deu.traineddata");
        anylineOcrConfig.setCharWhitelist("ISBN0123456789<>-X");
        anylineOcrConfig.setValidationRegex("^ISBN((-)?\\s*(13|10))?:?\\s*((978|979){1}-?\\s*)*[0-9]{1," +
                "5}-?\\s*[0-9]{2,7}-?\\s*[0-9]{2," +
                "7}-?\\s*[0-9X]$");
        // AUTO ScanMode automatically detects the correct text without any further parameters to be set
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.AUTO);

        scanView.setScanConfig("isbn_view_config.json");
        //init the scanViewPlugin config
        ScanViewPluginConfig ocrScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "isbn_view_config.json");
        //init the scan view
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), anylineOcrConfig, ocrScanViewPluginConfig, "OCR");
        //init the base config used for camera and flash
        BaseScanViewConfig ocrBaseScanViewConfig = new BaseScanViewConfig(getApplicationContext(), "isbn_view_config.json");
        //set the scan Base config
        scanView.setScanViewConfig(ocrBaseScanViewConfig);
        //set the scan view plugin to the scan view
        scanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {

                if (!result.toString().isEmpty()) {
                    setFeedbackViewActive(false);


                    Intent i = new Intent(ScanIsbnActivity.this, IsbnActivity.class);
                    i.putExtra(IsbnActivity.ISBN_INPUT, result.getResult().toString().trim());

                    startActivity(i);
                    overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);

                    setupScanProcessView(ScanIsbnActivity.this, result, getScanModule());
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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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