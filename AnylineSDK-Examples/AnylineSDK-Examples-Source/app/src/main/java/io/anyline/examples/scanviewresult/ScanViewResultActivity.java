package io.anyline.examples.scanviewresult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ScanningConfigurationActivity;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.examples.util.Constant;

/**
 * Scan Vew Result Activity for the Result Screen
 */

public class ScanViewResultActivity extends ScanningConfigurationActivity {

    private String scanModule;
    private HashMap<String, String> result;
    private ScanViewResultAdapter scanResultAdapter;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView confirmationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_no_menu);
        getLayoutInflater().inflate(R.layout.activity_result_scan_view, (ViewGroup) findViewById(R.id.placeholder));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));

        imageView = (ImageView) findViewById(R.id.controll_image);
        confirmationButton = (TextView) findViewById(R.id.confirmation_button);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            result= (HashMap<String, String>) intent.getSerializableExtra(Constant.SCAN_RESULT_DATA);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                scanModule = extras.getString(Constant.SCAN_MODULE, "").trim();

                Bitmap bmp =  BitmapUtil.getBitmap(extras.getString(Constant.SCAN_FULL_PICTURE_PATH));
                imageView.setImageBitmap(bmp);
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
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return null;
    }

    private void setupScanResultView(){
        if(scanModule.equals(getResources().getString(R.string.title_mrz))){

            //for the specific insertion order it is needed a linkedHashMap reconstruct here
            //android transform via bundle transfer the linkedHashMap into a Hashmap
            LinkedHashMap<String, String> orderedHashMap = new LinkedHashMap();

            orderedHashMap.put(getResources().getString(R.string.mrz_given_names), result.get(getResources().getString(R.string.mrz_given_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_sur_names), result.get(getResources().getString(R.string.mrz_sur_names)));
            orderedHashMap.put(getResources().getString(R.string.mrz_sex), result.get(getResources().getString(R.string.mrz_sex)));
            orderedHashMap.put(getResources().getString(R.string.mrz_date_of_birthday), result.get(getResources().getString(R.string.mrz_date_of_birthday)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_type), result.get(getResources().getString(R.string.mrz_document_type)));
            orderedHashMap.put(getResources().getString(R.string.mrz_document_number), result.get(getResources().getString(R.string.mrz_document_number)));
            orderedHashMap.put(getResources().getString(R.string.mrz_expiration_date), result.get(getResources().getString(R.string.mrz_expiration_date)));
            orderedHashMap.put(getResources().getString(R.string.mrz_country_code), result.get(getResources().getString(R.string.mrz_country_code)));

            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), orderedHashMap);

        }else if(scanModule.equals(getResources().getString(R.string.title_driving_license))){
            LinkedHashMap<String, String> orderedHashMapDrivingLicense = new LinkedHashMap();

            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_sur_names), result.get(getResources().getString(R.string.driving_license_sur_names)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_given_names), result.get(getResources().getString(R.string.driving_license_given_names)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_DOB), result.get(getResources().getString(R.string.driving_license_DOB)));
            orderedHashMapDrivingLicense.put(getResources().getString(R.string.driving_license_document_code), result.get(getResources().getString(R.string.driving_license_document_code)));

            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), orderedHashMapDrivingLicense);
        }else{

            scanResultAdapter = new ScanViewResultAdapter(this.getBaseContext(), result);
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