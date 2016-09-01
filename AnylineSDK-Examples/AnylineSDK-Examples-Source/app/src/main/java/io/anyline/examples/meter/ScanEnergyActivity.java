/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples.meter;

import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.models.AnylineImage;
import at.nineyards.anyline.modules.energy.EnergyResultListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ResultDialogBuilder;
import io.anyline.examples.SettingsFragment;

/**
 * Example activity for the Anyline-Energy-Module
 */
public class ScanEnergyActivity extends AppCompatActivity implements CameraOpenListener {

    private static final String TAG = ScanEnergyActivity.class.getSimpleName();
    protected EnergyScanView energyScanView;
    protected RadioGroup radioGroup;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_energy);
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get the view from the layout (check out the xml for the configuration of the view)
        energyScanView = (EnergyScanView) findViewById(R.id.energy_scan_view);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        energyScanView.setCameraOpenListener(this);

        // disable the reporting if set to off in preferences
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                SettingsFragment.KEY_PREF_REPORTING_ON, true)) {
            //The reporting of Analog Meter Results - including the photo of a scanned meter -
            // helps us in improving our product, and the customer experience.
            // However, if you wish to turn off this reporting feature, you can do it like this:
            energyScanView.setReportingEnabled(false);
        }
        // initialize Anyline with the license key and a Listener that is called if a result is found
        energyScanView.initAnyline(getString(R.string.anyline_license_key), new EnergyResultListener() {
            @Override
            public void onResult(EnergyScanView.ScanMode scanMode, String result,
                                 AnylineImage resultImage, AnylineImage fullImage) {

                // This is called when a result is found.
                // The scanMode is the mode the result was found for. The result is the actual result.
                // If the a meter reading was scanned two images are provided as well, one shows the targeted area only
                // the other shows the full image. (Images are null in barcode mode)
                // The result for meter readings is a String with leading zeros (if any) and no decimals.

                new ResultDialogBuilder(ScanEnergyActivity.this)
                        .setResultImage(resultImage)
                        .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24)
                        .setTextGravity(Gravity.CENTER)
                        .setText(getFormattedResult(result))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                energyScanView.startScanning();
                            }
                        })
                        .setTitle(getResultTitle())
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                energyScanView.startScanning();
                            }
                        })
                        .show();
            }
        });
    }

    protected void addRadioButton(int id, int stringId, boolean isChecked) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(id);
        radioButton.setText(stringId);
        radioButton.setChecked(isChecked);
        radioGroup.addView(radioButton);
    }

    protected String getResultTitle() {
        return null;
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
    public void onCameraOpened(int id, Camera camera, int width, int height) {
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
    private Spanned getFormattedResult(String result) {

        SpannableStringBuilder sb = new SpannableStringBuilder();

        for (int i = 0, n = result.length(); i < n; i++) {
            char text = result.charAt(i);
            sb.append(" ");
            sb.append(text);
            sb.append(" ");
            sb.setSpan(new BackgroundColorSpan(Color.BLACK), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Color.WHITE), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(" ");
        }
        return sb;
    }

    protected void showToast(String string) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
