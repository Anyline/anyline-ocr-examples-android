package io.anyline.examples.meter;

import android.os.Bundle;

import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanViewPlugin;
import io.anyline.view.ScanView;


public class ScanEnergySerialNumberActivity extends AbstractEnergyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the scan mode to start with
        MeterScanViewPlugin meterScanViewPlugin = ((MeterScanViewPlugin)energyScanView.getScanViewPlugin());
        meterScanViewPlugin.setScanMode(MeterScanMode.SERIAL_NUMBER);
    }

    @Override
    protected ScanView getScanView() {
        return null;
    }
@Override
    protected ScanModuleEnum.ScanModule getScanModule() {
        return ScanModuleEnum.ScanModule.ENERGY_SERIAL_NUMBER;
    }

    @Override
    public String getSelectedModeInformation() {
        return "";
    }


}
