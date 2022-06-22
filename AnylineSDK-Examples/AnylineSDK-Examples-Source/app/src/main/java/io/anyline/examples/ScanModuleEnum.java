package io.anyline.examples;

import android.content.Context;
import android.util.Log;

/**
 * Created by lorena on 06.02.18.
 */

public class ScanModuleEnum {
    private io.anyline.examples.ScanModuleEnum scanModule;

    public enum ScanModule {
        BARCODE,
        MRZ,
        IBAN,
        RED_BULL_CODE,
        ISBN,
        DOCUMENT,
        BOTTLECAP,
        RECORD,
        SCRABBLE,
        VOUCHER,
        ENERGY_ELECTRIC_DIGITS,
        ENERGY_AUTO_ANALOG_DIGITAL,
        ENERGY_SERIAL_NUMBER,
        LICENSE_PLATE,
        DRIVER_LICENSE,
        GERMAN_ID_FRONT,
        VEHICLE_IDENTIFICATION_NUMBER,
        SHIPPING_CONTAINER,
        SHIPPING_CONTAINER_VERTICAL,
        TIN,
        OCR,
        UNIVERSAL_ID,
        SERIAL_NUMBER,
        CATTLE_TAG,
        ID_CARD,
        PASSPORT_VISA,
        VEHICLE_REGISTRATION_CERTIFICATE,

        /**
         * @deprecated As the background-selection does not exist anymore, the mode should not be used anymore
         */
        @Deprecated
        ENERGY_ELECTRIC_BACKGROUND,
        ENERGY_GAS,
        ENERGY_WATER_METER,
        ENERGY_HEAT_METER,
        ENERGY_DIGITAL_METER,
        ENERGY_DIAL_METER,

        ENERGY_MULTI_METER
    }


    public ScanModuleEnum getScanModule() {
        return scanModule;
    }

    public void setScanModule(ScanModuleEnum scanModule) {
        this.scanModule = scanModule;
    }

    public static String getLabelFrom(Context context, ScanModule scanmodule) {
        String[] labels = context.getResources().getStringArray(R.array.scan_module_labels);
        String result = "";
        try {
            result = labels[ScanModule.valueOf(scanmodule.toString()).ordinal()];
        } catch (Exception e) {
            Log.e ("ScanModule", "error get Label: " + e);
            result = scanmodule.toString();
        }
        return result;
    }

    public static Boolean isIDScanModule (ScanModule scanModule) {
        return
                scanModule == ScanModule.MRZ ||
                scanModule == ScanModule.DRIVER_LICENSE ||
                scanModule == ScanModule.GERMAN_ID_FRONT ||
                scanModule == ScanModule.UNIVERSAL_ID ||
                scanModule == ScanModule.ID_CARD ||
                scanModule == ScanModule.PASSPORT_VISA ||
                scanModule == ScanModule.VEHICLE_REGISTRATION_CERTIFICATE;
    }
}

