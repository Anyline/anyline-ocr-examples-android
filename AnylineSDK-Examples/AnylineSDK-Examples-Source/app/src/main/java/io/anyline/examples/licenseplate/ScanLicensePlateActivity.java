package io.anyline.examples.licenseplate;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.licenseplate.LicensePlateResult;
import at.nineyards.anyline.modules.licenseplate.LicensePlateResultListener;
import at.nineyards.anyline.modules.licenseplate.LicensePlateScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;

public class ScanLicensePlateActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanLicensePlateActivity.class.getSimpleName();
    protected LicensePlateScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getLayoutInflater().inflate(R.layout.activity_scan_license_plate, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        final String license = getString(R.string.anyline_license_key);

        // Get the view from the layout
        scanView = (LicensePlateScanView) findViewById(R.id.license_plate_scan_view);

        // Configure the view (cutout, the camera resolution, etc.) via json (can also be done in xml in the layout)
        scanView.setConfig(new AnylineViewConfig(this, "license_plate_view_config.json"));

        scanView.setDebugListener(this);

        // initialize with the license and a listener
        scanView.initAnyline(license, new LicensePlateResultListener() {
            @Override
            public void onResult(LicensePlateResult licensePlateResult) {
                // Called when a valid result is found (minimum confidence is exceeded and validation with regex was ok)

                setFeedbackViewActive(false);

                String path = setupImagePath(licensePlateResult.getCutoutImage());

                startScanResultIntent(getResources().getString(R.string.title_license_plate), getLicensePlateResult(licensePlateResult), path);

                setupScanProcessView(ScanLicensePlateActivity.this, licensePlateResult, getScanModule());
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
        return ScanModuleEnum.ScanModule.LICENSE_PLATE;
    }


    private void restartScanningAfterResult() {
        setFeedbackViewActive(true);
        if (!scanView.isRunning()) {
            scanView.startScanning();
        }
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
        super.onBackPressed();
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
        } else if (AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)) {
            handleFeedback(FeedbackType.SHAKY);
        }
    }

    public HashMap<String, String> getLicensePlateResult(LicensePlateResult licensePlateResult) {

        HashMap<String, String> licensePlateResultData = new HashMap<>();

        licensePlateResultData.put(getResources().getString(R.string.license_plate_country) , (licensePlateResult.getCountry() == null || licensePlateResult.getCountry().isEmpty()) ?  getResources().getString(R.string.not_available) : licensePlateResult.getCountry());
        licensePlateResultData.put(getResources().getString(R.string.license_plate_result), (licensePlateResult.getResult() == null || licensePlateResult.getResult().isEmpty()) ? getResources().getString(R.string.not_available) : licensePlateResult.getResult());

        return licensePlateResultData;
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }
}
