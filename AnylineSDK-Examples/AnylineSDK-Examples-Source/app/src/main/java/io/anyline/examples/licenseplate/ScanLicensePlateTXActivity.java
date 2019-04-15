/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-10-27
 */
package io.anyline.examples.licenseplate;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.ocr.AnylineOcrConfig;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;
import io.anyline.view.ScanViewPluginConfig;


public class ScanLicensePlateTXActivity extends ScanActivity implements AnylineDebugListener {

	private static final String TAG = ScanLicensePlateTXActivity.class.getSimpleName();
	private ScanView scanView;

	@Override
	protected AnylineBaseModuleView getScanView() {
		return null;
	}

	@Override
	protected ScanModuleEnum.ScanModule getScanModule() {
		return ScanModuleEnum.ScanModule.LICENSE_PLATE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
				.scan_view_placeholder));

		init();

	}

	void init() {
		scanView = (ScanView)findViewById(R.id.scan_view);
		//init the scan view

		final AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();

		anylineOcrConfig.setLanguages("license_plate_nj_old_v4.traineddata", "NewsGoth.traineddata");
		anylineOcrConfig.setCustomCmdFile("license_plates_combined_tx.ale");

		ScanViewPluginConfig ocrScanViewPluginConfig = new ScanViewPluginConfig(getApplicationContext(), "license_plate_view_config.json");

		OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), anylineOcrConfig, ocrScanViewPluginConfig, "OCR");

		scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
			@Override
			public void onResult(OcrScanResult scanResult) {
				setFeedbackViewActive(false);

				String path = setupImagePath(scanResult.getCutoutImage());

				startScanResultIntent(getResources().getString(R.string.title_license_plate), getLicensePlateResult(scanResult), path);

				setupScanProcessView(ScanLicensePlateTXActivity.this, scanResult, getScanModule());
			}
		});
		scanViewPlugin.setDebugListener(this);
		scanView.setScanViewPlugin(scanViewPlugin);
	}


	@Override
	protected void onResume() {
		super.onResume();
		scanView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanView.stop();
		scanView.releaseCameraInBackground();
	}

	public HashMap<String, String> getLicensePlateResult(OcrScanResult result) {

		HashMap<String, String> licensePlateResultData = new HashMap<>();

		licensePlateResultData.put(getResources().getString(R.string.license_plate_result), (result.getResult() == null || result.getResult().isEmpty()) ? getResources().getString(R.string.not_available) : result.getResult().substring(1));

		return licensePlateResultData;
	}

	@Override
	public void onDebug(String s, Object o) {

	}

	@Override
	public void onRunSkipped(RunFailure runFailure) {
	}

}