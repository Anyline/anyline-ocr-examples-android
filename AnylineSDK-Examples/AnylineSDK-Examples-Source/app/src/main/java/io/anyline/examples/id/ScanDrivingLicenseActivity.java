

package io.anyline.examples.id;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.models.AnylineImage;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.id.DrivingLicenseIdentification;
import io.anyline.plugin.id.DrivingLicenseConfig;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IdScanPlugin;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.plugin.id.Identification;
import io.anyline.view.ScanView;


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

        drivingLicenseScanView = findViewById(R.id.scan_view);
        drivingLicenseScanView.init("universal_id_view_config.json");
        IdScanViewPlugin scanViewPlugin = (IdScanViewPlugin) drivingLicenseScanView.getScanViewPlugin();

        scanViewPlugin.addScanResultListener(new ScanResultListener<ScanResult<ID>>() {
            @Override
            public void onResult(ScanResult<ID> idScanResult) {
                Identification identification = (Identification) idScanResult.getResult();
                HashMap<String, String> data = (HashMap<String, String>) identification.getResultData();
                String imagePath = setupImagePath(idScanResult.getCutoutImage());
                String facePath = setupImagePath(new AnylineImage(identification.getFaceImage()));

                Intent intent = new Intent(ScanDrivingLicenseActivity.this, ScanUniversalIdResultActivity.class);
                intent.putExtra("resultData", data);
                intent.putExtra("scan_full_picture_path", imagePath);
                startActivity(intent);

                String s = new JSONObject(data).toString();
                setupScanProcessView(ScanDrivingLicenseActivity.this, s,
                                     getScanModule(), idScanResult.getCutoutImage().getBitmap(), null);
            }
        });

        drivingLicenseScanView.setScanViewPlugin(scanViewPlugin);
    }

    @Override
    protected ScanView getScanView() {
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

    public HashMap<String, String> getDrivingLicenseResult(DrivingLicenseIdentification drivingLicenseResult) {

        HashMap<String, String> drivingLicenseHashMap = new HashMap<>();

        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_given_names), (drivingLicenseResult.getGivenNames() == null || drivingLicenseResult.getGivenNames().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getGivenNames());
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_sur_names), (drivingLicenseResult.getSurname() == null || drivingLicenseResult.getSurname().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getSurname());
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_DOB), (drivingLicenseResult.getDateOfBirth() == null || drivingLicenseResult.getDateOfBirth().isEmpty()) ? getResources().getString(R.string.not_available) : dayOfBirthFormat(drivingLicenseResult.getDateOfBirth()));
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_document_code), (drivingLicenseResult.getDocumentNumber() == null || drivingLicenseResult.getDocumentNumber().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getDocumentNumber());
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_authority), (drivingLicenseResult.getAuthority() == null || drivingLicenseResult.getAuthority().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getAuthority());
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_issuing_date), (drivingLicenseResult.getDateOfIssue() == null || drivingLicenseResult.getDateOfIssue().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getDateOfIssue());
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_expiring_date), (drivingLicenseResult.getDateOfExpiry() == null || drivingLicenseResult.getDateOfExpiry().isEmpty()) ? getResources().getString(R.string.not_available) : dayOfBirthFormat(drivingLicenseResult.getDateOfExpiry()));
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_categories), (drivingLicenseResult.getCategories() == null || drivingLicenseResult.getCategories().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getCategories());
        drivingLicenseHashMap.put(getResources().getString(R.string.driving_license_POB), (drivingLicenseResult.getPlaceOfBirth() == null || drivingLicenseResult.getPlaceOfBirth().isEmpty()) ? getResources().getString(R.string.not_available) : drivingLicenseResult.getPlaceOfBirth());

        return drivingLicenseHashMap;
    }

    public String dayOfBirthFormat(String dayOfBirth) {

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
