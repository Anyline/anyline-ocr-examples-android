package io.anyline.examples.ocr;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineTINConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;

public class ScanTINActivity extends ScanActivity {
    private static final String TAG = ScanTINActivity.class.getSimpleName();
    private ScanView scanView;

    @Override
    protected ScanView getScanView() {
        return null;
    }
@Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.TIN;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        scanView = findViewById(R.id.scan_view);

        AnylineTINConfig tinConfig = new AnylineTINConfig();
        tinConfig.setScanMode(AnylineTINConfig.TINScanMode.FLEXIBLE);
        scanView.setScanConfig("tin_view_config.json");
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(this, getString(R.string.anyline_license_key), tinConfig, scanView.getScanViewPluginConfig(), "TIN");

        scanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {
                String path = setupImagePath(result.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.tin), getTINResult(result.getResult()), path);
                setupScanProcessView(ScanTINActivity.this, result, getScanModule());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanView.stop();
        scanView.releaseCameraInBackground();
    }

    protected HashMap<String, String> getTINResult (String result) {

        HashMap<String, String> tinResult = new HashMap();

        tinResult.put(getResources().getString(R.string.tin), result.isEmpty() ? getResources().getString(R.string.not_available) : result);

        return tinResult;
    }

}

