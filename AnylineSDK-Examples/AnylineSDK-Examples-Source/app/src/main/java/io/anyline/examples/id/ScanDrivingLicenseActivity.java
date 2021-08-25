

package io.anyline.examples.id;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.anyline.camera.CameraController;
import io.anyline.camera.CameraOpenListener;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.models.AnylineImage;
import io.anyline.plugin.ScanResult;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.id.ID;
import io.anyline.plugin.id.IdScanViewPlugin;
import io.anyline.plugin.id.Identification;
import io.anyline.view.ScanView;

import static io.anyline.examples.util.Constant.SCAN_FACE_PICTURE_PATH;
import static io.anyline.examples.util.Constant.SCAN_FULL_PICTURE_PATH;


public class ScanDrivingLicenseActivity extends ScanActivity implements CameraOpenListener {

    private ScanView drivingLicenseScanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_anyline_scan_view, findViewById(R.id.scan_view_placeholder));
        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)

        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        init();
        drivingLicenseScanView.setCameraOpenListener(this);
    }

    void init() {

        drivingLicenseScanView = findViewById(R.id.scan_view);
        drivingLicenseScanView.init("universal_id_driver_view_config.json");

        IdScanViewPlugin scanViewPlugin = (IdScanViewPlugin) drivingLicenseScanView.getScanViewPlugin();

        scanViewPlugin.addScanResultListener((ScanResultListener<ScanResult<ID>>) idScanResult -> {
            Identification identification = (Identification) idScanResult.getResult();
            HashMap<String, String> data = (HashMap<String, String>) identification.getResultData();
            String imagePath = setupImagePath(idScanResult.getCutoutImage());
            String facePath = setupImagePath(new AnylineImage(identification.getFaceImage()));

            Intent intent = new Intent(ScanDrivingLicenseActivity.this, ScanUniversalIdResultActivity.class);

            Set<String> setKeys = data.keySet();
            String[] arrayKeys = setKeys.toArray(new String[0]);
            Collection<String> values = data.values();
            String[] arrayValues = values.toArray(new String[0]);

            intent.putExtra("resultDataKeys", arrayKeys);
            intent.putExtra("resultDataValues", arrayValues);

            intent.putExtra(SCAN_FULL_PICTURE_PATH, imagePath);
            intent.putExtra(SCAN_FACE_PICTURE_PATH, facePath);
            startActivity(intent);

            String s = new JSONObject(data).toString();
            setupScanProcessView(ScanDrivingLicenseActivity.this, s,
                    getScanModule(), idScanResult.getCutoutImage().getBitmap(), null, idScanResult.getFaceImage().getBitmap());
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
        // the camera is opened async and this is called when the opening is finished
    }

    @Override
    public void onCameraError(Exception e) {
        // This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw new RuntimeException(e);
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.DRIVER_LICENSE;
    }
}
