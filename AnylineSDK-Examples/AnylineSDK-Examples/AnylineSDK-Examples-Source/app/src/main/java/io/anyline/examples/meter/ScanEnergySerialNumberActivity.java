package io.anyline.examples.meter;

import android.os.Bundle;
import android.view.ViewGroup;

import at.nineyards.anyline.modules.AnylineBaseModuleView;
import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;
import io.anyline.plugin.meter.MeterScanMode;
import io.anyline.plugin.meter.MeterScanViewPlugin;


public class ScanEnergySerialNumberActivity extends AbstractEnergyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the scan mode to start with
        MeterScanViewPlugin meterScanViewPlugin = ((MeterScanViewPlugin)energyScanView.getScanViewPlugin());
        meterScanViewPlugin.setScanMode(MeterScanMode.SERIAL_NUMBER);
    }

    @Override
    protected AnylineBaseModuleView getScanView() {
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
