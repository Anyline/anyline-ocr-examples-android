
package io.anyline.examples.tire;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.tire.CommercialTireIdConfig;
import io.anyline.plugin.tire.TireScanResult;
import io.anyline.plugin.tire.TireScanViewPlugin;
import io.anyline.view.ScanView;

public class ScanCommercialTireIdActivity extends ScanActivity {

    private ScanView scanView;
    private int orientation;

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
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, findViewById(R.id.scan_view_placeholder));

        scanView = findViewById(R.id.scan_view);
        orientation = this.getResources().getConfiguration().orientation;

        setUpOrientationButton();
        setUpScanView();
    }

    private void setUpScanView() {
        scanView.setScanConfig("tin_view_config.json");

        TireScanViewPlugin scanViewPlugin = getScanViewPlugin();

        scanView.setScanViewPlugin(scanViewPlugin);
    }

    @NotNull
    private TireScanViewPlugin getScanViewPlugin() {
        CommercialTireIdConfig commercialTireIdConfig = new CommercialTireIdConfig();

        TireScanViewPlugin scanViewPlugin = new TireScanViewPlugin(this, commercialTireIdConfig, scanView.getScanViewPluginConfig(), "COMMERCIAL_TIRE_ID");

        scanViewPlugin.addScanResultListener((ScanResultListener<TireScanResult>) result -> {
            String path = setupImagePath(result.getCutoutImage());
            startScanResultIntent(getResources().getString(R.string.commercial_tire_id), getResult(result.getResult()), path);
            setupScanProcessView(ScanCommercialTireIdActivity.this, result, getScanModule());
        });
        return scanViewPlugin;
    }

    private void setUpOrientationButton() {
        Button btn = findViewById(R.id.screen_orientation_button);
        btn.setVisibility(View.VISIBLE);

        btn.setOnClickListener(v -> {
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    @Override
    protected void onStop() {
        super.onStop();
        if (orientation != 0 && orientation != 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(orientation);
        }
    }

    protected LinkedHashMap<String, String> getResult(String result) {
        LinkedHashMap<String, String> r = new LinkedHashMap();
        r.put(getResources().getString(R.string.commercial_tire_id), result.isEmpty() ? getResources().getString(R.string.not_available) : result);
        return r;
    }
}

