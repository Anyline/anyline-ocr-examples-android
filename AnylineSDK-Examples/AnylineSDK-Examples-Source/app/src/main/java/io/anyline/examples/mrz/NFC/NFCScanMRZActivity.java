package io.anyline.examples.mrz.NFC;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;

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
        toolbar.setTitle("NFC");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scanView = findViewById(R.id.scan_view);

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

        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if(adapter == null ){
            new MaterialAlertDialogBuilder(this, R.style.Theme_MyApp_Dialog_Alert)
                    .setTitle("NFC error")
                    .setMessage("NFC passport reading is not supported on this device.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }).show();
        } else if(!adapter.isEnabled()){
            new MaterialAlertDialogBuilder(this, R.style.Theme_MyApp_Dialog_Alert)
                    .setTitle("NFC error")
                    .setMessage("You first have to enable NFC in your options!")
                    .setPositiveButton("NFC SETTINGS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }).show();
        } else {
            init();
        }

        if(scanView.getScanViewPlugin() != null){
            scanView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(scanView.getScanViewPlugin() != null){
            scanView.stop();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }
}
