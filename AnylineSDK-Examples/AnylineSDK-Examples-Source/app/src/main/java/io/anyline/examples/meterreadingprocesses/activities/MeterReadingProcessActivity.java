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
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanResult;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.view.ScanView;

public abstract class MeterReadingProcessActivity extends BaseToolbarActivity implements CompoundButton.OnCheckedChangeListener, ScanResultListener<MeterScanResult> {

    public static final String KEY_SHOW_MODE_CONTROLS = "key_show_mode_controls";

    protected View mBottomContainer;
    protected ScanView mEnergyScanView;
    private Handler mHandler;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean mShowModeControls;
    private long mScanStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_scanner);

        //mBottomContainer = (View) findViewById(R.id.bottom_container);
        mEnergyScanView = (ScanView) findViewById(R.id.energy_scan_view);
        mHandler = new Handler(Looper.getMainLooper());

        try {
         //  mEnergyScanView.init("multi_scanner_energy_config.json", getString(R.string.anyline_license_key));
        } catch (Exception e) {
            e.printStackTrace();
        }
/*

        MeterScanViewPlugin scanViewPlugin = (MeterScanViewPlugin) mEnergyScanView.getScanViewPlugin();

        mEnergyScanView.setScanViewPlugin(scanViewPlugin);
       // scanViewPlugin.addScanResultListener(ScanResultListener<MeterScanResult>)
        setMode(MeterScanMode.BARCODE);

        scanViewPlugin.addScanResultListener(this);
      //  mEnergyScanView.initAnyline(getString(R.string.anyline_license_key), this);
*/

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

        mEnergyScanView.stop();
        mEnergyScanView.releaseCameraInBackground();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEnergyScanView.start();
        mScanStartTime = System.currentTimeMillis();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMode(isChecked ? MeterScanMode.ANALOG_METER : MeterScanMode.BARCODE);
    }

    @Override
    public void onResult(MeterScanResult energyResult) {
        System.out.print("asfasfsa");
    }

    protected void setShowModeControls(boolean show) {
        mShowModeControls = show;
    }

    protected void setMode(MeterScanMode mode) {
      //  ((MeterScanViewPlugin)mEnergyScanView.getScanViewPlugin()).setScanMode(mode);
    }

    protected void setScanning(boolean scanning) {
        if (scanning) {
            //if (!mEnergyScanView.getScanViewPlugin().isRunning()) {
                mEnergyScanView.start();
            //}

            mScanStartTime = System.currentTimeMillis();

        } else {
            mEnergyScanView.stop();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
