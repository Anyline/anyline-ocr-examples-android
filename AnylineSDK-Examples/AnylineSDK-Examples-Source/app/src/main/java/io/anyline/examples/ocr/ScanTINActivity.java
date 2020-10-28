package io.anyline.examples.ocr;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

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
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        scanView = findViewById(R.id.scan_view);
        orientation = this.getResources().getConfiguration().orientation;


        Button btn = (Button) findViewById(R.id.screen_orientation_button);
        btn.setVisibility(View.VISIBLE);


        AnylineTINConfig tinConfig = new AnylineTINConfig();
        tinConfig.setScanMode(AnylineTINConfig.TINScanMode.FLEXIBLE);
        scanView.setScanConfig("tin_view_config.json");
        OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(this, getString(R.string.anyline_license_key), tinConfig, scanView.getScanViewPluginConfig(), "TIN");

        scanView.setScanViewPlugin(scanViewPlugin);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                }else {
                    orientation =ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

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
        //scanView.releaseCameraInBackground();
    }

    @Override
    protected void onStop() {

        super.onStop();
        if(orientation != 0 && orientation != 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(orientation);

        }

    }

    protected HashMap<String, String> getTINResult (String result) {

        HashMap<String, String> tinResult = new HashMap();

        tinResult.put(getResources().getString(R.string.tin), result.isEmpty() ? getResources().getString(R.string.not_available) : result);

        return tinResult;
    }

}

