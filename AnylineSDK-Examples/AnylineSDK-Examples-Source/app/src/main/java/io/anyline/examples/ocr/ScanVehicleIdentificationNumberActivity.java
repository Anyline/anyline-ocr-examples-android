
package io.anyline.examples.ocr;
import android.os.Bundle;
import android.view.ViewGroup;
import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineVINConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanVehicleIdentificationNumberActivity extends ScanActivity implements AnylineDebugListener {

    private static final String TAG = ScanVehicleIdentificationNumberActivity.class.getSimpleName();
    private ScanView scanView;
    private io.anyline.view.AnylineViewConfig anylineViewConfig;

    @Override
    protected ScanView getScanView() {
        return null;
    }
@Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.VEHICLE_IDENTIFICATION_NUMBER;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        init();

    }

    void init() {
        scanView = (ScanView) findViewById(R.id.scan_view);
        // create new Anyline OCR config
        AnylineVINConfig anylineVINConfig = new AnylineVINConfig();

        //init the scanViewPlugin config
        scanView.setScanConfig("vin_view_config.json");
        //init the scan view
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), anylineVINConfig, scanView.getScanViewPluginConfig(), "OCR");
        scanView.setScanViewPlugin(scanViewPlugin);

        scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
            @Override
            public void onResult(OcrScanResult result) {
                String path = setupImagePath(result.getCutoutImage());

                startScanResultIntent(getResources().getString(R.string.vin_reading_result), getVINResult(result.getResult().toString()), path);
                setupScanProcessView(ScanVehicleIdentificationNumberActivity.this, result, getScanModule());
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
    }

    protected HashMap<String, String> getVINResult(String result) {

        HashMap<String, String> serialNumberResult = new HashMap();

        serialNumberResult.put(getResources().getString(R.string.vin_reading_result), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );

        return serialNumberResult;
    }

    @Override
    public void onDebug(String s, Object o) {

    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {
    }

}