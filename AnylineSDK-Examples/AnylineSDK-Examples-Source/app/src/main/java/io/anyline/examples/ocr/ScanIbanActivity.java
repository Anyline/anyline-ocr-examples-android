package io.anyline.examples.ocr;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.camera.CameraConfig;
import at.nineyards.anyline.camera.CameraFeatures;
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
import io.anyline.examples.ocr.result.IbanResultView;

public class ScanIbanActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanIbanActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        String license = getString(R.string.anyline_license_key);
        // Get the view from the layout
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);


        // Configure the view (cutout, the camera resolution, etc.) via json (can also be done in xml in the layout)
        scanView.setConfig(new AnylineViewConfig(this, "iban_view_config.json"));

        // Copies given traineddata-file to a place where the core can access it.
        // This MUST be called for every traineddata file that is used (before startScanning() is called).
        // The file must be located directly in the assets directory (or in tessdata/ but no other folders are allowed)

        //Configure the OCR for IBANs
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        // AUTO ScanMode automatically detects the correct text without any further parameters to be set
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.LINE);
        // set the languages used for OCR
        anylineOcrConfig.setLanguages("USNr.any");
        // allow only capital letters and numbers
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        // a simple regex for a basic validation of the IBAN, results that don't match this, will not be returned
        // (full validation is more complex, as different countries have different formats)
        anylineOcrConfig.setValidationRegex("^[A-Z]{2}([0-9A-Z]\\s*){13,32}$");
        // set the height range the text can have
        anylineOcrConfig.setMinCharHeight(25);
        anylineOcrConfig.setMaxCharHeight(65);
        // the minimum confidence required to return a result, a value between 0 and 100.
        // (higher confidence means less likely to get a wrong result, but may be slower to get a result)
        anylineOcrConfig.setMinConfidence(70);
        // set the ocr config
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        // set individual camera settings for this example by getting the current preferred settings and adapting them
        CameraConfig camConfig = scanView.getPreferredCameraConfig();
        // change default focus mode to auto (works better if cutout is not in the center)
        camConfig.setFocusMode(CameraFeatures.FocusMode.AUTO);
        // autofocus is called in this interval (8000 is default)
        camConfig.setAutoFocusInterval(8000);
        // call autofocus if view is touched (true is default)
        camConfig.setFocusOnTouchEnabled(true);
        // focus where the cutout is (true is default)
        camConfig.setFocusRegionEnabled(true);
        // automatic exposure calculation based on where the cutout is (true is default)
        camConfig.setAutoExposureRegionEnabled(true);

        scanView.setDebugListener(this);

        // initialize with the license and a listener
        scanView.initAnyline(license, new AnylineOcrResultListener() {
            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {
                setFeedbackViewActive(false);

                String result = anylineOcrResult.getResult();

                String path = setupImagePath(anylineOcrResult.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.title_iban), getIbanResult(result), path);

                setupScanProcessView(ScanIbanActivity.this, anylineOcrResult, getScanModule());
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
        return ScanModuleEnum.ScanModule.IBAN;
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

    private HashMap<String, String> getIbanResult (String ibanResult){
        HashMap<String, String> ibanHashMap = new HashMap<>();

        ibanHashMap.put(getResources().getString(R.string.iban_reading_result) , (ibanResult == null || ibanResult.isEmpty()) ?  getResources().getString(R.string.not_available) : ibanResult.replaceAll("....(?!$)", "$0 "));

        return ibanHashMap;
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }
}
