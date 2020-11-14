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
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;
import io.anyline.plugin.ScanResultListener;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanResult;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.view.ScanView;


/**
 * Example activity for Dial Mteter Scanmode of the Anyline-Energy-Module
 * See the {@link AbstractEnergyActivity}
 * for most of the relevant implementation details.
 */
public class ScanDialMeterActivity extends AbstractEnergyActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        energyScanView.setScanConfig("dial_meter_view_config_new.json");
        //init the scanViewPlugin config
        //init the scan view
        MeterScanViewPlugin scanViewPlugin = new MeterScanViewPlugin(getApplicationContext(), energyScanView.getScanViewPluginConfig(), "ENERGY");
        energyScanView.setScanViewPlugin(scanViewPlugin);
        ((MeterScanViewPlugin)energyScanView.getScanViewPlugin()).setScanMode(MeterScanMode.DIAL_METER);

        scanViewPlugin.addScanResultListener(new ScanResultListener<MeterScanResult>() {
            @Override
            public void onResult(MeterScanResult result) {
                String energyResult = result.getResult();

                String path = setupImagePath(result.getCutoutImage());
                startScanResultIntent(getResources().getString(R.string.category_energy), getMeterReadingResul(energyResult), path);


                setupScanProcessView(ScanDialMeterActivity.this, result, getScanModule());
                foundBarcodeString = "";
            }

        });
    }


    @Override
    protected ScanView getScanView() {
        return null;
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


