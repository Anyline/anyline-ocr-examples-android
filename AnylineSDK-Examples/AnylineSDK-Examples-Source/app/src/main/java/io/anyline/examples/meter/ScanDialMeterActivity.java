/*
 * Anyline
 * ScanDialMeterActivity.java
 *
 * Copyright (c) 2017 Anyline GmbH
 *
 * Created by clemens at 2017-06-29
 */
package io.anyline.examples.meter;

import android.os.Bundle;
import android.view.ViewGroup;

import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;


/**
 * Example activity for Dial Mteter Scanmode of the Anyline-Energy-Module
 * See the {@link AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanDialMeterActivity extends AbstractEnergyActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        energyScanView.setScanMode(EnergyScanView.ScanMode.DIAL_METER);
    }

    @Override
    protected void inflateEnergyView() {
        getLayoutInflater().inflate(R.layout.activity_scan_dial_meter,
                (ViewGroup) findViewById(R.id.energy_view_placeholder));

    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_DIAL_METER;
    }

    @Override
    public String getSelectedModeInformation() {
        return ""; // we do not need this, as for the new analog, we do not have to select a special mode (e.g. digits before and after the decimal)
    }

}


