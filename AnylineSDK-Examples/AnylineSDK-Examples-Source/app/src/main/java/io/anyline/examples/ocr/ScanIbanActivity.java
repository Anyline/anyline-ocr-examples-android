package io.anyline.examples.ocr;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.camera.CameraConfig;
import at.nineyards.anyline.camera.CameraFeatures;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrResultListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.SettingsFragment;
import io.anyline.examples.ocr.result.IbanResultView;

public class ScanIbanActivity extends AppCompatActivity {

    private static final String TAG = ScanIbanActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;
    private IbanResultView ibanResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_anyline_ocr);

        addIbanResultView();

        String license = getString(R.string.anyline_license_key);
        // Get the view from the layout
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);
        // Configure the view (cutout, the camera resolution, etc.) via json (can also be done in xml in the layout)
        scanView.setConfig(new AnylineViewConfig(this, "iban_view_config.json"));


        //Configure the OCR for IBANs
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();

        // Set the languages used for OCR
        // Copies given traineddata-file to a place where the core can access it.
        // The file must be located directly in the assets directory (or in tessdata/ but no other folders are allowed)
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");

        // AUTO ScanMode automatically detects the correct text without any further parameters to be set
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.AUTO);

        // allow only capital letters and numbers
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        // The minimum confidence required to return a result, a value between 0 and 100.
        // (higher confidence means less likely to get a wrong result, but may be slower to get a result)
        anylineOcrConfig.setMinConfidence(65);
        // a simple regex for a basic validation of the IBAN, results that don't match this, will not be returned
        // (full validation is more complex, as different countries have different formats)
        anylineOcrConfig.setValidationRegex("^[A-Z]{2}([0-9A-Z]\\s*){13,32}$");
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

        // initialize with the license and a listener
        scanView.initAnyline(license, new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {
                // Called when a valid result is found (minimum confidence is exceeded and validation with regex was ok)
                ibanResultView.setResult(anylineOcrResult.getResult());
                ibanResultView.setVisibility(View.VISIBLE);
            }
        });

        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));

        ibanResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibanResultView.setVisibility(View.INVISIBLE);
                if (!scanView.isRunning()) {
                    scanView.startScanning();
                }
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();

        if(ibanResultView == null || ibanResultView.getVisibility() != View.VISIBLE){
            scanView.startScanning();
        }
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
            ibanResultView.setVisibility(View.INVISIBLE);
            if (!scanView.isRunning()) {
                scanView.startScanning();
            }
        } else {
            super.onBackPressed();
        }

    }
}
