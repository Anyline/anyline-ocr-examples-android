package io.anyline.examples.licenseplate;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

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
import io.anyline.examples.licenseplate.result.LicensePlateResultView;

public class ScanLicensePlateActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanLicensePlateActivity.class.getSimpleName();
    protected LicensePlateScanView scanView;
    private LicensePlateResultView licensePlateResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getLayoutInflater().inflate(R.layout.activity_scan_license_plate, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        //setContentView(R.layout.activity_anyline_ocr);
        addLicensePlateResultView();
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

                //getAwardedDialog();
                //addAwardedPoint();
                setFeedbackViewActive(false);

                String country = licensePlateResult.getCountry();
                String licensePlate = licensePlateResult.getResult();

                if (country != null && !country.isEmpty()) {
                    licensePlateResultView.setCountry(country);
                }

                licensePlateResultView.setLicensePlate(licensePlate);
                licensePlateResultView.setVisibility(View.VISIBLE);

                setupScanProcessView(ScanLicensePlateActivity.this, licensePlateResult, getScanModule());
            }
        });

        licensePlateResultView.setOnClickListener(new View.OnClickListener() {
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
        return ScanModuleEnum.ScanModule.LICENSE_PLATE;
    }

    private void addLicensePlateResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.license_plate_main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        licensePlateResultView = new LicensePlateResultView(this);
        licensePlateResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(licensePlateResultView, params);
    }


    private void restartScanningAfterResult() {
        clearAndHideLicensePlateResultView();
        setFeedbackViewActive(true);
        if (!scanView.isRunning()) {
            scanView.startScanning();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        clearAndHideLicensePlateResultView();
        scanView.startScanning();
    }

    private void clearAndHideLicensePlateResultView() {
        licensePlateResultView.setLicensePlate("");
        licensePlateResultView.setCountry("");
        licensePlateResultView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanView.cancelScanning();
        scanView.releaseCameraInBackground();
    }

    @Override
    public void onBackPressed() {
        if (licensePlateResultView.getVisibility() == View.VISIBLE) {
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
        } else if (AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)) {
            handleFeedback(FeedbackType.SHAKY);
        }
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }
}
