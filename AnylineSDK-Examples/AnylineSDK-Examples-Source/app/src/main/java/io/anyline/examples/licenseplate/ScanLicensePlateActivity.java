package io.anyline.examples.licenseplate;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.modules.licenseplate.LicensePlateResult;
import at.nineyards.anyline.modules.licenseplate.LicensePlateResultListener;
import at.nineyards.anyline.modules.licenseplate.LicensePlateScanView;
import io.anyline.examples.R;
import io.anyline.examples.SettingsFragment;
import io.anyline.examples.licenseplate.result.LicensePlateResultView;

public class ScanLicensePlateActivity extends AppCompatActivity {

    private static final String TAG = ScanLicensePlateActivity.class.getSimpleName();
    protected LicensePlateScanView scanView;
    private LicensePlateResultView licensePlateResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scan_license_plate);

        String license = getString(R.string.anyline_license_key);

        // Get the view from the layout
        scanView = (LicensePlateScanView) findViewById(R.id.license_plate_scan_view);
        // Configure the view (cutout, the camera resolution, etc.) via json
        // (can also be done in xml in the layout)
        scanView.setConfig(new AnylineViewConfig(this, "license_plate_view_config.json"));

        scanView.initAnyline(license, new LicensePlateResultListener() {
            @Override
            public void onResult(LicensePlateResult licensePlateResult) {
                // Called when a valid result is found
                String country = licensePlateResult.getCountry();
                String licensePlate = licensePlateResult.getResult();

                licensePlateResultView.setLicensePlate(licensePlate);
                if (country != null && !country.isEmpty()) {
                    licensePlateResultView.setCountry(country);
                }
                licensePlateResultView.setVisibility(View.VISIBLE);
            }
        });

        // disable the reporting if set to off in preferences
        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));
        addLicensePlateResultView();
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
        if(licensePlateResultView == null || licensePlateResultView.getVisibility() != View.VISIBLE){
            startScanning();
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
