package io.anyline.examples.meterreadingprocesses.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.nineyards.anyline.models.AnylineImage;
import at.nineyards.anyline.modules.energy.EnergyResult;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.database.DataBaseProcessesAdapter;
import io.anyline.examples.meterreadingprocesses.fragments.CustomerFragment;
import io.anyline.examples.model.Customer;
import io.anyline.examples.model.Reading;

public class CustomerIdentifyScannerActivity extends MeterReadingProcessActivity {

    public static final String KEY_ORDER_ID = "key_order_id";
    public static final String IS_WORKFORCE_PROCESS = "is_workforce_process";

    private static final int mustSync = 0;
    private static final int completed = 1;

    private boolean isWorkforceProcess;
    private int mOrderId;

    private Customer mCurrentCustomer;
    private Reading mCurrentReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            mOrderId = getIntent().getIntExtra(KEY_ORDER_ID, -1);
            isWorkforceProcess = getIntent().getBooleanExtra(IS_WORKFORCE_PROCESS, false);
        }

        //if workforceProcess and orderId is not received stop the activity
        //nothing to do as no result was found out in the database
        if (mOrderId < 0 && isWorkforceProcess) {
            finish();
            return;
        }

        setMode(EnergyScanView.ScanMode.BARCODE);
        setShowModeControls(false);
    }

    @Override
    public void onResult(EnergyResult energyResult) {
        super.onResult(energyResult);
        try {
            String result = energyResult.getResult();

            if (mCurrentReading != null) {
                try {
                    handleMeterScan(result, energyResult.getFullImage(), energyResult.getCutoutImage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            if (mCurrentCustomer == null) {
                DataBaseProcessesAdapter dataBase = new DataBaseProcessesAdapter(this.getApplicationContext());
                if (isWorkforceProcess) {

                    if(dataBase.getCustomerMeterIdListByOrderId(mOrderId) != null){
                        for (String meterId : dataBase.getCustomerMeterIdListByOrderId(mOrderId)) {
                            if(result.contains(meterId)){
                                mCurrentCustomer = dataBase.getCustomersByOrderIdAndMeterId(meterId, mOrderId);
                                break;
                            }
                        }
                    }

                } else {

                    if(dataBase.getCustomerMeterIdList() != null){
                        for (String meterId : dataBase.getCustomerMeterIdList()) {
                            if(result.contains(meterId)){
                                mCurrentCustomer = dataBase.getCustomersByMeterId(meterId);
                                break;
                            }
                        }
                    }
                }
                if (mCurrentCustomer != null) {
                    mCurrentReading = dataBase.getLastReadingByCustomerId(mCurrentCustomer.getId());
                }
            }

            if (mCurrentCustomer == null) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.customer_not_found))
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, null)
                        .show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (!mEnergyScanView.isRunning()) {
                            mEnergyScanView.startScanning();
                        }
                    }
                }, 3000);
            }

            processReading();
        }catch (Exception ex){}
    }

    //method used for insert data in a specific format in the database
    private String convertDateToString(Date date)
    {
        String dateString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try{
            dateString = simpleDateFormat.format( date );
        }catch (Exception ex ){
            ex.printStackTrace();
        }
        return dateString;
    }

    private void handleMeterScan(String readingResult, AnylineImage fullImage, AnylineImage cutoutImage){


        try {
            mCurrentReading.setFullImageLocalPath(setupImagePath(fullImage, false));
            mCurrentReading.setCutoutImageLocalPath(setupImagePath(cutoutImage, true));
            mCurrentReading.setNewReading(readingResult);
            mCurrentReading.setNewReadingDate(convertDateToString(new Date()));
            mCurrentCustomer.setIsSynced(mustSync);
            mCurrentCustomer.setIsCompleted(completed);
            mCurrentCustomer.setReading(mCurrentReading);

            mBottomContainer.setVisibility(View.GONE);

            startResultActivityIntent();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void processReading() {

        if (mCurrentReading == null) {
            return;
        }

        setMode(EnergyScanView.ScanMode.AUTO_ANALOG_DIGITAL_METER);
        setTitle(R.string.category_energy);
        setScanning(true);

        //make visible the customer detail container and setup data
        mBottomContainer.setVisibility(View.VISIBLE);
        setupCustomerDetails();
    }

    private String setupImagePath(AnylineImage image, boolean isCutout){
        Date date = new Date();
        String imagePath = "";
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(date);
            if(this.getExternalFilesDir(null) != null) {

                if(isCutout) {
                    imagePath = this.getExternalFilesDir(null)
                                                  .toString() + "/cutouts/" + "CUTOUT_IMAGE_" + mCurrentCustomer
                                                  .getMeterId() + "_" + timeStamp;
                }else{

                    imagePath = this
                                        .getExternalFilesDir(null)
                                        .toString() + "/readings/" + "READING_IMAGE_" + mCurrentCustomer.getMeterId() + "_" + timeStamp;
                }
            }else if(this.getFilesDir() != null){

                if(isCutout) {
                    imagePath = this.getFilesDir()
                                    .toString() + "/cutouts/" + "CUTOUT_IMAGE_" + mCurrentCustomer
                                        .getMeterId() + "_" + timeStamp;
                }else{

                    imagePath = this.getFilesDir().toString() + "/readings/" + "READING_IMAGE_" + mCurrentCustomer.getMeterId() + "_" + timeStamp;
                }
            }
            File fullFile = new File(imagePath);
            //create the directory
            fullFile.mkdirs();
            image.save(fullFile, 100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return imagePath;
    }

    private void startResultActivityIntent(){
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.KEY_CUSTOMER, mCurrentCustomer);
        if(isWorkforceProcess){
            intent.putExtra(ResultActivity.KEY_MODE_SAVE, true);
            startActivityForResult(intent, ResultActivity.ACTION_SAVE_RESULT);
        }else {
            intent.putExtra(ResultActivity.KEY_MODE_SEND, true);
            startActivityForResult(intent, ResultActivity.ACTION_SEND_RESULT);
        }
        overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
    }

    private void setupCustomerDetails(){
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

    protected void reScanScreen(){
        mCurrentReading = null;
        mCurrentCustomer = null;
        setMode(EnergyScanView.ScanMode.BARCODE);
        setTitle(R.string.intercom_barcode);
        setShowModeControls(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ResultActivity.ACTION_SAVE_RESULT && resultCode == RESULT_OK) {
            reScanScreen();

        }else if (requestCode == ResultActivity.ACTION_SEND_RESULT && resultCode == RESULT_OK) {
            mCurrentReading = null;
            finish();
        }else if (requestCode == ResultActivity.ACTION_SEND_RESULT && resultCode == ResultActivity.ACTION_RESCAN_RESULT) {
            mCurrentReading = null;
            reScanScreen();
        }
        else {
            //processReading();
            mBottomContainer.setVisibility(View.VISIBLE);
        }
    }


}
