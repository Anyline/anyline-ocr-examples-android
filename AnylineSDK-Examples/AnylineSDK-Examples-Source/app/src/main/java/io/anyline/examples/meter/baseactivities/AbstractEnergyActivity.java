package io.anyline.examples.meter.baseactivities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.barcode.Barcode;

import at.nineyards.anyline.camera.CameraController;
import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.barcode.NativeBarcodeResultListener;
import at.nineyards.anyline.modules.energy.EnergyResult;
import at.nineyards.anyline.modules.energy.EnergyResultListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanActivity;
import io.anyline.examples.dialog.SimpleAlertDialog;
import io.anyline.examples.ocr.ScanSerialNumberActivity;

/**
 * Base class for the Energymodule:
 * Contains all information that are identical in all Energyviews
 */
abstract public class AbstractEnergyActivity extends ScanActivity implements CameraOpenListener {
    private static final String TAG = AbstractEnergyActivity.class.getSimpleName();
    protected EnergyScanView energyScanView;
    protected String foundBarcodeString;
    private Switch barcodeSwitch;

    /**
     * inflates the required energy view to a placeholder
     */
    protected abstract void inflateEnergyView();

    public abstract String getSelectedModeInformation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_scan_energy_blank, (ViewGroup) findViewById(R.id.scan_view_placeholder));
        inflateEnergyView();

        foundBarcodeString = "";

        // get the view from the layout (check out the xml for the configuration of the view)
        energyScanView = (EnergyScanView) findViewById(R.id.energy_scan_view);

        // Usually the default scan mode would be set here, in this specific case it is done in the subclasses
        //energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        energyScanView.setCameraOpenListener(this);


        // initialize Anyline with the license key and a Listener that is called if a result is found
        energyScanView.initAnyline(getString(R.string.anyline_license_key), new EnergyResultListener() {
            @Override
            public void onResult(EnergyResult energyResult) {
                // This is called when a result is found.
                // The scanMode is the mode the result was found for. The result is the actual result.
                // If the a meter reading was scanned two images are provided as well, one shows the targeted area only
                // the other shows the full image. (Images are null in barcode mode)
                // The result for meter readings is a String with leading zeros (if any) and no decimals.


                final SimpleAlertDialog alert = new SimpleAlertDialog(AbstractEnergyActivity.this);

                String result = energyResult.getResult();

                alert.setMessage(getFormattedResult(result));

                // smaller text size for 6 digit heat meters, because of too narrow screen sizes (e.g. Galaxy Note 2, Galaxy Ace 4)
                if (energyResult.getScanMode() == EnergyScanView.ScanMode.SERIAL_NUMBER) {
                    alert.setMessageTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                } else if (energyResult.getScanMode() == EnergyScanView.ScanMode.HEAT_METER_6) {
                    alert.setMessageTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                } else {
                    alert.setMessageTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                }

                alert.setIcon(null);

                // needed to restart scanning for click outside of dialog
                final AlertDialog dialog = alert.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        resetTime();
                        if (!energyScanView.isRunning()) {
                            energyScanView.startScanning();
                        }
                    }
                });

                alert.setPositive(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                setupScanProcessView(AbstractEnergyActivity.this, energyResult, getScanModule());



                foundBarcodeString = ""; // reset the information about the last found barcode
            }
        });


        barcodeSwitch = (Switch) findViewById(R.id.barcode_scanner_switch);

        barcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {


                // enabled barcode detection and google play services are available
                if (isChecked) {
                    int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AbstractEnergyActivity.this);
                    if (errorCode == ConnectionResult.SUCCESS) {
                        foundBarcodeString = "";
                        energyScanView.enableBarcodeDetection(true, new NativeBarcodeResultListener() {
                                    @Override
                                    public void onBarcodesReceived(SparseArray<Barcode> sparseArray) {
                                        if (sparseArray != null && sparseArray.size() > 0) {
                                            // for demonstration purpose, we only show the latest found barcode (and only this one)
                                            foundBarcodeString = sparseArray.valueAt(0).displayValue;
                                        }
                                    }
                                }
                        );
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
                    energyScanView.disableBarcodeDetection();
                    foundBarcodeString = "";
                }

            }
        });

    }


    @Override
    protected AnylineBaseModuleView getScanView() {
        return energyScanView;
    }


    @Override
    protected void onResume() {
        super.onResume();

        //start the actual scanning
        energyScanView.startScanning();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //stop the scanning
        energyScanView.cancelScanning();
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

    /**
     * Format a meter reading to look a bit like a meter.
     *
     * @param result the meter reading
     * @return the formatted string
     */
    protected CharSequence getFormattedResult(String result) {

        SpannableStringBuilder sb = new SpannableStringBuilder();

        for (int i = 0, n = result.length(); i < n; i++) {
            char text = result.charAt(i);
            sb.append(" ");
            sb.append(text);
            sb.append(" ");
            sb.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.green_alpha_77)), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Color.BLACK), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(" ");
        }

        if (foundBarcodeString != null && !foundBarcodeString.isEmpty()) {
            String tmp = "Barcode: " + foundBarcodeString;
            sb.append("\n\n");
            sb.append(tmp);

            int start = result.length() * 4 + 2; // to get the right offset, we have above already length*4 + 2 for the two line breaks
            sb.setSpan(new StyleSpan(Typeface.ITALIC), start, start + tmp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new AbsoluteSizeSpan(18, true), start, start + tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//resize size
        }

        return sb;
    }

    @Override
    public Rect getCutoutRect() {
        return energyScanView.getCutoutRect();
    }


}