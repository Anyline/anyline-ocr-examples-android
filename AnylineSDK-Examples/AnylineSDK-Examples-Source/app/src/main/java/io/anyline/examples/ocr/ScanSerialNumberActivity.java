/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 */
package io.anyline.examples.ocr;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

import at.nineyards.anyline.AnylineDebugListener;
import at.nineyards.anyline.core.RunFailure;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.ocr.feedback.FeedbackType;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.ocr.AnylineOcrConfig;
import io.anyline.plugin.ocr.OcrScanResult;
import io.anyline.plugin.ocr.OcrScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanSerialNumberActivity extends ScanActivity implements AnylineDebugListener {

	private static final String TAG = ScanSerialNumberActivity.class.getSimpleName();
	private ScanView scanView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id
				.scan_view_placeholder));

		scanView = (ScanView) findViewById(R.id.scan_view);

		// see ScanIbanActivity for a more detailed description
		io.anyline.plugin.ocr.AnylineOcrConfig anylineOcrConfig = new AnylineOcrConfig();
		anylineOcrConfig.setValidationRegex("[A-Z0-9]{4,}");

		scanView.setScanConfig("serial_number_view_config.json");
		//init the scanViewPlugin config
		//init the scan view
		OcrScanViewPlugin scanViewPlugin = new OcrScanViewPlugin(getApplicationContext(), getString(R.string.anyline_license_key), anylineOcrConfig, scanView.getScanViewPluginConfig(), "OCR");

		scanView.setScanViewPlugin(scanViewPlugin);

		scanViewPlugin.addScanResultListener(new ScanResultListener<OcrScanResult>() {
			@Override
			public void onResult(OcrScanResult result) {
				String path = setupImagePath(result.getCutoutImage());

				startScanResultIntent(getResources().getString(R.string.serial_number), getSerialNumberResult(result.getResult().toString()), path);
				setupScanProcessView(ScanSerialNumberActivity.this, result, getScanModule());
			}

		});

		createFeedbackView(scanView);
	}

	   @Override
    protected ScanView getScanView() {
        return null;
    }



	@Override
	protected ScanModuleEnum.ScanModule getScanModule() {
		return ScanModuleEnum.ScanModule.VOUCHER;
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

	@Override
	public void onDebug(String name, Object value) {

		if (AnylineDebugListener.BRIGHTNESS_VARIABLE_NAME.equals(name) &&
				(AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.equals(value.getClass()) ||
						AnylineDebugListener.BRIGHTNESS_VARIABLE_CLASS.isAssignableFrom(value.getClass()))) {
			switch (scanView.getBrightnessFeedBack()) {
				case TOO_BRIGHT:
					handleFeedback(FeedbackType.TOO_BRIGHT);
					break;
				case TOO_DARK:
					handleFeedback(FeedbackType.TOO_DARK);
					break;
				case OK:
					handleFeedback(FeedbackType.PERFECT);
					break;
			}
		} else if(AnylineDebugListener.DEVICE_SHAKE_WARNING_VARIABLE_NAME.equals(name)){
			handleFeedback(FeedbackType.SHAKY);
		}
	}

	protected HashMap<String, String> getSerialNumberResult (String result) {

		HashMap<String, String> serialNumberResult = new HashMap();

		serialNumberResult.put(getResources().getString(R.string.universal_reading_result), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );

		return serialNumberResult;
	}

	@Override
	public void onRunSkipped(RunFailure runFailure) {
	}

}