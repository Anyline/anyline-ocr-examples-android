/*
 * Anyline
 * ScanMrzActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.id;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;

import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.core.exception_error_codes;
import io.anyline.AnylineDebugListener;
import io.anyline.camera.CameraController;
import io.anyline.camera.CameraOpenListener;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.util.BitmapUtil;
import io.anyline.models.AnylineImage;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IDFieldScanOptions;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.plugin.id.MrzConfig;
import io.anyline.plugin.id.MrzFieldScanOptions;
import io.anyline.plugin.id.MrzIdentification;
import io.anyline.view.ScanView;

/**
 * Example Activity for the Anyline-MRZ-Module.
 */
public class ScanMrzActivity extends ScanActivity implements CameraOpenListener, AnylineDebugListener {

    private static final String TAG = ScanMrzActivity.class.getSimpleName();
    private ScanView mrzScanView;
    private static Toast notificationToast;
    private String scanModeTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id.scan_view_placeholder));
        scanModeTitle = getIntent().getStringExtra("ACTIVITY_TITLE");

        if (scanModeTitle == null) {
            scanModeTitle = getString(R.string.mrz);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(scanModeTitle);
        }
        init();
    }


    void init() {
        mrzScanView = (ScanView) findViewById(R.id.scan_view);
        MrzConfig mrzConfig = new MrzConfig();

        MrzFieldScanOptions fieldScanOption = new MrzFieldScanOptions();
        fieldScanOption.setVizDateOfIssue(IDFieldScanOptions.FieldScanOption.DEFAULT);
        fieldScanOption.setVizAddress(IDFieldScanOptions.FieldScanOption.DEFAULT);
        fieldScanOption.setVizGivenNames(IDFieldScanOptions.FieldScanOption.DEFAULT);
        fieldScanOption.setVizSurname(IDFieldScanOptions.FieldScanOption.DEFAULT);
        fieldScanOption.setVizDateOfBirth(IDFieldScanOptions.FieldScanOption.DEFAULT);
        fieldScanOption.setVizDateOfExpiry(IDFieldScanOptions.FieldScanOption.DEFAULT);

        mrzConfig.setIdFieldScanOptions(fieldScanOption);
        mrzConfig.setStrictMode(false);
        mrzConfig.enableFaceDetection(true);

        mrzScanView.setScanConfig("mrz_view_config.json");
        //init the scan view
        IdScanViewPlugin scanViewPlugin = new IdScanViewPlugin(this, mrzScanView.getScanViewPluginConfig(), mrzConfig);
        mrzScanView.setScanViewPlugin(scanViewPlugin);
        scanViewPlugin.addScanResultListener((ScanResultListener<ScanResult<ID>>) idScanResult -> {
            MrzIdentification identification = (MrzIdentification) idScanResult.getResult();
            Bitmap currentBitmap = identification.getFaceImage();
            AnylineImage newImage = new AnylineImage(currentBitmap);
            //set the path of the mrz Image
            String path = setupImagePath(idScanResult.getCutoutImage());
            String facePath = setupImagePath(newImage);

            startScanResultIntent(getResources().getString(R.string.title_mrz), getIdentificationResult(identification), path, facePath);

            Gson gson = new Gson();
            String json = gson.toJson(getIdentificationResult(identification), LinkedHashMap.class);
            setupScanProcessView(ScanMrzActivity.this, json, getScanModule(),
                    BitmapUtil.getBitmap(path), null, currentBitmap);
        });
    }

    @Override
    protected ScanView getScanView() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_scan_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.info_tutorial) {
            showTutorial();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTutorial() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.scanning_tutorial_dialog_title)
                .setView(getLayoutInflater().inflate(R.layout.alert_dialog_scanning_tutorial, null))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> {
                    onResume();
                })
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //start the actual scanning
        mrzScanView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        mrzScanView.stop();
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        mrzScanView.releaseCameraInBackground();
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
        if ((scanModeTitle != null) && (scanModeTitle.equals(getString(R.string.title_passport_visa)))) {
            return ScanModuleEnum.ScanModule.PASSPORT_VISA;
        } else {
            return ScanModuleEnum.ScanModule.MRZ;
        }
    }


    public LinkedHashMap<String, String> getIdentificationResult(MrzIdentification identification) {

        LinkedHashMap<String, String> identificationResult = new LinkedHashMap<>();

        //DateFormat dateFormat =  java.text.DateFormat.getDateInstance(DateFormat.FULL);
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

        if (identification.getSurname() != null && !identification.getSurname().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_sur_names), identification.getSurname().trim());
        } else if (identification.getVizSurname() != null && !identification.getVizSurname().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_sur_names), identification.getVizSurname().trim());
        }

        if (identification.getGivenNames() != null && !identification.getGivenNames().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_given_names), identification.getGivenNames().trim());
        } else if (identification.getVizGivenNames() != null && !identification.getVizGivenNames().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_given_names), identification.getVizGivenNames().trim());
        }

        if (identification.getDateOfBirthObject() == null) {
            if (identification.getDateOfBirth() != null && (!identification.getDateOfBirth().trim().isEmpty())) {
                identificationResult.put(getResources().getString(R.string.mrz_date_of_birthday), getResources().getString(R.string.not_valid));
            } else if (identification.getVizDateOfBirth() != null && !identification.getVizDateOfBirth().trim().isEmpty()) {
                identificationResult.put(getResources().getString(R.string.mrz_date_of_birthday), dateFormat.format(identification.getVizDateOfBirth().trim()));
            }
        } else {
            identificationResult.put(getResources().getString(R.string.mrz_date_of_birthday),
                    dateFormat.format(identification.getDateOfBirthObject()));
        }

        if (identification.getVizDateOfIssue() != null && !identification.getVizDateOfIssue().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_viz_issue_date), dateFormat.format(identification.getVizDateOfIssueObject()));
        }

        if (identification.getDateOfExpiryObject() == null) {
            if (identification.getDateOfExpiry() != null && (!identification.getDateOfExpiry().isEmpty())) {
                identificationResult.put(getResources().getString(R.string.mrz_expiration_date), getResources().getString(R.string.not_valid));
            } else if (identification.getVizDateOfExpiry() != null && !identification.getVizDateOfExpiry().trim().isEmpty()) {
                identificationResult.put(getResources().getString(R.string.mrz_expiration_date), dateFormat.format(identification.getVizDateOfExpiry().trim()));
            }
        } else {
            identificationResult.put(getResources().getString(R.string.mrz_expiration_date),
                    dateFormat.format(identification.getDateOfExpiryObject()));
        }

        if (identification.getDocumentNumber() != null && !identification.getDocumentNumber().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_document_number), identification.getDocumentNumber().trim());
        }

        if (identification.getNationalityCountryCode() != null && !identification.getNationalityCountryCode().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_country_code), identification.getNationalityCountryCode().trim());
        }

        if (identification.getDocumentType() != null && !identification.getDocumentType().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_document_type), identification.getDocumentType().trim());
        }

        if (identification.getPersonalNumber() != null && !identification.getPersonalNumber().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.personal_number), identification.getPersonalNumber().trim());
        }

        if (identification.getSex() != null && !identification.getSex().trim().isEmpty()) {
            identificationResult.put(getResources().getString(R.string.mrz_sex), identification.getSex().trim());
        }

        if (identification.getNationalityCountryCode() != null && identification.getDocumentType() != null &&
                identification.getDocumentType().equals("ID") && identification.getNationalityCountryCode().equals("D")) {
            String address = null;
            if (identification.getVizAddress() != null) {
                address = identification.getVizAddress().replace("\\n", "\n");
            }
            if (address != null && !address.isEmpty()) {
                identificationResult.put(getResources().getString(R.string.mrz_viz_address), address.trim());
            }
        }

        return identificationResult;
    }


    @Override
    public void onDebug(String s, Object o) {
    }

    @Override
    public void onRunSkipped(RunFailure runFailure) {

        if (runFailure != null && runFailure.errorCode() == exception_error_codes.PointsOutOfCutout.swigValue()) {
            showToast(runFailure.getMessage());
        }
    }

    private void showToast(String st) {
        try {
            notificationToast.getView().isShown();
            notificationToast.setText(st);
        } catch (Exception e) {
            notificationToast = Toast.makeText(this, st, Toast.LENGTH_SHORT);
        }
        notificationToast.show();
    }
}
