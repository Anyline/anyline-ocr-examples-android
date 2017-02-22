package io.anyline.examples.ocr;

import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.util.List;

import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrError;
import at.nineyards.anyline.modules.ocr.AnylineOcrListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.SettingsFragment;
import io.anyline.examples.ocr.result.LicensePlateResultView;

public class ScanLicensePlateActivity extends AppCompatActivity {

    private static final String TAG = ScanLicensePlateActivity.class.getSimpleName();
    protected AnylineOcrScanView scanView;
    private LicensePlateResultView licensePlateResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_anyline_ocr);

        String license = getString(R.string.anyline_license_key);
        // Get the view from the layout
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);
        // Configure the view (cutout, the camera resolution, etc.) via json
        // (can also be done in xml in the layout)
        scanView.setConfig(new AnylineViewConfig(this, "license_plate_view_config.json"));

        // Copies given traineddata-file to a place where the core can access it.
        // This MUST be called for every traineddata file that is used
        // (before startScanning() is called).
        // The file must be located directly in the assets directory
        // (or in tessdata/ but no other folders are allowed)
        scanView.copyTrainedData("tessdata/GL-Nummernschild-Mtl7_uml.traineddata",
                "8ea050e8f22ba7471df7e18c310430d8");
        scanView.copyTrainedData("tessdata/Arial.traineddata", "9a5555eb6ac51c83cbb76d238028c485");
        scanView.copyTrainedData("tessdata/Alte.traineddata", "f52e3822cdd5423758ba19ed75b0cc32");
        scanView.copyTrainedData("tessdata/deu.traineddata", "2d5190b9b62e28fa6d17b728ca195776");

        // Configure the OCR for license plate scanning via a custom script file
        // This is how you could add custom scripts optimized by Anyline for your use-case
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setCustomCmdFile("license_plates.ale");

        // set the ocr config
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        // initialize with the license and a listener
        scanView.initAnyline(license, new AnylineOcrListener() {
            @Override
            public void onReport(String identifier, Object value) {
                // Called with interesting values, that arise during processing.
                // Some possibly reported values:
                //
                // $brightness - the brightness of the center region of the cutout as a float value
                // $confidence - the confidence, an Integer value between 0 and 100
                // $thresholdedImage - the current image transformed into black and white
                // $sharpness - the detected sharpness value (only reported if minSharpness > 0)
            }

            @Override
            public boolean onTextOutlineDetected(List<PointF> list) {
                // Called when the outline of a possible text is detected.
                // If false is returned, the outline is drawn automatically.
                return false;
            }

            @Override
            public void onResult(AnylineOcrResult result) {
                // Called when a valid result is found
                String results[] = result.getText().split("-");
                String country  = results[0];
                String licensePlate = results[1];

                licensePlateResultView.setLicensePlate(licensePlate);
                if(country != null && !country.isEmpty()) {
                    licensePlateResultView.setCountry(country);
                }
                licensePlateResultView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAbortRun(AnylineOcrError code, String message) {
                // Is called when no result was found for the current image.
                // E.g. if no text was found or the result is not valid.
            }
        });


        // disable the reporting if set to off in preferences
        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));
        addLicensePlateResultView();
    }

    private void addLicensePlateResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        licensePlateResultView = new LicensePlateResultView(this);
        licensePlateResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(licensePlateResultView, params);
        licensePlateResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAndHideLicensePlateResultView();
                startScanning();
            }
        });
    }

    private void clearAndHideLicensePlateResultView() {
        licensePlateResultView.setCountry("");
        licensePlateResultView.setLicensePlate("");
        licensePlateResultView.setVisibility(View.INVISIBLE);
    }

    private void startScanning() {
        // this must be called in onResume, or after a result to start the scanning again
        if (!scanView.isRunning()) {
            scanView.startScanning();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScanning();
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
            clearAndHideLicensePlateResultView();
            startScanning();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
