package io.anyline.examples.scanviewresult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private String scanModule;
    private HashMap<String, String> result;
    // private ScanViewResultAdapter scanResultAdapter;
    private BaseGridAdapter scanResultAdapter;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView confirmationButton;
    private TextView faceImageCaption;
    private ImageView faceImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan_view);
        recyclerView = (RecyclerView) findViewById(R.id.rv_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        imageView = (ImageView) findViewById(R.id.control_image);
        faceImageCaption = findViewById(R.id.textFaceImage);
        faceImageView = findViewById(R.id.face_image);
        confirmationButton = (TextView) findViewById(R.id.confirmation_button);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
          actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            result = (HashMap<String, String>) intent.getSerializableExtra(Constant.SCAN_RESULT_DATA);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                scanModule = extras.getString(Constant.SCAN_MODULE, "").trim();

                Bitmap bmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
                imageView.setImageBitmap(bmp);
                Bitmap faceBmp = BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FACE_PICTURE_PATH));
                if (faceBmp != null) {
                    faceImageCaption.setVisibility(View.VISIBLE);
                    faceImageView.setVisibility(View.VISIBLE);
                    faceImageView.setImageBitmap(faceBmp);
                }
            }
        } else {
            scanModule = savedInstanceState.getString(Constant.SCAN_MODULE);
        }


        setupScanResultView();

        recyclerView.setAdapter(scanResultAdapter);
        confirmationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();

            }
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

            orderedHashMap.put("HEADER_MRZ", getResources().getString(R.string.mrz_header));
            orderedHashMap.put(getResources().getString(R.string.mrz_given_names), result.get(getResources().getString(R.string.mrz_given_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_sur_names), result.get(getResources().getString(R.string.mrz_sur_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_sex), result.get(getResources().getString(R.string.mrz_sex)));
            orderedHashMap.put(getResources().getString(R.string.mrz_date_of_birthday), result.get(getResources().getString(R.string.mrz_date_of_birthday)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_type), result.get(getResources().getString(R.string.mrz_document_type)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_number), result.get(getResources().getString(R.string.mrz_document_number)));
            orderedHashMap.put(getResources().getString(R.string.mrz_expiration_date), result.get(getResources().getString(R.string.mrz_expiration_date)));
            orderedHashMap.put(getResources().getString(R.string.mrz_country_code), result.get(getResources().getString(R.string.mrz_country_code)));
            if (result.get(getResources().getString(R.string.personal_number)) != null) {
                orderedHashMap.put(getResources().getString(R.string.personal_number), result.get(getResources().getString(R.string.personal_number)));
            }

            if(result.get(getResources().getString(R.string.mrz_viz_sur_names)) != null ||
                    result.get(getResources().getString(R.string.mrz_viz_given_names)) != null ||
                    result.get(getResources().getString(R.string.mrz_viz_dob)) != null ||
                    result.get(getResources().getString(R.string.mrz_viz_date_of_expiry)) != null ||
                    result.get(getResources().getString(R.string.mrz_viz_issue_date)) != null ||
                    result.get(getResources().getString(R.string.mrz_viz_address)) != null)
                orderedHashMap.put("HEADER", getResources().getString(R.string.mrz_VIZ_header));
            if (result.get(getResources().getString(R.string.mrz_viz_sur_names)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_sur_names), result.get(getResources().getString(R.string.mrz_viz_sur_names)));
            }
            if (result.get(getResources().getString(R.string.mrz_viz_given_names)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_given_names), result.get(getResources().getString(R.string.mrz_viz_given_names)));
            }
            if (result.get(getResources().getString(R.string.mrz_viz_dob)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_dob), result.get(getResources().getString(R.string.mrz_viz_dob)));
            }
            if (result.get(getResources().getString(R.string.mrz_viz_date_of_expiry)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_date_of_expiry), result.get(getResources().getString(R.string.mrz_viz_date_of_expiry)));
            }
            if (result.get(getResources().getString(R.string.mrz_viz_issue_date)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_issue_date), result.get(getResources().getString(R.string.mrz_viz_issue_date)));
            }
            if (result.get(getResources().getString(R.string.mrz_viz_address)) != null) {
                orderedHashMap.put(getResources().getString(R.string.mrz_viz_address), result.get(getResources().getString(R.string.mrz_viz_address)));
            }

            scanResultAdapter = new BaseGridAdapter(this.getApplicationContext(), orderedHashMap);
            //scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), orderedHashMap);

        } else if (scanModule.equals(getResources().getString(R.string.title_driving_license))) {
            LinkedHashMap<String, String> orderedHashMapDrivingLicense = new LinkedHashMap();

            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_sur_names), result.get(getResources().getString(R.string.driving_license_sur_names)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_given_names), result.get(getResources().getString(R.string.driving_license_given_names)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_DOB), result.get(getResources().getString(R.string.driving_license_DOB)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_document_code), result.get(getResources().getString(R.string.driving_license_document_code)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_authority), result.get(getResources().getString(R.string.driving_license_authority)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_expiring_date), result.get(getResources().getString(R.string.driving_license_expiring_date)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_issuing_date), result.get(getResources().getString(R.string.driving_license_issuing_date)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_categories), result.get(getResources().getString(R.string.driving_license_categories)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_POB), result.get(getResources().getString(R.string.driving_license_POB)));

            scanResultAdapter = new BaseGridAdapter(this.getBaseContext(), orderedHashMapDrivingLicense);

        } else if (scanModule.equals(getResources().getString(R.string.title_german_id_front))) {
            LinkedHashMap<String, String> orderedHashMapGermanIdFront = new LinkedHashMap();

            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_surnames), result.get(getResources().getString(R.string.german_id_front_surnames)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_given_names), result.get(getResources().getString(R.string.german_id_front_given_names)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_DOB), result.get(getResources().getString(R.string.german_id_front_DOB)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_document_nr), result.get(getResources().getString(R.string.german_id_front_document_nr)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_nationality), result.get(getResources().getString(R.string.german_id_front_nationality)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_expiring_date), result.get(getResources().getString(R.string.german_id_front_expiring_date)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_can), result.get(getResources().getString(R.string.german_id_front_can)));
            orderedHashMapGermanIdFront.put(getResources().getString(R.string.german_id_front_POB), result.get(getResources().getString(R.string.german_id_front_POB)));

            scanResultAdapter = new BaseGridAdapter(this.getBaseContext(), orderedHashMapGermanIdFront);

        } else if (scanModule.equals(getResources().getString(R.string.category_energy))) {
            LinkedHashMap<String, String> orderedHashMapEnergy = new LinkedHashMap();

            orderedHashMapEnergy.put(getResources().getString(R.string.reading_result), result.get(getResources().getString(R.string.reading_result)));
            orderedHashMapEnergy.put(getResources().getString(R.string.barcode), result.get(getResources().getString(R.string.barcode)));

            scanResultAdapter = new BaseGridAdapter(this.getBaseContext(), orderedHashMapEnergy);
        }  else {

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