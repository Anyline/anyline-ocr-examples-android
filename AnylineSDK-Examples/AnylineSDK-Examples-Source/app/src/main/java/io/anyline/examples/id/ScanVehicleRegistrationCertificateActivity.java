package io.anyline.examples.id;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

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

public class ScanVehicleRegistrationCertificateActivity extends ScanActivity {

    ScanView scanView;
    private int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, findViewById(R.id.scan_view_placeholder));

        orientation = this.getResources().getConfiguration().orientation;

        setUpOrientationButton();
        init();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("Vehicle Registration Certificate");
        }
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

    void init(){
        scanView = findViewById(R.id.scan_view);
        scanView.init("vehicle_registration_certificate_view_config.json");
        IdScanViewPlugin idScanViewPlugin = (IdScanViewPlugin) scanView.getScanViewPlugin();

        idScanViewPlugin.addScanResultListener((ScanResultListener<ScanResult<ID>>) idScanResult -> {
            Identification identification = (Identification) idScanResult.getResult();
            HashMap<String, String> data = (HashMap<String, String>) identification.getResultData();
            String imagePath = setupImagePath(idScanResult.getCutoutImage());
            Intent intent = new Intent(ScanVehicleRegistrationCertificateActivity.this, ScanUniversalIdResultActivity.class);

            // convert linkedHashmap into two arrays as LinkedHashMap cannot pe passed from one activity to the other:
            Set<String> setKeys = data.keySet();
            String[] arrayKeys = setKeys.toArray(new String[setKeys.size()]);
            Collection<String> values = data.values();
            String[] arrayValues = values.toArray(new String[values.size()]);

            intent.putExtra("resultDataKeys", arrayKeys);
            intent.putExtra("resultDataValues", arrayValues);

//            intent.putExtra("resultData", data);
            intent.putExtra("scan_full_picture_path", imagePath);
            startActivity(intent);

            setupScanProcessView(ScanVehicleRegistrationCertificateActivity.this, idScanResult, getScanModule());

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
        return ScanModuleEnum.ScanModule.VEHICLE_REGISTRATION_CERTIFICATE;
    }
}
