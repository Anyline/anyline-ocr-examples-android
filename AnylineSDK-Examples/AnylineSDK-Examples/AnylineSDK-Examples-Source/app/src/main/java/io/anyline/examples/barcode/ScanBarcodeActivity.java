/*
 * Anyline
 * ScanBarcodeActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.barcode;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.barcode.BarcodeScanPlugin;
import io.anyline.plugin.barcode.BarcodeScanResult;
import io.anyline.plugin.barcode.BarcodeScanViewPlugin;
import io.anyline.view.ScanView;

/**
 * Example activity for the Anyline-Barcode-Module
 */
public class ScanBarcodeActivity extends ScanActivity implements CameraOpenListener {

	private static final String TAG = ScanBarcodeActivity.class.getSimpleName();
	private ScanView barcodeScanView;
	private TextView resultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view,
				(ViewGroup) findViewById(R.id.scan_view_placeholder));

		resultText = (TextView) findViewById(R.id.text_result);

		barcodeScanView = (ScanView) findViewById(R.id.scan_view);
		// add a camera open listener that will be called when the camera is opened or an error occurred
		//  this is optional (if not set a RuntimeException will be thrown if an error occurs)
		barcodeScanView.setCameraOpenListener(this);
		// the view can be configured via a json file in the assets, and this config is set here
		// (alternatively it can be configured via xml, see the Energy Example for that)


		barcodeScanView.setScanConfig("barcode_view_config.json");

		BarcodeScanPlugin scanPlugin = new BarcodeScanPlugin(getApplicationContext(), "barcode", getString(R.string.anyline_license_key));
		BarcodeScanViewPlugin scanViewPlugin = new BarcodeScanViewPlugin(getApplicationContext(), scanPlugin, barcodeScanView.getScanViewPluginConfig());

		barcodeScanView.setScanViewPlugin(scanViewPlugin);
		scanViewPlugin.addScanResultListener(new ScanResultListener<BarcodeScanResult>() {
			@Override
			public void onResult(BarcodeScanResult result) {
				resultText.setText(result.getResult());

				//setup the scan process
				setupScanProcessView(ScanBarcodeActivity.this, result, getScanModule());

				//reset as the scanning will never stop (cancelOnResult = false)
				resetTime();
			}

		});
	}

	@Override
	protected AnylineBaseModuleView getScanView() {
		return null;
	}

	@Override
	protected ScanModuleEnum.ScanModule getScanModule() {
		return ScanModuleEnum.ScanModule.BARCODE;
	}

	@Override
	protected void onResume() {
		super.onResume();
		resultText.setText("");
		//start the actual scanning
		barcodeScanView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//stop the scanning
		barcodeScanView.stop();
		//release the camera (must be called in onPause, because there are situations where
		// it cannot be auto-detected that the camera should be released)
		barcodeScanView.releaseCameraInBackground();
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
}
