/*
 * Anyline
 * DocumentScanViewUIActivity.java
 *
 * Copyright (c) 2019 Anyline GmbH. All rights reserved.
 *
 * Created by Gerhard S. at 2019-09-05
 */

package io.anyline.examples.document;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import io.anyline.examples.R;
import io.anyline.view.DocumentScanViewConfig;
import io.anyline.view.DocumentScanViewUI;
import io.anyline.view.ScanPage;


public class DocumentScanViewUIActivity extends AppCompatActivity {

    private io.anyline.view.DocumentScanViewUI documentScanViewUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_document_scan_view_ui);

        // license and configuration are required for initialization of the documentScanViewUI:
        String licenseKey = getIntent().getExtras().getString("License");
        String configJson = getIntent().getExtras().getString("Config");

        // init the documentScanViewUI from the layout file:
        documentScanViewUI = findViewById(R.id.document_scan_view_ui);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Document Scanner UI");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // initialize the documentScanViewConfig from a scan-view config file:
        DocumentScanViewConfig documentScanViewConfig = new DocumentScanViewConfig(this, "document_scan_view_config.json");

        documentScanViewUI.init(licenseKey, documentScanViewConfig, configJson, savedInstanceState);

        documentScanViewUI.setDocumentScanViewListener(new DocumentScanViewUI.DocumentScanViewListener() {

            @Override
            public void onSave(ArrayList<ScanPage> scannedPages) {
                documentScanViewUI.stopScanning();

                // pass a list of scanned pages to the calling activity:
                Intent data = new Intent();
                data.putExtra(DocScanUIMainActivity.RESULT_PAGES, scannedPages);
                DocumentScanViewUIActivity.this.setResult(DocScanUIMainActivity.RESULT_SWITCH, data);
                DocumentScanViewUIActivity.this.finish();
            }

            @Override
            public void onCancel() {
                DocumentScanViewUIActivity.this.finish();
            }
        });
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        // save state of activity before an activity is paused:
        savedInstanceState = documentScanViewUI.addSavedInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // is required, otherwise scanning will not happen:
        documentScanViewUI.startScanning();
    }


    @Override
    protected void onPause() {
        super.onPause();
        documentScanViewUI.stopScanning();
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
        // back button collapses bottom sheet if this is open
        if (documentScanViewUI.isBottomSheetExpanded()) {
            documentScanViewUI.collapseBottomSheet();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
        }
    }
}
