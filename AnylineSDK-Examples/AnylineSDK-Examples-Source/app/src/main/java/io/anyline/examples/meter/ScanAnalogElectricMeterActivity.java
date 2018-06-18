/*
 * Anyline
 * ScanEnergyActivity.java
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
 * <p/>
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanAnalogElectricMeterActivity extends AbstractAnalogEnergyActivity {

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_ELECTRIC_DIGITS;
    }


}
