package io.anyline.examples.ocr;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineCattleTagConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;

public class ScanCowtagActivity extends ScanActivity {
    private static final String TAG = ScanSerialNumberActivity.class.getSimpleName();
    private ScanView scanView;

    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.VOUCHER;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        scanView = (ScanView) findViewById(R.id.scan_view);

        AnylineCattleTagConfig cattleTagConfig = new AnylineCattleTagConfig();
        //init the scanViewPlugin config
        // scanView.setScanViewConfig(new BaseScanViewConfig(this, "scrabble_view_config_new.json"));
        scanView.setScanConfig("cow_tag_view_config.json");
        //init the scan view
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), cattleTagConfig, scanView.getScanViewPluginConfig(), "OCR");

        scanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {
                String path = setupImagePath(result.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.title_cowtag), getSerialNumberResult(result.getResult().toString()), path);
                setupScanProcessView(ScanCowtagActivity.this, result, getScanModule());
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



    protected HashMap<String, String> getSerialNumberResult (String result) {

        HashMap<String, String> serialNumberResult = new HashMap();

        serialNumberResult.put(getResources().getString(R.string.title_cowtag), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );

        return serialNumberResult;
    }


}
