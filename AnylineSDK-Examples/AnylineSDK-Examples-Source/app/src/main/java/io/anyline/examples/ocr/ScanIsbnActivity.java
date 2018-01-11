package io.anyline.examples.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrResultListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.SettingsFragment;
import io.anyline.examples.ocr.apis.IsbnActivity;

public class ScanIsbnActivity extends AppCompatActivity {
    private static final String TAG = ScanIsbnActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_anyline_ocr);

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // see ScanScrabbleActivity for a more detailed description
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");
        // use predefined whitelist and regular expression
        anylineOcrConfig.setCharWhitelist(AnylineOcrConfig.AnylineOcrRegex.ISBN.getWhiteList());
        anylineOcrConfig.setValidationRegex(AnylineOcrConfig.AnylineOcrRegex.ISBN.getRegex());
        // AUTO ScanMode automatically detects the correct text without any further parameters to be set
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.AUTO);
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setConfig(new AnylineViewConfig(this, "isbn_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrResultListener() {
            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {
                if (!anylineOcrResult.getResult().isEmpty()) {
                    Intent i = new Intent(ScanIsbnActivity.this, IsbnActivity.class);
                    i.putExtra(IsbnActivity.ISBN_INPUT, anylineOcrResult.getResult().trim());
                    startActivity(i);
                }
            }
        });

        // disable the reporting if set to off in preferences
        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                SettingsFragment.KEY_PREF_REPORTING_ON, true));
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
}
