package io.anyline.examples.meter;

import android.os.Bundle;
import android.view.ViewGroup;

import at.nineyards.anyline.modules.energy.EnergyScanView;
import io.anyline.examples.R;
import io.anyline.examples.ScanModuleEnum;
import io.anyline.examples.meter.baseactivities.AbstractEnergyActivity;


public class ScanEnergySerialNumberActivity extends AbstractEnergyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the scan mode to start with
        energyScanView.setScanMode(EnergyScanView.ScanMode.SERIAL_NUMBER);
        // a simple regex for a basic validation of the Energy Serial Number, results that don't match this, will not be returned
        energyScanView.setSerialNumberValidationRegex("[A-Z0-9]{4,}");
        energyScanView.setSerialNumberCharWhitelist("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Override
    protected void inflateEnergyView() {
        getLayoutInflater().inflate(R.layout.activity_scan_energy, (ViewGroup) findViewById(R.id.energy_view_placeholder));
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
