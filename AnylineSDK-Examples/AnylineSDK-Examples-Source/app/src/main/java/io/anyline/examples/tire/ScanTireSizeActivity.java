
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
import io.anyline.plugin.tire.TireScanDetailedResult;
import io.anyline.plugin.tire.TireScanResult;
import io.anyline.plugin.tire.TireScanViewPlugin;
import io.anyline.plugin.tire.TireSizeConfig;
import io.anyline.plugin.tire.TireSizeScanResult;
import io.anyline.view.ScanView;

public class ScanTireSizeActivity extends ScanActivity {

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
        TireSizeConfig tireSizeConfig = new TireSizeConfig();

        TireScanViewPlugin scanViewPlugin = new TireScanViewPlugin(this, tireSizeConfig, scanView.getScanViewPluginConfig(), "TIRE_SIZE");

        scanViewPlugin.addScanResultListener((ScanResultListener<TireScanResult>) result -> {

            // Cast to TireScanResult in order to get a simple result string
            TireScanResult tireScanResult = (TireScanResult) result;

            // simpleResult contains simply the result as a string
            String simpleResult = tireScanResult.getResult();


            // Cast to TireSizeScanResult in order to get detailed results
            TireSizeScanResult tireSizeScanResult = (TireSizeScanResult) result;

            // tireScanDetailedResult contains many more properties, e.g. a prettified result, prettified result with meta data, etc.
            TireScanDetailedResult tireScanDetailedResult = tireSizeScanResult.getResult();

            // Example: get prettified result with meta data
            String prettifiedResultWithMetaData = tireScanDetailedResult.getPrettifiedStringWithMeta().getText();

            // Example: get simple string result from TireScanDetailedResult
            String alsoSimpleResult = tireScanDetailedResult.getText().getText();

            // Example: get confidence for simple string result
            int simpleResultConfidence = tireScanDetailedResult.getText().getConfidence();


            String path = setupImagePath(result.getCutoutImage());
            startScanResultIntent(getResources().getString(R.string.tire_size), getResult(tireSizeScanResult.getResult()), path);
            setupScanProcessView(ScanTireSizeActivity.this, result, getScanModule());
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

    protected LinkedHashMap<String, String> getResult(TireScanDetailedResult result) {
        LinkedHashMap<String, String> resultMap = new LinkedHashMap();

        if (result.getPrettifiedString()!= null) {
            resultMap.put(
                    getString(R.string.text),
                    result.getPrettifiedString().getText()
            );
        }

        if (result.getWidth() != null) {
            resultMap.put(
                    getString(R.string.width),
                    result.getWidth().getText()
            );
        }

        if (result.getRatio() != null) {
            resultMap.put(
                    getString(R.string.ratio),
                    result.getRatio().getText()
            );
        }

        if (result.getConstruction() != null && !result.getConstruction().getText().equals("R)")) {
            resultMap.put(
                    getString(R.string.construction),
                    result.getConstruction().getText()
            );
        }

        if (result.getDiameter() != null) {
            resultMap.put(
                    getString(R.string.diameter),
                    result.getDiameter().getText()
            );
        }

        if (result.getLoadIndex() != null) {
            resultMap.put(
                    getString(R.string.load_index),
                    result.getLoadIndex().getText()
            );
        }

        if (result.getSpeedRating() != null) {
            resultMap.put(
                    getString(R.string.speed_rating),
                    result.getSpeedRating().getText()
            );
        }

        if (result.getPrettifiedStringWithMeta() != null) {
            resultMap.put(
                    getString(R.string.text_long),
                    result.getPrettifiedStringWithMeta().getText()
            );
        }

        if (result.getText().getText() != null) {
            resultMap.put(
                    getString(R.string.scanned_text),
                    result.getText().getText()
            );
        }

        if (result.toJson() != null) {
            resultMap.put(
                    getString(R.string.complete_json),
                    result.toJson()
            );
        }

        return resultMap;
    }
}

