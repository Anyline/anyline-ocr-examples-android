/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-12-03
 */
package io.anyline.examples.meter;

import android.view.ViewGroup;

import at.nineyards.anyline.camera.CameraOpenListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyDigitSelection;

/**
 * Example activity for the Anyline-Energy-Module
 * <p>Note: Heat Meter scanning is in ALPHA stage, API and everything may change!</p>
 * <p/>
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanDigitalHeatMeterActivity extends AbstractEnergyDigitSelection implements CameraOpenListener {


    @Override
    protected void inflateEnergyView() {
        getLayoutInflater().inflate(R.layout.activity_scan_heat_meter,
                (ViewGroup) findViewById(R.id.energy_view_placeholder));
    }

    @Override
    protected void chooseSelectionMode(int numVisible) {
        if (numVisible == 4) {
            energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_4);
            if (!energyScanView.isRunning()) {
                energyScanView.startScanning();
            }
        } else if (numVisible == 5) {
            energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_5);
            if (!energyScanView.isRunning()) {
                energyScanView.startScanning();
            }
        } else if (numVisible == 6) {
            energyScanView.setScanMode(EnergyScanView.ScanMode.HEAT_METER_6);
            if (!energyScanView.isRunning()) {
                energyScanView.startScanning();
            }
        }
    }

    @Override
    protected int startWithVisibleDigits() {
        return 4;
    }

    @Override
    protected int getMinDigits() {
        return 4;
    }

    @Override
    protected int getMaxDigits() {
        return 6;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_HEAT_METER;
    }

    @Override
    public String getSelectedModeInformation() {
        return Integer.toString(countVisibleDigits());
    }


    @Override
    protected void inflateDigitSelectionView() {
        getLayoutInflater().inflate(R.layout.energy_selection_digital,
                (ViewGroup) findViewById(R.id.energy_selection_placeholder));

    }
}
