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
import io.anyline.examples.ocr.apis.RecordSearchActivity;

public class ScanRecordActivity extends AppCompatActivity {

    private static final String TAG = ScanRecordActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_anyline_ocr);

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);


        //Configure the OCR for Record Numbers
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        // use the LINE mode, since the numbers can be of different length
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.LINE);
        // Set the languages used for OCR
        // Copies given traineddata-file to a place where the core can access it.
        // The file must be located directly in the assets directory (or in tessdata/ but no other folders are allowed)
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");
        // allow only capital letters and some numbers - these are the only characters the SDK will consider
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-.");
        // a simple regex for a basic validation of the record numbers
        anylineOcrConfig.setValidationRegex("^([A-Z]+\\s*-*\\s*)?[0-9A-Z-\\s\\.]{3,}$");
        // the characters height is 15 pixels minimum (make sure your cutout size is aligned to this)
        anylineOcrConfig.setMinCharHeight(15);
        // the characters height is 70 pixels maximum (make sure your cutout size is aligned to this)
        anylineOcrConfig.setMaxCharHeight(70);
        // the minimum confidence required to return a result, a value between 0 and 100.
        // (higher confidence means less likely to get an incorrect result, but may be slower to deliver a result)
        anylineOcrConfig.setMinConfidence(75);
        // we don't want to remove small contours, as a . for example should be kept in the result
        anylineOcrConfig.setRemoveSmallContours(false);
        // we also don't want whitespaces to be removed - they are required for a search with the scanned record number
        anylineOcrConfig.setRemoveWhitespaces(false);
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setConfig(new AnylineViewConfig(this, "record_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrResultListener() {
            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {
                if (!anylineOcrResult.getResult().isEmpty()) {
                    Intent i = new Intent(ScanRecordActivity.this, RecordSearchActivity.class);
                    i.putExtra(RecordSearchActivity.RECORD_INPUT, anylineOcrResult.getResult().trim());
                    startActivity(i);
                }
            }
        });

        // disable the reporting if set to off in preferences
        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));
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
