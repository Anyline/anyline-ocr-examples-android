/*
 * Anyline
 * ScanMrzActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.mrz;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.id.DrivingLicenseResult;
import io.anyline.plugin.id.DrivingLicenseConfig;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IdScanPlugin;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.view.ScanView;

/**
 * Example Activity for the Anyline-DrivingLicense-Module.
 */
public class ScanDrivingLicenseActivity extends ScanActivity implements CameraOpenListener {

	private static final String TAG = ScanDrivingLicenseActivity.class.getSimpleName();
	private ScanView drivingLicenseScanView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id.scan_view_placeholder));
		// add a camera open listener that will be called when the camera is opened or an error occurred
		//  this is optional (if not set a RuntimeException will be thrown if an error occurs)

		// the view can be configured via a json file in the assets, and this config is set here
		// (alternatively it can be configured via xml, see the Energy Example for that)
		init();
		drivingLicenseScanView.setCameraOpenListener(this);
	}

	void init() {
		drivingLicenseScanView = (ScanView) findViewById(R.id.scan_view);

		//init the scanViewPlugin config
		drivingLicenseScanView.setScanConfig("driving_license_view_config.json");
		//ScanViewPluginConfig config = new ScanViewPluginConfig(getApplicationContext(), "driving_license_view_config_new.json");
		//init the scan view
		IdScanPlugin scanPlugin = new IdScanPlugin(getApplicationContext(), "driving_license", getString(R.string.anyline_license_key), new DrivingLicenseConfig());
		IdScanViewPlugin scanViewPlugin = new IdScanViewPlugin(getApplicationContext(), scanPlugin, drivingLicenseScanView.getScanViewPluginConfig());

		scanViewPlugin.addScanResultListener(new ScanResultListener<ScanResult<ID>>() {
			@Override
			public void onResult(ScanResult<ID> idScanResult) {

				DrivingLicenseResult resultString = (DrivingLicenseResult) idScanResult.getResult();
				String path = setupImagePath(idScanResult.getCutoutImage());
				startScanResultIntent(getResources().getString(R.string.title_driving_license), getDrivingLicenseResult(resultString), path);

				setupScanProcessView(ScanDrivingLicenseActivity.this, idScanResult, getScanModule());
			}


		});

		drivingLicenseScanView.setScanViewPlugin(scanViewPlugin);
	}

	@Override
	protected AnylineBaseModuleView getScanView() {
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		drivingLicenseScanView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//stop the scanning
		drivingLicenseScanView.stop();
		//release the camera (must be called in onPause, because there are situations where
		// it cannot be auto-detected that the camera should be released)
		drivingLicenseScanView.releaseCameraInBackground();
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

	public HashMap<String, String> getDrivingLicenseResult(DrivingLicenseResult drivingLicenseResult) {

		HashMap<String, String> drivingLicenseHashMap = new HashMap<>();

		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_given_names) , (drivingLicenseResult.getGivenName() == null || drivingLicenseResult.getGivenName().isEmpty()) ?  getResources().getString(R.string.not_available) : drivingLicenseResult.getGivenName());
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_sur_names) , (drivingLicenseResult.getSurName() == null || drivingLicenseResult.getSurName().isEmpty()) ?  getResources().getString(R.string.not_available) : drivingLicenseResult.getSurName());
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_DOB), (drivingLicenseResult.getDateOfBirth() == null || drivingLicenseResult.getDateOfBirth().isEmpty()) ? getResources().getString(R.string.not_available) : dayOfBirthFormat(drivingLicenseResult.getDateOfBirth()));
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_document_code), (drivingLicenseResult.getLicenseNumber() == null || drivingLicenseResult.getLicenseNumber().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getLicenseNumber());
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_authority) , (drivingLicenseResult.getAuthority() == null || drivingLicenseResult.getAuthority().isEmpty()) ?  getResources().getString(R.string.not_available) : drivingLicenseResult.getAuthority());
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_issuing_date) , (drivingLicenseResult.getIssuingDate() == null || drivingLicenseResult.getIssuingDate().isEmpty()) ?  getResources().getString(R.string.not_available) : drivingLicenseResult.getIssuingDate());
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_expiring_date), (drivingLicenseResult.getExpirationDate() == null || drivingLicenseResult.getExpirationDate().isEmpty()) ? getResources().getString(R.string.not_available) : dayOfBirthFormat(drivingLicenseResult.getExpirationDate()));
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_categories), (drivingLicenseResult.getCategories() == null || drivingLicenseResult.getCategories().isEmpty()) ? getResources().getString(R.string.not_available) : dayOfBirthFormat(drivingLicenseResult.getCategories()));
		drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_POB), (drivingLicenseResult.getPlaceOfBirth() == null || drivingLicenseResult.getPlaceOfBirth().isEmpty()) ? getResources().getString(R.string.not_available) : dayOfBirthFormat(drivingLicenseResult.getPlaceOfBirth()));

		return drivingLicenseHashMap;
	}

	public String dayOfBirthFormat(String dayOfBirth){

		String dateString = dayOfBirth;
		String inputFormat = "ddMMyyyy";
		String outputFormat = "yyyy-MM-dd";
//		if(Integer.parseInt(dayOfBirth.substring(3,5)) > 12 || Integer.parseInt(dayOfBirth.substring(4,6)) <= 12){
//			inputFormat = "yyyyMMdd";
//		}
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
		SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
		try {
			dateString = outputDateFormat.format(inputDateFormat.parse(dayOfBirth));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dateString;
	}
}
