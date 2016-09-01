/*
 * Anyline
 * ScanAnalogGasMeterActivity.java
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
public class ScanAnalogGasMeterActivity extends ScanEnergyActivity {

    private static final String TAG = ScanAnalogGasMeterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //See ScanEnergyActivity, this just adds buttons to select the mode

        // set the scan mode to start with
        energyScanView.setScanMode(EnergyScanView.ScanMode.GAS_METER);

        addRadioButton(R.id.radio_button_gas_4, R.string.gas_meter_4, false);
        addRadioButton(R.id.radio_button_gas_5, R.string.gas_meter_5, true);
        addRadioButton(R.id.radio_button_gas_6, R.string.gas_meter_6, false);
        addRadioButton(R.id.radio_button_gas_7, R.string.gas_meter_7, false);


        // switch the scan mode depending on user selection
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_button_gas_4) {
                    showToast(getString(R.string.header_config, getString(R.string.gas_meter_4)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.ANALOG_METER_4);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_gas_5) {
                    showToast(getString(R.string.header_config, getString(R.string.gas_meter_5)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.GAS_METER);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_gas_6) {
                    showToast(getString(R.string.header_config, getString(R.string.gas_meter_6)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.GAS_METER_6);
                    energyScanView.startScanning();
                } else if (checkedId == R.id.radio_button_gas_7) {
                    showToast(getString(R.string.header_config, getString(R.string.gas_meter_7)));

                    energyScanView.setScanMode(EnergyScanView.ScanMode.ANALOG_METER_7);
                    energyScanView.startScanning();
                }
            }
        });
    }

    @Override
    protected String getResultTitle() {
        return getString(R.string.gas_meter);
    }
}
