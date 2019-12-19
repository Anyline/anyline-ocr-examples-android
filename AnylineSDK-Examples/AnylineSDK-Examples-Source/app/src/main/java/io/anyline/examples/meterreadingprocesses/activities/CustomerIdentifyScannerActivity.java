package io.anyline.examples.meterreadingprocesses.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.nineyards.anyline.models.AnylineImage;
import io.anyline.examples.R;
import io.anyline.examples.database.DataBaseProcessesAdapter;
import io.anyline.examples.meterreadingprocesses.fragments.CustomerFragment;
import io.anyline.examples.model.Customer;
import io.anyline.examples.model.Reading;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.barcode.BarcodeScanPlugin;
import io.anyline.plugin.barcode.BarcodeScanViewPlugin;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanPlugin;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;
import io.anyline.view.SerialScanViewComposite;

public class CustomerIdentifyScannerActivity extends AppCompatActivity {

    private int mOrderId;

    protected View mBottomContainer;
    protected ScanView mEnergyScanView;
    private androidx.appcompat.widget.Toolbar toolbar;

    private Customer mCurrentCustomer;
    private Reading mCurrentReading;
    private boolean isWorkforceProcess;
    private SerialScanViewComposite composite;
    private BarcodeScanViewPlugin barcodeSVP;
    private MeterScanViewPlugin meterSVP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_scanner);

        toolbar = findViewById(R.id.toolbar);
        //toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light));
        toolbar.setTitle("Barcode");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBottomContainer = findViewById(R.id.fragment_container);
        mEnergyScanView = findViewById(R.id.energy_scan_view);

        composite = new SerialScanViewComposite("SERIAL");

        ScanViewPluginConfig barcodeScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "barcode_view_config.json");
        BarcodeScanPlugin barcodeScanPlugin = new BarcodeScanPlugin(this, "barcodePlugin", getString(R.string.anyline_license_key));
        barcodeSVP = new BarcodeScanViewPlugin(this, barcodeScanPlugin, barcodeScanViewPluginConfig);


        ScanViewPluginConfig meterScanViewConfig = new ScanViewPluginConfig(getApplicationContext(), "energy_view_config.json");
        MeterScanPlugin meterScanPlugin = new MeterScanPlugin(this, "meterPlugin", getString(R.string.anyline_license_key));
        meterSVP = new MeterScanViewPlugin(this, meterScanPlugin, meterScanViewConfig);
        meterSVP.setScanMode(MeterScanMode.AUTO_ANALOG_DIGITAL_METER);

        barcodeSVP.addScanResultListener(new ScanResultListener() {
            @Override
            public void onResult(ScanResult energyResult) {

                toolbar.setTitle("Meter Reading");
                try {
                    String result = energyResult.getResult().toString();

                    if (mCurrentReading != null) {
                        try {
                            handleMeterScan(result, energyResult.getFullImage(), energyResult.getCutoutImage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    if (mCurrentCustomer == null) {
                        DataBaseProcessesAdapter dataBase = new DataBaseProcessesAdapter(CustomerIdentifyScannerActivity.this);
                        if (isWorkforceProcess) {

                            if (dataBase.getCustomerMeterIdListByOrderId(mOrderId) != null) {
                                for (String meterId : dataBase.getCustomerMeterIdListByOrderId(mOrderId)) {
                                    if (result.contains(meterId)) {
                                        mCurrentCustomer = dataBase.getCustomersByOrderIdAndMeterId(meterId, mOrderId);
                                        break;
                                    }
                                }
                            }

                        } else {

                            if (dataBase.getCustomerMeterIdList() != null) {
                                for (String meterId : dataBase.getCustomerMeterIdList()) {
                                    if (result.contains(meterId)) {
                                        mCurrentCustomer = dataBase.getCustomersByMeterId(meterId);
                                        break;
                                    }
                                }
                            }

                            if (mCurrentCustomer != null) {
                                mCurrentReading = dataBase.getLastReadingByCustomerId(mCurrentCustomer.getId());
                            }
                        }

                        if (mCurrentCustomer == null) {
                            new AlertDialog.Builder(CustomerIdentifyScannerActivity.this)
                                    .setMessage(getString(R.string.customer_not_found))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, null)
                                    .show();

                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    //if (!mEnergyScanView.getScanViewPlugin().isRunning()) {
                                    mEnergyScanView.start();
                                    //}
                                }
                            }, 3000);
                        }

                        processReading();
                    }
                } catch (Exception ex) {
                }
            }
        });


        meterSVP.addScanResultListener(new ScanResultListener() {
            @Override
            public void onResult(ScanResult energyResult) {
                handleMeterScan(energyResult.getResult().toString(), energyResult.getFullImage(), energyResult.getCutoutImage());
            }
        });

        composite.add(barcodeSVP);
        composite.add(meterSVP);

        mEnergyScanView.setScanViewPlugin(composite);
    }


    //method used for insert data in a specific format in the database
    private String convertDateToString(Date date) {
        String dateString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            dateString = simpleDateFormat.format(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateString;
    }

    private void handleMeterScan(String readingResult, AnylineImage fullImage, AnylineImage cutoutImage) {


        try {
            mCurrentReading.setFullImageLocalPath(setupImagePath(fullImage, false));
            mCurrentReading.setCutoutImageLocalPath(setupImagePath(cutoutImage, true));
            mCurrentReading.setNewReading(readingResult);
            mCurrentReading.setNewReadingDate(convertDateToString(new Date()));
            mCurrentCustomer.setReading(mCurrentReading);
            mBottomContainer.setVisibility(View.GONE);

            startResultActivityIntent();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void processReading() {

        if (mCurrentReading == null) {
            return;
        }
        setTitle(R.string.category_energy);

        //make visible the customer detail container and setup data
        mBottomContainer.setVisibility(View.VISIBLE);
        setupCustomerDetails();
    }

    private void startResultActivityIntent() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.KEY_CUSTOMER, mCurrentCustomer);
        if (isWorkforceProcess) {
            intent.putExtra(ResultActivity.KEY_MODE_SAVE, true);
            startActivityForResult(intent, ResultActivity.ACTION_SAVE_RESULT);
        } else {
            intent.putExtra(ResultActivity.KEY_MODE_SEND, true);
            startActivityForResult(intent, ResultActivity.ACTION_SEND_RESULT);
        }
        overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
    }

    private String setupImagePath(AnylineImage image, boolean isCutout) {
        Date date = new Date();
        String imagePath = "";
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(date);
            if (this.getExternalFilesDir(null) != null) {

                if (isCutout) {
                    imagePath = this.getExternalFilesDir(null)
                            .toString() + "/cutouts/" + "CUTOUT_IMAGE_" + mCurrentCustomer
                            .getMeterId() + "_" + timeStamp;
                } else {

                    imagePath = this
                            .getExternalFilesDir(null)
                            .toString() + "/readings/" + "READING_IMAGE_" + mCurrentCustomer.getMeterId() + "_" + timeStamp;
                }
            } else if (this.getFilesDir() != null) {

                if (isCutout) {
                    imagePath = this.getFilesDir()
                            .toString() + "/cutouts/" + "CUTOUT_IMAGE_" + mCurrentCustomer
                            .getMeterId() + "_" + timeStamp;
                } else {

                    imagePath = this.getFilesDir().toString() + "/readings/" + "READING_IMAGE_" + mCurrentCustomer.getMeterId() + "_" + timeStamp;
                }
            }
            File fullFile = new File(imagePath);
            //create the directory
            fullFile.mkdirs();
            image.save(fullFile, 100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return imagePath;
    }


    private void setupCustomerDetails() {
        CustomerFragment customerFragment = new CustomerFragment();
        Bundle args = new Bundle();
        args.putBoolean(CustomerFragment.KEY_PARTIAL, true);
        args.putParcelable(CustomerFragment.KEY_CUSTOMER, mCurrentCustomer);
        customerFragment.setArguments(args);

        //start the fragment transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, customerFragment);
        transaction.commit();
    }

    protected void reScanScreen() {
        setTitle(R.string.intercom_barcode);
        mCurrentReading = null;
        mCurrentCustomer = null;
        mBottomContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ResultActivity.ACTION_SEND_RESULT && resultCode == RESULT_OK) {
            mCurrentReading = null;
            finish();
        } else if (requestCode == ResultActivity.ACTION_SEND_RESULT && resultCode == ResultActivity.ACTION_RESCAN_RESULT) {
            mCurrentReading = null;
        }
        reScanScreen();
    }

    @Override
    protected void onPause() {
        mEnergyScanView.stop();
        mEnergyScanView.releaseCameraInBackground();
        reScanScreen();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEnergyScanView.start();
        if (composite.getId().equals(barcodeSVP.getId())) {
            toolbar.setTitle("Barcode");
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
