/*
 * Anyline
 * ScanAnalogDigitalMeterActivity.java
 *
 * Copyright (c) 2017 Anyline GmbH
 *
 * Created by Hanna at 2017-04-07
 */
package io.anyline.examples.meter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ViewGroup;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanViewPlugin;


/**
 * Example activity for the automatic Analog/Digital Scanmode of the Anyline-Energy-Module
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanAutoAnalogDigitalMeterActivity extends AbstractEnergyActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MeterScanViewPlugin)energyScanView.getScanViewPlugin()).setScanMode(MeterScanMode.AUTO_ANALOG_DIGITAL_METER);
    }


    @Override
    protected AnylineBaseModuleView getScanView() {
        return null;
    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_AUTO_ANALOG_DIGITAL;
    }


    @Override
    public String getSelectedModeInformation() {
        return ""; // we do not need this, as for the new analog, we do not have to select a special mode (e.g. digits before and after the decimal)
    }
}


