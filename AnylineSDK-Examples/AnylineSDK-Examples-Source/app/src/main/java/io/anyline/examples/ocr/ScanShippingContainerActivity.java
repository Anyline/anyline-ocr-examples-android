package io.anyline.examples.ocr;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

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


public class ScanShippingContainerActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanVoucherCodeActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // create new Anyline OCR config
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();

        // set languages to config
        anylineOcrConfig.setLanguages("USNr.any");

        // set command file to config
        anylineOcrConfig.setCustomCmdFile("container_scanner.ale");

        // AUTO ScanMode automatically detects the correct text without any further parameters to be set
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.AUTO);

        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setDebugListener(this);

        //set the json file for the view configuration
        scanView.setConfig(new AnylineViewConfig(this, "shipping_container_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {

                String result = anylineOcrResult.getResult();

                setFeedbackViewActive(false);

                String path = setupImagePath(anylineOcrResult.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.title_shipping_container), getShippingContainerResult(result), path);

                setupScanProcessView(ScanShippingContainerActivity.this, anylineOcrResult, getScanModule());
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
        return ScanModuleEnum.ScanModule.SHIPPING_CONTAINER;
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

    private HashMap<String, String> getShippingContainerResult(String shippingContainerResult){
        HashMap<String, String> shippingContainerHashMap = new HashMap<>();

        shippingContainerHashMap.put(getResources().getString(R.string.shipping_reading_result) , (shippingContainerResult == null || shippingContainerResult.isEmpty()) ?  getResources().getString(R.string.not_available) : shippingContainerResult);

        return shippingContainerHashMap;
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }

}