/*
 * Anyline
 * ScanEnergyActivity.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-10-27
 */
package io.anyline.examples.meter;

import android.os.Bundle;

import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;
import io.anyline.view.ScanView;

/**
 * <p>Example activity to scan digital meters.</p>
 * <p>Note: Digital Meter scanning is in ALPHA stage, API and everything may change!</p>
 * <p>
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 * </p>
 */
public class ScanDigitalEnergyMeterActivity extends AbstractEnergyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the scan mode to start with
        //energyScanView.setScanMode(EnergyScanView.ScanMode.DIGITAL_METER);
    }


    @Override
    protected ScanView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_DIGITAL_METER;
    }

    @Override
    public String getSelectedModeInformation() {
        return "";
    }


}
