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
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrResultListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.SettingsFragment;
import io.anyline.examples.ocr.result.BottlecapResultView;

public class ScanBottlecapActivity extends AppCompatActivity {

    private static final String TAG = ScanBottlecapActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;
    private BottlecapResultView bottlecapResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_anyline_ocr);

        addBottlecapResultView();

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        // see ScanRedBullCodeActivity for a more detailed description
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setLanguages("tessdata/bottlecap.traineddata");
        anylineOcrConfig.setCharWhitelist("123456789ABCDEFGHJKLMNPRSTUVWXYZ");
        anylineOcrConfig.setMinCharHeight(14);
        anylineOcrConfig.setMaxCharHeight(65);
        anylineOcrConfig.setMinConfidence(75);
        anylineOcrConfig.setValidationRegex("^[0-9A-Z]{3}\n[0-9A-Z]{3}\n[0-9A-Z]{3}");
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
        anylineOcrConfig.setCharCountX(3);
        anylineOcrConfig.setCharCountY(3);
        anylineOcrConfig.setCharPaddingXFactor(0.3);
        anylineOcrConfig.setCharPaddingYFactor(0.5);
        anylineOcrConfig.setIsBrightTextOnDark(true);
        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setConfig(new AnylineViewConfig(this, "bottlecap_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult anylineOcrResult) {
                bottlecapResultView.setResult(anylineOcrResult.getResult());
                bottlecapResultView.setVisibility(View.VISIBLE);
            }
        });

        // disable the reporting if set to off in preferences
        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));
        bottlecapResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottlecapResultView.setVisibility(View.INVISIBLE);
                if (!scanView.isRunning()) {
                    scanView.startScanning();
                }
            }
        });
    }

    private void addBottlecapResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        bottlecapResultView = new BottlecapResultView(this);
        bottlecapResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(bottlecapResultView, params);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(bottlecapResultView == null || bottlecapResultView.getVisibility() != View.VISIBLE){
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
        //close the result view on back press if it is open
        if (bottlecapResultView.getVisibility() == View.VISIBLE) {
            bottlecapResultView.setVisibility(View.INVISIBLE);
            if (!scanView.isRunning()) {
                scanView.startScanning();
            }
        } else {
            super.onBackPressed();
        }

    }

}
