package io.anyline.examples.mrz;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collection;

import io.anyline.examples.R;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginComposite;
import io.anyline.view.SerialScanViewComposite;

public class ScanSerialActivity extends AppCompatActivity {
    private static final String TAG = ScanSerialActivity.class.getSimpleName();
    private ScanView scanView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anyline_scan_view);

        String lic = getString(R.string.anyline_license_key);
        scanView = findViewById(R.id.scan_view);

        scanView.init("serial_scan_view_config.json", lic);


        ScanViewPluginComposite composite = (SerialScanViewComposite) scanView.getScanViewPlugin();


        composite.addScanResultListener(new ScanResultListener() {
            @Override
            public void onResult(ScanResult result) {
                // only triggered if all plugins reached a result
                StringBuilder sb = new StringBuilder();
                for (ScanResult subResult : (Collection<ScanResult>) result.getResult()) {
                    sb.append(subResult.getPluginId()).append(": ").append(subResult.getResult()).append("\n");
                }
                Toast.makeText(ScanSerialActivity.this, sb.toString(), Toast.LENGTH_LONG).show();

            }
        });

        scanView.setScanViewPlugin(composite);

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
}
