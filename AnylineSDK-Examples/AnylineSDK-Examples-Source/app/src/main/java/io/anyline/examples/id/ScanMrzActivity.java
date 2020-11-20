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
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.core.exception_error_codes;
import at.nineyards.anyline.models.AnylineImage;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id.scan_view_placeholder));

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
		IdScanViewPlugin scanViewPlugin = new IdScanViewPlugin(getApplicationContext(), mrzScanView.getScanViewPluginConfig(), mrzConfig);
		mrzScanView.setScanViewPlugin(scanViewPlugin);
		scanViewPlugin.addScanResultListener(new ScanResultListener<ScanResult<ID>>() {
			@Override
			public void onResult(ScanResult<ID> idScanResult) {
				MrzIdentification identification = (MrzIdentification) idScanResult.getResult();
				Bitmap currentBitmap = identification.getFaceImage();
				AnylineImage newImage = new AnylineImage(currentBitmap);
				//set the path of the mrz Image
				String path = setupImagePath(idScanResult.getCutoutImage());
				String facePath = setupImagePath(newImage);

				startScanResultIntent(getResources().getString(R.string.title_mrz), getIdentificationResult(identification), path, facePath);

				setupScanProcessView(ScanMrzActivity.this, idScanResult, getScanModule());
			}
		});
	}

	   @Override
    protected ScanView getScanView() {
        return null;
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
		return ScanModuleEnum.ScanModule.MRZ;
	}

	public LinkedHashMap<String, String> getIdentificationResult(MrzIdentification identification) {

		LinkedHashMap<String, String> identificationResult = new LinkedHashMap<>();

		//DateFormat dateFormat =  java.text.DateFormat.getDateInstance(DateFormat.FULL);
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
		identificationResult.put("HEADER_MRZ", getResources().getString(R.string.mrz_header));
		identificationResult.put(getResources().getString(R.string.mrz_document_type) , (identification.getDocumentType() == null || identification.getDocumentType().isEmpty()) ?  getResources().getString(R.string.not_available) : identification.getDocumentType());
		identificationResult.put(getResources().getString(R.string.mrz_country_code), (identification.getNationalityCountryCode() == null || identification.getNationalityCountryCode().isEmpty()) ? getResources().getString(R.string.not_available) : identification.getNationalityCountryCode());
		identificationResult.put(getResources().getString(R.string.mrz_document_number), (identification.getDocumentNumber() == null || identification.getDocumentNumber().isEmpty()) ? getResources().getString(R.string.not_available) : identification.getDocumentNumber());
		identificationResult.put(getResources().getString(R.string.mrz_sur_names),(identification.getSurname() == null || identification.getSurname().isEmpty()) ? getResources().getString(R.string.not_available) : identification.getSurname());
		identificationResult.put(getResources().getString(R.string.mrz_given_names),(identification.getGivenNames() == null || identification.getGivenNames().isEmpty()) ? getResources().getString(R.string.not_available) : identification.getGivenNames());
		if(identification.getDateOfExpiryObject() == null){
			if(identification.getDateOfExpiry() != null && (!identification.getDateOfExpiry().isEmpty())){
				identificationResult.put(getResources().getString(R.string.mrz_expiration_date), getResources().getString(R.string.not_valid));
			}else{
				identificationResult.put(getResources().getString(R.string.mrz_expiration_date), getResources().getString(R.string.not_available));
			}
		}else{
			identificationResult.put(getResources().getString(R.string.mrz_expiration_date), dateFormat.format(identification.getDateOfExpiryObject()));
		}

		if(identification.getDateOfBirthObject() == null){
			if(identification.getDateOfBirth() != null && (!identification.getDateOfBirth().isEmpty())){
				identificationResult.put(getResources().getString(R.string.mrz_date_of_birthday), getResources().getString(R.string.not_valid));
			}else{
				identificationResult.put(getResources().getString(R.string.mrz_date_of_birthday), getResources().getString(R.string.not_available));
			}
		}else{
			identificationResult.put(getResources().getString(R.string.mrz_date_of_birthday), dateFormat.format(identification.getDateOfBirthObject()));
		}
		identificationResult.put(getResources().getString(R.string.mrz_sex), (identification.getSex() == null || identification.getSex().isEmpty()) ?  getResources().getString(R.string.not_available) : identification.getSex());
		if(identification.getPersonalNumber() != null && !identification.getPersonalNumber().isEmpty()){
			identificationResult.put(getResources().getString(R.string.personal_number), identification.getPersonalNumber());
		}

		if(identification.getVizSurname() != null && !identification.getVizSurname().isEmpty() ||
				identification.getGivenNames() != null && !identification.getGivenNames().isEmpty() ||
				identification.getVizDateOfBirth() != null && !identification.getVizDateOfBirth().isEmpty() ||
				identification.getVizDateOfExpiryObject() != null && !identification.getVizDateOfExpiry().isEmpty() ||
				identification.getVizDateOfIssue() != null && !identification.getVizDateOfIssue().isEmpty() ||
				identification.getVizAddress() != null && !identification.getVizAddress().isEmpty()) {
			identificationResult.put("HEADER", getResources().getString(R.string.mrz_VIZ_header));
		}

		if(identification.getVizSurname() != null && !identification.getVizSurname().isEmpty()){
			identificationResult.put(getResources().getString(R.string.mrz_viz_sur_names), identification.getVizSurname());
		}
		if(identification.getVizGivenNames() != null && !identification.getVizGivenNames().isEmpty()){
			identificationResult.put(getResources().getString(R.string.mrz_viz_given_names), identification.getGivenNames());
		}
		if(identification.getVizDateOfBirth() != null && !identification.getVizDateOfBirth().isEmpty()){
			identificationResult.put(getResources().getString(R.string.mrz_viz_dob), dateFormat.format(identification.getVizDateOfBirthObject()));
		}
		if(identification.getVizDateOfExpiry() != null && !identification.getVizDateOfExpiry().isEmpty()){
			identificationResult.put(getResources().getString(R.string.mrz_viz_date_of_expiry), dateFormat.format(identification.getVizDateOfExpiryObject()));
		}
		if(identification.getVizDateOfIssue() != null && !identification.getVizDateOfIssue().isEmpty()){
			identificationResult.put(getResources().getString(R.string.mrz_viz_issue_date), dateFormat.format(identification.getVizDateOfIssueObject()));
		}

		if(identification.getNationalityCountryCode() != null && identification.getDocumentType() != null && identification.getDocumentType().equals("ID") && identification.getNationalityCountryCode().equals("D")) {
			String address = null;
			if (identification.getVizAddress() != null) {
				address = identification.getVizAddress().replace("\\n", "\n");
			}
			identificationResult.put(getResources().getString(R.string.mrz_viz_address), (address == null || address.isEmpty()) ? getResources().getString(R.string.not_available) : address);
		}
		return identificationResult;
	}


	@Override
	public void onDebug(String s, Object o) {
	}

	@Override
	public void onRunSkipped(RunFailure runFailure) {

		if(runFailure!=null && runFailure.errorCode() == exception_error_codes.PointsOutOfCutout.swigValue()){
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
