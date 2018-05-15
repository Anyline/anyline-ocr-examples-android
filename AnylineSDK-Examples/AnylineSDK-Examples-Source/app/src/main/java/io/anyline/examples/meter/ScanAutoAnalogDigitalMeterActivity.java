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

import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;


/**
 * Example activity for the automatic Analog/Digital Scanmode of the Anyline-Energy-Module
 * See the {@link io.anyline.examples.meter.baseactivities.AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanAutoAnalogDigitalMeterActivity extends AbstractEnergyActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        energyScanView.setScanMode(EnergyScanView.ScanMode.AUTO_ANALOG_DIGITAL_METER);
    }

    @Override
    protected void inflateEnergyView() {
        getLayoutInflater().inflate(R.layout.activity_scan_energy,
                (ViewGroup) findViewById(R.id.energy_view_placeholder));

    }

    @Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_AUTO_ANALOG_DIGITAL;
    }


    @Override
    public String getSelectedModeInformation() {
        return ""; // we do not need this, as for the new analog, we do not have to select a special mode (e.g. digits before and after the decimal)
    }

//    @Override
//    protected CharSequence getFormattedResult(String result) {
//
//        SpannableStringBuilder sb = new SpannableStringBuilder();
//
//        for (int i = 0, n = result.length(); i < n; i++) {
//            char text = result.charAt(i);
//            sb.append(" ");
//            sb.append(text);
//            sb.append(" ");
//            sb.setSpan(new BackgroundColorSpan(Color.BLACK), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            sb.setSpan(new ForegroundColorSpan(Color.WHITE), i * 4, i * 4 + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            sb.append(" ");
//        }
//
//
//        if (foundBarcodeString != null && !foundBarcodeString.isEmpty()) {
//            String tmp = "Barcode: " + foundBarcodeString;
//            sb.append("\n\n");
//            sb.append(tmp);
//
//            int start = result.length() * 4 + 2; // to get the right offset, we have above already length*4 + 2 for the two line breaks
//            sb.setSpan(new StyleSpan(Typeface.ITALIC), start, start + tmp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            sb.setSpan(new AbsoluteSizeSpan(18, true), start, start + tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//resize size
//
//        }
//
//        return sb;
//    }
}


