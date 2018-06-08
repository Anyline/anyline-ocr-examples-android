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

public class ScanBottlecapActivity extends ScanActivity implements AnylineDebugListener {

    private AnylineOcrScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id.scan_view_placeholder));


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

                //set the path for the image which will be shown in the result screen
                String path = setupImagePath(anylineOcrResult.getCutoutImage());
                //start the resultScanView activity
                startScanResultIntent(getResources().getString(R.string.title_bottlecap), getBottlecapResult(result), path);

                setupScanProcessView(ScanBottlecapActivity.this, anylineOcrResult, getScanModule());
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
    public void onBackPressed() {
        //close the result view on back press if it is open
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);

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

    private HashMap<String, String> getBottlecapResult (String bottlecapResult){
        HashMap<String, String> bottlecapHashMap = new HashMap<>();

        bottlecapHashMap.put(getResources().getString(R.string.bottlecap_reading_result) , (bottlecapResult == null || bottlecapResult.isEmpty()) ?  getResources().getString(R.string.not_available) : bottlecapResult);

        return bottlecapHashMap;
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }
}
