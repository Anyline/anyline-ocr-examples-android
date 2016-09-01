/*
 * Anyline
 * ScanAnalogWaterMeterActivity.java
 *
 * Copyright (c) 2016 9yards GmbH
 *
 * Created by martin at 2016-04-26
 */

package io.anyline.examples.meter;

import android.os.Bundle;
import android.widget.RadioGroup;

import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;

/**
 * Example activity for the Anyline-Energy-Module
 */
public class ScanAnalogWaterMeterActivity extends ScanEnergyActivity {

    private static final String TAG = ScanAnalogWaterMeterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //See ScanEnergyActivity, this just adds buttons to select the mode

        // set the scan mode to start with
        energyScanView.setScanMode(EnergyScanView.ScanMode.WATER_METER_WHITE);

        addRadioButton(R.id.radio_button_water_white, R.string.water_meter_white, true);
        addRadioButton(R.id.radio_button_water_black, R.string.water_meter_black, false);


        // switch the scan mode depending on user selection
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_button_water_white) {
                    showToast(getString(R.string.header_config, getString(R.string.water_meter_white)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.WATER_METER_WHITE);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_water_black) {
                    showToast(getString(R.string.header_config, getString(R.string.water_meter_black)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.WATER_METER_BLACK);
                    energyScanView.startScanning();
                }
            }
        });
    }

    @Override
    protected String getResultTitle() {
        return getString(R.string.water_meter);
    }
}
