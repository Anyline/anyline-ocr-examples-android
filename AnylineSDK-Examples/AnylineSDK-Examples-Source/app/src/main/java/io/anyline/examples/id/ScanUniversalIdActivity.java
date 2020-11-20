package io.anyline.examples.id;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import java.util.HashMap;

import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ScanRunSkippedListener;
import io.anyline.plugin.ScanRunSkippedReason;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.plugin.id.Identification;
import io.anyline.view.ScanView;

public class ScanUniversalIdActivity extends ScanActivity implements ScanRunSkippedListener {

    ScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, findViewById(R.id.scan_view_placeholder));
        init();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("Universal ID");
        }
    }

    void init(){
        scanView = findViewById(R.id.scan_view);
        scanView.init("universal_id_view_config.json");
        IdScanViewPlugin idScanViewPlugin = (IdScanViewPlugin) scanView.getScanViewPlugin();
        scanView.getScanViewPlugin().addScanRunSkippedListener(this);


        idScanViewPlugin.addScanResultListener((ScanResultListener<ScanResult<ID>>) idScanResult -> {
            Identification identification = (Identification) idScanResult.getResult();
            HashMap<String, String> data = (HashMap<String, String>) identification.getResultData();
            String imagePath = setupImagePath(idScanResult.getCutoutImage());
            Intent intent = new Intent(ScanUniversalIdActivity.this, ScanUniversalIdResultActivity.class);
            intent.putExtra("resultData", data);
            intent.putExtra("scan_full_picture_path", imagePath);
            startActivity(intent);

            setupScanProcessView(ScanUniversalIdActivity.this, idScanResult, getScanModule());

        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        scanView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        scanView.stop();
    }

    @Override
    protected ScanView getScanView() {
        return scanView;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.UNIVERSAL_ID;
    }

    @Override
    public void onRunSkipped(ScanRunSkippedReason reason) {
        if(reason.getCode() == 5555){
            Toast.makeText(this, "This version doesn't support all document types yet. Stay tuned for more.", Toast.LENGTH_SHORT).show();
        }
    }
}
