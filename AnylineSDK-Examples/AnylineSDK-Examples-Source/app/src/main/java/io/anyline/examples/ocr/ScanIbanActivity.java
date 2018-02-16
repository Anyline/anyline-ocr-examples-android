package io.anyline.examples.ocr;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
    private IbanResultView ibanResultView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_ocr, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        addIbanResultView();

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
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.AUTO);
        // set the languages used for OCR
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");
        // allow only capital letters and numbers
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        // a simple regex for a basic validation of the IBAN, results that don't match this, will not be returned
        // (full validation is more complex, as different countries have different formats)
        anylineOcrConfig.setValidationRegex("^[A-Z]{2}([0-9A-Z]\\s*){13,32}$");
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

                ibanResultView.setResult(result);
                ibanResultView.setVisibility(View.VISIBLE);

                setupScanProcessView(ScanIbanActivity.this, anylineOcrResult, getScanModule());
            }
        });

        ibanResultView.setOnClickListener(new View.OnClickListener() {
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
        return ScanModuleEnum.ScanModule.IBAN;
    }

    private void addIbanResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        ibanResultView = new IbanResultView(this);
        ibanResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(ibanResultView, params);
    }


    private void restartScanningAfterResult() {
        ibanResultView.setVisibility(View.INVISIBLE);
        setFeedbackViewActive(true);
        resetTime();
        if (!scanView.isRunning()) {
            scanView.startScanning();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        ibanResultView.setVisibility(View.INVISIBLE);

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
        if (ibanResultView.getVisibility() == View.VISIBLE) {
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
