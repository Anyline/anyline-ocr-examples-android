package io.anyline.examples.ocr.tin;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;

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

        OcrScanViewPlugin scanViewPlugin = getScanViewPluginWithScanMode(AnylineTINConfig.ScanMode.UNIVERSAL);

        scanView.setScanViewPlugin(scanViewPlugin);
    }

    @NotNull
    private OcrScanViewPlugin getScanViewPluginWithScanMode(AnylineTINConfig.ScanMode scanMode) {
        AnylineTINConfig tinConfig = new AnylineTINConfig();
        tinConfig.setScanMode(scanMode);

        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(this, tinConfig, scanView.getScanViewPluginConfig(), "TIN");

        scanViewPlugin.addScanResultListener((ScanResultListener<OcrScanResult>) result -> {
            String path = setupImagePath(result.getCutoutImage());
            startScanResultIntent(getResources().getString(R.string.tin), getTINResult(result.getResult()), path);
            setupScanProcessView(ScanTINActivity.this, result, getScanModule());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_tin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
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

    protected HashMap<String, String> getTINResult(String result) {
        HashMap<String, String> tinResult = new HashMap();
        tinResult.put(getResources().getString(R.string.tin), result.isEmpty() ? getResources().getString(R.string.not_available) : result);
        return tinResult;
    }
}