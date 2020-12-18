/*
 * Anyline
 * ScanBarcodeActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.barcode;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.barcode.Barcode;
import io.anyline.plugin.barcode.BarcodeFormat;
import io.anyline.plugin.barcode.BarcodeScanPlugin;
import io.anyline.plugin.barcode.BarcodeScanResult;
import io.anyline.plugin.barcode.BarcodeScanViewPlugin;
import io.anyline.view.ScanView;

//import io.anyline.examples.baseactivities.BarcodeListView;

/**
 * Example activity for the Anyline-Barcode-Module
 */
public class ScanBarcodeActivity extends ScanActivity implements CameraOpenListener {

	private static final String TAG = ScanBarcodeActivity.class.getSimpleName();
	private ScanView barcodeScanView;
	private BarcodePrefferences barcodePrefferences;
	private TextView resultText;
	private ArrayList<String> preselectedItems;
	private ArrayList<String> defaultItems;
	private LinearLayout barcodeContiner;
	private Switch barcodeSwitch;


	BarcodeScanPlugin scanPlugin = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view,
				(ViewGroup) findViewById(R.id.scan_view_placeholder));

		barcodePrefferences = BarcodePrefferences.getInstance(this);
		preselectedItems = barcodePrefferences.getArrayString();
		resultText = (TextView) findViewById(R.id.text_result);
		barcodeContiner = findViewById(R.id.barcode_switch_container);
		barcodeSwitch = (Switch) findViewById(R.id.barcode_scanner_switch);
		barcodeScanView = (ScanView) findViewById(R.id.scan_view);
		// add a camera open listener that will be called when the camera is opened or an error occurred
		//  this is optional (if not set a RuntimeException will be thrown if an error occurs)
		barcodeScanView.setCameraOpenListener(this);
		// the view can be configured via a json file in the assets, and this config is set here
		// (alternatively it can be configured via xml, see the Energy Example for that)

		barcodeContiner.setVisibility(View.VISIBLE);

		barcodeScanView.setScanConfig("barcode_view_config.json");

		scanPlugin = new BarcodeScanPlugin(getApplicationContext(), "barcode");
		BarcodeScanViewPlugin scanViewPlugin = new BarcodeScanViewPlugin(getApplicationContext(), scanPlugin, barcodeScanView.getScanViewPluginConfig());
		scanPlugin.setCancelOnResult(false);
		if(preselectedItems.size() == 0){
			barcodePrefferences.setDefault();
			preselectedItems = barcodePrefferences.getArrayString();
		}
		scanViewPlugin.setMultiBarcode(false);
		setBarcodeTypes(preselectedItems);

		barcodeScanView.setScanViewPlugin(scanViewPlugin);
		scanViewPlugin.addScanResultListener(new ScanResultListener<BarcodeScanResult>() {
			@Override
			public void onResult(BarcodeScanResult result) {

				String path = setupImagePath(result.getCutoutImage());
				barcodeScanView.getScanViewPlugin().setCancelOnResult(false);
				//setup the scan process
				startScanResultIntent(getResources().getString(R.string.category_barcodes), getBarcodeResult(result.getResult()), path);

				setupScanProcessView(ScanBarcodeActivity.this, result, getScanModule());

				//reset as the scanning will never stop (cancelOnResult = false)
				resetTime();
			}

		});

		barcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {


				// enabled barcode detection and google play services are available
				if (isChecked) {
					scanViewPlugin.setMultiBarcode(true);
					//scanViewPlugin.

					// disabled barcode detection
				} else {
					scanViewPlugin.setMultiBarcode(false);
					//energyScanView.getCameraView().disableBarcodeDetection();
					//foundBarcodeString = "";
				}

			}
		});
	}

	@Override
	protected ScanView getScanView() {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		// check if the request code is same as what is passed  here it is 2
		if(requestCode==2)
		{
			barcodeScanView.stop();
			preselectedItems = barcodePrefferences.getArrayString();


			if(preselectedItems.size() > 0 && !preselectedItems.contains("ALL")) {
				setBarcodeTypes(preselectedItems);
			}
			barcodeScanView.start();

		}
	}

	private void setBarcodeTypes(ArrayList<String> preselectedItems){
		BarcodeFormat barcodeFormatEAN8 = preselectedItems.contains("UPC/EAN")? BarcodeFormat.EAN_8 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatEAN13 = preselectedItems.contains("UPC/EAN")? BarcodeFormat.EAN_13 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatUPCA = preselectedItems.contains("UPC/EAN")? BarcodeFormat.UPC_A : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatUPCE = preselectedItems.contains("UPC/EAN")? BarcodeFormat.UPC_E : BarcodeFormat.UNKNOWN;
		//				BarcodeFormat barcodeFormatCupon = preselectedItems.contains("Bookland EAN")? BarcodeFormat.BOOKLAND : BarcodeFormat.UNKNOWN;
		//BarcodeFormat barcodeFormatCoupon = preselectedItems.contains("UCC Cupon Code")? BarcodeFormat.COUPON : BarcodeFormat.UNKNOWN;
		//				BarcodeFormat barcodeFormatGS1_Databar = preselectedItems.contains("Bookland EAN")? BarcodeFormat.BOOKLAND : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatGS1_Databar = preselectedItems.contains("GS1 Databar & Composite Codes")? BarcodeFormat.RSS_14 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatComposite = preselectedItems.contains("GS1 Databar & Composite Codes")? BarcodeFormat.RSS_EXPANDED : BarcodeFormat.UNKNOWN;

		BarcodeFormat barcodeFormatCode128 = preselectedItems.contains("Code 128")? BarcodeFormat.CODE_128 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatGS1_128 = preselectedItems.contains("GS1-128")? BarcodeFormat.GS1_128 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatISTB_128 = preselectedItems.contains("ISBT 128")? BarcodeFormat.ISBT_128 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatCode39 = preselectedItems.contains("Code 39")? BarcodeFormat.CODE_39 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatTRIOPTIC = preselectedItems.contains("Trioptic Code 39")? BarcodeFormat.TRIOPTIC : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatCode32 = preselectedItems.contains("Code 32")? BarcodeFormat.CODE_32 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatCode93 = preselectedItems.contains("Code 93")? BarcodeFormat.CODE_93 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatITF = preselectedItems.contains("Interleaved 2 of 5")? BarcodeFormat.ITF : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatMatrix = preselectedItems.contains("Matrix 2 of 5")? BarcodeFormat.MATRIX_2_5 : BarcodeFormat.UNKNOWN;

		BarcodeFormat barcodeFormatDiscrete = preselectedItems.contains("Code 25")? BarcodeFormat.DISCRETE_2_5 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatCodabar = preselectedItems.contains("Codabar")? BarcodeFormat.CODABAR : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatMSI = preselectedItems.contains("MSI")? BarcodeFormat.MSI : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatCode11 = preselectedItems.contains("Code 11")? BarcodeFormat.CODE_11 : BarcodeFormat.UNKNOWN;

		//BarcodeFormat barcodeFormatCode25 = preselectedItems.contains("Code 25")? BarcodeFormat.Code_25 : BarcodeFormat.UNKNOWN;

		BarcodeFormat barcodeFormatUSPostnet = preselectedItems.contains("US Postnet")? BarcodeFormat.US_POSTNET : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatUSPlanet = preselectedItems.contains("US Planet")? BarcodeFormat.US_PLANET : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatUKPostal = preselectedItems.contains("UK Postal")? BarcodeFormat.POST_UK : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatUSPS = preselectedItems.contains("USPS 4CB / OneCode / Intelligent Mail")? BarcodeFormat.USPS_4CB : BarcodeFormat.UNKNOWN;

		BarcodeFormat barcodeFormatPDF = preselectedItems.contains("PDF417")? BarcodeFormat.PDF_417 : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatMicroPDF417 = preselectedItems.contains("MicroPDF417")? BarcodeFormat.MICRO_PDF : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatDataMatrix = preselectedItems.contains("Data Matrix")? BarcodeFormat.DATA_MATRIX : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatQR = preselectedItems.contains("QR Code")? BarcodeFormat.QR_CODE : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatMicroQR = preselectedItems.contains("MicroQR")? BarcodeFormat.MICRO_QR : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatGS1 = preselectedItems.contains("GS1 QR Code")? BarcodeFormat.GS1_QR_CODE : BarcodeFormat.UNKNOWN;
		BarcodeFormat barcodeFormatAZTEC = preselectedItems.contains("Aztec")? BarcodeFormat.AZTEC : BarcodeFormat.UNKNOWN;

		scanPlugin.setBarcodeFormats(barcodeFormatDiscrete, barcodeFormatAZTEC,barcodeFormatPDF, barcodeFormatCodabar, barcodeFormatCode39, barcodeFormatCode93, barcodeFormatCode128, barcodeFormatDataMatrix, barcodeFormatEAN8, barcodeFormatEAN13, barcodeFormatQR, barcodeFormatUPCA, barcodeFormatUPCE, barcodeFormatGS1_128, barcodeFormatISTB_128, barcodeFormatTRIOPTIC,barcodeFormatCode32, barcodeFormatITF, barcodeFormatMatrix, barcodeFormatMSI, barcodeFormatCode11,
				barcodeFormatUSPostnet,barcodeFormatUSPlanet, barcodeFormatUKPostal,barcodeFormatUSPS,  barcodeFormatMicroPDF417, barcodeFormatMicroQR,barcodeFormatGS1, barcodeFormatGS1_Databar, barcodeFormatComposite);


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

	protected HashMap<String, String> getBarcodeResult (List<Barcode> result) {

		LinkedHashMap<String, String> barcodeResult = new LinkedHashMap<>();


		for(int i = 0; i< result.size(); i++){
			//((Barcode)result.get(i)).getValue()
			barcodeResult.put("HEADER" + (i+1), getString(R.string.category_barcodes) + " " + (i+1));

			barcodeResult.put(getString(R.string.barcode_result) + i, (result.get(i).getValue() == null && result.get(i).getValue().isEmpty())?  getResources().getString(R.string.not_available) : result.get(i).getValue());
			barcodeResult.put(getString(R.string.barcode_format)+ i, (result.get(i).getBarcodeFormat() == null)?  getResources().getString(R.string.not_available) : result.get(i).getBarcodeFormat().toString());

		}
		//tinResult.put(getResources().getString(R.string.tin), result.isEmpty() ? getResources().getString(R.string.not_available) : result);

		return barcodeResult;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem edit_item = menu.add(0, 0, 0, "");
		edit_item.setIcon(R.drawable.ic_settings);
		edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == 0) {
			Intent intent=new Intent(ScanBarcodeActivity.this, BarcodeListViewActivity.class);
			startActivityForResult(intent, 2);// Activity is started with requestCode 2
		}
		return super.onOptionsItemSelected(item);
	}

}
