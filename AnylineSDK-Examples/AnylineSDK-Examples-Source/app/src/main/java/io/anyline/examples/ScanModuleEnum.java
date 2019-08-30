package io.anyline.examples;

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
        TIN,

        /**
         * @deprecated As the background-selection does not exist anymore, the mode should not be used anymore
         */
        @Deprecated
        ENERGY_ELECTRIC_BACKGROUND,
        ENERGY_GAS,
        ENERGY_WATER_METER,
        ENERGY_HEAT_METER,
        ENERGY_DIGITAL_METER,
        ENERGY_DIAL_METER
    }

    public ScanModuleEnum getScanModule() {
        return scanModule;
    }

    public void setScanModule(ScanModuleEnum scanModule) {
        this.scanModule = scanModule;
    }

}

