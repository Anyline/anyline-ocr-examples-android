package io.anyline.examples.meterreadingprocesses.activities;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;

import at.nineyards.anyline.modules.energy.EnergyResult;
import at.nineyards.anyline.modules.energy.EnergyResultListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.baseactivities.BaseToolbarActivity;

public abstract class MeterReadingProcessActivity extends BaseToolbarActivity implements CompoundButton.OnCheckedChangeListener, EnergyResultListener {

    public static final String KEY_SHOW_MODE_CONTROLS = "key_show_mode_controls";

    protected View mBottomContainer;
    protected EnergyScanView mEnergyScanView;
    private Handler mHandler;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean mShowModeControls;
    private long mScanStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_scanner);

        mBottomContainer = (View) findViewById(R.id.bottom_container);
        mEnergyScanView = (EnergyScanView) findViewById(R.id.energy_scan_view);
        mHandler = new Handler(Looper.getMainLooper());

        setMode(EnergyScanView.ScanMode.BARCODE);
        mEnergyScanView.initAnyline(getString(R.string.anyline_license_key), this);

        if (getIntent() != null) {
            mShowModeControls = getIntent().getBooleanExtra(KEY_SHOW_MODE_CONTROLS, false);
        }

        setShowModeControls(true);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.processes_scan_screen_title);
    }

    @Override
    protected void onPause() {

        mEnergyScanView.cancelScanning();
        mEnergyScanView.releaseCameraInBackground();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEnergyScanView.startScanning();
        mScanStartTime = System.currentTimeMillis();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMode(isChecked ? EnergyScanView.ScanMode.ANALOG_METER : EnergyScanView.ScanMode.BARCODE);
    }

    @Override
    public void onResult(EnergyResult energyResult) {
    }

    protected void setShowModeControls(boolean show) {
        mShowModeControls = show;
    }

    protected void setMode(EnergyScanView.ScanMode mode) {
        mEnergyScanView.setScanMode(mode);
    }

    protected void setScanning(boolean scanning) {
        if (scanning) {
            if (!mEnergyScanView.isRunning()) {
                mEnergyScanView.startScanning();
            }
            mScanStartTime = System.currentTimeMillis();

        } else {
            mEnergyScanView.cancelScanning();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
