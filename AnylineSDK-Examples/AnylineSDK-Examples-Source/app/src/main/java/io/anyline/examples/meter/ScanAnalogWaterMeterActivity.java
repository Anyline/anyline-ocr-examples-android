/*
 * Anyline
 * ScanAnalogWaterMeterActivity.java
 *
 * Copyright (c) 2016 9yards GmbH
 *
 * Created by martin at 2016-04-26
 */

package io.anyline.examples.meter;

import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractAnalogEnergyActivity;

/**
 * Example activity for the Anyline-Energy-Module
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanAnalogWaterMeterActivity extends AbstractAnalogEnergyActivity {


    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_WATER_METER;
    }
}
