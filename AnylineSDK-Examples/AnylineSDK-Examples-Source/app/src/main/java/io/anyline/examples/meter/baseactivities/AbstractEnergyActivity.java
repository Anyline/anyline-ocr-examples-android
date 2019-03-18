package io.anyline.examples.meter.baseactivities;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;

import java.util.HashMap;
import java.util.List;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.camera.CameraView;
import at.nineyards.anyline.camera.NativeBarcodeResultListener;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.barcode.BarcodeFormat;
import io.anyline.plugin.barcode.BarcodeScanResult;
import io.anyline.plugin.meter.MeterScanResult;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.view.ScanView;

/**
 * Base class for the Energymodule:
 * Contains all information that are identical in all Energyviews
 */
abstract public class AbstractEnergyActivity extends ScanActivity implements CameraOpenListener {
    private static final String TAG = AbstractEnergyActivity.class.getSimpleName();
    protected ScanView energyScanView;
    protected String foundBarcodeString;
    private Switch barcodeSwitch;

    /**
     * inflates the required energy view to a placeholder
     */
//    protected abstract void inflateEnergyView();

    public abstract String getSelectedModeInformation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_scan_energy_blank, (ViewGroup) findViewById(R.id.scan_view_placeholder));
        getLayoutInflater().inflate(R.layout.activity_scan_energy,
                (ViewGroup) findViewById(R.id.energy_view_placeholder));


        foundBarcodeString = "";

        // get the view from the layout (check out the xml for the configuration of the view)
        energyScanView = (ScanView) findViewById(R.id.energy_scan_view);

        // Usually the default scan mode would be set here, in this specific case it is done in the subclasses
        //energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        energyScanView.setCameraOpenListener(this);
        barcodeSwitch = (Switch) findViewById(R.id.barcode_scanner_switch);

        // initialize Anyline with the license key and a Listener that is called if a result is found
        try {
            energyScanView.init("energy_view_config.json", getString(R.string.anyline_license_key));
        } catch (Exception e) {
            e.printStackTrace();
        }

        MeterScanViewPlugin scanViewPlugin = (MeterScanViewPlugin) energyScanView.getScanViewPlugin();

        scanViewPlugin.addScanResultListener(new ScanResultListener<MeterScanResult>() {
            @Override
            public void onResult(MeterScanResult result) {
                String energyResult = result.getResult();

                String path = setupImagePath(result.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.category_energy), getMeterReadingResul(energyResult), path);


                setupScanProcessView(AbstractEnergyActivity.this, result, getScanModule());
                foundBarcodeString = "";
            }

        });

       // energyScanView.addScanViewPlugin(scanViewPlugin);
//        energyScanView.initAnyline(getString(R.string.anyline_license_key), new EnergyResultListener() {
//            @Override
//            public void onResult(EnergyResult energyResult) {
//                // This is called when a result is found.
//                // The scanMode is the mode the result was found for. The result is the actual result.
//                // If the a meter reading was scanned two images are provided as well, one shows the targeted area only
//                // the other shows the full image. (Images are null in barcode mode)
//                // The result for meter readings is a String with leading zeros (if any) and no decimals.
//
//
//                String result = energyResult.getResult();
//
//                String path = setupImagePath(energyResult.getCutoutImage());
//                startScanResultIntent(getResources().getString(R.string.category_energy), getMeterReadingResul(result), path);
//
//                setupScanProcessView(AbstractEnergyActivity.this, energyResult, getScanModule());
//
//                foundBarcodeString = ""; // reset the information about the last found barcode
//            }
//        });

        barcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {


                // enabled barcode detection and google play services are available
                if (isChecked) {
                    int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AbstractEnergyActivity.this);
                    if (errorCode == ConnectionResult.SUCCESS) {
                        foundBarcodeString = "";
                        energyScanView.getCameraView().enableBarcodeDetection(new NativeBarcodeResultListener() {
                            @Override
                            public void onFailure(String e) {

                            }

                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                if (barcodes != null && barcodes.size() > 0) {
                                    // for demonstration purpose, we only show the latest found barcode (and only this one)
                                    String barcode = barcodes.get(0).getDisplayValue();
                                    foundBarcodeString = barcode;

                                }
                            }
                        }, null);
                    } else {
                        GoogleApiAvailability.getInstance().getErrorDialog(AbstractEnergyActivity.this, errorCode, 0, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                barcodeSwitch.setChecked(false);
                                if (!AbstractEnergyActivity.this.isFinishing()) {
                                    Toast.makeText(AbstractEnergyActivity.this, getString(R.string.barcode_not_operational), Toast.LENGTH_LONG).show();
                                }
                            }
                        }).show();

                    }


                    // disabled barcode detection
                } else {
                    energyScanView.getCameraView().disableBarcodeDetection();
                    foundBarcodeString = "";
                }

            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();

        //start the actual scanning
        energyScanView.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        energyScanView.stop();
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        energyScanView.releaseCameraInBackground();
    }

    @Override
    public void onCameraOpened(CameraController cameraController, int width, int height) {
        //the camera is opened async and this is called when the opening is finished,
        // with the used camera and the used frame resolution
        Log.d(TAG, "Camera opened successfully. Frame resolution " + width + " x " + height);
    }

    @Override
    public void onCameraError(Exception e) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw new RuntimeException(e);
    }

    protected HashMap<String, String> getMeterReadingResul (String result) {

        HashMap<String, String> meterReadingResult = new HashMap();

        meterReadingResult.put(getResources().getString(R.string.reading_result), (result.isEmpty() || result ==null) ? getResources().getString(R.string.not_available) : result );
        meterReadingResult.put(getResources().getString(R.string.barcode), (foundBarcodeString.isEmpty() || foundBarcodeString ==null) ? getResources().getString(R.string.not_available) : foundBarcodeString);

        return meterReadingResult;
    }


}