package io.anyline.examples.mrz.NFC;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import io.anyline.examples.R;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.plugin.id.MrzConfig;
import io.anyline.plugin.id.MrzFieldConfidences;
import io.anyline.plugin.id.MrzIdentification;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;

public class NFCScanMRZActivity extends AppCompatActivity {


    private ScanView scanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_mrz_scan);
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("NFC SCANNER");
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scanView = findViewById(R.id.scan_view);
        init();
    }

    private void init() {
        MrzConfig mrzConfig = new MrzConfig();
        MrzFieldConfidences mrzFieldConfidences = new MrzFieldConfidences();
        mrzFieldConfidences.setDateOfBirth(60);
        mrzFieldConfidences.setDocumentNumber(80);
        mrzFieldConfidences.setDateOfExpiry(80);
        mrzConfig.setIdMinFieldConfidences(mrzFieldConfidences);

        ScanViewPluginConfig mrzScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "mrz_view_config.json");

        IdScanViewPlugin scanViewPlugin = new IdScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), mrzScanViewPluginConfig, mrzConfig);

        scanViewPlugin.addScanResultListener(new ScanResultListener<ScanResult<ID>>() {
            @Override
            public void onResult(ScanResult<ID> idScanResult) {
                MrzIdentification identification = (MrzIdentification) idScanResult.getResult();

                String passportNumber = identification.getDocumentNumber();
                String dateOfExpiry = identification.getDateOfExpiry();
                String birthDate = identification.getDateOfBirth();

                Intent intent = new Intent(NFCScanMRZActivity.this, NFCScanActivity.class);
                intent.putExtra("pn", passportNumber);
                intent.putExtra("de", dateOfExpiry);
                intent.putExtra("bd", birthDate);

                startActivity(intent);
            }
        });


        scanView.setScanViewPlugin(scanViewPlugin);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
