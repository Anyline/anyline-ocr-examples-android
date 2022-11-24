package io.anyline.examples.scanviewresult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ScanningConfigurationActivity;
import io.anyline.examples.baseadapters.BaseGridAdapter;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.examples.util.Constant;
import io.anyline.view.ScanView;

/**
 * Scan Vew Result Activity for the Result Screen
 */

public class ScanViewResultActivity extends ScanningConfigurationActivity {

    private static final String BARCODE_RESULT = "BARCODE_RESULT";
    private static final String MULTI_METER_SCAN_RESULT = "MULTI_METER_SCAN_RESULT";
    private static final String SCAN_MODULE_MULTI_METER = "MULTI_METER";

    private String scanModule;
    private LinkedHashMap<String, String> result;
    // private ScanViewResultAdapter scanResultAdapter;
    private BaseGridAdapter scanResultAdapter;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private Button confirmationButton;
    private TextView faceImageCaption;
    private ImageView faceImageView;
    private ImageView controlImage2;
    private ImageView controlImage3;

    private String imagePath = null;
    private String imagePath2 = null;
    private String imagePath3 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan_view);
        recyclerView = findViewById(R.id.rv_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        imageView = findViewById(R.id.control_image);
        controlImage2 = findViewById(R.id.control_image2);
        controlImage3 = findViewById(R.id.control_image3);

        faceImageCaption = findViewById(R.id.textBackImage);
        faceImageView = findViewById(R.id.face_image);
        confirmationButton = findViewById(R.id.confirmation_button);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String scanResultDataString = (String) intent.getSerializableExtra(Constant.SCAN_RESULT_DATA);
            result = (LinkedHashMap<String, String>) new Gson().fromJson(scanResultDataString, LinkedHashMap.class);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                scanModule = extras.getString(Constant.SCAN_MODULE, "").trim();

                Bitmap bmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
                imageView.setImageBitmap(bmp);
                Bitmap faceBmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FACE_PICTURE_PATH));
                if (faceBmp != null) {
                    faceImageView.setVisibility(View.VISIBLE);
                    faceImageView.setImageBitmap(faceBmp);
                }
            }
        } else {
            scanModule = savedInstanceState.getString(Constant.SCAN_MODULE);
        }


        setupScanResultView();

        recyclerView.setAdapter(scanResultAdapter);
        confirmationButton.setOnClickListener(v -> {
            onBackPressed();
        });
        setupScanResult();
    }

    @Override
    protected ScanView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }

    private void setupScanResultView() {
        if (scanModule.equals(getResources().getString(R.string.title_mrz))) {

            //for the specific insertion order it is needed a linkedHashMap reconstruct here
            //android transform via bundle transfer the linkedHashMap into a Hashmap
            LinkedHashMap<String, String> orderedHashMap = new LinkedHashMap();
            orderedHashMap.put(getResources().getString(R.string.mrz_sur_names), result.get(getResources().getString(R.string.mrz_sur_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_given_names), result.get(getResources().getString(R.string.mrz_given_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_date_of_birthday), result.get(getResources().getString(R.string.mrz_date_of_birthday)));
            if (result.get(getResources().getString(R.string.mrz_viz_issue_date)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_issue_date), result.get(getResources().getString(R.string.mrz_viz_issue_date)));
            }
            orderedHashMap.put(getResources().getString(R.string.mrz_expiration_date), result.get(getResources().getString(R.string.mrz_expiration_date)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_number), result.get(getResources().getString(R.string.mrz_document_number)));
            orderedHashMap.put(getResources().getString(R.string.mrz_country_code), result.get(getResources().getString(R.string.mrz_country_code)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_type), result.get(getResources().getString(R.string.mrz_document_type)));
            if (result.get(getResources().getString(R.string.personal_number)) != null) {
                orderedHashMap.put(getResources().getString(R.string.personal_number), result.get(getResources().getString(R.string.personal_number)));
            }
            orderedHashMap.put(getResources().getString(R.string.mrz_sex), result.get(getResources().getString(R.string.mrz_sex)));
            if (result.get(getResources().getString(R.string.mrz_viz_address)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_address), result.get(getResources().getString(R.string.mrz_viz_address)));
            }

            scanResultAdapter = new BaseGridAdapter(this.getApplicationContext(), orderedHashMap);

        } else if (scanModule.equals(getResources().getString(R.string.category_energy))) {
            LinkedHashMap<String, String> orderedHashMapEnergy = new LinkedHashMap();

            orderedHashMapEnergy.put(getResources().getString(R.string.reading_result), result.get(getResources().getString(R.string.reading_result)));
            orderedHashMapEnergy.put(getResources().getString(R.string.barcode), result.get(getResources().getString(R.string.barcode)));

            scanResultAdapter = new BaseGridAdapter(this.getBaseContext(), orderedHashMapEnergy);
        } else if (scanModule.equals(getResources().getString(R.string.category_barcodes))) {
            LinkedHashMap<String, String> orderedHashMapEnergy = new LinkedHashMap();
            for (int i = 0; i < result.size() / 3; i++) {
                orderedHashMapEnergy.put("HEADER" + i, getString(R.string.category_barcodes) + " " + (i + 1));

                orderedHashMapEnergy.put(getResources().getString(R.string.barcode_result) + (i + 1), result.get(getResources().getString(R.string.barcode_result) + i));
                orderedHashMapEnergy.put(getResources().getString(R.string.barcode_format) + (i + 1), result.get(getResources().getString(R.string.barcode_format) + i));
            }
            scanResultAdapter = new BaseGridAdapter(this.getBaseContext(), orderedHashMapEnergy);
        } else {

            scanResultAdapter = new BaseGridAdapter(this.getBaseContext(), new LinkedHashMap<>(result));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.activity_close_translate);
    }
}
