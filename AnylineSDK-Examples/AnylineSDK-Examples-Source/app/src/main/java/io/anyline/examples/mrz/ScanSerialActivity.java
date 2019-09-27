package io.anyline.examples.mrz;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collection;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.view.AbstractScanViewPluginComposite;
import io.anyline.view.ScanView;
import io.anyline.view.SerialScanViewComposite;

public class ScanSerialActivity extends ScanActivity {
    private static final String TAG = ScanSerialActivity.class.getSimpleName();
    private ScanView scanView;


    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
                .scan_view_placeholder));

        String lic = getString(R.string.anyline_license_key);
        scanView = findViewById(R.id.scan_view);

        scanView.init("serial_scan_view_config.json", lic);


        AbstractScanViewPluginComposite composite = (SerialScanViewComposite) scanView.getScanViewPlugin();


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
