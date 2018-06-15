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
/**
 * Example Vehicle Identification Number Scan
 */

public class ScanVehicleIdentificationNumberActivity extends ScanActivity implements AnylineDebugListener {
    private static final String TAG = io.anyline.examples.licenseplate.ScanLicensePlateActivity.class.getSimpleName();
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
        anylineOcrConfig.setLanguages("VIN.any");

        // set command file to config
        anylineOcrConfig.setCustomCmdFile("vin.ale");

        // set config to scan view
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        // use the view configuration defined in "vin_view_config.json" for scanning vehicle identification numbers
        scanView.setConfig(new AnylineViewConfig(this, "vin_view_config.json"));

        scanView.setDebugListener(this);

        scanView.initAnyline(lic, new AnylineOcrResultListener() {
            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {


                setFeedbackViewActive(false);

                String result = anylineOcrResult.getResult();

                String path = setupImagePath(anylineOcrResult.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.vin), getVINResult(result), path);

                setupScanProcessView(ScanVehicleIdentificationNumberActivity.this, anylineOcrResult, getScanModule());

            }
        });

        createFeedbackView(scanView);
    }

    private HashMap<String, String> getVINResult(String vinResult){
        HashMap<String, String> VINHashMap = new HashMap<>();

        VINHashMap.put(getResources().getString(R.string.vin_reading_result) , (vinResult == null || vinResult.isEmpty()) ?  getResources().getString(R.string.not_available) : vinResult);

        return VINHashMap;
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.VEHICLE_IDENTIFICATION_NUMBER;
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

