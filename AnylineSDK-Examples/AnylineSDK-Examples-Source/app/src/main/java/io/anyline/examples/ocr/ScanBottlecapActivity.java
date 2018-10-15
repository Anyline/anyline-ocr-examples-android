
package io.anyline.examples.ocr;
import android.os.Bundle;
import android.view.ViewGroup;
import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.BaseScanViewConfig;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;


public class ScanBottlecapActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanBottlecapActivity.class.getSimpleName();
    private ScanView scanView;

    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.BOTTLECAP;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        init();
    }
    // see ScanIbanActivity for a more detailed description

    void init() {
        scanView = (ScanView) findViewById(R.id.scan_view);

        final AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();

        anylineOcrConfig.setLanguages("bottlecap.traineddata");
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

        //init the scanViewPlugin config
        ScanViewPluginConfig ocrScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "bottlecap_view_config.json");
        //init the scan view
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), anylineOcrConfig, ocrScanViewPluginConfig, "OCR");
        //init the base config used for camera and flash
        BaseScanViewConfig ocrBaseScanViewConfig = new BaseScanViewConfig(getApplicationContext(), "bottlecap_view_config.json");
        //set the scan Base config
        scanView.setScanViewConfig(ocrBaseScanViewConfig);
        //set the scan view plugin to the scan view
        scanView.setScanViewPlugin(scanViewPlugin);
        //add the scan result listener
        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {
                String path = setupImagePath(result.getCutoutImage());

                startScanResultIntent(getResources().getString(R.string.title_bottlecap), getSerialNumberResult(result.getResult().toString()), path);
                setupScanProcessView(ScanBottlecapActivity.this, result, getScanModule());

            }

        });


        //set the debug listener
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

    protected HashMap<String, String> getSerialNumberResult (String result) {

        HashMap<String, String> serialNumberResult = new HashMap();

        serialNumberResult.put(getResources().getString(R.string.bottlecap_reading_result), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );

        return serialNumberResult;
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
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

}