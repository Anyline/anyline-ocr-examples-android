
package io.anyline.examples.id;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.HashMap;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.models.AnylineImage;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.id.GermanIdFrontConfig;
import io.anyline.plugin.id.GermanIdFrontIdentification;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IdScanPlugin;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanGermanIdFrontActivity extends ScanActivity implements CameraOpenListener {

	private static final String TAG = ScanGermanIdFrontActivity.class.getSimpleName();
	private ScanView germanIdFrontScanView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, (ViewGroup) findViewById(R.id.scan_view_placeholder));
		// add a camera open listener that will be called when the camera is opened or an error occurred
		//  this is optional (if not set a RuntimeException will be thrown if an error occurs)

		// the view can be configured via a json file in the assets, and this config is set here
		// (alternatively it can be configured via xml, see the Energy Example for that)
		init();
		germanIdFrontScanView.setCameraOpenListener(this);
	}

	void init() {
		germanIdFrontScanView = (ScanView) findViewById(R.id.scan_view);

		GermanIdFrontConfig config = new GermanIdFrontConfig();
		config.enableFaceDetection(true);

		//init the scanViewPlugin config
		germanIdFrontScanView.setScanConfig("german_id_view_config.json");
		//ScanViewPluginConfig config = new ScanViewPluginConfig(getApplicationContext(), "driving_license_view_config_new.json");
		//init the scan view
		IdScanPlugin scanPlugin = new IdScanPlugin(getApplicationContext(), "german_id_front", config);
		IdScanViewPlugin scanViewPlugin = new IdScanViewPlugin(getApplicationContext(), scanPlugin, germanIdFrontScanView.getScanViewPluginConfig());

		scanViewPlugin.addScanResultListener(new ScanResultListener<ScanResult<ID>>() {
			@Override
			public void onResult(ScanResult<ID> idScanResult) {

				GermanIdFrontIdentification resultString = (GermanIdFrontIdentification) idScanResult.getResult();
				String path = setupImagePath(idScanResult.getCutoutImage());
				String facePath = setupImagePath(new AnylineImage(resultString.getFaceImage()));

				startScanResultIntent(getResources().getString(R.string.title_german_id_front), getGermanIdFrontResult(resultString), path, facePath);

				setupScanProcessView(ScanGermanIdFrontActivity.this,
									 new JSONObject(getGermanIdFrontResult(resultString)).toString(), getScanModule(),
									 idScanResult.getCutoutImage().getBitmap(), null, null);
			}

		});

		germanIdFrontScanView.setScanViewPlugin(scanViewPlugin);
	}

	   @Override
    protected ScanView getScanView() {
        return null;
    }


	@Override
	protected void onResume() {
		super.onResume();

		germanIdFrontScanView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//stop the scanning
		germanIdFrontScanView.stop();
		//release the camera (must be called in onPause, because there are situations where
		// it cannot be auto-detected that the camera should be released)
		germanIdFrontScanView.releaseCameraInBackground();
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
		return ScanModuleEnum.ScanModule.GERMAN_ID_FRONT;
	}

	public HashMap<String, String> getGermanIdFrontResult(GermanIdFrontIdentification germanIdFrontResult) {

		HashMap<String, String> germanIdFrontHashMap = new HashMap<>();

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_given_names) , (germanIdFrontResult.getGivenNames() == null || germanIdFrontResult.getGivenNames().isEmpty()) ?  getResources().getString(R.string.not_available) : germanIdFrontResult.getGivenNames());
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_surnames) , (germanIdFrontResult.getSurname() == null || germanIdFrontResult.getSurname().isEmpty()) ?  getResources().getString(R.string.not_available) : germanIdFrontResult.getSurname());
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_DOB), (germanIdFrontResult.getDateOfBirthObject() == null) ? getResources().getString(R.string.not_available) :  dateFormat.format(germanIdFrontResult.getDateOfBirthObject()));
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_document_nr), (germanIdFrontResult.getDocumentNumber() == null || germanIdFrontResult.getDocumentNumber().isEmpty()) ? getResources().getString(R.string.not_available) : germanIdFrontResult.getDocumentNumber());
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_nationality) , (germanIdFrontResult.getNationality() == null || germanIdFrontResult.getNationality().isEmpty()) ?  getResources().getString(R.string.not_available) : germanIdFrontResult.getNationality());
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_expiring_date), (germanIdFrontResult.getDateOfExpiryObject() == null) ? getResources().getString(R.string.not_available) : dateFormat.format(germanIdFrontResult.getDateOfExpiryObject()));
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_can), (germanIdFrontResult.getCardAccessNumber() == null || germanIdFrontResult.getCardAccessNumber().isEmpty()) ? getResources().getString(R.string.not_available) : germanIdFrontResult.getCardAccessNumber());
		germanIdFrontHashMap.put(getResources().getString(R.string.german_id_front_POB), (germanIdFrontResult.getPlaceOfBirth() == null || germanIdFrontResult.getPlaceOfBirth().isEmpty()) ? getResources().getString(R.string.not_available) : germanIdFrontResult.getPlaceOfBirth());

		return germanIdFrontHashMap;
	}
}
