/*
 * Anyline
 * ScanMrzActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.drivinglicense;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import at.nineyards.anyline.modules.ocr.AnylineOcrResult;
import at.nineyards.anyline.modules.ocr.AnylineOcrResultListener;
import at.nineyards.anyline.modules.ocr.AnylineOcrScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;

/**
 * Example Activity for the Anyline-DrivingLicense-Module.
 */
public class ScanDrivingLicenseActivity extends ScanActivity implements CameraOpenListener {

    private static final String TAG = ScanDrivingLicenseActivity.class.getSimpleName();
    private AnylineOcrScanView anylineOcrScanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_scan_driver_license, (ViewGroup) findViewById(R.id.scan_view_placeholder));

        anylineOcrScanView = (AnylineOcrScanView) findViewById(R.id.ocr_view);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        anylineOcrScanView.setCameraOpenListener(this);
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        anylineOcrScanView.setConfigFromAsset("driving_license_view_config.json");

        AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
        anylineOcrConfig.setCustomCmdFile("anyline_austrian_driving_license.ale");
        anylineOcrConfig.setLanguages("tessdata/eng_no_dict.traineddata", "tessdata/deu.traineddata");
        anylineOcrScanView.setAnylineOcrConfig(anylineOcrConfig);

        // initialize Anyline with the license key and a Listener that is called if a result is found
        anylineOcrScanView.initAnyline(getString(R.string.anyline_license_key), new AnylineOcrResultListener() {

            @Override
            public void onResult(AnylineOcrResult result) {
                // This is called when a result is found.
                // The Identification includes all the data read from the driving license
                // as scanned and the given image shows the scanned driving license

                String resultString = result.getResult();
                String path = setupImagePath(result.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.title_driving_license), getDrivingLicenseResult(resultString), path);

                setupScanProcessView(ScanDrivingLicenseActivity.this, result, getScanModule());
            }
        });

    }

    @Override
    protected AnylineBaseModuleView getScanView() {
        return anylineOcrScanView;
    }

    @Override
    public Rect getCutoutRect() {
        return anylineOcrScanView.getCutoutRect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        anylineOcrScanView.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        anylineOcrScanView.cancelScanning();
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        anylineOcrScanView.releaseCameraInBackground();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onCameraOpened(CameraController cameraController, int width, int height) {
        //the camera is opened async and this is called when the opening is finished
        Log.d(TAG, "Camera opened successfully. Frame resolution " + width + " x " + height);
    }

    @Override
    public void onCameraError(Exception e) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw new RuntimeException(e);
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.DRIVER_LICENSE;
    }

    public HashMap<String, String> getDrivingLicenseResult(String drivingLicenseResult) {

        HashMap<String, String> drivingLicenseHashMap = new HashMap<>();


        String[] results = drivingLicenseResult.split("\\|");
        String firstName = null ;
        String secondName = null;
        String dob = null;
        String code = null;

        //The result order is predefined and it is like it follows
        if(results.length == 4){

            //first is surname
            secondName = results[0];
            //second is firstname
            firstName = results[1];
            //third is date of birth
            dob = results[2];
            //the last one is the document code
            code = results[3];
        }

        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_given_names) , (firstName == null || firstName.isEmpty()) ?  getResources().getString(R.string.not_available) : firstName);
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_sur_names) , (secondName == null || secondName.isEmpty()) ?  getResources().getString(R.string.not_available) : secondName);
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_DOB), (dob == null || dob.isEmpty()) ? getResources().getString(R.string.not_available) : dob);
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_document_code), (code == null || code.isEmpty()) ? getResources().getString(R.string.not_available) : code);

        return drivingLicenseHashMap;
    }

//    public String dayOfBirthFormat(String dayOfBirth){
//
//        String dateString = dayOfBirth;
//        String inputFormat = "ddMMyyyy";
//        String outputFormat = "dd.MM.yyyy";
//        if(Integer.parseInt(dayOfBirth.substring(2,4)) > 12 || Integer.parseInt(dayOfBirth.substring(4,6)) <= 12){
//            inputFormat = "yyyyMMdd";
//        }
//        SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
//        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
//        try {
//            dateString = outputDateFormat.format(inputDateFormat.parse(dayOfBirth));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return dateString;
//    }
}
