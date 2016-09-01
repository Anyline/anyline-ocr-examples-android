/*
 * Anyline
 * ScanEnergyActivity.java
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
public class ScanAnalogElectricMeterActivity extends ScanEnergyActivity {

    private static final String TAG = ScanAnalogElectricMeterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //See ScanEnergyActivity, this just adds buttons to select the mode

        // set the scan mode to start with
        energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER);

        addRadioButton(R.id.radio_button_electric, R.string.electric_meter_auto, true);
        addRadioButton(R.id.radio_button_electric_5_1, R.string.electric_meter_5_1, false);
        addRadioButton(R.id.radio_button_electric_6_1, R.string.electric_meter_6_1, false);
        addRadioButton(R.id.radio_button_electric_7, R.string.electric_meter_7, false);
        addRadioButton(R.id.radio_button_electric_white, R.string.electric_meter_white, false);


        // switch the scan mode depending on user selection
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_button_electric) {
                    showToast(getString(R.string.header_config, getString(R.string.electric_meter_short)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_electric_5_1) {
                    showToast(getString(R.string.header_config, getString(R.string.electric_meter_5_1_short)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER_5_1);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_electric_6_1) {
                    showToast(getString(R.string.header_config, getString(R.string.electric_meter_6_1_short)));
                    energyScanView.setScanMode(EnergyScanView.ScanMode.ELECTRIC_METER_6_1);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_electric_7) {
                    showToast(getString(R.string.header_config, getString(R.string.electric_meter_7_short)));
                    energyScanView.setScanMode(EnergyScanView.ScanMode.ANALOG_METER_7);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_electric_white) {
                    showToast(getString(R.string.header_config, getString(R.string.electric_meter_white_short)));
                    energyScanView.setScanMode(EnergyScanView.ScanMode.ANALOG_METER_WHITE);
                    energyScanView.startScanning();
                }
            }
        });
    }

    @Override
    protected String getResultTitle() {
        return getString(R.string.electric_meter);
    }
}
