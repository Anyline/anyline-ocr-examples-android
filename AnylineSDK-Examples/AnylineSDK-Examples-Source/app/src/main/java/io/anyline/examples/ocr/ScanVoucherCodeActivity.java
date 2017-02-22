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
import io.anyline.examples.ocr.result.VoucherCodeResultView;

public class ScanVoucherCodeActivity extends AppCompatActivity {

    private static final String TAG = ScanVoucherCodeActivity.class.getSimpleName();
    private AnylineOcrScanView scanView;
    private VoucherCodeResultView voucherCodeResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_anyline_ocr);

        addVoucherCodeResultView();

        String lic = getString(R.string.anyline_license_key);
        scanView = (AnylineOcrScanView) findViewById(R.id.scan_view);

        scanView.copyTrainedData("tessdata/anyline_capitals.traineddata", "cee65c74833eb85e3c31e213b25e72a2");

        // see ScanIbanActivity for a more detailed description
        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setTesseractLanguages("anyline_capitals");
        anylineOcrConfig.setCharWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        anylineOcrConfig.setValidationRegex("[A-Z0-9]{8}$");
        anylineOcrConfig.setMinCharHeight(45);
        anylineOcrConfig.setMaxCharHeight(85);
        anylineOcrConfig.setMinConfidence(85);
        anylineOcrConfig.setScanMode(AnylineOcrConfig.ScanMode.GRID);
        anylineOcrConfig.setCharCountX(8);
        anylineOcrConfig.setCharCountY(1);
        anylineOcrConfig.setCharPaddingXFactor(0.5);
        anylineOcrConfig.setIsBrightTextOnDark(true);

        scanView.setAnylineOcrConfig(anylineOcrConfig);

        scanView.setConfig(new AnylineViewConfig(this, "voucher_code_view_config.json"));

        scanView.initAnyline(lic, new AnylineOcrListener() {
            @Override
            public void onReport(String identifier, Object value) {
            }

            @Override
            public boolean onTextOutlineDetected(List<PointF> list) {
                return false;
            }

            @Override
            public void onResult(AnylineOcrResult result) {
                voucherCodeResultView.setResult(result.getText());
                voucherCodeResultView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAbortRun(AnylineOcrError code, String message) {
            }
        });
        // disable the reporting if set to off in preferences
        scanView.setReportingEnabled(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment
                .KEY_PREF_REPORTING_ON, true));
        voucherCodeResultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voucherCodeResultView.setVisibility(View.INVISIBLE);
                if (!scanView.isRunning()) {
                    scanView.startScanning();
                }
            }
        });
    }

    private void addVoucherCodeResultView() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        voucherCodeResultView = new VoucherCodeResultView(this);
        voucherCodeResultView.setVisibility(View.INVISIBLE);

        mainLayout.addView(voucherCodeResultView, params);
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
        if (voucherCodeResultView.getVisibility() == View.VISIBLE) {
            voucherCodeResultView.setVisibility(View.INVISIBLE);
            if (!scanView.isRunning()) {
                scanView.startScanning();
            }
        } else {
            super.onBackPressed();
        }

    }
}
