package io.anyline.examples.ocr;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import java.util.List;

import at.nineyards.anyline.camera.AnylineViewConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrError;
import at.nineyards.anyline.modules.ocr.AnylineOcrListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.SettingsFragment;
import io.anyline.examples.ocr.apis.AnagramActivity;

public class ScanScrabbleActivity extends AppCompatActivity {

    private static final String TAG = ScanScrabbleActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_anyline_ocr);

        String lic = getString(R.string.anyline_license_key);
        // Get the view from the layout
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // Copies given traineddata-file to a place where the core can access it.
        // This MUST be called for every traineddata file that is used (before startScanning() is called).
        // The file must be located directly in the assets directory (or in tessdata/ but no other folders are allowed)
        scanView.copyTrainedData("tessdata/scrabble.traineddata", "855d8088928ee058257f64ccac2837eb");

        //Configure the OCR for Scrabble
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        // Use the grid mode, since it's a one line grid with up to 7 characters
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
        // set the languages used for OCR
        anylineOcrConfig.setTesseractLanguages("scrabble");
        // allow only capital letters plus some german Umlaute
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZÄÜÖ");
        // set the height range the text can have
        anylineOcrConfig.setMinCharHeight(30);
        anylineOcrConfig.setMaxCharHeight(60);
        // the minimum confidence required to return a result, a value between 0 and 100.
        // (higher confidence means less likely to get a wrong result, but may be slower to get a result)
        anylineOcrConfig.setMinConfidence(80);
        // a simple regex for a basic validation of the scrabble characters. We require at least 4 characters, and a maximum of 10 (usually it would be 7, but Umlaute may use two ASCII symbols)
        anylineOcrConfig.setValidationRegex("^[A-ZÄÜÖ]{7,10}$");
        // the character count in a row may be up to 7
        anylineOcrConfig.setCharCountX(7);
        // and we only have one row of characters
        anylineOcrConfig.setCharCountY(1);
        // the characters may be up to 1.7 times their width (horizontally) apart from each other in this example
        anylineOcrConfig.setCharPaddingXFactor(1.7);
        // the characters may be up to 0.5 times their width (vertically) apart from each other in this example
        // setting the charPaddingYFactor is not necessary in this example, since there is only one row
        anylineOcrConfig.setCharPaddingYFactor(0.5);
        // the text is dark on brither background
        anylineOcrConfig.setIsBrightTextOnDark(false);

        // set the ocr config
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        // Configure the view (cutout, the camera resolution, etc.) via json (can also be done in xml in the layout)
        scanView.setConfig(new AnylineViewConfig(this, "scrabble_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrListener() {
            @Override
            public void onReport(String identifier, Object value) {
                // Called with interesting values, that arise during processing.
                // Some possibly reported values:
                //
                // $brightness - the brightness of the center region of the cutout as a float value
                // $confidence - the confidence, an Integer value between 0 and 100
                // $thresholdedImage - the current image transformed into black and white
            }

            @Override
            public boolean onTextOutlineDetected(List<PointF> list) {
                // Called when the outline of a possible text is detected.
                // If false is returned, the outline is drawn automatically.
                return false;
            }

            @Override
            public void onResult(AnylineOcrResult result) {
                // Called when a valid result is found (minimum confidence is exceeded and validation with regex was ok)
                if (result.getText() != null && !result.getText().isEmpty()) {
                    Intent i = new Intent(ScanScrabbleActivity.this, AnagramActivity.class);
                    i.putExtra(AnagramActivity.SCRABBLE_INPUT, result.getText().trim());
                    startActivity(i);
                }
            }

            @Override
            public void onAbortRun(AnylineOcrError code, String message) {
                // Is called when no result was found for the current image.
                // E.g. if no text was found or the result is not valid.
            }
        });

        // disable the reporting if set to off in preferences
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                SettingsFragment.KEY_PREF_REPORTING_ON, true)) {
            // The reporting of results - including the photo of a scanned meter -
            // helps us in improving our product, and the customer experience.
            // However, if you wish to turn off this reporting feature, you can do it like this:
            scanView.setReportingEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //we use a postdelay for 'start scanning' to improve the user experience:
        //otherwise the scrabble result would be shown faster as the user realizes the scanning
        //process has already started
        scanView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    scanView.startScanning();
                }
            }
        }, 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanView.cancelScanning();
        scanView.releaseCameraInBackground();
    }

}
